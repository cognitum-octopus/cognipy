using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;

namespace CogniPy.CNL.EN
{
    public class endict
    {
        Dictionary<String, String> pp = new Dictionary<String, String>();
        Dictionary<String, String> i_pp = new Dictionary<String, String>();
        Dictionary<String, String> sp = new Dictionary<String, String>();
        Dictionary<String, String> i_sp = new Dictionary<String, String>();
        Dictionary<String, String> pl = new Dictionary<String, String>();
        Dictionary<String, String> i_pl = new Dictionary<String, String>();

        public endict(Stream s)
        {
            if (s == null)
                return;

            StreamReader sr = new StreamReader(s);
            try
            {
                init(sr);
            }
            catch (Exception ex)
            {
#if DEBUG
                Debugger.Break();
#endif
                throw;
            }
        }

        private void init(StreamReader tr)
        {
            while (!tr.EndOfStream)
            {
                if (tr.ReadLine().StartsWith("%plural form"))
                    break;
            }
            while (!tr.EndOfStream)
            {
                String str = tr.ReadLine();
                if (str.StartsWith("%simple past"))
                    break;
                var v = str.Split(new char[] { ':' });
                pl.Add(v[1], v[0]);
                i_pl.Add(v[0], v[1]);
            }
            while (!tr.EndOfStream)
            {
                String str = tr.ReadLine();
                if (str.StartsWith("%past participle"))
                    break;
                var v = str.Split(new char[] { ':' });
                sp.Add(v[1], v[0]);
                i_sp.Add(v[0], v[1]);
            }
            while (!tr.EndOfStream)
            {
                String str = tr.ReadLine();
                if (str == null)
                    break;
                var v = str.Split(new char[] { ':' });
                pp.Add(v[1], v[0]);
                i_pp.Add(v[0], v[1]);
            }

        }

        public enum WordKind { PastParticiple, SimplePast, PluralFormNoun, PluralFormVerb, NormalForm };

        public String toDL_Simple(String A, WordKind k)
        {
            if (A.Length > 0 && char.IsDigit(A[0]))
                return A;

            if (k == WordKind.SimplePast)
            {
                if (A == "was")
                    return "was";
                else if (sp.ContainsKey(A))
                    return sp[A];
                else
                {
                    if (A.EndsWith("ed") && A.Length != 2)
                        return A.Substring(0, A.Length - 2);
                    else if (A.EndsWith("d") && A.Length != 1)
                        return A.Substring(0, A.Length - 1);
                    else
                        return A;
                }
            }
            else if (k == WordKind.PastParticiple)
            {
                if (pp.ContainsKey(A))
                    return pp[A];
                else
                {
                    if (A == "was")
                        return "was";
                    else if (A == "is" || A == "are")
                        return "be";
                    else if (A.EndsWith("s") && A.Length != 1)
                        return A.Substring(0, A.Length - 1);
                    else
                        return A;
                }
            }
            else if (k == WordKind.PluralFormNoun)
            {
                if (pl.ContainsKey(A))
                    return pl[A];
                else
                {
                    if (A.EndsWith("s") && A.Length != 1)
                        return A.Substring(0, A.Length - 1);
                    else
                        return A;
                }
            }
            else if (k == WordKind.PluralFormVerb)
            {
                if (A == "was")
                    return "was";
                else if (A == "are" || A == "is")
                    return "be";
                else
                    return A;
            }
            else return A;
        }

        public String toN_Simple(String A, WordKind k)
        {
            if (A.Length > 0 && char.IsDigit(A[0]))
                return A;

            if (k == WordKind.SimplePast)
            {
                if (i_sp.ContainsKey(A))
                    return i_sp[A];
                if (A == "was")
                {
                    return "was";
                }
                else if (A == "be")
                    return "is";
                else
                {
                    if (A.EndsWith("e") && A.Length != 1)
                        return A + "d";
                    else
                        return A + "ed";
                }
            }
            else if (k == WordKind.PastParticiple)
            {
                if (i_pp.ContainsKey(A))
                    return i_pp[A];
                else if (A == "was")
                    return "was";
                else if (A == "be")
                    return "is";
                else
                {
                    return A + "s";
                }
            }
            else if (k == WordKind.PluralFormNoun)
            {
                if (i_pl.ContainsKey(A))
                    return i_pl[A];
                else
                {
                    return A + "s";
                }
            }
            else if (k == WordKind.PluralFormVerb)
            {
                if (A == "was")
                    return "was";
                else if (A == "be")
                    return "are";
                else
                {
                    return A;
                }
            }
            else return A;
        }
    }
}
