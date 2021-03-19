using System;
using System.Text;
using System.IO;

namespace Tools
{
	public class GenBase
	{
        public bool m_partial = false;
        
        public ErrorHandler erh;
		protected GenBase(ErrorHandler eh) { erh = eh; }
		public TextWriter m_outFile;
		protected Encoding m_scriptEncoding = Encoding.ASCII;
		protected bool toupper = false;
		protected string ScriptEncoding 
		{
			set 
			{
				m_scriptEncoding = Encoding.ASCII;
			}
		}
		public string m_outname;
		// convenience functions
		protected int Braces(int a,string b,ref int p,int max) 
		{
			int rv = a;
			int quote = 0;
			for (;p<max;p++)
				if (b[p]=='\\')
					p++;
				else if (quote==0 && b[p]=='{')
					rv++;
				else if (quote==0 && b[p]=='}') 
				{
					if (--rv ==0) 
					{
						p++;
						break;
					}
				}
				else if (b[p]==quote)
					quote=0;
				else if (b[p]=='\'' || b[p]=='"')
					quote = b[p];
			return rv;
		}
		protected string ToBraceIfFound(ref string buf,ref int p,ref int max,CsReader inf)
		{
			int q = p;
			int brack = Braces(0,buf,ref p,max);
			string rv = buf.Substring(q,p-q);
			while (inf!=null && brack>0) 
			{
				buf=inf.ReadLine();
				max=buf.Length;
				if (max==0)
					Error(47,q,"EOF in action or class def??");
				p=0; 
				rv += '\n';
				brack = Braces(brack,buf,ref p,max);
				rv += buf.Substring(0,p);
			}
			return rv;
		}
		// convenience functions
		public bool White(string buf, ref int offset,int max) 
		{
			while (offset<max && 
				(buf[offset]==' '||buf[offset]=='\t'))
				offset++;
			return offset<max; // false if nothing left
		} 
		public bool NonWhite(string buf, ref int offset,int max) 
		{
			while (offset<max && 
				(buf[offset]!=' ' && buf[offset]!='\t'))
				offset++;
			return offset<max; // false if nothing left
		}
		public int EmitClassDefin(string b,ref int p,int max,CsReader inf,string defbas,out string bas, out string name,bool lx) 
		{
			name = ""; 
			bas = defbas;
			NonWhite(b,ref p,max);
			White(b,ref p,max);
			for(;p<max&&b[p]!=':'&&b[p]!=';';p++)
				name += b[p];
			if (b[p]==':') 
				for(p++,bas="";p<max&&b[p]!=';';p++)
					bas += b[p];
			if (b[p]!=';')
				Error(48,p,"Bad script");
			int num = new TokClassDef(this,name,bas).m_yynum;
			m_outFile.WriteLine("//%+{0}+{1}",name,num);
			m_outFile.Write("public class ");
			m_outFile.Write(name);
			m_outFile.Write(" : "+bas);
			m_outFile.WriteLine("{");
			m_outFile.WriteLine("public override string yyname { get { return \""+name+"\"; }}");
			m_outFile.WriteLine("public override int yynum { get { return "+num+"; }}");
			if (lx) 
				m_outFile.WriteLine("public "+name+"(Lexer yym):base(yym){ }}");
			else 
				m_outFile.WriteLine("public "+name+"(Parser yyq):base(yyq){ }}");
			return num;
		}
		public void Error(int n, int p, string str) 
		{
			if (m_outFile!=null) 
			{
				m_outFile.WriteLine();
				m_outFile.WriteLine("#error Generator failed earlier. Fix the parser script and run ParserGenerator again.");
			}
			erh.Error(new CSToolsException(n,sourceLineInfo(p),"",str));
		}
		public virtual SourceLineInfo sourceLineInfo(int pos) { return new SourceLineInfo(pos); }
		public int line(int pos) { return sourceLineInfo(pos).lineNumber; }
		public int position(int pos) { return sourceLineInfo(pos).rawCharPosition; }
		public string Saypos(int pos) { return sourceLineInfo(pos).ToString(); }
		public Production m_prod = null; // current production being parsed
		public int LastSymbol = 2;
	}
}
