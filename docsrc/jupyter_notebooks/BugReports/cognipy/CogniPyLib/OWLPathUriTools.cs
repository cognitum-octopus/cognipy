using CogniPy.ARS;
using org.semanticweb.owlapi.model;
using System;
using System.IO;

namespace CogniPy.OWL
{
    public static class OWLPathUriTools
    {
        static public string Path2Uri(string fileName)
        {
            return Uri.EscapeUriString(fileName.Replace('\\', '/'));
        }

        static public string Uri2Path(string uri)
        {
            return Uri.UnescapeDataString(uri);
        }

        static public IRI Path2IRI(string fileName)
        {
            return IRI.create(fileName.Replace('\\', '/'));
        }

        static public string IRI2Path(IRI iri)
        {
            return iri.toURI().toString().Replace('\\', '/');
        }

        static public string CombinePath(string dir, string end)
        {
            var pt = Path.GetFullPath(dir).Replace("/", "\\");
            var et = end.Replace("/", "\\");
            return pt + ((pt.EndsWith("\\") || et.StartsWith("\\")) ? "" : "\\") + et;
        }

        static public string CombineUri(string uri, string end)
        {
            var urun = Uri.UnescapeDataString(uri);
            return Uri.EscapeUriString(urun + (urun.EndsWith("/") ? "" : "/") + Uri.UnescapeDataString(end));
        }

        public static string Iri2Dl(string uri, EntityKind type)
        {
            InvTransform invTrans = new InvTransform();

            var n = invTrans.renderEntity(new OwlName() { iri = OWL.OWLPathUriTools.Path2IRI(uri) }, type);
            if (n == null)
                return "";
            return n;
        }
    }
}
