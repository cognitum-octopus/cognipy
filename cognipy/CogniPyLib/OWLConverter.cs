using CogniPy.OWL;
using org.semanticweb.owlapi.apibinding;
using org.semanticweb.owlapi.model;
using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;

namespace CogniPy
{
    public static class OWLConverter
    {

        private static string SerializeDoc(XmlDocument doc)
        {
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.Indent = true;
            settings.IndentChars = "  ";
            settings.NewLineOnAttributes = false;
            settings.NamespaceHandling = NamespaceHandling.OmitDuplicates;
            settings.Encoding = Encoding.UTF8;
            settings.OmitXmlDeclaration = true;

            StringBuilder sb = new StringBuilder();
            sb.AppendLine("<?xml version = '1.0' encoding = 'UTF-8'?>");
            XmlWriter writer = XmlWriter.Create(sb, settings);
            doc.Save(writer);
            return sb.ToString();
        }

        public static string PathToIRIString(string path)
        {
            return OWLPathUriTools.Path2IRI(path).toString();
        }

        public static OWLOntology ontology = null;

        public static string GetOWLXML(CNL.DL.Paragraph para, CNL.DL.Paragraph paraFromAnnotStatements, bool owlXml, string externext, Dictionary<string, string> invUriMappings, string ontologyBase = null, Dictionary<string, Tuple<string, string>> prefixes = null, string defaultGuid = null, Dictionary<string, List<string>> owlOntologyAnnotation = null, string generatedBy = "Cognitum FluentEditor2015")
        {
            prefixes = prefixes ?? new Dictionary<string, Tuple<string, string>>();
            if (ontologyBase == null)
                ontologyBase = "http://www.ontorion.com/ontologies/Ontology" + Guid.NewGuid().ToString("N");

            //var ontologyBase = (ontologyNs ?? "http://www.ontorion.com/ontologies/Ontology" + Guid.NewGuid().ToString("N"));
            //if(!String.IsNullOrEmpty(defaultGuid))
            //    ontologyBase = (ontologyNs ?? "http://www.ontorion.com/ontologies/Ontology" + defaultGuid);

            if (!ontologyBase.EndsWith("/") && !ontologyBase.EndsWith("#") && !ontologyBase.Contains("#"))
                ontologyBase += "#";

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            CogniPy.ARS.Transform transform = new CogniPy.ARS.Transform();
            transform.InvUriMappings = invUriMappings;
            var df = manager.getOWLDataFactory();
            ontology = manager.createOntology(IRI.create(ontologyBase));

            org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat owlxmlFormat = null;
            if (owlXml)
            {
                owlxmlFormat = new org.semanticweb.owlapi.io.OWLXMLOntologyFormat();
            }
            else
            {
                owlxmlFormat = new org.semanticweb.owlapi.io.RDFXMLOntologyFormat();
            }

            owlxmlFormat.setDefaultPrefix(ontologyBase);

            foreach (var kv in prefixes)
            {
                owlxmlFormat.setPrefix(kv.Key.Replace("$", "."), kv.Value.Item1); // should we put here the Item2 (the location) and not the inner namespace???
                if (!String.IsNullOrEmpty(kv.Value.Item2) && !(kv.Value.Item2.EndsWith(".encnl") || kv.Value.Item2.EndsWith(".encnl#"))) // do not export cnl imports (not in OWL!)
                {
                    // here we need to use Item1 because we cannot export into OWL the specific location of the ontology ---> this is bad practice as in this way we loose the generality of the owl file
                    // imagine that the location is C://mydirectory/... and I add into an owl file: owl:import "C:/mydirectory/". This mean that only on my computer I will be able to import this file.
                    // On the other hand if we write owl:import "namespace of the file" there is a good chance that when someone else will open the file, the file will be imported from internet.

                    var decl = manager.getOWLDataFactory().getOWLImportsDeclaration(OWLPathUriTools.Path2IRI(kv.Value.Item1.TrimEnd('#')));
                    manager.applyChange(new AddImport(ontology, decl));
                }
            }

            manager.setOntologyFormat(ontology, owlxmlFormat);

            if (owlOntologyAnnotation != null)
            {
                foreach (var keyVal in owlOntologyAnnotation)
                {
                    foreach (string val in keyVal.Value)
                    {
                        manager.applyChange(new AddOntologyAnnotation(
                                    ontology,
                                    manager.getOWLDataFactory().getOWLAnnotation(
                                         manager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(keyVal.Key)),
                                         manager.getOWLDataFactory().getOWLLiteral(val))));
                    }
                }
            }

            transform.setOWLDataFactory(false, ontologyBase, df, owlxmlFormat, CNL.EN.CNLFactory.lex);

            var conv = transform.Convert(para, paraFromAnnotStatements);

            foreach (var axiom in conv.axioms)
            {
                if (axiom.comment != null)
                {
                    //    var dp = axiom.comment.IndexOf(':');
                    //    var x = axiom.comment.Substring(0, dp);
                    //    if (x.Trim() == "Namespace")
                    //    {
                    //        var ontologyIri = axiom.comment.Substring(dp + 1).Trim();
                    //        if (ontologyIri.EndsWith(".")) ontologyIri = ontologyIri.Substring(0, ontologyIri.Length - 1);
                    //        if (ontologyIri.StartsWith("\'") && ontologyIri.Length > 2)
                    //            ontologyIri = ontologyIri.Substring(1, ontologyIri.Length - 2).Replace("\'\'", "\'");
                    //        manager.removeOntology(ontology);
                    //        ontology = manager.createOntology(IRI.create(ontologyIri));
                    //        om = new org.coode.xml.OWLOntologyXMLNamespaceManager(manager, ontology);
                    //        om.setDefaultNamespace(ontologyIri + "#");
                    //        transform.setOWLDataFactory(df, om, CNL.EN.CNLFactory.lex);
                    //    }
                    //    else if (x.Trim() == "References")
                    //    {
                    //        var refs = ReferenceManager.ParseReferences(axiom.comment.Substring(dp));
                    //        foreach (Match match in refs)
                    //        {
                    //            var onto = match.Groups["ont"].Value;
                    //            if (onto.StartsWith("\'") && onto.Length > 2)
                    //                onto = onto.Substring(1, onto.Length - 2).Replace("\'\'", "\'").Trim();

                    //            if (!string.IsNullOrEmpty(onto))
                    //            {
                    //                if (onto.ToLower().EndsWith(".encnl"))
                    //                    onto = OWLConverter.PathToIRIString(onto.Substring(0, onto.Length - ".encnl".Length) + externext);


                    //                var ns = match.Groups["ns"].Value;
                    //                if (ns.StartsWith("\'") && ns.Length > 2)
                    //                    ns = ns.Substring(1, ns.Length - 2).Replace("\'\'", "\'").Trim();
                    //                else
                    //                    if (string.IsNullOrEmpty(ns))
                    //                        ns = onto;

                    //                om.setPrefix(match.Groups["pfx"].Value, ns);
                    //                owlxmlFormat.setPrefix(match.Groups["pfx"].Value, ns);
                    //                var decl = manager.getOWLDataFactory().getOWLImportsDeclaration(OWLPathUriTools.Path2IRI(onto));
                    //                manager.applyChange(new AddImport(ontology, decl));
                    //            }
                    //        }
                    //    }
                    //    else
                    //    {
                    //        //manager.applyChange(new AddOntologyAnnotation(
                    //        //    ontology,
                    //        //    manager.getOWLDataFactory().getOWLAnnotation(
                    //        //         manager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(ontologyBase+x)),
                    //        //         manager.getOWLDataFactory().getOWLLiteral(axiom.comment.Substring(dp)))));
                    //    }
                }
                else if (axiom.axiom != null)
                    manager.addAxiom(ontology, axiom.axiom);
            }

            foreach (var axiom in conv.additions)
                manager.addAxiom(ontology, axiom);

            var ontout = new org.semanticweb.owlapi.io.StringDocumentTarget();
            manager.saveOntology(ontology, owlxmlFormat, ontout);
            manager.removeOntology(ontology);
            var retdoc = new XmlDocument();
            retdoc.LoadXml(ontout.toString());
            foreach (var elem in retdoc.ChildNodes)
            {
                if (elem is XmlComment)
                    retdoc.RemoveChild(elem as XmlComment);
            }
            retdoc.AppendChild(retdoc.CreateComment("Generated by " + generatedBy + ", a part of Ontorion(TM) Knowledge Management Framework, (with support of OwlApi)"));

            return SerializeDoc(retdoc);
        }

        //public static string GetOWLXML(CNL.DL.Paragraph para)
        //{
        //    var ontologyIri = "http://www.ontorion.com/ontologies/Ontology" + Guid.NewGuid().ToString("N") + "#";
        //    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        //    Ontorion.ARS.Transform transform = new Ontorion.ARS.Transform();
        //    var df = manager.getOWLDataFactory();
        //    var ontology = manager.createOntology(IRI.create(ontologyIri));
        //    var om = new org.coode.xml.OWLOntologyXMLNamespaceManager(manager, ontology);
        //    om.setDefaultNamespace(ontologyIri);
        //    transform.setOWLDataFactory(df, om, CNL.EN.CNLFactory.lex);

        //    var conv = transform.Convert(para);

        //    foreach (var axiom in conv.axioms)
        //    {
        //        if (axiom.comment != null)
        //        {
        //            var dp = axiom.comment.IndexOf(':');
        //            var x = axiom.comment.Substring(0, dp);
        //            if (x.Trim() == "Namespace")
        //            {
        //                ontologyIri = axiom.comment.Substring(dp + 1).Trim();
        //                if (ontologyIri.EndsWith(".")) ontologyIri = ontologyIri.Substring(0, ontologyIri.Length - 1);
        //                if (ontologyIri.StartsWith("\'") && ontologyIri.Length > 2)
        //                    ontologyIri = ontologyIri.Substring(1, ontologyIri.Length - 2).Replace("\'\'", "\'");
        //                manager.removeOntology(ontology);
        //                ontology = manager.createOntology(IRI.create(ontologyIri));
        //                om = new org.coode.xml.OWLOntologyXMLNamespaceManager(manager, ontology);
        //                om.setDefaultNamespace(ontologyIri);
        //                transform.setOWLDataFactory(df, om, CNL.EN.CNLFactory.lex);
        //            }
        //        }
        //    }

        //    var owlxmlFormat = new org.semanticweb.owlapi.io.OWLXMLOntologyFormat();
        //    XmlDocument retdoc = new XmlDocument();
        //    {
        //        var ontout = new org.semanticweb.owlapi.io.StringDocumentTarget();

        //        foreach (var axiom in conv.axioms)
        //        {
        //            if (axiom.comment != null)
        //            {
        //                var dp = axiom.comment.IndexOf(':');
        //                var x = axiom.comment.Substring(0, dp);
        //                if (x.Trim() == "References")
        //                {
        //                    var refs = ReferenceManager.ParseReferences(axiom.comment.Substring(dp));
        //                    foreach (Match match in refs)
        //                    {
        //                        var onto = match.Groups["ont"].Value;
        //                        if (onto.StartsWith("\'") && onto.Length > 2)
        //                            onto = onto.Substring(1, onto.Length - 2).Replace("\'\'", "\'").Trim();

        //                        if (!string.IsNullOrEmpty(onto))
        //                        {
        //                            om.setPrefix(match.Groups["pfx"].Value, onto);
        //                            owlxmlFormat.setPrefix(match.Groups["pfx"].Value, onto);
        //                            var decl = manager.getOWLDataFactory().getOWLImportsDeclaration(OWLPathUriTools.Path2IRI(onto));
        //                            manager.applyChange(new AddImport(ontology, decl));
        //                        }
        //                    }
        //                }
        //            }
        //        }
        //        manager.saveOntology(ontology, owlxmlFormat, ontout);
        //        manager.removeOntology(ontology);
        //        retdoc.LoadXml(ontout.toString());
        //        foreach (var elem in retdoc.ChildNodes)
        //        {
        //            if (elem is XmlComment)
        //                retdoc.RemoveChild(elem as XmlComment);
        //        }
        //        retdoc.AppendChild(retdoc.CreateComment("Generated by Cognitum FluentEditor2, a part of Ontorion(TM) Knowledge Management Framework, (with support of OwlApi)"));
        //    }
        //    conv = transform.Convert(para);

        //    foreach (var axiom in conv.axioms)
        //    {
        //        if (axiom.axiom != null)
        //        {
        //            ontology = manager.createOntology(IRI.create(ontologyIri));
        //            transform.setOWLDataFactory(df, new org.coode.xml.OWLOntologyXMLNamespaceManager(manager, ontology), CNL.EN.CNLFactory.lex);
        //            manager.addAxiom(ontology, axiom.axiom);
        //            XmlDocument XMLdoc = new XmlDocument();
        //            var ontout = new org.semanticweb.owlapi.io.StringDocumentTarget();
        //            manager.saveOntology(ontology, owlxmlFormat, ontout);
        //            manager.removeOntology(ontology);
        //            XMLdoc.LoadXml(ontout.toString());
        //            var child = retdoc.CreateElement("X");
        //            child.InnerXml = XMLdoc.ChildNodes[1].LastChild.OuterXml;
        //            retdoc.ChildNodes[1].AppendChild(child.FirstChild);
        //        }
        //        else if (axiom.comment != null)
        //        {
        //            var dp = axiom.comment.IndexOf(':');
        //            var x = axiom.comment.Substring(0, dp);
        //            if (x.Trim() != "References")
        //            {
        //                retdoc.ChildNodes[1].AppendChild(retdoc.CreateComment(axiom.comment));
        //            }
        //        }
        //    }

        //    foreach (var axiom in conv.additions)
        //    {
        //        ontology = manager.createOntology(IRI.create(ontologyIri));
        //        transform.setOWLDataFactory(df, new org.coode.xml.OWLOntologyXMLNamespaceManager(manager, ontology), CNL.EN.CNLFactory.lex);
        //        manager.addAxiom(ontology, axiom);
        //        XmlDocument XMLdoc = new XmlDocument();
        //        var ontout = new org.semanticweb.owlapi.io.StringDocumentTarget();
        //        manager.saveOntology(ontology, owlxmlFormat, ontout);
        //        manager.removeOntology(ontology);
        //        XMLdoc.LoadXml(ontout.toString());
        //        var child = retdoc.CreateElement("X");
        //        child.InnerXml = XMLdoc.ChildNodes[1].LastChild.OuterXml;
        //        retdoc.ChildNodes[1].AppendChild(child.FirstChild);
        //    }

        //    return SerializeDoc(retdoc);
        //}

    }
}


