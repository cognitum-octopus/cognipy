package tools;

import java.util.*;

/*	public class Test
    {
        static string curline = "";
        static int pos = 0;
        static bool EOF = false;
        static void GetLine(TextReader f)
        {
            curline = f.ReadLine();
            pos = 0;
            if (curline == null)
                EOF = true;
        }
        static int GetInt(TextReader f)
        {
            int v = 0;
            bool s = false;
            while (pos<curline.Length) 
            {
                char c = curline[pos++];
                if (c==' ')
                    continue;
                if (c=='-')
                {
                    s = true;
                    continue;
                }
                if (c==',')
                {
                    if (s)
                        v = -v;
                    if (pos==curline.Length)
                        GetLine(f);
                    return v;
                }
                if (c>='0' && c<='9')
                {
                    v = v*10 + (c-'0');
                    continue;
                }
                throw new Exception("illegal character");
            }
            throw new Exception("bad line");
        }
        public static void Main(string[] args)
        {
            TextWriter x = new StreamWriter("out.txt");
            Hashtable t = new Hashtable();
            t["John"] = 12;
            t["Mary"] = 34;
            Serialiser sr = new Serialiser(x);
            Console.WriteLine("Encoding");
            sr.Serialise(t);
            x.Close();
            ArrayList a = new ArrayList();
            TextReader y = new StreamReader("out.txt");
            GetLine(y);
            while (!EOF)
                a.Add(GetInt(y));
            y.Close();
            for (int k=0;k<a.Count;k++)
                Console.WriteLine((int)a[k]); 
            int[] b = new int[a.Count];
            for (int k=0;k<a.Count;k++)
                b[k] = (int)a[k];
            Serialiser dr = new Serialiser(b);
            Hashtable h = (Hashtable)dr.Deserialise();
            foreach (DictionaryEntry d in h)
                Console.WriteLine((string)d.Key + "->" + (int)d.Value);
        }
    } */