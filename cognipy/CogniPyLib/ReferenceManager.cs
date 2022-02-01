using CogniPy.ARS;
using CogniPy.CNL;
using CogniPy.OWL;
using org.semanticweb.owlapi.apibinding;
using org.semanticweb.owlapi.model;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net;
using System.Text.RegularExpressions;
using System.Xml;

namespace CogniPy
{
    public class ReferenceManager
    {
        Func<string, IEnumerable<string>> getForms = null;
        public void setForms(Func<string, IEnumerable<string>> forms)
        {
            getForms = forms;
        }
        public bool OWLReferencesProblem = false;

        //not thread safe
        private string curentlyLoadingOnt;
        protected Lazy<OWLOntologyManager> owlManager = null;
        public OWLOntologyManager OwlManager { get { return owlManager.Value; } }

        public ReferenceManager(Func<string, IEnumerable<string>> getForms)
        {
            this.getForms = getForms;
            CreateOWLManager();
        }

        private void CreateOWLManager()
        {
            owlManager = new Lazy<OWLOntologyManager>(() =>
            {
                var oman = OWLManager.createOWLOntologyManager();
                var mil = new MissingImportListenerImpl(this);
                oman.setSilentMissingImportsHandling(true);
                oman.addMissingImportListener(mil);
                oman.setOntologyLoaderConfiguration(config);
                return oman;
            });
        }

        class MissingImportListenerImpl : MissingImportListener
        {
            ReferenceManager workspace;
            public MissingImportListenerImpl(ReferenceManager workspace)
            {
                this.workspace = workspace;
            }

            public void importMissing(MissingImportEvent mie)
            {
                workspace.NewError = true;

                if (!workspace.BrokenImports.ContainsKey(workspace.curentlyLoadingOnt))
                {
                    workspace.BrokenImports.Add(workspace.curentlyLoadingOnt, new HashSet<string>());
                }

                string importedOntology = mie.getImportedOntologyURI().toURI().toString();

                workspace.BrokenImports[workspace.curentlyLoadingOnt].Add(importedOntology);

                Exception creationException = (Exception)mie.getCreationException();

                if (!workspace.ErrorsOnImports.ContainsKey(importedOntology))
                    workspace.ErrorsOnImports.Add(importedOntology, new HashSet<string>());

                if (!workspace.ExceptionsOnImports.ContainsKey(importedOntology))
                    workspace.ExceptionsOnImports.Add(importedOntology, new HashSet<Exception>());

                if (workspace.ErrorsOnImports.ContainsKey(importedOntology) && !workspace.ErrorsOnImports[importedOntology].Contains(creationException.Message))
                {
                    workspace.ErrorsOnImports[importedOntology].Add(creationException.Message);
                    workspace.ExceptionsOnImports[importedOntology].Add(creationException);
                }
            }
        }

        public class VirtualSiteIRIMapper : OWLOntologyIRIMapper
        {
            public string currDir;

            ReferenceManager workspace;
            public VirtualSiteIRIMapper(ReferenceManager workspace)
            {
                this.workspace = workspace;
            }

            protected string getFileName(string url, string extension)
            {
                if (url.EndsWith("/") || url.EndsWith("#"))
                {
                    string tmp = url.Remove(url.Length - 1, 1);
                    return tmp + extension;
                }
                else
                    return url;
            }

            protected string getFilePath(string ontoStr, string dir, string extension = ".rdf")
            {
                if (String.IsNullOrEmpty(dir))
                    return null;

                string fn = System.IO.Path.GetFileName(ontoStr);
                if (ontoStr.EndsWith("/") || ontoStr.EndsWith("#")) // the ontologyIRI was not a filePath. Try to remove the last / and add .rdf....
                {
                    fn = System.IO.Path.GetFileName(getFileName(ontoStr, extension));
                }

                var gfn = System.IO.Path.Combine(dir, fn);

                return gfn;
            }

            ///
            /// Checks the file exists or not.
            ///
            /// The URL of the remote file.
            /// True : If the file exits, False if file not exists
            private bool RemoteFileExists(string url)
            {
                try
                {
                    //Creating the HttpWebRequest
                    WebRequest request = HttpWebRequest.Create(url);
                    //Setting the Request method HEAD to make it fast.
                    request.Method = "HEAD";
                    request.Timeout = 5000;
                    //Getting the Web Response.
                    try
                    {
                        using (var response = (HttpWebResponse)request.GetResponse())
                            return (response.StatusCode == HttpStatusCode.OK);
                    }
                    catch (WebException ex)
                    {
                        // this code is add for the case in which a proxy with authentication is added but the proxy do not support HEAD requests. We can in this case try with a GET request to make it work.
                        if ((ex.Response is HttpWebResponse) && (ex.Response as HttpWebResponse).StatusCode == HttpStatusCode.ProxyAuthenticationRequired)
                        {
                            request = HttpWebRequest.Create(url);
                            request.Method = "GET";
                            request.Timeout = 10000;
                            using (var response = (HttpWebResponse)request.GetResponse())
                                return (response.StatusCode == HttpStatusCode.OK);
                        }
                        else
                            return false;
                    }
                }
                catch
                {
                    return false;
                }
            }


            protected bool IsLocalFile(IRI ontologyIRI, out IRI fileIRI, string extension = ".rdf")
            {
                bool localFile = false;
                fileIRI = null;
                var ontoStr = OWLPathUriTools.IRI2Path(ontologyIRI);

                string gfn = getFilePath(ontoStr, currDir, extension);
                if (!System.IO.File.Exists(gfn))
                {
                    var dirs = Directory.GetDirectories(currDir);
                    foreach (string dir in dirs)
                    {
                        gfn = getFilePath(ontoStr, dir, extension);
                        if (System.IO.File.Exists(gfn))
                        {
                            localFile = true;
                            break;
                        }
                    }
                }
                else
                {
                    localFile = true;
                }

                fileIRI = IRI.create(UriFromFilePath(gfn));
                return localFile;
            }

            protected bool IsRemoteFile(IRI ontologyIRI, out IRI remoteIRI)
            {
                remoteIRI = null;
                // not local --> try remotely
                if (RemoteFileExists(ontologyIRI.toString()))
                {
                    return true;
                }
                else if (RemoteFileExists(getFileName(ontologyIRI.toString(), ".rdf"))) // remotely not found --> try remotely adding .rdf to the name.
                {
                    remoteIRI = IRI.create(getFileName(ontologyIRI.toString(), ".rdf"));
                    return true;
                }
                else
                    return false;
            }

            /// <summary>
            /// Function called by the ontology manager when an ontology is loading. If the function returns null, it means: use the ontologyIRI
            /// This functions searches (in order):
            ///   * locally (starting from the current folder (= folder of the file we are importing, = folder of the current file where the reference is written)
            ///       - using the name as given
            ///       - using the name after removing the last / and adding .rdf
            ///   * remotely
            ///       - using the url as it is
            ///       - using the url after removing the last / and adding .rdf
            /// </summary>
            /// <param name="ontologyIRI"></param>
            /// <returns></returns>
            public IRI getDocumentIRI(IRI ontologyIRI)
            {
                IRI actualIRI = null;
                // currDir is empty when the ontology we are importing is not local.
                // in this case we also have to check if the file is present at ontologyIRI or if we need to change the IRI where it is located
                if (String.IsNullOrEmpty(currDir) && !IsRemoteFile(ontologyIRI, out actualIRI))
                {
                    return null;
                }
                else if (String.IsNullOrEmpty(currDir)) // if currDir was null and we are here, it means the file was found in the remoteFile function --> use actualIRI.
                    return actualIRI;

                // prefer local
                if (IsLocalFile(ontologyIRI, out actualIRI))
                {
                    return actualIRI;
                }
                else if (IsLocalFile(ontologyIRI, out actualIRI, ".owl"))
                {
                    return actualIRI;
                }
                else if (IsRemoteFile(ontologyIRI, out actualIRI))
                {
                    return actualIRI;
                }
                else
                    return null;
            }
        };

        private Dictionary<string, HashSet<Exception>> ExceptionsOnImports = new Dictionary<string, HashSet<Exception>>();
        private Dictionary<string, ReferenceTags> LoadedOntologies = new Dictionary<string, ReferenceTags>();
        public Dictionary<string, CNL.DL.Paragraph> LoadedDlAsts = new Dictionary<string, CNL.DL.Paragraph>();
        private Dictionary<string, HashSet<string>> BrokenImports = new Dictionary<string, HashSet<string>>();
        private Dictionary<string, HashSet<string>> ErrorsOnImports = new Dictionary<string, HashSet<string>>();
        private List<ParseException> SyntaxErrors = new List<ParseException>();

        public IEnumerable<Tuple<string, bool>> GetReferencedOntologies(string onto, bool cnl)
        {
            foreach (var x in LoadedOntologies)
            {
                if (cnl ? (x.Value.FENamespace == onto) : (x.Value.ontologyLocation == onto))
                    foreach (var r in x.Value.referencedTags)
                        yield return Tuple.Create(r.ontologyLocation == null ? r.FENamespace : r.ontologyLocation, r.ontologyLocation == null);
            }
        }

        public class ReferenceTags
        {
            public HashSet<string> concepts = new HashSet<string>();
            public HashSet<string> roles = new HashSet<string>();
            public HashSet<string> dataroles = new HashSet<string>();
            public HashSet<string> instances = new HashSet<string>();
            public HashSet<string> datatypes = new HashSet<string>();
            public string baseNamespace = null;
            public string FENamespace = null;
            public string ontologyLocation = null;
            public bool IsDirectlyReferencedTag = true;
            // parameter telling us if the reference was uploaded correctly (if there was some error while uploading it will be false)
            public bool fullyUploaded = true;
            // if the ast content is null --> this reference tag was not imported
            public bool isImported
            {
                get
                {
                    if (dlAst.Statements == null)
                        return false;

                    return true;
                }
            }


            public List<ReferenceTags> referencedTags = new List<ReferenceTags>();
            public Dictionary<Tuple<EntityKind, string>, string> uriMapping = new Dictionary<Tuple<EntityKind, string>, string>();
            public Dictionary<string, string> invUriMapping = new Dictionary<string, string>();
            public CNL.DL.Paragraph dlAst = new CNL.DL.Paragraph(null);
        }

        public List<string> getAnnotationForOntology(string ontologyIri)
        {
            List<string> allAnnot = new List<string>();
            var ontIt = owlManager.Value.getOntologies().iterator();
            while (ontIt.hasNext())
            {
                OWLOntology onto = (OWLOntology)ontIt.next();

                IRI currIRI = onto.getOntologyID().getOntologyIRI();

                if (currIRI != null && currIRI.toString() == ontologyIri)
                {
                    var annotIt = onto.getAnnotations().iterator();
                    while (annotIt.hasNext())
                    {
                        OWLAnnotation annot = (OWLAnnotation)annotIt.next();
                        string val = "";
                        if (annot.getValue() is OWLLiteral)
                        {
                            OWLLiteral owlLit = (OWLLiteral)annot.getValue();
                            val = owlLit.getLiteral().Replace("\'", "\''");
                        }
                        else if (annot.getValue() is IRI)
                        {
                            var ir = (IRI)annot.getValue();
                            val = ir.toString();
                        }


                        string result = "'" + annot.getProperty().getIRI() + "'::: '" + val + "'";
                        allAnnot.Add(result);
                    }
                    break;
                }
            }

            return allAnnot;
        }

        public List<CogniPy.CNL.W3CAnnotation> getAnnotationForEntity(CogniPy.CNL.DL.DlName dlName, CogniPy.ARS.EntityKind kind, Func<string, string> pfx2ns)
        {
            List<CogniPy.CNL.W3CAnnotation> allAnnot = new List<CogniPy.CNL.W3CAnnotation>();

            List<OWLOntology> loadedOntologies = new List<OWLOntology>();
            var ontIt = owlManager.Value.getOntologies().iterator();
            while (ontIt.hasNext())
            {
                loadedOntologies.Add((OWLOntology)ontIt.next());
            }

            if (loadedOntologies.Count == 0)
                return allAnnot;

            CogniPy.ARS.OwlNameingConventionCamelCase owlNameing = new CogniPy.ARS.OwlNameingConventionCamelCase();
            ARS.OwlName owlName = owlNameing.FromDL(dlName, CNL.EN.CNLFactory.lex, pfx2ns, kind);

            foreach (OWLOntology ont in loadedOntologies)
            {
                ////////////////////////////////////////
                if (owlManager.Value.getOntologyFormat(ont) == null)
                    continue;
                var invtransform = new CogniPy.ARS.InvTransform(owlManager.Value, ont, null);
                ////////////////////////////////////////

                var signIt = ont.getEntitiesInSignature(owlName.iri).iterator();
                if (!signIt.hasNext())
                {
                    string newIri = owlName.iri.toString().Replace("#", "");
                    signIt = ont.getEntitiesInSignature(org.semanticweb.owlapi.model.IRI.create(newIri)).iterator();
                }

                while (signIt.hasNext())
                {
                    OWLEntity ent = (OWLEntity)signIt.next();

                    var annotIt = ent.getAnnotationAssertionAxioms(ont).iterator();
                    while (annotIt.hasNext())
                    {
                        OWLAnnotationAssertionAxiom annot = (OWLAnnotationAssertionAxiom)annotIt.next();
                        CogniPy.CNL.DL.DLAnnotationAxiom dlNameAnnot = (CogniPy.CNL.DL.DLAnnotationAxiom)invtransform.Convert(annot);
                        allAnnot.Add(new W3CAnnotation(true) { External = true, Language = dlNameAnnot.language, Type = dlNameAnnot.annotName, Value = dlNameAnnot.value });
                    }
                }
            }

            return allAnnot;
        }

        public static string UriFromFilePath(string pth)
        {
            return new java.io.File(pth).toURI().toString();
        }

        public IEnumerable<Exception> GetExceptionsOnImports(string uri)
        {
            string pathUri = null;
            try
            {
                if (System.IO.Path.IsPathRooted(uri))
                    if (System.IO.File.Exists(uri))
                        pathUri = UriFromFilePath(uri);
            }
            catch { }

            if (ExceptionsOnImports.ContainsKey(uri))
                return ExceptionsOnImports[uri];
            else if (!String.IsNullOrEmpty(pathUri) && ErrorsOnImports.ContainsKey(pathUri))
                return ExceptionsOnImports[pathUri];
            else
                return new List<Exception>();
        }

        public IEnumerable<ParseException> GetSyntaxErrors()
        {
            return SyntaxErrors;
        }

        public IEnumerable<string> GetErrorsOnImports(string uri)
        {
            string pathUri = null;
            try
            {
                if (System.IO.Path.IsPathRooted(uri))
                    if (System.IO.File.Exists(uri))
                        pathUri = UriFromFilePath(uri);
            }
            catch { }

            if (ErrorsOnImports.ContainsKey(uri))
                return ErrorsOnImports[uri];
            else if (!String.IsNullOrEmpty(pathUri) && ErrorsOnImports.ContainsKey(pathUri))
                return ErrorsOnImports[pathUri];
            else
                return new List<string>();
        }

        public bool NewError = false;

        // decides what should be done when a reference inside an owl document is not present.
        public enum OWLMissingOntologyReferencesStrategy
        {
            Throw_Exception = 0,
            Retry = 1
        }

        public XmlDocument ontodoc;

        /// <summary>
        /// dictionary with <prefix,namespace> for all (imported and non-imported) the references inside the ontology.
        /// </summary>
        public SortedDictionary<string, string> AllReferences = new SortedDictionary<string, string>();
        /// <summary>
        /// dictionary with <namespace,location> of the direct imports in the ontology.
        /// </summary>
        public Dictionary<string, string> DirectImports = new Dictionary<string, string>();

        public string DefaultNamespace;
        private string _currentFilePath = null;
        public string CurrentFilePath
        {
            set { _currentFilePath = value; }
        }

        // can defaultPfx be null??
        public enum WhatToLoad
        {
            FromUri,
            CnlFromString,
            OwlRdfFromString
        }

        List<string> recursivelyLoadedOntologies = new List<string>();

        public bool LoadOntology(WhatToLoad whatToLoad, CNLTools tools, string source, out HashSet<string> brokenImports, out ReferenceTags tags, out CNL.DL.Paragraph dlast, string rootontology = null, CogniPy.ARS.NameingConventionKind nck = CogniPy.ARS.NameingConventionKind.CamelCase, string defaultPfx = null, bool convertToAst = true, bool insertDependentAsts = true, OWLMissingOntologyReferencesStrategy missingReferencesStrategy = OWLMissingOntologyReferencesStrategy.Throw_Exception, bool loadAnnotations = true, bool useDefaultNamespaceAsFullUri = false, bool isFirstLevel = true)
        {

        RETRY:
            if (rootontology == null)
            {
                recursivelyLoadedOntologies = new List<string>();
                recursivelyLoadedOntologies.Add(source);
            }
            NewError = false;
            OWLReferencesProblem = false;

            string oldBaseNamespace = null;
            tags = null;
            brokenImports = null;
            dlast = null;
            if (defaultPfx != null && (defaultPfx.EndsWith("#") || defaultPfx.EndsWith("/")))
                defaultPfx.Replace("#", "").Replace("/", "");

            bool isendl = (whatToLoad == WhatToLoad.CnlFromString);
            bool fromString = (whatToLoad != WhatToLoad.FromUri);
            if (!fromString)
            {
                try
                {
                    if (System.IO.Path.GetExtension(source) == ".encnl")
                        isendl = true;
                }
                catch (Exception e)
                {
                    if (!ErrorsOnImports.ContainsKey(source))
                        ErrorsOnImports.Add(source, new HashSet<string>());
                    ErrorsOnImports[rootontology ?? source].Add(e.Message);
                    if (!ExceptionsOnImports.ContainsKey(source))
                        ExceptionsOnImports.Add(source, new HashSet<Exception>());
                    ExceptionsOnImports[rootontology ?? source].Add(e);
                    return false;
                }
                if (LoadedOntologies.ContainsKey(source) && LoadedOntologies[source].FENamespace != defaultPfx) // the ontology uri is the same but the user changed the FENamespace!
                {
                    oldBaseNamespace = LoadedOntologies[source].baseNamespace;
                    this.ForgetOntology(source, false); // remove the ontology from the loadedOntology list but do not remove them from the owlManager so that it will be faster to check them again
                }
                else
                {
                    if (LoadedOntologies.ContainsKey(source))
                    {
                        LoadedOntologies.Remove(source);
                        LoadedDlAsts.Remove(source);
                    }
                    oldBaseNamespace = source;
                }
            }

            if (isendl) // encnl reference
            {
                dlast = null;

                try
                {
                    if (!fromString && LoadedOntologies.ContainsKey(source)) // check that the ontology was not already loaded.
                    {
                        if (!BrokenImports.ContainsKey(source))
                        {
                            tags = LoadedOntologies[source];
                            dlast = LoadedDlAsts[source];
                            return true;
                        }
                        else
                        {
                            brokenImports = BrokenImports[source];
                            return false;
                        }
                    }

                    string txtToParse;
                    if (!fromString)
                    {
                        txtToParse = System.IO.File.ReadAllText(source);
                    }
                    else
                    {
                        txtToParse = source;
                    }
                    var sentences = tools.splitSentences(txtToParse).ToList();

                    tags = new ReferenceTags();

                    dlast = new CNL.DL.Paragraph(null) { Statements = new List<CNL.DL.Statement>() };
                    var cnlDefaultNamespace = "http://www.cognitum.eu/onto";
                    foreach (var sentence in sentences)
                    {
                        if (sentence.isAnnotation) //[T1-FE2-579] //&& loadAnnotations )
                        {
                            var dp = sentence.line.IndexOf(':');
                            var x = sentence.line.Substring(0, dp);
                            if (x.Trim() == "Namespace")
                            {
                                var ontologyIri = sentence.line.Substring(dp + 1).Trim();
                                if (ontologyIri.EndsWith(".")) ontologyIri = ontologyIri.Substring(0, ontologyIri.Length - 1);
                                if (ontologyIri.StartsWith("\'") && ontologyIri.Length > 2)
                                    ontologyIri = ontologyIri.Substring(1, ontologyIri.Length - 2).Replace("\'\'", "\'");
                                ontologyIri = ontologyIri.Replace(" ", "");
                                ontologyIri = ontologyIri.Replace("\\", "/");
                                if (System.IO.Path.IsPathRooted(ontologyIri))
                                    ontologyIri = "file:" + ontologyIri;
                                cnlDefaultNamespace = ontologyIri;
                            }
                            else if (x.Trim() == "References") // found another reference in the file --> load it.
                            {
                                var refs = CNLTools.ParseReferences(sentence.line); //.Substring(dp)
                                foreach (Match match in refs)
                                {
                                    string onto, pfx, ns;
                                    CNLTools.GetReferencePieces(match, out pfx, out onto, out ns);
                                    {
                                        ReferenceTags innertags;
                                        HashSet<string> innerbrokenImports;
                                        CogniPy.CNL.DL.Paragraph innerdlast;

                                        if ((String.IsNullOrWhiteSpace(onto) || !loadAnnotations) && !AllReferences.ContainsKey(pfx)) // shortcut for load annotations
                                        {
                                            // case in which there is no location --> it is not an import.
                                            if (String.IsNullOrEmpty(ns))
                                                if (!String.IsNullOrEmpty(onto))
                                                    AllReferences.Add(pfx, onto);
                                                else
                                                    AllReferences.Add(pfx, "http://unknown.prefix/" + pfx);
                                            else
                                                AllReferences.Add(pfx, ns);

                                            continue;
                                        }

                                        if (String.IsNullOrWhiteSpace(onto))
                                            continue;

                                        if (!new Uri(onto, UriKind.RelativeOrAbsolute).IsAbsoluteUri)
                                        {
                                            var filePathLoc = (!fromString) ? source : _currentFilePath;
                                            onto = System.IO.Path.GetFullPath(System.IO.Path.Combine(System.IO.Path.GetDirectoryName(filePathLoc), onto));
                                        }

                                        if (onto == _currentFilePath) // the file we need to analyze next is the same as the current file. Skip
                                            continue;

                                        if (!recursivelyLoadedOntologies.Contains(onto))
                                        {
                                            recursivelyLoadedOntologies.Add(onto);
                                            bool loaded = false;
                                            if (LoadOntology(WhatToLoad.FromUri, tools, onto, out innerbrokenImports, out innertags, out innerdlast, source, nck, pfx, convertToAst, insertDependentAsts, missingReferencesStrategy, true, false, false))
                                            {
                                                loaded = true;
                                                if (!DirectImports.ContainsKey(innertags.baseNamespace))
                                                {
                                                    DirectImports.Add(innertags.baseNamespace, onto);
                                                    if (!AllReferences.ContainsKey(pfx))
                                                        AllReferences.Add(pfx, innertags.baseNamespace);
                                                }
                                                tags.concepts.UnionWith(innertags.concepts);
                                                tags.roles.UnionWith(innertags.roles);
                                                tags.dataroles.UnionWith(innertags.dataroles);
                                                tags.instances.UnionWith(innertags.instances);
                                                tags.datatypes.UnionWith(innertags.datatypes);
                                                tags.referencedTags.Add(innertags);
                                                if (insertDependentAsts && innerdlast != null)
                                                {
                                                    dlast.Statements.AddRange(innerdlast.Statements);
                                                }
                                            }
                                            else if (brokenImports != null)
                                                brokenImports.UnionWith(innerbrokenImports);
                                            else
                                                brokenImports = innerbrokenImports;

                                            if (!AllReferences.ContainsKey(pfx))
                                            {
                                                if (!loaded && !String.IsNullOrWhiteSpace(ns))
                                                    AllReferences.Add(pfx, ns);
                                                else if (!loaded)
                                                    AllReferences.Add(pfx, onto);
                                            }
                                        }
                                    }
                                }
                            }
                            var ast = tools.GetEN2DLAst(sentence.line, false);
                            if (ast != null)
                                dlast.Statements.AddRange(ast.Statements);
                        }
                        else
                        {
                            try
                            {
                                try
                                {
                                    if (string.IsNullOrWhiteSpace(sentence.line))
                                        continue;

                                    var ast = tools.GetEN2DLAst(sentence.line, true);

                                    if (!string.IsNullOrEmpty(defaultPfx))
                                    {
                                        CogniPy.CNL.DL.SetDefaultPfxVisitor defPfxVis = new CogniPy.CNL.DL.SetDefaultPfxVisitor(defaultPfx);
                                        defPfxVis.Visit(ast);
                                    }

                                    dlast.Statements.AddRange(ast.Statements);

                                    var sign = tools.GetDLAstSignature(ast);
                                    foreach (var smb in sign)
                                    {
                                        var inam = smb.Item2;
                                        if (smb.Item1 == ARS.EntityKind.Instance)
                                        {
                                            var en = CogniPy.CNL.EN.ENNameingConvention.FromDL(new CogniPy.CNL.DL.DlName() { id = inam }, true).Split();
                                            tags.instances.Add(inam);//(en.Combine().id);
                                        }
                                        else
                                        {
                                            var en = CogniPy.CNL.EN.ENNameingConvention.FromDL(new CogniPy.CNL.DL.DlName() { id = inam }, false).Split();
                                            if (smb.Item1 == ARS.EntityKind.Role)
                                                tags.roles.Add(inam);//(en.Combine().id);
                                            else if (smb.Item1 == ARS.EntityKind.DataRole)
                                                tags.dataroles.Add(inam);//(en.Combine().id);
                                            else if (smb.Item1 == ARS.EntityKind.Concept)
                                                tags.concepts.Add(inam);//(en.Combine().id);
                                            else if (smb.Item1 == ARS.EntityKind.DataType)
                                                tags.datatypes.Add(inam);//(en.Combine().id);
                                        }
                                    }
                                }
                                catch (ParseException ex)
                                {
                                    SyntaxErrors.Add(new ParseException(ex.Message,ex.Pos+sentence.start-1, txtToParse));
                                    continue;
                                }
                            }
                            catch (Exception ex)
                            {
                                Debug.WriteLine("Krr22" + ex.Message);
                            }
                        }

                        DefaultNamespace = cnlDefaultNamespace;
                    }
                    if (SyntaxErrors.Count == 1)
                        throw SyntaxErrors[0];
                    else if (SyntaxErrors.Count > 0)
                        throw new AggregateParseException(SyntaxErrors);

                    if (dlast == null || dlast.Statements.Count == 0)
                        throw new ParseException(txtToParse.Trim().Length==0?"Empty input": "All cnl sentences are incorrect." , 0, txtToParse);

                    if (String.IsNullOrEmpty(DefaultNamespace)) DefaultNamespace = "http://www.ontorion.com/ontologies/Ontology" + Guid.NewGuid().ToString("N");

                    tags.baseNamespace = DefaultNamespace;
                    if (!String.IsNullOrEmpty(defaultPfx))
                        tags.FENamespace = defaultPfx;
                    LoadedOntologies.Add(source, tags);
                    LoadedDlAsts.Add(source, dlast);
                    tags.dlAst = dlast;
                    return true;
                }
                catch (Exception e)
                {

                    if (!BrokenImports.ContainsKey(rootontology ?? source))
                        BrokenImports.Add(rootontology ?? source, new HashSet<string>());
                    BrokenImports[rootontology ?? source].Add(source);
                    if (!ErrorsOnImports.ContainsKey(rootontology ?? source))
                    {
                        ErrorsOnImports.Add(rootontology ?? source, new HashSet<string>());
                        ExceptionsOnImports.Add(rootontology ?? source, new HashSet<Exception>());
                    }

                    if (ErrorsOnImports.ContainsKey(rootontology ?? source) && !ErrorsOnImports[rootontology ?? source].Contains(e.Message))
                    {
                        ErrorsOnImports[rootontology ?? source].Add(e.Message);
                        ExceptionsOnImports[rootontology ?? source].Add(e);
                    }

                    if(BrokenImports.ContainsKey(source))
                        brokenImports = BrokenImports[source];
                    return false;
                }
            }
            else // owl reference
            {
                try
                {
                    try
                    {
                        // check if the input uri is local...
                        string sourcePath = source.Replace("file:/", ""); // remove the file:/ (this is there is it was already recognized as a file)
                        sourcePath = Uri.UnescapeDataString(sourcePath);
                        if (!fromString)
                        {
                            VirtualSiteIRIMapper mapper = null;
                            if (System.IO.Path.IsPathRooted(sourcePath) && System.IO.File.Exists(sourcePath))
                            {
                                mapper = new VirtualSiteIRIMapper(this) { currDir = System.IO.Path.GetDirectoryName(sourcePath) }; // add a local iri mapper
                                source = UriFromFilePath(sourcePath);
                            }
                            else
                            {
                                if (!String.IsNullOrEmpty(_currentFilePath) && System.IO.Path.IsPathRooted(sourcePath) && System.IO.File.Exists(sourcePath))
                                    mapper = new VirtualSiteIRIMapper(this) { currDir = System.IO.Path.GetDirectoryName(_currentFilePath) }; // add a local iri mapper
                                else
                                    mapper = new VirtualSiteIRIMapper(this) { currDir = null }; // add a local iri mapper
                            }
                            owlManager.Value.addIRIMapper(mapper);
                        }
                    }
                    catch { }

                    if (!fromString && LoadedOntologies.ContainsKey(source))
                    {
                        if (!BrokenImports.ContainsKey(source) || missingReferencesStrategy == OWLMissingOntologyReferencesStrategy.Retry)
                        {
                            tags = LoadedOntologies[source];
                            dlast = LoadedDlAsts[source];
                            if (BrokenImports.ContainsKey(source))
                            {
                                brokenImports = BrokenImports[source];
                                OWLReferencesProblem = true;
                            }
                            return true;
                        }
                        else
                        {
                            brokenImports = BrokenImports[source];
                            return false;
                        }
                    }

                    curentlyLoadingOnt = fromString ? "" : source;

                    OWLOntology ontology = null;

                    try
                    {
                        // http://ehc.ac/p/owlapi/mailman/message/28914114/

                        bool containsOntology = false;
                        foreach (org.semanticweb.owlapi.model.OWLOntology owlOntology in owlManager.Value.getOntologies().toArray().ToList())
                        {
                            var iri = owlOntology.getOntologyID().getVersionIRI();
                            if (iri != null)
                                if (iri.compareTo(OWLPathUriTools.Path2IRI(source)) == 0 && !fromString)
                                {
                                    ontology = owlOntology;
                                    containsOntology = true;
                                    break;
                                }
                        }

                        if (!fromString && convertToAst && !containsOntology)
                        {
                            ontology = owlManager.Value.loadOntology(OWLPathUriTools.Path2IRI(source));
                        }

                        /*
                         * some boolean algebra to prove that the previous if-elseif-else structure is equivalent to this.
                         *    a => fromString 
                         *    b => convertToAST
                         *    c => containsOntology
                         *    
                         *    Previously we had if (~a . b . ~c) {A} else if (~a . c) {B} else {C} 
                         *    
                         *    Reaching  A and B is clearly equivalent however C is not obvious. To prove it is equivalent we treat the previous else case as the complement of the union of A and B: 
                         *    
                         *    ~ ((~a . b . ~c) + (~a . c)) <=> ~(~a . (c + b . ~c)) <=> 
                         *    
                         *    ~(~a . (b + c)) <=> 
                         *
                         *     [ a          +       ~b          .       ~c      ]                                    
                         *      fromString || !containsOntology && !convertToAst
                         *      
                         *   The case C is now the else if below
                         */
                        else if (fromString || !containsOntology && !convertToAst)
                        {
                            if (fromString)
                            {
                                var ontin = new org.semanticweb.owlapi.io.StringDocumentSource(source);
                                ontology = owlManager.Value.loadOntologyFromOntologyDocument(ontin);
                            }
                            else
                            {
                                ontodoc = new XmlDocument();
                                ontodoc.Load(source);
                                var ontin = new org.semanticweb.owlapi.io.StringDocumentSource(ontodoc.OuterXml);
                                ontology = owlManager.Value.loadOntologyFromOntologyDocument(ontin);
                            }
                        }

                        if (NewError && missingReferencesStrategy != OWLMissingOntologyReferencesStrategy.Retry)
                        {
                            owlManager.Value.removeOntology(ontology);
                            brokenImports = BrokenImports[source];
                            return false;
                        }
                        else if (NewError && missingReferencesStrategy == OWLMissingOntologyReferencesStrategy.Retry)
                        {
                            brokenImports = BrokenImports[source];
                            OWLReferencesProblem = true;
                        }

                        if (convertToAst)
                        {
                            CogniPy.ARS.InvTransform invtransform = new CogniPy.ARS.InvTransform(owlManager.Value, ontology, source, nck, getForms);

                            dlast = SetOWLOntologyTagsAndAst(ontology, invtransform, source, defaultPfx, out tags, useDefaultNamespaceAsFullUri);

                            if (!String.IsNullOrWhiteSpace(invtransform.defaultNs))
                                DefaultNamespace = invtransform.defaultNs;

                            var refs = invtransform.Pfx2ns.Keys;
                            foreach (var refi in refs)
                            {
                                if (!string.IsNullOrEmpty(refi))
                                {
                                    AllReferences[refi] = invtransform.Pfx2ns[refi];
                                }
                            }

                            if (isFirstLevel)
                                DirectImports = new Dictionary<string, string>();

                            var imports = ontology.getImportsDeclarations().iterator();
                            while (imports.hasNext()) // there are direct imports....
                            {
                                // is it right not to add a direct import in DirectImports if reff is null?? 
                                // This could be because no default namespace is defined inside but still there is this import....
                                var impp = imports.next() as OWLImportsDeclaration;
                                OWLOntology impOnt = owlManager.Value.getImportedOntology(impp);

                                var reff = impp.getIRI().toString();

                                var impSource = reff;

                                if (impOnt != null) // if imported ontology if different from null --> we have imported it, let's get the DocumentIRI (location) and OntoriogyID (namespace)
                                {
                                    if (impOnt.getOntologyID().getOntologyIRI() != null)
                                    {

                                        var refKey = AllReferences.FirstOrDefault(reference => reference.Value.Contains(reff)).Key;

                                        // replace the reff in AllReferences.
                                        reff = impOnt.getOntologyID().getOntologyIRI().toString();

                                        if (!reff.EndsWith("/") && !reff.EndsWith("#") && !reff.Contains("#"))
                                            reff += "#";


                                        if (!String.IsNullOrEmpty(refKey))
                                            if (AllReferences.ContainsKey(refKey))
                                                AllReferences[refKey] = reff;

                                        IRI importedIRI = owlManager.Value.getOntologyDocumentIRI(impOnt);
                                        if (importedIRI != null)
                                        {
                                            // [a]
                                            impSource = importedIRI.toString();
                                        }
                                    }
                                }

                                if (!reff.EndsWith("/") && !reff.EndsWith("#") && !reff.Contains("#"))
                                    reff += "#";

                                // At this point impSource should be updated with the appropriate namespace so, all if-else if belowe would be wasteless
                                // and should be replaced by  the single sentence:  
                                if (!String.IsNullOrEmpty(reff))
                                    DirectImports.Add(reff, impSource);
                                if (String.IsNullOrEmpty(AllReferences.FirstOrDefault(reference => reference.Value.Contains(reff)).Key) && String.IsNullOrEmpty(AllReferences.FirstOrDefault(reference => reference.Value.Contains(reff + "#")).Key) && String.IsNullOrEmpty(AllReferences.FirstOrDefault(reference => reference.Value.Contains(reff + "/")).Key))
                                {
                                    string pfx = null;
                                    if (reff.Split('/') != null)
                                    {
                                        var spl = reff.Split('/');
                                        var end = spl.Last();
                                        if(end=="")
                                        {
                                            if (spl.Length > 1)
                                                end = spl[spl.Length - 2];
                                        }
                                        pfx = end.Replace("#", "").Replace("/", "") + "PFX"+Guid.NewGuid().ToString().Replace("-","");
                                    }
                                    else
                                    {
                                        pfx = reff + "pfx";
                                    }
                                    // this will solve a problem that appeared when a referenced ontology was using the same pfx used in the another referenced ontology or in the main ontology.
                                    // nevertheless it means that AllReferences will not contain all references. Maybe we should change the AllReferences to a <string,List<string>> dictionary?
                                    if (!AllReferences.ContainsKey(pfx))
                                        AllReferences.Add(pfx, reff);
                                }

                                // if no error was found for the imported ontology, add the referencedTags and ast to the memory.
                                if (!ErrorsOnImports.ContainsKey(reff) || !ErrorsOnImports.ContainsKey(reff.Replace("#", "")))
                                {
                                    string impPrefix = AllReferences.FirstOrDefault(x => x.Value == reff).Key;
                                    ReferenceTags impTags;
                                    CNL.DL.Paragraph impAst = null;

                                    // This case is reached for imports as http://purl.org/dc/elements/1.1/ which for some reason is detected as importable (no import error in the owlapi) but never gets imported
                                    if (impOnt != null)
                                    {
                                        invtransform = new ARS.InvTransform(owlManager.Value, impOnt, impSource, nck, getForms);
                                        impAst = SetOWLOntologyTagsAndAst(impOnt, invtransform, impSource, impPrefix, out impTags, useDefaultNamespaceAsFullUri);
                                        impTags.ontologyLocation = impSource;
                                        tags.referencedTags.Add(impTags);
                                    }

                                    if (insertDependentAsts && impAst != null)
                                    {
                                        dlast.Statements.AddRange(impAst.Statements);
                                    }
                                }
                            }
                        }

                        // BUG FIX START --- FE2-190 (http://ehc.ac/p/owlapi/mailman/message/28914114/)
                        var logIRI = ((org.semanticweb.owlapi.model.OWLOntology)ontology).getOntologyID().getOntologyIRI();
                        if (logIRI == null)
                        {
                            string iri = source;
                            if (!source.EndsWith("/") && !source.EndsWith("#") && !source.Contains("#"))
                                iri += "#";
                            logIRI = IRI.create(iri); // if the ontology doesn't contain an IRI give the same as the source 
                        }

                        var newID = new org.semanticweb.owlapi.model.OWLOntologyID(logIRI, OWLPathUriTools.Path2IRI(source));

                        owlManager.Value.applyChange(new org.semanticweb.owlapi.model.SetOntologyID((org.semanticweb.owlapi.model.OWLOntology)ontology, newID));
                        // BUG FIX END --- FE2-190 

                        //owlManager.Value.removeOntology(ontology);
                        return true;
                    }
                    catch (OWLOntologyAlreadyExistsException)
                    {
                        CreateOWLManager();
                        ForgetOntology(source, false);
                        goto RETRY;
                    }
                    catch (Exception ex)
                    {
                        if (ontology != null)
                            owlManager.Value.removeOntology(ontology);
                        if (BrokenImports.ContainsKey(source))
                            brokenImports = BrokenImports[source];
                        if (!ErrorsOnImports.ContainsKey(rootontology ?? source))
                        {
                            ErrorsOnImports.Add(rootontology ?? source, new HashSet<string>());
                            ExceptionsOnImports.Add(rootontology ?? source, new HashSet<Exception>());
                        }

                        if (ErrorsOnImports.ContainsKey(rootontology ?? source) && !ErrorsOnImports[rootontology ?? source].Contains(ex.Message))
                        {
                            ErrorsOnImports[rootontology ?? source].Add(ex.Message);
                            ExceptionsOnImports[rootontology ?? source].Add(ex);
                        }

                        return false;
                    }
                }
                catch (Exception e)
                {
                    if (BrokenImports.ContainsKey(source))
                        brokenImports = BrokenImports[source];
                    if (!ErrorsOnImports.ContainsKey(rootontology ?? source))
                    {
                        ErrorsOnImports.Add(rootontology ?? source, new HashSet<string>());
                        ExceptionsOnImports.Add(rootontology ?? source, new HashSet<Exception>());
                    }

                    if (ErrorsOnImports.ContainsKey(rootontology ?? source) && !ErrorsOnImports[rootontology ?? source].Contains(e.Message))
                    {
                        ErrorsOnImports[rootontology ?? source].Add(e.Message);
                        ExceptionsOnImports[rootontology ?? source].Add(e);
                    }
                }
            }
            return false;
        }

        internal CNL.DL.Paragraph SetOWLOntologyTagsAndAst(OWLOntology ontology, CogniPy.ARS.InvTransform invtransform, string source, string defaultPfx, out ReferenceTags tags, bool useDefaultNamespaceAsFullUri = false)
        {
            tags = new ReferenceTags();
            CNL.DL.Paragraph dlast = invtransform.Convert(ontology);
            tags.uriMapping = invtransform.UriMappings;
            tags.invUriMapping = invtransform.InvUriMappings;

            if (ontology.getOntologyID().getOntologyIRI() != null)
                tags.baseNamespace = ontology.getOntologyID().getOntologyIRI().toString().Split('#').First();
            else
                tags.baseNamespace = source;

            if (!tags.baseNamespace.EndsWith("/") && !tags.baseNamespace.EndsWith("#") && !tags.baseNamespace.Contains("#"))
                tags.baseNamespace += "#";

            if (!String.IsNullOrEmpty(defaultPfx))
                tags.FENamespace = defaultPfx;
            else // case in which there are no prefixes defined for this element.
            {
                string[] baseNamespEl = tags.baseNamespace.Replace("#", "").Split('/');
                if (baseNamespEl.Count() > 0)
                {
                    tags.FENamespace = baseNamespEl[baseNamespEl.Count() - 1];
                }
            }

            if (!string.IsNullOrEmpty(defaultPfx))
            {
                CogniPy.CNL.DL.SetDefaultPfxVisitor defPfxVis = new CogniPy.CNL.DL.SetDefaultPfxVisitor(defaultPfx);
                defPfxVis.Visit(dlast);
            }
            else if (useDefaultNamespaceAsFullUri && !String.IsNullOrWhiteSpace(tags.baseNamespace))
            {
                CogniPy.CNL.DL.SetDefaultPfxVisitor defPfxVis = new CogniPy.CNL.DL.SetDefaultPfxVisitor(null, tags.baseNamespace);
                defPfxVis.Visit(dlast);
            }

            var ser = new CogniPy.CNL.DL.Serializer(false);
            ser.Serialize(dlast);
            var sign = ser.GetTaggedSignature();


            foreach (var smb in sign)
            {
                if (smb.Item1 == ARS.EntityKind.Instance)
                {
                    var inam = smb.Item2;
                    if (!String.IsNullOrWhiteSpace(inam) && !inam.StartsWith("["))
                    {
                        if (inam.StartsWith("_")) inam = inam.Substring(1);
                        tags.instances.Add(inam);
                    }
                }
                if (smb.Item1 == ARS.EntityKind.Role && !String.IsNullOrWhiteSpace(smb.Item2))
                    tags.roles.Add(smb.Item2);
                if (smb.Item1 == ARS.EntityKind.DataRole && !String.IsNullOrWhiteSpace(smb.Item2))
                    tags.dataroles.Add(smb.Item2);
                if (smb.Item1 == ARS.EntityKind.Concept && !String.IsNullOrWhiteSpace(smb.Item2))
                    tags.concepts.Add(smb.Item2);
                if (smb.Item1 == ARS.EntityKind.DataType && !String.IsNullOrWhiteSpace(smb.Item2))
                    tags.datatypes.Add(smb.Item2);
            }

            if (!LoadedOntologies.ContainsKey(source))
            {
                LoadedOntologies.Add(source, tags);
                LoadedDlAsts.Add(source, dlast);
            }

            tags.dlAst = dlast;

            return dlast;
        }

        private bool isForgetAllCommand = false;
        public void ForgetAllOntologies()
        {
            isForgetAllCommand = true;
            CreateOWLManager();


            var k = LoadedOntologies.Keys.ToArray();
            foreach (var uri in k)
                ForgetOntology(uri, false);

            var k2 = BrokenImports.Keys.ToArray();
            foreach (var uri in k2)
                ForgetOntology(uri, false);

            ExceptionsOnImports = new Dictionary<string, HashSet<Exception>>();
            BrokenImports = new Dictionary<string, HashSet<string>>();
            ErrorsOnImports = new Dictionary<string, HashSet<string>>();
        }

        // If forgetOntology = true --> also remove the ontology from the owlManager (--> the only way I found is by creating again the owl manager)
        public void ForgetOntology(string uri, bool resetOntologyManager = true) // remove all informations about this ontology in the refence manager. 
        {
            bool isendl = false;
            try
            {
                if (System.IO.Path.GetExtension(uri) == ".encnl")
                    isendl = true;
            }
            catch
            {
                return;
            }


            if (!isendl) // for owl/rdf/....
            {
                if (!isForgetAllCommand && resetOntologyManager)
                {
                    if (owlManager.Value.getOntology(IRI.create(uri)) != null)
                        CreateOWLManager(); // !! this will erase ALL ontologies previously loaded.... it could not be what we expect when we are cleaning only one ontology! 
                    // The problem is that using  owlManager.Value.removeOntology(owlManager.Value.getOntology(IRI.create(uri))); is not removing the referenced ontologies errors
                    // --> if we do not do this, the next time we will load the ontology the missing import listener is not called again....
                }

                try
                {
                    if (System.IO.Path.IsPathRooted(uri))
                        if (System.IO.File.Exists(uri))
                            uri = UriFromFilePath(uri);
                }
                catch { }
            }

            if (LoadedOntologies.ContainsKey(uri)) // remove the ontology if loaded
            {
                //if (owlManager.Value.getOntology(IRI.create(uri)) != null)
                //    owlManager.Value.removeOntology(owlManager.Value.getOntology(IRI.create(uri)));
                LoadedOntologies.Remove(uri);
                LoadedDlAsts.Remove(uri);
            }

            if (BrokenImports.ContainsKey(uri)) // forget all import problem
            {
                foreach (string relOnt in BrokenImports[uri])
                {
                    if (ExceptionsOnImports.ContainsKey(relOnt))
                    { // remove exceptions
                        ExceptionsOnImports.Remove(relOnt);
                    }

                    if (ErrorsOnImports.ContainsKey(relOnt)) // remove errors
                        ErrorsOnImports.Remove(relOnt);
                }
                BrokenImports.Remove(uri);
            }

            if (ExceptionsOnImports.ContainsKey(uri))
            {
                ExceptionsOnImports.Remove(uri);
            }

            if (ErrorsOnImports.ContainsKey(uri))
            {
                ErrorsOnImports.Remove(uri);
            }
        }

        OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
        public void setConnectionProxy(string host, int port)
        {
            setConnectionProxy(host, port, null, null);
        }

        public void setConnectionProxy(string host, int port, string userName, string password)
        {
            if (!String.IsNullOrWhiteSpace(host))
            {
                java.net.SocketAddress addr = new
                    java.net.InetSocketAddress(host, port);
                java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, addr);
                if (!String.IsNullOrWhiteSpace(userName) && !String.IsNullOrWhiteSpace(password))
                    config = config.setProxy(proxy, userName, password);
                else
                    config = config.setProxy(proxy);
            }
            else
            {
                config = config.setProxy(null);
            }
            if (owlManager.IsValueCreated)
                owlManager.Value.setOntologyLoaderConfiguration(config);
        }
    }
}
