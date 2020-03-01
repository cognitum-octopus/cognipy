using CogniPy.ARS;
using CogniPy.CNL;
using CogniPy.Executing.HermiTClient;
using CogniPy.models;
using CogniPy.Splitting;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Text.RegularExpressions;

namespace CogniPy
{

    public class CogniPySvr : ICogniPySvr
    {

        CogniPy.CNL.DL.Paragraph paragraph;
        HermiTReasoningService _reasoner = null;
        HermiTReasoningService reasoner
        {
            get
            {
                if (_reasoner == null)
                    throw new InvalidOperationException("The reasoner is not initialized. You need to call Load... before calling this method.");
                else
                    return _reasoner;
            }
            set { _reasoner = value; }
        }

        CogniPy.ReferenceManager.ReferenceTags tags = new CogniPy.ReferenceManager.ReferenceTags();
        CNLTools tools = null;

        Dictionary<string, string> AllReferences;
        //        static SpellFactory engine = null;
        //        static object engineGuard = new object();
        //        static bool engineLoaded = false;

        bool debugModeOn = false;
        bool modalChecker = false;
        bool alreadyMaterialized = false;
        public bool PassParamsInCNL { set; get; }
        string ontologyBase;
        IOwlNameingConvention namc = new OwlNameingConventionCamelCase();
        public dynamic Outer;
        public bool SWRLOnly = false;

        HashSet<string> objectroles = new HashSet<string>();
        HashSet<string> dataroles = new HashSet<string>();
        HashSet<string> instances = new HashSet<string>();
        HashSet<string> concepts = new HashSet<string>();
        HashSet<string> datatypes = new HashSet<string>();
        Dictionary<string, Dictionary<string, List<AnnotationResult>>> annotations = new Dictionary<string, Dictionary<string, List<AnnotationResult>>>();

        static string KWDBEG = @"\b(?<!(\-|[A-z]|[0-9]))";
        static string KWDEND = @"(?!\-)\b";
        static string KWDTHE = "(the-\".*\"\\s*)";
        static Regex kwds = null;

        static DLToOWLNameConv pfxman = new DLToOWLNameConv();

        static CogniPySvr()
        {
            //remove log4j appender warning
            {
                org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
                var app = new org.apache.log4j.ConsoleAppender(
                    new org.apache.log4j.PatternLayout(org.apache.log4j.PatternLayout.DEFAULT_CONVERSION_PATTERN));
                app.setThreshold(org.apache.log4j.Priority.FATAL);
                root.addAppender(app);
            }

        }

        public CogniPySvr()
        {
            var tpy = typeof(CogniPy.CNL.EN.CNLFactory);
            CNLTools.RegisterCNLFactory("en", tpy);
            AllReferences = new Dictionary<string, string>();
            tools = new CNLTools("en");

            //lock (engineGuard)
            //{
            //    if (engineLoaded)
            //        return;
            //    LanguageConfig enConfig = new LanguageConfig();
            //    enConfig.LanguageCode = "en";
            //    enConfig.Processors = 1;
            //    enConfig.HunspellAffFile = "en_US.aff";
            //    enConfig.HunspellDictFile = "en_US.dic";
            //    enConfig.HunspellKey = "";
            //    enConfig.HyphenDictFile = "hyph_en_US.dic";
            //    //enConfig. MyThesIdxFile = "th_en_US_new.idx";
            //    enConfig.MyThesDatFile = "th_en_US_new.dat";
            //    Hunspell.NativeDllPath = "";
            //    engine = new SpellFactory(enConfig);
            //    engineLoaded = true;
            //}
            kwds = new Regex(
                KWDBEG + @"(" + string.Join(@"|", from k in tools.GetAllKeywords() select Regex.Escape(k)) + "|" + KWDTHE + @")" + KWDEND + @"|\.|,|\(|\)|\'"
                , System.Text.RegularExpressions.RegexOptions.IgnoreCase | RegexOptions.ExplicitCapture);

        }

        //static IEnumerable<string> GetForms(string word)
        //{
        //    HashSet<string> ret = new HashSet<string>();
        //    lock (engineGuard)
        //    {
        //        return engine.Stem(word);
        //    }
        //}

        public string GetStatementId(string cnlStatement)
        {
            var dlAst = tools.GetEN2DLAst(cnlStatement);
            return tools.SerializeDLAst(dlAst).Replace("\r\n", "");
        }

        //      bool traceOn = true;
        public void LoadCnl(string filename, bool loadAnnotations, bool materialize, bool modalChecker = false)
        {
            //            System.Diagnostics.Trace.WriteIf(this.traceOn, string.Format("onto.LoadCnl(\'{0}\',{1},{2})",Path.GetFullPath( filename).Replace("\\","/"),loadAnnotations,materialize) );
            LoadCnl(filename, null, loadAnnotations, materialize, modalChecker);
        }

        public void LoadRdf(string uri, bool loadAnnotations, bool materialize, bool modalChecker = false)
        {
            LoadRdf(uri, null, loadAnnotations, materialize, modalChecker);
        }


        public void LoadRdfFromString(string rdf, bool loadAnnotations, bool materialize, bool modalChecker = false)
        {
            LoadRdfFromString(rdf, null, loadAnnotations, materialize, modalChecker);
        }

        public void LoadCnlFromString(string cnl, bool loadAnnotations, bool materialize, bool modalChecker = false)
        {
            LoadCnlFromString(cnl, null, loadAnnotations, materialize, modalChecker);
        }

        public string CnlFromUri(string uri, string type)
        {
            var n = reasoner.renderEntityFromUri(uri, (type == null || type == "instance") ? CogniPy.ARS.EntityKind.Instance : (type == "concept" ? CogniPy.ARS.EntityKind.Concept : CogniPy.ARS.EntityKind.Role));
            if (n == null)
                return "";

            var enN = CogniPy.CNL.EN.ENNameingConvention.FromDL(new CogniPy.CNL.DL.DlName() { id = n }, (type == null || type == "instance") ? CogniPy.CNL.EN.endict.WordKind.NormalForm : (type == "concept" ? CogniPy.CNL.EN.endict.WordKind.NormalForm : CogniPy.CNL.EN.endict.WordKind.PastParticiple), type == "instance");
            return enN.id;
        }

        public string UriFromCnl(string cnl, string type)
        {
            var dl = CogniPy.CNL.EN.ENNameingConvention.ToDL(new CogniPy.CNL.EN.EnName() { id = cnl }, (type == null || type == "instance") ? CogniPy.CNL.EN.endict.WordKind.NormalForm : (type == "concept" ? CogniPy.CNL.EN.endict.WordKind.NormalForm : CogniPy.CNL.EN.endict.WordKind.PastParticiple)).id;

            var n = reasoner.renderUriFromEntity(dl, (type == null || type == "instance") ? CogniPy.ARS.EntityKind.Instance : (type == "concept" ? CogniPy.ARS.EntityKind.Concept : CogniPy.ARS.EntityKind.Role));
            if (n == null)
                return "";

            return n;
        }


        public void SetValue(string instance, string datarole, object val)
        {
            reasoner.SetValue(instance, datarole, val);
        }

        private void Load(ReferenceManager.WhatToLoad whatToLoad, string contentToLoad, CogniPy.CNL.DL.Paragraph impliAst, bool loadAnns = false, bool materialize = false, bool modalChecker = false)
        {
            ReferenceManager rm = new ReferenceManager(/*GetForms*/null);
            HashSet<string> brokenImports;
            if (whatToLoad == ReferenceManager.WhatToLoad.FromUri)
                rm.CurrentFilePath = contentToLoad;

            if (!rm.LoadOntology(whatToLoad, tools, contentToLoad,
                out brokenImports, out tags, out paragraph,
                null, CogniPy.ARS.NameingConventionKind.Smart, null, true, loadAnnotations: loadAnns))
            {
                var excepts = rm.GetExceptionsOnImports(contentToLoad);
                if (excepts.Count() > 0)
                    throw excepts.First();
                else
                    throw new InvalidOperationException("Unknown error during import");
            }

            this.ontologyBase = rm.DefaultNamespace;
            var pfx2nss = rm.AllReferences.ToDictionary(i => i.Key, i => i.Value);
            AllReferences = pfx2nss;
            ontologyBase = ontologyBase ?? "http://ontorion.com/unknown.owl";
            rm.DefaultNamespace = ontologyBase;

            var r = new HermiTReasoningService(paragraph, impliAst, ReasoningMode.RL, namc, ontologyBase, uriMapping(), invUriMapping(), pfx2nss);
            this.modalChecker = modalChecker;
            r.TheAccessObject = this;
            r.Outer = Outer;
            reasoner = r;

            if (materialize)
                Materialize();

            if (loadAnns)
                LoadAnnotations(paragraph);
        }



        private void LoadAnnotations(CogniPy.CNL.DL.Paragraph paragraph)
        {

            CogniPy.CNL.AnnotationManager annotManToTranslate = new AnnotationManager();

            foreach (var stmt in paragraph.Statements)
            {
                if (stmt is CogniPy.CNL.DL.Annotation)
                {
                    var ann = stmt as CogniPy.CNL.DL.Annotation;
                    var cnlSent = tools.GetENDLFromAst(ann, true);

                    if (cnlSent.StartsWith(CogniPy.CNL.AnnotationManager.ANNOTATION_START))
                    {
                        CogniPy.CNL.AnnotationManager annotMan = new CogniPy.CNL.AnnotationManager();
                        annotMan.loadW3CAnnotationsFromText(cnlSent, false, x => CogniPy.CNL.EN.ENNameingConvention.ToDL(new CogniPy.CNL.EN.EnName() { id = x }, CogniPy.CNL.EN.endict.WordKind.NormalForm).id);

                        annotManToTranslate.appendAnnotations(annotMan);

                        // add the prefix to ns manager the prefixes/namespace map derived from the annotations
                        foreach (var pfx2nsIn in annotMan.getPfx2NsDefinedInLoadedAnnotations())
                        {
                        }

                        // add the prefixes that where not recognized by the namespace manager.
                        foreach (var pfx in annotMan.getUnknownPrefixesInAnnotations())
                        {

                        }
                    }

                }
            }
            foreach (var subj in annotManToTranslate.GetAnnotationSubjects())
            {
                Dictionary<string, List<AnnotationResult>> annotsEl = new Dictionary<string, List<AnnotationResult>>();
                var subjn = EN(subj.Key, subj.Value == "Instance");
                foreach (var w3ann in annotManToTranslate.GetAnnotations(subj.Key))
                {
                    AnnotationResult annot = new AnnotationResult();
                    annot.Subject = subjn;
                    annot.SubjectType = subj.Value;
                    annot.Language = w3ann.Language;
                    annot.Property = EN(w3ann.Type, false);
                    annot.Value = w3ann.Value;
                    if (!annotsEl.ContainsKey(annot.Property))
                        annotsEl.Add(annot.Property, new List<AnnotationResult>());
                    annotsEl[annot.Property].Add(annot);
                }
                if (!annotations.ContainsKey(subjn))
                    annotations.Add(subjn, annotsEl);
                else
                {
                    foreach (var k in annotations.Keys)
                    {
                        if (annotations[subjn].ContainsKey(k))
                            annotations[subjn][k].AddRange(annotsEl[k]);
                        else
                            annotations[subjn].Add(k, annotsEl[k]);
                    }
                }
            }
        }

        void LoadRdf(string uri, CogniPy.CNL.DL.Paragraph impliAst, bool loadAnns = false, bool materialize = false, bool modalChecker = false)
        {
            Load(ReferenceManager.WhatToLoad.FromUri, uri, impliAst, loadAnns, materialize, modalChecker);
        }

        void LoadRdfFromString(string rdf, CogniPy.CNL.DL.Paragraph impliAst, bool loadAnns = false, bool materialize = false, bool modalChecker = false)
        {
            Load(ReferenceManager.WhatToLoad.OwlRdfFromString, rdf, impliAst, loadAnns, materialize, modalChecker);
        }

        void LoadCnl(string filename, CogniPy.CNL.DL.Paragraph impliAst, bool loadAnns = false, bool materialize = false, bool modalChecker = false)
        {
            Load(ReferenceManager.WhatToLoad.FromUri, filename, impliAst, loadAnns, materialize, modalChecker);
        }

        void LoadCnlFromString(string cnl, CogniPy.CNL.DL.Paragraph impliAst, bool loadAnns, bool materialize = false, bool modalChecker = false)
        {
            Load(ReferenceManager.WhatToLoad.CnlFromString, cnl, impliAst, loadAnns, materialize, modalChecker);
        }

        internal void GetOwlUriMapping(ref Dictionary<Tuple<EntityKind, string>, string> owlMapping, ReferenceManager.ReferenceTags tag)
        {
            foreach (var m in tag.uriMapping)
                if (!owlMapping.ContainsKey(m.Key))
                    owlMapping.Add(m.Key, m.Value);

            foreach (var t in tag.referencedTags)
                GetOwlUriMapping(ref owlMapping, t);
        }

        internal void GetOwlInvUriMapping(ref Dictionary<string, string> owlMapping, ReferenceManager.ReferenceTags tag)
        {
            foreach (var m in tag.invUriMapping)
                if (!owlMapping.ContainsKey(m.Key))
                    owlMapping.Add(m.Key, m.Value);

            foreach (var t in tag.referencedTags)
                GetOwlInvUriMapping(ref owlMapping, t);
        }

        internal Dictionary<string, string> invUriMapping()
        {
            var ret = new Dictionary<string, string>();
            GetOwlInvUriMapping(ref ret, tags);
            return ret;
        }

        internal Dictionary<Tuple<EntityKind, string>, string> uriMapping()
        {
            var ret = new Dictionary<Tuple<EntityKind, string>, string>();
            GetOwlUriMapping(ref ret, tags);
            return ret;
        }

        public string ToRDF(bool includeImplicitKnowledge)
        {
            if (_reasoner == null)
                return "";
            return reasoner.GetOWLXML(includeImplicitKnowledge);
        }

        public void SetProperty(string prop, string name, object val)
        {
            Outer.SetProperty(prop, name, val);
        }

        public object GetProperty(string prop, string name)
        {
            return Outer.GetProperty(prop, name);
        }

        public string[] ListProperties(string prop)
        {
            return Outer.ListProperties(prop);
        }

        public void ClearProperties(string prop)
        {
            Outer.ClearProperties(prop);
        }

        public string ToCNL(bool includeAnnotations)
        {
            return ToCNL(false, includeAnnotations);
        }

        public string ToCNL(bool includeImplicitKnowledge, bool includeAnnotations)
        {
            if (_reasoner == null)
                return "";
            return tools.GetENDLFromAst(reasoner.GetParagraph(includeImplicitKnowledge), includeAnnotations);
        }

        public string ToCNL(CogniPy.CNL.DL.Statement stmt)
        {
            return tools.GetENDLFromAst(stmt);
        }

        private bool isConcept(string conc)
        {
            return conc.StartsWith("a ") || conc.StartsWith("an ") || Char.IsLower(conc[0]);
        }

        public object GetAnnotationValue(string subj, string prop, string lang, string type)
        {
            if (annotations.ContainsKey(subj))
            {
                var ann = annotations[subj];
                if (ann.ContainsKey(prop))
                {
                    var ap = ann[prop];
                    foreach (var a in ap)
                    {
                        if ((lang == null || a.Language == lang) && (type == null || a.SubjectType == type))
                            return a.Value;
                    }
                }
            }
            return null;
        }

        public IEnumerable<AnnotationResult> GetAnnotationsForSignature(IEnumerable<string> cnlEntities)
        {
            List<AnnotationResult> res = new List<AnnotationResult>();

            foreach (var subj in cnlEntities)
            {
                if (annotations.ContainsKey(subj))
                    foreach (var v in annotations[subj].Values)
                        res.AddRange(v);
            }

            return res;
        }

        Dictionary<string, ConstraintResult> constraints = null;

        private void LoadConstrains()
        {
            if (constraints != null)
                return;

            var allConstraints = reasoner.GetAllConstraints();
            constraints = new Dictionary<string, ConstraintResult>();
            foreach (var cc in allConstraints)
            {
                var nam = EN(cc.Concept, false);
                if (!constraints.ContainsKey(nam))
                    constraints.Add(nam, new ConstraintResult() { Concept = nam, Relations = new Dictionary<CogniPy.CNL.DL.Statement.Modality, List<string>>(), ThirdElement = new Dictionary<CogniPy.CNL.DL.Statement.Modality, List<string>>() });

                var cr = constraints[nam];

                foreach (CogniPy.CNL.DL.Statement.Modality mod in cc.Relations.Keys)
                {
                    if (cr.Relations.ContainsKey(mod))
                    {
                        cr.Relations[mod].AddRange(cc.Relations[mod].Select(x => EN(x, true)));
                    }
                    else
                    {
                        cr.Relations.Add(mod, cc.Relations[mod].Select(x => EN(x, true)).ToList());
                    }
                }


                foreach (CogniPy.CNL.DL.Statement.Modality mod in cc.ThirdElement.Keys)
                {
                    if (cr.ThirdElement.ContainsKey(mod))
                    {
                        cr.ThirdElement[mod].AddRange(cc.ThirdElement[mod].Select(x => x.StartsWith("(some ") ? x : EN(x, true)).ToList());
                    }
                    else
                    {
                        cr.ThirdElement.Add(mod, cc.ThirdElement[mod].Select(x => x.StartsWith("(some ") ? x : EN(x, true)).ToList());
                    }
                }
            }
        }

        public Tuple<List<string>, List<List<object>>> GetConstrainsForSubject(string concept)
        {
            var ret = new List<List<object>>();
            var cr = GetConstraints(new List<string>() { concept });
            var rex = cr.FirstOrDefault();
            var rels = rex.Value.Relations;
            var tep = rex.Value.ThirdElement;
            foreach (var mod in rels.Keys)
            {
                var r1 = rels[mod];
                var t1 = tep[mod];
                for (int i = 0; i < r1.Count; i++)
                {
                    ret.Add(new object[] { mod.ToString(), r1[i], t1[i] }.ToList());
                }
            }
            return Tuple.Create<List<string>, List<List<object>>>(new string[] { "modality", "relation", "range" }.ToList(), ret);
        }

        public Dictionary<string, ConstraintResult> GetConstraints(List<string> descriptions)
        {


            //Get all needed concepts to check.
            Dictionary<string, List<string>> conceptsToReturn = new Dictionary<string, List<string>>();
            foreach (var desc in descriptions)
            {
                string name = desc;
                if (!conceptsToReturn.ContainsKey(name))
                {
                    var jenaConcepts = GetSuperConceptsOf(name, false);

                    conceptsToReturn.Add(name, jenaConcepts);
                    if (isConcept(desc))
                        conceptsToReturn[name].Add(name.Replace("a ", "").Replace("an ", ""));

                }
            }

            LoadConstrains();
            var constraintsByDescription = new Dictionary<string, ConstraintResult>();

            foreach (var desc in conceptsToReturn)
            {
                ConstraintResult cr = new ConstraintResult() { Concept = null, Relations = new Dictionary<CogniPy.CNL.DL.Statement.Modality, List<string>>(), ThirdElement = new Dictionary<CogniPy.CNL.DL.Statement.Modality, List<string>>() };

                foreach (var kn in desc.Value)
                {
                    if (!constraints.ContainsKey(kn))
                        continue;

                    var cc = constraints[kn];
                    foreach (CogniPy.CNL.DL.Statement.Modality mod in cc.Relations.Keys)
                    {
                        if (cr.Relations.ContainsKey(mod))
                        {
                            cr.Relations[mod].AddRange(cc.Relations[mod].Select(x => EN(x, true)));
                        }
                        else
                        {
                            cr.Relations.Add(mod, cc.Relations[mod].Select(x => EN(x, true)).ToList());
                        }
                    }


                    foreach (CogniPy.CNL.DL.Statement.Modality mod in cc.ThirdElement.Keys)
                    {
                        if (cr.ThirdElement.ContainsKey(mod))
                        {
                            cr.ThirdElement[mod].AddRange(cc.ThirdElement[mod].Select(x => x.StartsWith("(some ") ? x : EN(x, true)).ToList());
                        }
                        else
                        {
                            cr.ThirdElement.Add(mod, cc.ThirdElement[mod].Select(x => x.StartsWith("(some ") ? x : EN(x, true)).ToList());
                        }
                    }

                }
                if (cr.Relations.Count() != 0 || cr.ThirdElement.Count() != 0) constraintsByDescription.Add(desc.Key, cr);
            }

            return constraintsByDescription;
        }

        public List<string> ToCNLList(bool includeAnnotations)
        {
            return ToCNLList(false, true, includeAnnotations);
        }

        public List<string> ToCNLList(bool includeImplicitKnowledge, bool removeTrivials, bool includeAnnotations)
        {
            var cnlList = new HashSet<string>();
            if (_reasoner == null)
                return cnlList.ToList();
            var stmts = new List<CogniPy.CNL.DL.Statement>();
            var annots = new List<CogniPy.CNL.DL.Statement>();
            var trct = new Dictionary<HashSet<string>, CogniPy.CNL.DL.Statement>();
            foreach (var stmt in reasoner.GetParagraph(includeImplicitKnowledge, includeImplicitKnowledge).Statements)
            {
                if (stmt is CogniPy.CNL.DL.Annotation)
                {
                    if ((stmt as CogniPy.CNL.DL.Annotation).txt.StartsWith("%Annotations:"))
                        annots.Add(stmt);
                    else
                        continue;
                }

                var pr = new CogniPy.CNL.DL.Paragraph(null) { Statements = new List<CogniPy.CNL.DL.Statement>() { stmt } };

                if (!includeImplicitKnowledge || removeTrivials)
                {
                    if ((stmt is CogniPy.CNL.DL.InstanceOf && (stmt as CogniPy.CNL.DL.InstanceOf).C is CogniPy.CNL.DL.Top) ||
                       (stmt is CogniPy.CNL.DL.Subsumption && (stmt as CogniPy.CNL.DL.Subsumption).D is CogniPy.CNL.DL.Top) ||
                       (stmt is CogniPy.CNL.DL.RoleInclusion && (stmt as CogniPy.CNL.DL.RoleInclusion).D is CogniPy.CNL.DL.Top) ||
                       (stmt is CogniPy.CNL.DL.DataRoleInclusion && (stmt as CogniPy.CNL.DL.DataRoleInclusion).D is CogniPy.CNL.DL.Top))
                    {
                        var sg = DLToys.GetSignatureFromParagraph(pr);
                        trct.Add(sg, stmt);
                        continue;
                    }

                    stmts.Add(stmt);
                }

                var cnlSent = tools.GetENDLFromAst(pr, false);

                if (!string.IsNullOrEmpty(cnlSent))
                    cnlList.Add(cnlSent.Replace("\r\n", ""));
            }
            if (!includeImplicitKnowledge || removeTrivials)
            {
                var sign = DLToys.GetSignatureFromParagraph(new CogniPy.CNL.DL.Paragraph(null) { Statements = stmts });
                foreach (var kv in trct)
                {
                    if (sign.Intersect(kv.Key).Count() == 0)
                    {
                        var cnlSent = tools.GetENDLFromAst(kv.Value);
                        cnlList.Add(cnlSent.Replace("\r\n", ""));
                    }
                }
            }

            if (includeAnnotations)
            {
                //We collect all annotations together in order to create single Annotations: block
                var pr = new CogniPy.CNL.DL.Paragraph(null) { Statements = new List<CogniPy.CNL.DL.Statement>(annots) };
                var annotsCnlBlock = tools.GetENDLFromAst(pr, includeAnnotations);
                if (!string.IsNullOrEmpty(annotsCnlBlock))
                    cnlList.Add(annotsCnlBlock);
            }

            return cnlList.ToList();
        }

        public List<CogniPyStatement> ToCNLStatementList()
        {
            return ToCNLStatementList(false);
        }
        public List<CogniPyStatement> ToCNLStatementList(bool includeImplicitKnowledge)
        {
            var dlEnConverter = new DLENConverter(tools, (ns) => ns, (pfx) => pfx, "");

            var cnlList = new List<CogniPyStatement>();
            if (_reasoner == null)
                return cnlList;
            foreach (var stmt in reasoner.GetParagraph(includeImplicitKnowledge).Statements)
            {
                StatementType type;
                var attr = (CogniPy.CNL.DL.StatementAttr)stmt.GetType().GetCustomAttributes(typeof(CogniPy.CNL.DL.StatementAttr), true).First();
                switch (attr.type)
                {
                    case CogniPy.CNL.DL.StatementType.Concept:
                        type = StatementType.Concept; break;
                    case CogniPy.CNL.DL.StatementType.Instance:
                        type = StatementType.Instance; break;
                    case CogniPy.CNL.DL.StatementType.Role:
                        type = StatementType.Role; break;
                    case CogniPy.CNL.DL.StatementType.Rule:
                        type = StatementType.Rule; break;
                    case CogniPy.CNL.DL.StatementType.Annotation:
                        type = StatementType.Annotation; continue; //skip the annotations.

                    default:
                        throw new ArgumentException("Internal error while retrieving statements.");
                }
                if (stmt.modality != CogniPy.CNL.DL.Statement.Modality.IS)
                    type = StatementType.Constraint;

                var signature = DLToys.GetSignatureFromStatement(stmt);
                var concepts = new HashSet<string>();
                var roles = new HashSet<string>();
                var dataroles = new HashSet<string>();
                var instances = new HashSet<string>();

                foreach (var element in signature)
                {
                    if (element.StartsWith("C:"))
                        concepts.Add(dlEnConverter.EN(element.Substring(2), false));
                    if (element.StartsWith("I:"))
                        instances.Add(dlEnConverter.EN(element.Substring(2), true));
                    if (element.StartsWith("R:"))
                        roles.Add(dlEnConverter.EN(element.Substring(2), false));
                    if (element.StartsWith("D:"))
                        dataroles.Add(dlEnConverter.EN(element.Substring(2), false));
                }

                var statementCNL = tools.GetENDLFromAst(stmt, false, (ns) => ns);
                cnlList.Add(new CogniPyStatement() { CnlStatement = statementCNL, Concepts = concepts, Instances = instances, Roles = roles, DataRoles = dataroles, Type = type });
            }

            return cnlList;
        }

        public CogniPy.CNL.DL.Paragraph GetParagrah(bool includeImplicitKnowledge = true)
        {
            return reasoner.GetParagraph(includeImplicitKnowledge);
        }

        public string[] SelectInstancesSPARQLDetails(string cnl)
        {
            var node = tools.GetEN2DLNode(cnl);
            Dictionary<string, string> roleMapping;
            Dictionary<string, string> attrMapping;
            string defaultInstance;
            return reasoner.SparqlTransform.ConvertToGetInstancesOfDetails(node, null, null, out roleMapping, out attrMapping, out defaultInstance, true, false);
        }

        public string SelectTypesOfSPARQL(string cnl, bool direct)
        {
            var node = tools.GetEN2DLNode(cnl);
            Dictionary<string, string> roleMapping;
            Dictionary<string, string> attrMapping;
            string defaultInstance;
            var querySelect = reasoner.SparqlTransform.ConvertToGetTypesOf(node, null, null, out roleMapping, out attrMapping, out defaultInstance, 0, -1, true, direct);
            return querySelect;
        }

        public string SelectInstancesSPARQL(string cnl, bool direct = false)
        {
            var node = tools.GetEN2DLNode(cnl);
            Dictionary<string, string> roleMapping;
            Dictionary<string, string> attrMapping;
            string defaultInstance;
            var querySelect = reasoner.SparqlTransform.ConvertToGetInstancesOf(node, null, null, out roleMapping, out attrMapping, out defaultInstance, 0, -1, true, direct);
            return querySelect;
        }

        public string SelectSubconceptsSPARQL(string cnl, bool direct)
        {
            var node = tools.GetEN2DLNode(cnl);
            var querySelect = reasoner.SparqlTransform.ConvertToGetSubconceptsOf(node, direct, false, 0, -1, true);
            return querySelect;
        }

        public string SelectSuperconceptsSPARQL(string cnl, bool direct)
        {
            var node = tools.GetEN2DLNode(cnl);
            var querySelect = reasoner.SparqlTransform.ConvertToGetSuperconceptsOf(node, direct, false, 0, -1, true);
            return querySelect;
        }


        private void InvalidateMaterialization()
        {
            alreadyMaterialized = false;
        }

        public IEnumerable<InstanceDescription> DescribeInstancesByName(IEnumerable<string> instances)
        {
            var instancesDeduped = new HashSet<string>(instances);
            var result = new List<InstanceDescription>();
            var ret = DescribeInstances((instancesDeduped.Count > 1 ? "either " : "") + string.Join(",", instancesDeduped));
            foreach (var inst in instancesDeduped)
                if (ret.ContainsKey(inst))
                    result.Add(ret[inst]);
            return result;
        }

        public Dictionary<string, InstanceDescription> DescribeInstances(string query)
        {
            Materialize();
            Dictionary<string, InstanceDescription> results = new Dictionary<string, InstanceDescription>();
            var node = tools.GetEN2DLNode(query);
            if (node is CogniPy.CNL.DL.Atomic || (node is CogniPy.CNL.DL.InstanceSet))
            {
                var l = this.reasoner.GetInstancesOfFromModelFastURI(node);
                foreach (var ins in l)
                {
                    var instanceName = CnlFromUri(ins.Item1, "instance");
                    if (!results.ContainsKey(instanceName))
                        results.Add(instanceName, new InstanceDescription() { AttributeValues = new Dictionary<string, IEnumerable<object>>(), RelatedInstances = new Dictionary<string, IEnumerable<string>>(), Instance = instanceName });

                    var prps = this.reasoner.GetAllPriopertiesFastFromURI(ins.Item2);
                    foreach (var prp in prps)
                    {
                        var propName = CnlFromUri(prp.Item2, "role");
                        if (prp.Item1)
                        {
                            if (!results[instanceName].RelatedInstances.ContainsKey(propName))
                                results[instanceName].RelatedInstances.Add(propName, new HashSet<string>());

                            ((HashSet<string>)results[instanceName].RelatedInstances[propName]).Add(CnlFromUri(prp.Item3.ToString(), "instance"));
                        }
                        else
                        {
                            if (!results[instanceName].AttributeValues.ContainsKey(propName))
                                results[instanceName].AttributeValues.Add(propName, new HashSet<object>());

                            ((HashSet<object>)results[instanceName].AttributeValues[propName]).Add(prp.Item3);
                        }
                    }
                }
            }
            else
            {
                var qq = SelectInstancesSPARQLDetails(query);

                var totSparql = "SELECT " + qq[1] + " ?r ?d ?y {" +
                    "{" + qq[1] + " rdf:type owl:NamedIndividual " + ". " + qq[2] + ". " +
                    "OPTIONAL{ " + qq[1] + " ?r ?y. ?r rdf:type owl:ObjectProperty} " +
                    "} UNION {" + qq[1] + " rdf:type owl:NamedIndividual " + ". " + qq[2] + ". " +
                    "OPTIONAL{ " + qq[1] + " ?d ?y. ?d rdf:type owl:DatatypeProperty} " +
                    "}";
                if (qq[3] != null)
                    totSparql = totSparql + ". FILTER(" + qq[3] + ")";
                totSparql = totSparql + "}";

                {
                    var res = SparqlQuery(totSparql, true, false);
                    var x0idx = res.Item1.IndexOf(qq[1].Substring(1));
                    var ridx = res.Item1.IndexOf("r");
                    var didx = res.Item1.IndexOf("d");
                    var yidx = res.Item1.IndexOf("y");

                    var str = from x in res.Item2 select Tuple.Create(CnlFromUri(x[x0idx].ToString(), "instance"), (x[ridx] != null) ? CnlFromUri(x[ridx].ToString(), "role") : null, (x[didx] != null) ? CnlFromUri(x[didx].ToString(), "role") : null, x[yidx]);
                    foreach (var x in str)
                    {
                        if (!results.ContainsKey(x.Item1))
                            results.Add(x.Item1, new InstanceDescription() { AttributeValues = new Dictionary<string, IEnumerable<object>>(), RelatedInstances = new Dictionary<string, IEnumerable<string>>(), Instance = x.Item1 });

                        if (x.Item2 != null)
                        {
                            if (!results[x.Item1].RelatedInstances.ContainsKey(x.Item2))
                                results[x.Item1].RelatedInstances.Add(x.Item2, new HashSet<string>());
                            ((HashSet<string>)results[x.Item1].RelatedInstances[x.Item2]).Add(CnlFromUri(x.Item4.ToString(), "instance"));
                        }

                        if (x.Item3 != null)
                        {
                            if (!results[x.Item1].AttributeValues.ContainsKey(x.Item3))
                                results[x.Item1].AttributeValues.Add(x.Item3, new HashSet<object>());
                            ((HashSet<object>)results[x.Item1].AttributeValues[x.Item3]).Add(x.Item4);
                        }
                    }
                }

            }
            return results;
        }

        public class RuleEntity
        {
            public string Name { get; set; }
            public object Value { get; set; }
        }

        EventHandler<HermiTReasoningService.DebugTraceEventArgs> _ruleDebugger = null;

        public string CnlFromDLString(string dl)
        {
            return tools.GetENDLFromAst(tools.GetDLAst(dl));
        }

        Action<string, List<RuleEntity>> _debugListener;
        Func<string, List<RuleEntity>, Tuple<string, List<RuleEntity>>> _converter;
        public void SetDebugListener(Action<string, List<RuleEntity>> debugListener, Func<string, List<RuleEntity>, Tuple<string, List<RuleEntity>>> converter)
        {
            _debugListener = debugListener;
            _converter = converter;
            debugModeOn = true;

            _ruleDebugger = new EventHandler<HermiTReasoningService.DebugTraceEventArgs>((s, tea) =>
            {
                if (!tea.TraceMessage.StartsWith("#"))
                {
                    var enR = CnlFromDLString(tea.TraceMessage);

                    var entitiesPerRule = new List<RuleEntity>();
                    foreach (var kv in tea.Binding)
                    {
                        var vn = kv.Key.Substring(1);
                        var lidx = vn.LastIndexOf('-');
                        if (lidx >= 0)
                        {
                            var avn = vn.Substring(0, lidx);
                            int r;
                            if (int.TryParse(vn.Substring(lidx + 1), out r))
                                vn = avn + "(" + r.ToString() + ")";
                            else
                                vn = avn;
                        }

                        entitiesPerRule.Add(new RuleEntity()
                        {
                            Name = vn,
                            Value = kv.Value.Item1 == null ? kv.Value.Item2 : this.EN(kv.Value.Item2.ToString(), kv.Value.Item1 == "Instance")
                        });
                    }
                    _debugListener(enR, entitiesPerRule);
                }
                else
                {
                    var entitiesPerRule = new List<RuleEntity>();
                    foreach (var kv in tea.Binding)
                    {
                        entitiesPerRule.Add(new RuleEntity()
                        {
                            Name = kv.Key,
                            Value = kv.Value.Item1 == null ? kv.Value.Item2 : this.EN(kv.Value.Item2.ToString(), kv.Value.Item1 == "Instance")
                        });
                    }
                    var cnv = _converter(tea.TraceMessage, entitiesPerRule);
                    _debugListener(cnv.Item1, cnv.Item2);
                }
            });
        }

        private void Materialize()
        {
            if (alreadyMaterialized)
                return;

            ReasoningMode TBox;
            ReasoningMode ABox;

            if (SWRLOnly)
            {
                TBox = ReasoningMode.SWRL;
                ABox = ReasoningMode.SWRL;
            }
            else
            {
                TBox = ReasoningMode.RL;
                ABox = ReasoningMode.RL;
            }
            reasoner.debugModeOn = debugModeOn;
            reasoner.exeRulesOn = true;
            if (debugModeOn)
                reasoner.DebugTrace += _ruleDebugger;
            reasoner.Materialization(TBox, ABox, false, modalChecker);
            alreadyMaterialized = true;
        }

        public Tuple<List<string>, List<List<object>>> SparqlQuery(string query, bool materialize, bool asCnl)
        {
            var res = SparqlQueryInternal(query, materialize, true, null);
            if (asCnl)
                res = TranslateQueryResultsIntoCnlInPlace(res);
            return res;
        }

        public Tuple<List<string>, List<List<object>>> SparqlQueryInternal(string query, bool materialize = true, bool detectTypesOfNodes = true, string defaultKindOfNode = null)
        {
            if (materialize)
                Materialize();
            var res = reasoner.SparqlQuery(query, invUriMapping(), detectTypesOfNodes, defaultKindOfNode);
            var cols = res.GetCols();
            var rows = res.GetRows().ToList();
            return Tuple.Create(cols, rows);
        }

        public string GetReasoningInfo()
        {
            Materialize();
            return reasoner.GetReasoningInfo();
        }

        public Tuple<List<string>, List<List<object>>> GetAnnotationsForSubject(string subj, string prop = "", string lang = "")
        {
            var cols = (new string[] { "subject", "subjectType", "property", "value", "language" }).ToList();
            List<List<object>> res = new List<List<object>>();

            if (annotations.ContainsKey(subj))
            {
                var annots = annotations[subj];
                foreach (var ann2 in annots.Values)
                {
                    foreach (var ann in ann2)
                    {
                        if ((lang == "" || (lang == ann.Language)) && (prop == "" || prop == ann.Property))
                        {
                            var rw = new List<object>();
                            rw.Add(ann.Subject);
                            rw.Add(ann.SubjectType);
                            rw.Add(ann.Property);
                            rw.Add(ann.Value);
                            rw.Add(ann.Language);
                            res.Add(rw);
                        }
                    }
                }
            }

            return Tuple.Create(cols, res);
        }

        public Tuple<List<string>, List<List<object>>> TranslateQueryResultsIntoCnlInPlace(Tuple<List<string>, List<List<object>>> result)
        {
            foreach (var x in result.Item2)
            {
                for (int i = 0; i < x.Count; i++)
                {
                    if (x[i] is CogniPy.GraphEntity)
                        x[i] = CnlFromUri(x[i].ToString(), ((CogniPy.GraphEntity)x[i]).Kind);
                }
            }
            return result;
        }

        private readonly string[] toFilter = new string[3] { "\"Thing\"[owl]", "\"NamedIndividual\"[owl]", "class[owl]" };

        public List<string> GetSuperConceptsOf(string cnlName, bool direct)
        {
            Materialize();
            var node = tools.GetEN2DLNode(cnlName);
            if (!direct && (node is CogniPy.CNL.DL.Atomic || ((node is CogniPy.CNL.DL.InstanceSet) && (node as CogniPy.CNL.DL.InstanceSet).Instances.Count == 1 && (node as CogniPy.CNL.DL.InstanceSet).Instances[0] is CogniPy.CNL.DL.NamedInstance)))
            {
                var l = this.reasoner.GetSuperConceptsOfFromModelFast(node);
                return (from x in l select CnlFromUri(x, "concept")).ToList();
            }
            else
            {
                string sparql;
                if ((node is CogniPy.CNL.DL.InstanceSet) && (node as CogniPy.CNL.DL.InstanceSet).Instances.Count == 1 && (node as CogniPy.CNL.DL.InstanceSet).Instances[0] is CogniPy.CNL.DL.NamedInstance)
                    sparql = SelectTypesOfSPARQL(cnlName, direct);
                else
                {
                    if (!(node is CogniPy.CNL.DL.Atomic) && !(node is CogniPy.CNL.DL.Top))
                        throw new NotImplementedException("It works only for atomic concept names");
                    sparql = SelectSuperconceptsSPARQL(cnlName, direct);
                }
                return TranslateQueryResultsIntoCnlInPlace(SparqlQueryInternal(sparql, true, false, "concept")).Item2.SelectMany(x => x.Where(z => !toFilter.Contains(z)).Select(y => (y as String))).ToList();
            }
        }

        private List<string> r_GetSuperConcepts(string concept, bool direct)
        {
            var toRet = (from l in reasoner.GetSuperConcepts(tools.GetEN2DLNode(concept), direct, false)
                         select new List<string>(tools.Morphology(l, "", "NormalForm", false))).ToList();

            return toRet.SelectMany(sc => sc).ToList();
        }

        public List<string> GetSubConceptsOf(string cnlName, bool direct)
        {
            Materialize();
            var node = tools.GetEN2DLNode(cnlName);
            if (!direct && (node is CogniPy.CNL.DL.Atomic))
            {
                var l = this.reasoner.GetSubConceptsOfFromModelFast(node as CogniPy.CNL.DL.Atomic);
                return (from x in l select CnlFromUri(x, "concept")).ToList();
            }
            else
            {
                if (!(node is CogniPy.CNL.DL.Atomic) && !(node is CogniPy.CNL.DL.Top))
                    throw new NotImplementedException("It works only for atomic concept names");
                string sparql;
                sparql = SelectSubconceptsSPARQL(cnlName, direct);
                return TranslateQueryResultsIntoCnlInPlace(SparqlQueryInternal(sparql, true, false, "concept")).Item2.SelectMany(x => x.Where(z => !toFilter.Contains(z)).Select(y => (y as String))).ToList();
            }
        }

        private List<List<string>> r_GetSubConceptsOf(string concept, bool direct)
        {
            return (from l in reasoner.GetSubConcepts(tools.GetEN2DLNode(concept), direct, false)
                    select new List<string>(tools.Morphology(l, "", "NormalForm", false))).ToList();
        }

        public List<string> GetInstancesOf(string cnlName, bool direct)
        {
            Materialize();
            var node = tools.GetEN2DLNode(cnlName);
            if (!direct && (node is CogniPy.CNL.DL.Atomic || ((node is CogniPy.CNL.DL.InstanceSet) && (node as CogniPy.CNL.DL.InstanceSet).Instances.Count == 1 && (node as CogniPy.CNL.DL.InstanceSet).Instances[0] is CogniPy.CNL.DL.NamedInstance)))
            {
                var l = this.reasoner.GetInstancesOfFromModelFast(node);
                return (from x in l select CnlFromUri(x, "instance")).ToList();
            }
            else
            {
                Dictionary<string, string> roleMapping;
                Dictionary<string, string> attrMapping;
                string defaultInstance;
                var sparql = reasoner.SparqlTransform.ConvertToGetInstancesOf(node, null, null, out roleMapping, out attrMapping, out defaultInstance, 0, -1, true, direct);
                return TranslateQueryResultsIntoCnlInPlace(SparqlQueryInternal(sparql, true, false, "instance")).Item2.SelectMany(x => x.Where(z => !toFilter.Contains(z)).Select(y => (y as String))).ToList();
            }
        }

        private List<List<string>> r_GetInstancesOf(string concept, bool direct)
        {
            return (from l in reasoner.GetInstancesOf(tools.GetEN2DLNode(concept), direct)
                    select new List<string>(tools.Morphology(l, "", "NormalForm", true))).ToList();
        }

        private string[] r_GetEquivalentConceptsOf(string concept)
        {
            return (from l in reasoner.GetEquivalentConcepts(tools.GetEN2DLNode(concept), false)
                    select tools.Morphology(new string[] { l }, "", "NormalForm", false).First()).ToArray();
        }

        ///////////// AP

        public string DLFromUri(string uri, string type)
        {
            var n = reasoner.renderEntityFromUri(uri, type == "instance" ? CogniPy.ARS.EntityKind.Instance : (type == "concept" ? CogniPy.ARS.EntityKind.Concept : CogniPy.ARS.EntityKind.Role));
            if (n == null)
                return "";
            return n;
        }

        public string InstanceDL(string en)
        {
            if (en.Length >= 2)
                if (char.IsUpper(en[0]) && char.IsUpper(en[1]))
                    return en;
            return "_" + en;
        }

        public string ID(string en)
        {
            return en.Substring(1);
        }

        public string EN(string en)
        {
            lock (tools)
                return tools.GetDL(en, true);
        }

        public string EN(string dl, bool bigName, CogniPy.CNL.EN.endict.WordKind wrdKnd = CogniPy.CNL.EN.endict.WordKind.NormalForm)
        {
            if (dl == "⊤")
                return "thing";

            var allParts = CogniPy.CNL.EN.ENNameingConvention.FromDL(new CogniPy.CNL.DL.DlName() { id = dl }, wrdKnd, bigName).Split();
            if (!System.String.IsNullOrWhiteSpace(allParts.term) && allParts.term.StartsWith("<") && allParts.term.EndsWith(">"))
            {
                var nss = allParts.term.Substring(1, allParts.term.Length - 2);
                if (nss == ontologyBase) // remove if the namespace is the default one.
                    allParts.term = null;
                else
                {
                    var tterm = AllReferences.ContainsKey(nss) ? AllReferences[nss] : null;
                    if (!System.String.IsNullOrWhiteSpace(tterm))
                        allParts.term = tterm;
                }
            }

            return allParts.Combine().id;
        }


        public HashSet<string> SplitText(string stxt)
        {
            try
            {
                HashSet<string> newScript = new HashSet<string>();
                if (stxt.Trim() != "")
                {
                    lock (tools)
                    {
                        var ast = tools.GetENAst(stxt, true) as CogniPy.CNL.EN.paragraph;
                        foreach (var stmt in ast.sentences)
                            newScript.Add(tools.GetENFromAstSentence(stmt, true));
                    }
                }
                return newScript;
            }
            catch
            {
                //if (showBox)
                //    MessageBox.Show(this, "There are errors in the knowledge. Please fix them.", "Errors!", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return null;
            }
        }

        public void SetProgress(double completed)
        {
            //  _ctrl.SetProgress(completed);
        }

        public void WriteMessage(int priority, string message)
        {

        }

        private class SimplePopulator : CogniPy.CNL.DL.Populator
        {
            CogniPySvr _parent;
            public SimplePopulator(CogniPySvr _parent)
            {
                this._parent = _parent;
            }

            public IEnumerable<KeyValuePair<string, string>> Populate(string sentenceBeginning, string str, List<string> forms, int max)
            {

                var ret = new List<KeyValuePair<string, string>>();

                foreach (var form in forms)
                {
                    // ask the main window for the elements to add from the current window text (i=0) or from the referenced ontologies (i=1)....
                    for (int i = 0; i <= 0; i++)
                    {
                        bool external = i == 0;
                        var kv = form.Split(':');
                        var retD = _parent.Populate(false, kv[0], str, kv[1]);
                        foreach (var d in retD) // add it in returned strings
                            ret.Add(new KeyValuePair<string, string>((external ? "e" : "i") + ":" + form, d));
                    }
                }

                return ret;
            }


        }


        IEnumerable<string> getConcepts()
        {
            if (concepts.Count == 0)
                return new string[] { "<noun>" };
            else
                return concepts;
        }

        IEnumerable<string> getAllRoles()
        {
            var ret = new HashSet<string>();
            if (objectroles.Count == 0)
                ret.Add("<verb>");
            else
                ret.UnionWith(objectroles);
            if (dataroles.Count == 0)
                ret.Add("<attribute>");
            else
                ret.UnionWith(dataroles);
            return ret;
        }

        IEnumerable<string> getDataRoles()
        {
            if (dataroles.Count == 0)
                return new string[] { "<attribute>" };
            else
                return dataroles;
        }

        IEnumerable<string> getDatatypes()
        {
            if (datatypes.Count == 0)
                return new string[] { "<datatype>" };
            else
                return datatypes;
        }

        IEnumerable<string> getInstances()
        {
            if (instances.Count == 0)
                return new string[] { "<Proper-Name>" };
            else
                return instances;
        }

        public IEnumerable<string> Populate(bool LoadExternal, string What, string Start, string Form)
        {
            if (!LoadExternal)
            {
                switch (What)
                {
                    case "datarole": return tools.Morphology(getDataRoles(), Start, Form, false);
                    case "role": return tools.Morphology(getAllRoles(), Start, Form, false);
                    case "concept": return tools.Morphology(getConcepts(), Start, Form, false);
                    case "instance": return tools.Morphology(getInstances(), Start, Form, true);
                    case "datatype": return tools.Morphology(getDatatypes(), Start, Form, false);
                }
            }
            else
            {
                switch (What)
                {
                    //case "datarole": return tools.Morphology(getRefDataRoles(), Start, Form, false); 
                    //case "role": return tools.Morphology(getAllRefRoles(), Start, Form, false); 
                    //case "concept": return tools.Morphology(getRefConcepts(), Start, Form, false);
                    //case "instance": return tools.Morphology(getRefInstances(), Start, Form, true);
                    //case "datatype": return tools.Morphology(getRefDatatypes(), Start, Form, false); 
                }
            }
            throw new InvalidOperationException();
        }

        public string[] AutoComplete(string full)
        {
            var sign = tools.GetDLAstSignature(reasoner.GetParagraph(false));
            foreach (var smb in sign)
            {
                var inam = smb.Item2;
                var en = CogniPy.CNL.EN.ENNameingConvention.FromDL(new CogniPy.CNL.DL.DlName() { id = inam }, true).Split();
                if (smb.Item1 == CogniPy.ARS.EntityKind.Instance)
                    instances.Add(inam);//en.Combine().id);
                else if (smb.Item1 == CogniPy.ARS.EntityKind.Role)
                    objectroles.Add(inam);//(en.Combine().id);
                else if (smb.Item1 == CogniPy.ARS.EntityKind.DataRole)
                    dataroles.Add(inam);//(en.Combine().id);
                else if (smb.Item1 == CogniPy.ARS.EntityKind.Concept)
                    concepts.Add(inam);//(en.Combine().id);
                else if (smb.Item1 == CogniPy.ARS.EntityKind.DataType)
                    datatypes.Add(inam);//(en.Combine().id);
            }

            List<KeyValuePair<string, string>> symbols;
            var ret = tools.AutoComplete(new SimplePopulator(this), full, out symbols, int.MaxValue);
            if (ret == null)
            {
                var insts = this.Populate(false, "instance", full, "").ToArray();
                if (insts.Length == 0)
                    insts = new string[] { "<Proper-Name>" };
                return new string[] { "Every", "Every-single-thing", "If", "No", "Nothing", "Something", "The", "X" }.Where(s => s.ToLower().StartsWith(full.ToLower())).Union(insts).ToArray();
            }
            else
                return ret.ToArray();
        }

        public string Highlight(string text)
        {
            return kwds.Replace(text, new MatchEvaluator((kw) => { return char.IsLetter(kw.Value.First()) ? "**" + kw.Value + "**" : kw.Value; })).Replace("\r\n", "\r\n\r\n");
        }

        public HashSet<string> KnowledgeSplit(string knowledge)
        {
            var ret = new HashSet<string>();
            var lines = SplitText(knowledge);

            foreach (var l in lines)
                ret.Add(l);

            return ret;
        }



        string toDL(string name, bool isRole)
        {
            return CogniPy.CNL.EN.ENNameingConvention.ToDL(new CogniPy.CNL.EN.EnName() { id = name }, isRole ? CogniPy.CNL.EN.endict.WordKind.PastParticiple : CogniPy.CNL.EN.endict.WordKind.NormalForm).id;
        }

        public void MergeWith(CogniPySvr x, bool materialize = true)
        {
            if (materialize)
            {
                Materialize();
                x.Materialize();
            }

            reasoner.MergeWith(x.reasoner);
        }

        private CogniPySvr(CogniPySvr other)
        {
            this.tags = other.tags;
            this.paragraph = other.paragraph;
            this.tools = new CNLTools("en");


            this.ontologyBase = other.ontologyBase;
            this.Outer = other.Outer;
            this.reasoner = other.reasoner.Clone(this, Outer);
            this.annotations = other.annotations;
            this.alreadyMaterialized = other.alreadyMaterialized;
        }

        public CogniPySvr CloneForAboxChangesOnly()
        {
            Materialize();
            return new CogniPySvr(this);
        }

        public string Kaka(string ka)
        {
            return "aaa";
        }

        object toVal(string v)
        {
            return CogniPy.CNL.DL.Value.ToObject(CogniPy.CNL.DL.Value.MakeFrom(v.Substring(0, 1), v.Substring(2)));
        }

        public void RemoveInstance(string name)
        {
            Materialize();
            var dl = toDL(name, false);
            reasoner.RemoveInstance(dl);
        }

        public void KnowledgeInsert(string text, bool loadAnnotations, bool materialize)
        {
            if (materialize)
                Materialize();

            var para = tools.GetEN2DLAst(text);
            if (loadAnnotations)
                LoadAnnotations(para);

            reasoner.AddRemoveKnowledge(para, true, SWRLOnly);
        }

        public void KnowledgeDelete(string text, bool materialize)
        {
            if (materialize)
                Materialize();
            reasoner.AddRemoveKnowledge(tools.GetEN2DLAst(text), false, SWRLOnly);
        }

        public string Why(string text, bool materialize)
        {
            if (materialize)
                Materialize();
            return reasoner.Why(tools.GetEN2DLAst(text));
        }

        IEnumerable<Tuple<string, string, string, object>> ConvertAssertsToTuples(string[] asserts)
        {
            for (var i = 0; i < asserts.Length; i += 4)
                yield return Tuple.Create<string, string, string, object>(asserts[i], toDL(asserts[i + 1], false), asserts[i + 2] != "" ? toDL(asserts[i + 2], true) : "", asserts[i] == "D" ? toVal(asserts[i + 3]) : toDL(asserts[i + 3], false));
        }

        public void AssertionsInsert(string[] asserts)
        {
            Materialize();
            reasoner.AddRemoveAssertions(ConvertAssertsToTuples(asserts), true, SWRLOnly);
        }

        public void AssertionsDelete(string[] asserts)
        {
            Materialize();
            reasoner.AddRemoveAssertions(ConvertAssertsToTuples(asserts), false, SWRLOnly);
        }


        public static string GetVersionInfo()
        {
            return GetVersionInfo(null, 0, new HashSet<string>());
        }

        private static string GetVersionInfo(Assembly thisAsm, int n, HashSet<string> mark)
        {
            if (thisAsm == null)
                thisAsm = Assembly.GetExecutingAssembly();

            StringBuilder ret = new StringBuilder();
            ret.AppendLine(thisAsm.GetName().ToString());
            //ret.Append(thisAsm.GetName().Version.ToString());
            foreach (var dep in thisAsm.GetReferencedAssemblies())
            {
                try
                {
                    if (!mark.Contains(dep.FullName))
                    {
                        mark.Add(dep.FullName);
                        var asm = Assembly.Load(dep.FullName);
                        if (n > 0)
                            ret.Append(new string(' ', n));
                        var str = GetVersionInfo(asm, n + 1, mark);

                        if (str != "")
                            ret.Append(str);
                    }
                }
                catch { }
            }
            return ret.ToString();
        }

        //RBinding

        public Tuple<List<string>, List<List<object>>> SparqlQueryForInstancesWithDetails(string query)
        {
            var r = DescribeInstances(query);
            //calculate all colums
            var colDic = new Dictionary<string, int>();
            var cols = new List<string>();
            int idx = 0;
            colDic.Add("Instance", idx++);
            cols.Add("Instance");
            foreach (var instkv in r.Values)
            {
                foreach (var x in instkv.RelatedInstances.Keys)
                    if (!colDic.ContainsKey(x))
                    {
                        colDic.Add(x, idx++);
                        cols.Add(x);
                    }
                foreach (var x in instkv.AttributeValues.Keys)
                    if (!colDic.ContainsKey(x))
                    {
                        colDic.Add(x, idx++);
                        cols.Add(x);
                    }
            }
            List<List<object>> vals = new List<List<object>>();
            var pro = new object[idx];
            foreach (var kv in r)
            {
                var lst = new List<object>(pro);
                lst[0] = kv.Key;
                foreach (var xy in kv.Value.RelatedInstances)
                {
                    var v = xy.Value.ToArray();
                    lst[colDic[xy.Key]] = v.Length == 1 ? (object)v[0] : (object)v;
                }
                foreach (var xy in kv.Value.AttributeValues)
                {
                    var v = xy.Value.ToArray();
                    lst[colDic[xy.Key]] = v.Length == 1 ? (object)v[0] : (object)v;
                }
                vals.Add(lst);
            }
            return Tuple.Create(cols, vals);
        }

        public Tuple<List<string>, List<List<object>>> SparqlQueryForInstancesWithDetails_o(string query)
        {
            var qq = SelectInstancesSPARQLDetails(query);
            var sparqlCom = "SELECT DISTINCT " + qq[1] + " ?r ?y {" + qq[2] + "." + qq[1] + " ?r ?y. " + qq[1] + " rdf:type owl:NamedIndividual. ?r rdf:type ";
            var sparqlInvCom = "SELECT DISTINCT " + qq[1] + " ?r ?y {" + qq[2] + ".?y ?r " + qq[1] + ". " + qq[1] + " rdf:type owl:NamedIndividual. ?r rdf:type ";
            var sparqlD = sparqlCom + "owl:DatatypeProperty}";
            var sparqlO = sparqlCom + "owl:ObjectProperty}";
            var sparqlInvO = sparqlInvCom + "owl:ObjectProperty}";

            var fc = new Dictionary<Tuple<string, string>, HashSet<object>>();
            {
                var res = SparqlQueryInternal(sparqlO);
                var x0idx = res.Item1.IndexOf(qq[1].Substring(1));
                var ridx = res.Item1.IndexOf("r");
                var yidx = res.Item1.IndexOf("y");

                var str = from x in res.Item2 select Tuple.Create(CnlFromUri(x[x0idx].ToString(), "instance"), CnlFromUri(x[ridx].ToString(), "role"), CnlFromUri(x[yidx].ToString(), "instance"));
                foreach (var x in str)
                {
                    var k = Tuple.Create(x.Item1, x.Item2);
                    if (!fc.ContainsKey(k))
                        fc.Add(k, new HashSet<object>());
                    fc[k].Add(x.Item3);
                }
            }
            {
                var res = SparqlQueryInternal(sparqlInvO);
                var x0idx = res.Item1.IndexOf(qq[1].Substring(1));
                var ridx = res.Item1.IndexOf("r");
                var yidx = res.Item1.IndexOf("y");

                var str = from x in res.Item2 select Tuple.Create(CnlFromUri(x[x0idx].ToString(), "instance"), CnlFromUri(x[ridx].ToString(), "role"), CnlFromUri(x[yidx].ToString(), "instance"));
                foreach (var x in str)
                {
                    var k = Tuple.Create(x.Item1, x.Item2 + "^");
                    if (!fc.ContainsKey(k))
                        fc.Add(k, new HashSet<object>());
                    fc[k].Add(x.Item3);
                }
            }
            {
                var res = SparqlQueryInternal(sparqlD);
                var x0idx = res.Item1.IndexOf(qq[1].Substring(1));
                var ridx = res.Item1.IndexOf("r");
                var yidx = res.Item1.IndexOf("y");

                var str = from x in res.Item2 select Tuple.Create(CnlFromUri(x[x0idx].ToString(), "instance"), CnlFromUri(x[ridx].ToString(), "datarole"), x[yidx]);
                foreach (var x in str)
                {
                    var k = Tuple.Create(x.Item1, x.Item2);
                    if (!fc.ContainsKey(k))
                        fc.Add(k, new HashSet<object>());
                    fc[k].Add(x.Item3);
                }
            }

            int idx = 0;
            var colDic = new Dictionary<string, int>();
            colDic.Add("Instance", idx++);
            var cols = new List<string>();
            cols.Add("Instance");

            foreach (var kv in fc)
                if (!colDic.ContainsKey(kv.Key.Item2))
                {
                    colDic.Add(kv.Key.Item2, idx++);
                    cols.Add(kv.Key.Item2);
                }

            var rowsDic = new Dictionary<string, object[]>();

            foreach (var kv in fc)
            {
                object[] lst = null;
                if (!rowsDic.ContainsKey(kv.Key.Item1))
                {
                    lst = new object[idx + 1];
                    lst[0] = kv.Key.Item1;
                    rowsDic.Add(kv.Key.Item1, lst);
                }
                else
                    lst = rowsDic[kv.Key.Item1];

                if (kv.Value.Count > 1)
                    lst[colDic[kv.Key.Item2]] = kv.Value.ToArray();
                else if (kv.Value.Count == 1)
                    lst[colDic[kv.Key.Item2]] = kv.Value.First();
            }

            {
                var sparqlInstances = SelectInstancesSPARQL(query);
                var res = SparqlQueryInternal(sparqlInstances);
                foreach (var r in res.Item2)
                {
                    var ins = CnlFromUri(r.First().ToString(), "instance");
                    if (!rowsDic.ContainsKey(ins))
                    {
                        var lst = new object[idx + 1];
                        lst[0] = ins;
                        rowsDic.Add(ins, lst);
                    }

                }
            }

            var rows = new List<List<object>>();
            foreach (var r in rowsDic)
                rows.Add(r.Value.ToList());


            return TranslateQueryResultsIntoCnlInPlace(Tuple.Create(cols, rows));
        }


    }
}
