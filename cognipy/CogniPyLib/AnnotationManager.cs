using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;

namespace Ontorion.CNL
{
    public class W3CAnnotation : IEquatable<W3CAnnotation>, IEqualityComparer<W3CAnnotation>, INotifyPropertyChanged
    {
        public W3CAnnotation(bool isDL)
        {
            this.isDL = isDL;
        }

        private string _type;
        public string Type
        {
            get { return _type; }
            set { _type = value; }
        }

        private object _value;
        public object FormattedValue
        {
            get
            {
                if (_value is String)
                    return System.Net.WebUtility.HtmlDecode(_value.ToString());
                else
                    return _value;
            }
        }
        public object Value
        {
            get { return _value; }
            set
            {
                _value = value;
                parseIfString();
            }
        }
        // TODO ALESSANDRO this is not solving completely the problem.... probably to solve it completely we will need to change the way in which the value is taken from OWL and when a new annotation is entered... is it enough to deal
        // with it in the appendAnnotation method? Probably not....
        void parseIfString()
        {
            if (_value is String)
            {
                string val = (string)_value;
                _value = val.Replace("\n", "");
                if (val.StartsWith("'") && val.EndsWith("'"))
                    _value = val.Substring(1).Substring(0, val.Length - (1 + 1));//remove starting and trailing single quotes.
            }
        }
        private string _language = null;
        public string Language
        {
            get { return _language; }
            set { _language = value; }
        }

        private bool _external = false;
        public bool External
        {
            get { return _external; }
            set { _external = value; }
        }
        public override string ToString()
        {
            string ret = Type + " ";
            if (Value is Double)
            {
                ret += (Double)Value;
            }
            else if (Value is Int64)
            {
                ret += (Int64)Value;
            }
            else if (Value is DateTime)
            {
                ret += (DateTime)Value;
            }
            else if (Value is String)
            {
                var val = (String)Value;
                if (!System.String.IsNullOrWhiteSpace(val))
                {
                    if (!val.StartsWith("'"))
                        val = "'" + val;
                    if (!val.EndsWith("'"))
                        val += "'";
                }
                else
                    val += "''";
                ret += val;
            }

            if (!string.IsNullOrEmpty(Language))
                ret += "@" + Language;
            return ret;
        }

        public bool isDL
        {
            get;
            private set;
        }

        public bool Equals(W3CAnnotation other)
        {
            var otVal = other.Value.ToString().Replace("'", "");
            var thisVal = this.Value.ToString().Replace("'", "");

            if (this.Language == other.Language && this.Type == other.Type && thisVal == otVal)
                return true;
            else
                return false;
        }

        public bool Equals(W3CAnnotation x, W3CAnnotation y)
        {
            if (x.Equals(y))
                return true;

            return false;
        }

        public int GetHashCode(W3CAnnotation obj)
        {
            //int hash = obj.Type.GetHashCode() + obj.Value.GetHashCode();
            //if (!String.IsNullOrEmpty(obj.Language))
            //    hash += obj.Language.GetHashCode();

            return 0;
        }

        // ESCHOI TEST
        public event PropertyChangedEventHandler PropertyChanged;
        // Create the OnPropertyChanged method to raise the event
        protected void OnPropertyChanged(string name)
        {
            PropertyChangedEventHandler handler = PropertyChanged;
            if (handler != null)
            {
                handler(this, new PropertyChangedEventArgs(name));
            }
        }
    }

    public class AnnotationManager
    {
        public AnnotationManager()
        {
        }

        private AnnotationManager(Dictionary<string, List<W3CAnnotation>> _annotatedElements, Dictionary<string, string> subjectType, Dictionary<string, Tuple<string, string>> pfx2NsInLoadedAnnotations)
        {
            this._annotatedElements = _annotatedElements;
            this.subjectType = subjectType;
            this.pfx2NsInLoadedAnnotations = pfx2NsInLoadedAnnotations;
        }

        public static string ANNOTATION_START = "Annotations:";
        // regex to extract the w3c annotations. These are written in the Annotations: part.
        // this regex expect something like: annotatedConcept: annotationType value@language
        // value can be: a string ("'), a number, a date (written with -).
        static string allSubjectQuotedStart = @"(""|{""|The-""|THE-""|the-""|_"")";
        static string allPossibleSubjectEnd = @"(""|""}|"")";
        static string allPossibleReferenceEnding = @"\[[^\s\]]*\]|:[^\s]*";
        public static Regex w3cAnnotRg = new Regex(@"^\s*(\r\n)?(?<annotated>(("+allSubjectQuotedStart+@"?[^""]*"+allPossibleSubjectEnd+@")("+allPossibleReferenceEnding+@")?\s|([\S]*)))(?<annotatedKind>[^:]*):\s*(?<type>\S*)\s+[""']?(?<value>(((?<=[""'])([^""']|\\'|\\"")*(?=[""'])))|([0-9-\.,]*))[""']?(@(?<language>[a-zA-Z-]*))?\s*($|(?<dot>\.))",
 RegexOptions.Compiled |
 RegexOptions.Multiline);

        private Dictionary<string, string> subjectType = new Dictionary<string, string>();
        private Dictionary<string, List<W3CAnnotation>> _annotatedElements = new Dictionary<string, List<W3CAnnotation>>();

        // TODO [AnnotationManager]: This commented part can be used to standardize the DL sentences we are storing inside the annotationManager. 
        // the problem with this is that after standardizing it the string will not be in DL so we cannot parse it as a DL string.....
        // this is a problem because if someone is reading the content of the annotation manager from outside it will get incorrect DL string.
        // One way to go would be to keep two versions: a standardized one internally and the DL string. 
        // To check if a subject is contained in the manager, we should compare standardized strings
        // To return outside we should always use the DL string.
        /// /////////////////////// FROM CNL TOOLS //////////////////////////////////////
        //private Tools.Parser dlParser = new Ontorion.CNL.DL.dl();
        //private Ontorion.CNL.DL.Paragraph GetDLAst(string text, bool throwOnError = true)
        //{
        //    if (text.Trim() == "") return null;
        //    Tools.SYMBOL smb = dlParser.Parse(text);
        //    if (!(smb is Ontorion.CNL.DL.Paragraph))   // get null on syntax error
        //    {
        //        if (smb is Tools.error)
        //        {
        //            if (throwOnError)
        //                throw new ParseException((smb as Tools.error).ToString(), (smb as Tools.error).Line, (smb as Tools.error).Position, (smb as Tools.error).pos, text);
        //        }
        //        return null;
        //    }
        //    else
        //    {
        //        return smb as Ontorion.CNL.DL.Paragraph;
        //    }
        //}
        //////////////////////////// FROM CNL TOOLS END ///////////////////////////////////////// 

        //private string GetStandardizedDLString(string dlString)
        //{
        //    var par = GetDLAst(dlString,false);
        //    if (par == null)
        //        return dlString;
        //    var ser = new Ontorion.CNL.DL.StandardizedSerializer();
        //    return ser.Serialize(par);
        //}

        public bool ContainsAnnotationSubject(string annotSubj)
        {
            //if (_annotatedElements.ContainsKey(GetStandardizedDLString(annotSubj)))
            if (_annotatedElements.ContainsKey(annotSubj))
                return true;
            else if (_annotatedElements.ContainsKey(annotSubj.Replace("\r\n", ""))) // maybe it was a Statement with \r\n? (internall not \r\n is kept)
                return true;
            else
                return false;
        }

        /// <summary>
        /// Loads the W3CAnnotations found in the line. By default the annotationManager is using DL internally if the text you are giving is not in DL, use the inputTranslator argument
        /// If the subject of an annotation is a Statement, then the Statement is expected to be written like: "statement where quote is ''"
        /// </summary>
        /// <param name="line"></param>
        /// <param name="append">Decides if the text should be appended to the annotationManager or not</param>
        /// <param name="inputTranslator">Used internally to translate for CNL to DL. Use it if the input is not in DL. From ENCNL, the translator is: x => Ontorion.CNL.EN.ENNameingConvention.ToDL(new Ontorion.CNL.EN.EnName() { id = x }, Ontorion.CNL.EN.endict.WordKind.NormalForm).id </param>
        /// <returns></returns>
        public void loadW3CAnnotationsFromText(string line, bool append = false, Func<string, string> inputTranslator = null)
        {
            if (String.IsNullOrEmpty(line) || String.IsNullOrWhiteSpace(line) || !line.Contains(":"))
                return;

            var localAnnotationManager = new AnnotationManager();

            var dp = 0;
            if (line.Contains(ANNOTATION_START))
                dp = line.IndexOf(':') + 1;

            bool newAnnotationSubjects = false;
            var refs = w3cAnnotRg.Matches(line.Substring(dp).Trim());
            foreach (Match match in refs)
            {
                string annotated = match.Groups["annotated"].Value;
                string type = match.Groups["type"].Value;
                string val = match.Groups["value"].Value;
                string kind = match.Groups["annotatedKind"].Value;
                // if one of these values are null this means that something went wrong during parsing... someone changed it manually?
                // in this case we skip this line.
                if (String.IsNullOrWhiteSpace(annotated) || String.IsNullOrWhiteSpace(type) || String.IsNullOrWhiteSpace(val) || String.IsNullOrWhiteSpace(kind))
                    continue;

                var res = ParseSubjectKind(kind);
                annotated = annotated.Trim();
                type = type.Trim();

                if (inputTranslator != null)
                {
                    if (res != ARS.EntityKind.Statement && !CNLTools.isSurelyDLEntity(annotated, res))
                        annotated = inputTranslator(annotated);

                    if (!CNLTools.isSurelyDLEntity(type, ARS.EntityKind.Role))
                        type = inputTranslator(type);
                }

                // if the subject is a statement, then the annotation manager keeps it internally as: statement with quotes inside (no quotes around!)
                if (res == ARS.EntityKind.Statement && annotated.StartsWith("\"") && annotated.EndsWith("\""))
                    annotated = annotated.Substring(1, annotated.Length - 2).Replace("\''","\"");
                else if (res == ARS.EntityKind.Statement && !annotated.StartsWith("\"") && !annotated.EndsWith("\"")) //statement should be quoted! If not, continue.
                    continue;

                if (!localAnnotationManager.ContainsAnnotationSubject(annotated.Trim()))
                {
                    newAnnotationSubjects = true;
                }
                localAnnotationManager.appendAnnotations(annotated.Trim(), kind.Trim(), new List<W3CAnnotation>() { new W3CAnnotation(true) { Type = type.Trim(), Value = val.Trim(), Language = match.Groups["language"].Value } });
            }

            if (newAnnotationSubjects && NewAnnotationSubject != null && FireNewSubjectEvent)
            {
                NewAnnotationSubject(this);
            }

            if (!append)
            {
                this.clearAnnotations();
            }

            appendAnnotations(localAnnotationManager);

            if (!append)
                this.AssumeNotModifiedNow();
        }

        /// <summary>
        /// Appends all annotations contained in annotExt into the currently loaded annotations.
        /// </summary>
        /// <param name="annotExt">Dictionary&lt;subject,list of annotations&gt;</param>
        private void appendAnnotations(Dictionary<string, List<W3CAnnotation>> annotExt)
        {
            if (annotExt == null)
                return;

            foreach (var ann in annotExt)
            {
                appendAnnotations(ann.Key, null, ann.Value);
            }
        }
        /// <summary>
        /// Append all annotations contained in dlannot to the annotations currently loaded into this Annotation Manager
        /// </summary>
        /// <param name="subj">subject of the annotations to load</param>
        /// <param name="subjType">type of the subject (role, instance,...)</param>
        /// <param name="dlannot">list of annotation to append.</param>
        /// <param name="recordChanges">If true all annotations that are added will be recorded into the AddedAnnotations</param>
        public void appendAnnotations(string subj, string subjType, List<W3CAnnotation> dlannot)
        {
            if (dlannot != null && dlannot.Count > 0)
            {
                if (!_annotatedElements.ContainsKey(subj))
                {
                    //if (subjType == ARS.EntityKind.Statement.ToString())
                    //    subj = GetStandardizedDLString(subj);
                    _annotatedElements.Add(subj, new List<W3CAnnotation>());
                    if (subjectType != null)
                        subjectType.Add(subj, subjType);
                    if (NewAnnotationSubject != null && FireNewSubjectEvent)
                    {
                        NewAnnotationSubject(this);
                    }
                }
                foreach (var ann in dlannot)
                {
                    if (!_annotatedElements[subj].Contains(ann))
                    {
                        if (!ann.isDL)
                            throw new Exception("The annotation manager can be used only with DL annotations.");
                        _annotatedElements[subj].Add(new W3CAnnotation(true) { Type = ann.Type, Value = ann.Value, Language = ann.Language, External = ann.External });

                        if (regxForPrefixes.IsMatch(ann.Type))
                        {
                            var prefix = regxForPrefixes.Match(ann.Type).ToString();
                            prefix = prefix.Trim();
                            if (wellKnownPrefixToNamespace.ContainsKey(prefix))
                            {
                                if (!pfx2NsInLoadedAnnotations.ContainsKey(prefix) && !prefix.StartsWith("<") && !prefix.EndsWith(">"))
                                    pfx2NsInLoadedAnnotations.Add(prefix, new Tuple<string, string>(wellKnownPrefixToNamespace[prefix], ""));
                            }
                            else if (!prefix.StartsWith("<") && !prefix.EndsWith(">"))
                            {
                                unknownPrefixes.Add(prefix);
                            }
                        }
                    }
                }
                _isModified = true;
            }
        }

        /// <summary>
        /// To add annotations. ! if the type is a statement, the subject should NOT be quoted!
        /// </summary>
        /// <param name="subj"></param>
        /// <param name="subjType"></param>
        /// <param name="dlannot"></param>
        public void Add(string subjExt, string subjType, List<W3CAnnotation> dlannot)
        {
            var subj = subjExt;
            if (subjType == ARS.EntityKind.Statement.ToString())
            {
                subj = subj.Replace("\r\n", "").Replace("''","\"");
            }

            appendAnnotations(subj, subjType, dlannot);
        }

        /// <summary>
        /// Appends all the annotations present in the annotMan to the annotations currently loaded in this annotation Manager.
        /// </summary>
        /// <param name="annotMan"></param>
        public void appendAnnotations(AnnotationManager annotMan)
        {
            if (annotMan == null)
                return;

            foreach (var ann in annotMan.GetAnnotationSubjects())
            {
                appendAnnotations(ann.Key, ann.Value, annotMan.GetAnnotations(ann.Key));
            }
        }

        /// <summary>
        /// Appends a DL annotation axiom into the current annotations.
        /// ! If the subject is a statement, it is expected without \r\n and NOT inside quotes!
        /// </summary>
        /// <param name="dLAnnotationAxiom"></param>
        public void appendAnnotations(DL.DLAnnotationAxiom dLAnnotationAxiom)
        {
            if (!String.IsNullOrWhiteSpace(dLAnnotationAxiom.value) && !String.IsNullOrWhiteSpace(dLAnnotationAxiom.annotName) && !String.IsNullOrWhiteSpace(dLAnnotationAxiom.subject))
            {
                var knd = ParseSubjectKind(dLAnnotationAxiom.subjKind);
                var subj = dLAnnotationAxiom.subject;
                appendAnnotations(subj, dLAnnotationAxiom.subjKind, new List<W3CAnnotation>() { new W3CAnnotation(true) { Type = dLAnnotationAxiom.annotName, Value = dLAnnotationAxiom.value, Language = !String.IsNullOrWhiteSpace(dLAnnotationAxiom.language) ? dLAnnotationAxiom.language : "" } });
            }
        }

        /// <summary>
        /// Returns a string in which all the annotations currently loaded are serialized.
        /// </summary>
        /// <returns>Serialized annotations.</returns>
        public string SerializeAnnotations()
        {
            StringBuilder annots = new StringBuilder();
            if (_annotatedElements.Count() != 0)
            {
                annots.AppendLine("Annotations:");
                foreach (var annotEl in _annotatedElements)
                {
                    var subj = annotEl.Key;
                    if (subjectType[annotEl.Key] == ARS.EntityKind.Statement.ToString())
                        subj = "\"" + subj.Replace("\"","''") + "\"";

                    foreach (var annotContain in annotEl.Value)
                    {
                        annots.AppendLine(subj + " " + subjectType[annotEl.Key] + ": " + annotContain.ToString());
                    }
                }
                annots.Append(".");
            }
            return annots.ToString();
        }

        /// <summary>
        /// here we store the prefix to namespace that are generally used.
        /// TODO ALESSANDRO the namespaces should be added automatically when importing from OWL! but right now this is not done....
        /// </summary>
        Dictionary<string, string> wellKnownPrefixToNamespace = new Dictionary<string, string>(){
            {"rdf", @"http://www.w3.org/1999/02/22-rdf-syntax-ns#"},
            {"rdfs", @"http://www.w3.org/2000/01/rdf-schema#"},
            {"owl", @"http://www.w3.org/2002/07/owl#"},
            {"dcterms", @"http://purl.org/dc/terms/"},
            {"skos", @"http://www.w3.org/2004/02/skos/core#"}
        };

        Regex regxForPrefixes = new Regex(@"(?<=\[).*?(?=\])");
        Dictionary<string, Tuple<string, string>> pfx2NsInLoadedAnnotations = new Dictionary<string, Tuple<string, string>>();
        /// <summary>
        /// Returns the Pfx2Ns map created on the basis of the annotations loaded into the AnnotationManager.
        /// !! Beware that only known annotations are loaded into the Pfx2Ns map.
        /// </summary>
        /// <returns></returns>
        public Dictionary<string, Tuple<string, string>> getPfx2NsDefinedInLoadedAnnotations()
        {
            return pfx2NsInLoadedAnnotations;
        }

        List<string> unknownPrefixes = new List<string>();
        /// <summary>
        /// Returns the unknown prefixes found in the annotations
        /// </summary>
        /// <returns></returns>
        public List<string> getUnknownPrefixesInAnnotations()
        {
            return unknownPrefixes;
        }

        public static List<string> SerializeAnnotations(string annotSubj, string subjType, List<W3CAnnotation> annotEl)
        {
            var subj = annotSubj;
            if (subjType == ARS.EntityKind.Statement.ToString())
                subj = "\"" + subj.Replace("\"", "''") + "\"";

            var allAnnot = new List<string>();
            foreach (var ann in annotEl)
            {
                allAnnot.Add(subj + " " + subjType + ": " + ann.ToString());
            }
            return allAnnot;
        }

        public void clearAnnotations()
        {
            _annotatedElements = new Dictionary<string, List<W3CAnnotation>>();
            subjectType = new Dictionary<string, string>();
            pfx2NsInLoadedAnnotations = new Dictionary<string, Tuple<string, string>>();
            AssumeNotModifiedNow();
        }

        public AnnotationManager Copy()
        {
            return new AnnotationManager(_annotatedElements, subjectType, pfx2NsInLoadedAnnotations);
        }

        /// <summary>
        /// Compare the annotations loaded in this annotation manager to the annotations loaded in the annotNew.
        /// </summary>
        /// <param name="annotNew">Manager to which you want to compare the current annotations</param>
        /// <returns>An annotations manager diff structure which contains the difference between the two annotation managers</returns>
        public AnnotationManagerDiff Compare(AnnotationManager annotNew)
        {
            AnnotationManagerDiff diffMan = new AnnotationManagerDiff();
            // get the difference between the keys of the two managers.
            var keysInRef = new HashSet<string>(this.GetAnnotationSubjects().Select(x => x.Key).ToList());
            var keysInNew = new HashSet<string>(annotNew.GetAnnotationSubjects().Select(x => x.Key).ToList());
            // all keys present in annotReference and not in annotNew should be added in removedAnnotations
            var keysInRefNotInNew = keysInRef.Except(keysInNew);
            foreach (var annKey in keysInRefNotInNew)
            {
                diffMan.RemovedAnnotations.Add(new Tuple<string, string>(annKey, this.subjectType[annKey]), this.GetAnnotations(annKey));
            }
            // all keys present in annotNew and not in annotReference should be added in addedAnnotations
            var keysInNewNotInRef = keysInNew.Except(keysInRef);
            foreach (var annKey in keysInNewNotInRef)
            {
                diffMan.AddedAnnotations.Add(new Tuple<string, string>(annKey, annotNew.subjectType[annKey]), annotNew.GetAnnotations(annKey));
            }

            // if they have both the same key --> check if each annotation is the same
            var keysInBoth = keysInRef.Intersect(keysInNew);
            foreach (var annKey in keysInBoth)
            {
                var annInRef = new HashSet<W3CAnnotation>(this.GetAnnotations(annKey));
                var annInNew = new HashSet<W3CAnnotation>(annotNew.GetAnnotations(annKey));
                var annInRefNotInNew = annInRef.Except(annInNew, new W3CAnnotation(true)).ToList();
                if (annInRefNotInNew.Count > 0)
                    diffMan.RemovedAnnotations.Add(new Tuple<string, string>(annKey, this.subjectType[annKey]), annInRefNotInNew);
                var annInNewNotInRef = annInNew.Except(annInRef, new W3CAnnotation(true)).ToList();
                if (annInNewNotInRef.Count > 0)
                    diffMan.AddedAnnotations.Add(new Tuple<string, string>(annKey, annotNew.subjectType[annKey]), annInNewNotInRef);
            }

            return diffMan;
        }

        /// <summary>
        /// Update the annotation for subj by changing the old annotation with the new one. If the newW3CEl is null --> the old one is removed.
        /// If oldW3CEl is null --> all annotations relative to subj are removed.
        /// </summary>
        /// <param name="subj"></param>
        /// <param name="oldW3CEl"></param>
        /// <param name="newW3CEl">If null, only remove the oldOne</param>
        /// <returns></returns>
        public bool UpdateAnnotation(string subj, W3CAnnotation oldW3CEl = null, W3CAnnotation newW3CEl = null)
        {
            if (oldW3CEl != null && newW3CEl != null && oldW3CEl.Equals(newW3CEl))
                return false;

            if (_annotatedElements.ContainsKey(subj))
            {
                if (oldW3CEl == null)
                {
                    _annotatedElements.Remove(subj);
                    subjectType.Remove(subj);
                    _isModified = true;
                    return true;
                }

                if (_annotatedElements[subj].Contains(oldW3CEl))
                {
                    _annotatedElements[subj].Remove(oldW3CEl);
                    if (newW3CEl != null)
                    {
                        _annotatedElements[subj].Add(newW3CEl);
                    }
                    else if (_annotatedElements[subj].Count == 0)
                    {
                        _annotatedElements.Remove(subj);
                        subjectType.Remove(subj);
                    }
                    _isModified = true;
                    return true;
                }
            }
            return false;
        }

        //TODO ALESSANDRO this flag can be used to check if the annotation manager has been changed from the last save.
        // it should be reset when saving the cnl file and it should be checked when the window is closed.
        private bool _isModified = false;
        /// <summary>
        /// True if the annotations have been modified from the last time that AssumeNotModifiedNow has been called.
        /// </summary>
        public bool isModified { get { return _isModified; } }
        public void AssumeNotModifiedNow()
        {
            _isModified = false;
        }

        private void UpdatePossibleENCNLLabelsAndLang(Func<string, string> inputTranslator)
        {
            possibleLabels = new List<string>();
            possibleLang = new List<string>();
            foreach (var annotEl in _annotatedElements)
            {
                foreach (var labl in annotEl.Value)
                {
                    if (!possibleLabels.Contains(labl.Type))
                    {
                        possibleLabels.Add(inputTranslator(labl.Type));
                    }
                    if (!possibleLang.Contains(labl.Language))
                    {
                        possibleLang.Add(labl.Language);
                    }
                }
            }
        }

        /// <summary>
        /// event that is fired when a new annotation subject is added.
        /// </summary>
        public event NewAnnotationSubjectHandler NewAnnotationSubject;

        private List<string> possibleLabels = null;
        /// <summary>
        /// searches in the currently loaded annotations all the possible types
        /// </summary>
        /// <param name="inputTranslator">This is needed to translate the annotations (internally stored in DL) to the ouput language. From ENCNL, the translator is: x => Ontorion.CNL.EN.ENNameingConvention.ToDL(new Ontorion.CNL.EN.EnName() { id = x }, Ontorion.CNL.EN.endict.WordKind.NormalForm).id  </param>
        /// <returns></returns>
        public List<string> getPossibleTypesENCNL(Func<string, string> inputTranslator)
        {
            if (isModified || possibleLabels == null)
            {
                UpdatePossibleENCNLLabelsAndLang(inputTranslator);
            }
            return possibleLabels;
        }

        private List<string> possibleLang = null;
        public bool FireNewSubjectEvent = true;
        /// <summary>
        /// Searches in the currently loaded annotations all the possible languages.
        /// </summary>
        /// <param name="inputTranslator">This is needed to translate the annotations (internally stored in DL) to the ouput language. From ENCNL, the translator is: x => Ontorion.CNL.EN.ENNameingConvention.ToDL(new Ontorion.CNL.EN.EnName() { id = x }, Ontorion.CNL.EN.endict.WordKind.NormalForm).id  </param>
        /// <returns></returns>
        public List<string> getPossibleLang(Func<string, string> inputTranslator)
        {
            if (isModified || possibleLang == null)
            {
                UpdatePossibleENCNLLabelsAndLang(inputTranslator);
            }
            return possibleLang;
        }

        public List<W3CAnnotation> GetAnnotations(string annotSubj)
        {
            //var subj = GetStandardizedDLString(annotSubj);
            var subj = annotSubj;
            if (_annotatedElements.ContainsKey(subj))
                return _annotatedElements[subj];
            else if (_annotatedElements.ContainsKey(subj.Replace("\r\n", "")))
                return _annotatedElements[subj.Replace("\r\n", "")];
            else
                return null;
        }

        /// <summary>
        /// Returns the dictionary with (subject,subjectType)
        /// </summary>
        /// <returns></returns>
        public Dictionary<string, string> GetAnnotationSubjects()
        {
            return new Dictionary<string, string>(subjectType);
        }

        /// <summary>
        /// Returns all annotations contained in the manager as DLAnnotationAxiom.
        /// </summary>
        /// <param name="pfx2ns">If not null, it will be used to transform each prefix found to the full namespace.</param>
        /// <returns></returns>
        public Dictionary<ARS.EntityKind, List<DL.DLAnnotationAxiom>> getDLAnnotationAxioms(Func<string, string> pfx2ns = null)
        {
            var dlAnnotatedAxioms = new Dictionary<ARS.EntityKind, List<DL.DLAnnotationAxiom>>();
            foreach (var annotKv in _annotatedElements)
            {
                var kind = ParseSubjectKind(subjectType[annotKv.Key]);
                if (!dlAnnotatedAxioms.ContainsKey(kind))
                    dlAnnotatedAxioms.Add(kind, new List<DL.DLAnnotationAxiom>());
                var dlSubj = CNLTools.DLToFullUri(annotKv.Key, kind, pfx2ns);
                foreach (var annotEl in annotKv.Value)
                {
                    var nameToUse = annotEl.Type;

                    dlAnnotatedAxioms[kind].Add(new DL.DLAnnotationAxiom(null, dlSubj, subjectType[annotKv.Key], CNLTools.DLToFullUri(annotEl.Type, ARS.EntityKind.Role, pfx2ns), annotEl.Language, (string)annotEl.Value));
                }
            }
            return dlAnnotatedAxioms;
        }

        public static Ontorion.ARS.EntityKind ParseSubjectKind(string kind)
        {
            Ontorion.ARS.EntityKind result;
            if (Enum.TryParse(kind, true, out result))
            {
                return result;
            }
            else
                throw new Exception("Could not parse " + kind + " to an EntityKind.");
        }
    }
    public delegate void NewAnnotationSubjectHandler(object sender);

    /// <summary>
    /// A class containing the difference between two annotation managers.
    /// </summary>
    public class AnnotationManagerDiff
    {
        private Dictionary<Tuple<string, string>, List<W3CAnnotation>> _addedAnnotations = new Dictionary<Tuple<string, string>, List<W3CAnnotation>>();
        public Dictionary<Tuple<string, string>, List<W3CAnnotation>> AddedAnnotations
        {
            get { return _addedAnnotations; }
        }

        private Dictionary<Tuple<string, string>, List<W3CAnnotation>> _removedAnnotations = new Dictionary<Tuple<string, string>, List<W3CAnnotation>>();
        public Dictionary<Tuple<string, string>, List<W3CAnnotation>> RemovedAnnotations
        {
            get { return _removedAnnotations; }
        }
    }

}
