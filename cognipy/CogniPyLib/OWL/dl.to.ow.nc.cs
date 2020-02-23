using System;
using System.Collections.Generic;
using System.Text;
using org.semanticweb.owlapi.model;
using org.semanticweb.owlapi.vocab;
using System.Globalization;
using org.semanticweb.owlapi.reasoner;
using Ontorion.CNL.DL;
using Ontorion.CNL.EN;
using org.coode.xml;
using System.Text.RegularExpressions;
using System.Diagnostics;
using org.semanticweb.owlapi.util;

namespace Ontorion.ARS
{
    public class DLToOWLNameConv 
    {
        Dictionary<string, string> Pfx2ns = new Dictionary<string, string>();
        Dictionary<string, string> Ns2pfx = new Dictionary<string, string>();
        string defaultNs;

        Ontorion.CNL.EN.endict lex = null;

        public void ClearOWLFormat()
        {
            Pfx2ns = new Dictionary<string, string>();
            Ns2pfx = new Dictionary<string, string>();
            lex = null;
        }

        public void setOWLFormat(string defaultNS,PrefixOWLOntologyFormat namespaceManager, Ontorion.CNL.EN.endict lex)
        {
            this.defaultNs = defaultNS;
            var map = namespaceManager.getPrefixName2PrefixMap();
            var keys = map.keySet().iterator();
            while (keys.hasNext())
            {
                var k = keys.next().ToString();
                var v = map.get(k).ToString();
                k = k.Split(':')[0];
                if (string.IsNullOrEmpty(k))
                {
                    defaultNs = v;
                    continue;
                }
                if(!Pfx2ns.ContainsKey(k))
                    Pfx2ns.Add(k, v);
                if (!Ns2pfx.ContainsKey(v))
                    Ns2pfx.Add(v, k);
            }

            this.lex = lex;
        }

        IOwlNameingConvention owlNameingConvention = new OwlNameingConventionCamelCase();
        public Dictionary<string, string> InvUriMappings = new Dictionary<string, string>();

        private static NamespaceUtil namespaceUtil = new NamespaceUtil();

        // this is used ToDL names --> namespace should be inside <>
        string ns2pfx(string arg)
        {
            if (arg == null)
                return "<"+defaultNs+">";

            if (!arg.EndsWith("/") && !arg.EndsWith("#") && !arg.Contains("#"))
                arg += "#";

            if (!Ns2pfx.ContainsKey(arg) && !arg.StartsWith("<") && !arg.EndsWith(">"))
                return "<" + arg + ">";
            else if (!Ns2pfx.ContainsKey(arg))
                return arg;

            return Ns2pfx[arg];
        }

        string pfx2ns(string arg)
        {
            if (arg == null)
                return defaultNs;

            if (!Pfx2ns.ContainsKey(arg))
            {
                if (arg.StartsWith("<") && arg.EndsWith(">"))
                {
                    var argg = arg.Substring(1, arg.Length - 2);
                    if (!argg.EndsWith("/") && !argg.EndsWith("#") && !argg.Contains("#"))
                        argg += "#";
                    return argg;
                }
                else
                {
                    if(namespaceUtil.getNamespace2PrefixMap().containsValue(arg))
                    {
                        var iter = namespaceUtil.getNamespace2PrefixMap().keySet().iterator();
                        while(iter.hasNext())
                        {
                            var k = iter.next();
                            if (namespaceUtil.getNamespace2PrefixMap().get(k).ToString() == arg)
                                return k.ToString();
                        }
                    }
                    else
                        return "http://unknown.prefix/" + arg + "#";
                }
            }

            return Pfx2ns[arg];
        }

        public IRI getIRIFromId(string qname, EntityKind makeFor)
        {
            if (InvUriMappings.ContainsKey(qname))
                return IRI.create(InvUriMappings[qname]);
            else
            {
                DlName dl = new DlName() { id = qname };
                var dlp = dl.Split();
                if (InvUriMappings.ContainsKey(dlp.name) && dlp.term == ns2pfx(IRI.create(InvUriMappings[dlp.name]).getNamespace()))
                    return IRI.create(InvUriMappings[dlp.name]);
                else
                    return owlNameingConvention.FromDL(new DlName() { id = qname }, lex, pfx2ns, makeFor).iri;
            }
        }

        public DlName ToDL(string uri, ARS.EntityKind makeFor)
        {
            var owlName = new Ontorion.ARS.OwlName() { iri = IRI.create(uri) };
             return owlNameingConvention.ToDL(owlName, lex, ns2pfx, makeFor); 
        }

    }
}
