package cognipy.cnl;

import tools.*;
import cognipy.*;
import java.util.*;
import java.nio.file.*;

public interface ICNLFactory
{
	tools.Lexer getLexer();

	tools.Parser getParser();

	boolean isEOL(TOKEN tok);

	boolean IsAnnot(TOKEN tok);

	boolean isParagraph(SYMBOL smb);

	void setPfx2NsSource(tangible.Func1Param<String, String> pfx2Ns);


	DL.Paragraph InvConvert(SYMBOL smb, boolean useFullUri);
	DL.Paragraph InvConvert(SYMBOL smb);
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: DL.Paragraph InvConvert(SYMBOL smb, bool useFullUri = false, Func<string, string> pfx2ns = null);
	cognipy.cnl.dl.Paragraph InvConvert(SYMBOL smb, boolean useFullUri, tangible.Func1Param<String, String> pfx2ns);


	Object Convert(DL.Statement stmast, boolean usePrefixes);
	Object Convert(DL.Statement stmast);
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: object Convert(DL.Statement stmast, bool usePrefixes = false, Func<string, string> ns2pfx = null);
	Object Convert(cognipy.cnl.dl.Statement stmast, boolean usePrefixes, tangible.Func1Param<String, String> ns2pfx);


	Object Convert(DL.IAccept nodeast, boolean usePrefixes);
	Object Convert(DL.IAccept nodeast);
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: object Convert(DL.IAccept nodeast, bool usePrefixes = false, Func<string, string> ns2pfx = null);
	Object Convert(cognipy.cnl.dl.IAccept nodeast, boolean usePrefixes, tangible.Func1Param<String, String> ns2pfx);


	Object Convert(DL.Paragraph para, boolean usePrefixes);
	Object Convert(DL.Paragraph para);
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: object Convert(DL.Paragraph para, bool usePrefixes = false, Func<string, string> ns2pfx = null);
	Object Convert(cognipy.cnl.dl.Paragraph para, boolean usePrefixes, tangible.Func1Param<String, String> ns2pfx);


	String Serialize(Object enast, boolean serializeAnnotations, tangible.OutObject<AnnotationManager> annotMan);
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: string Serialize(object enast, bool serializeAnnotations, out AnnotationManager annotMan, bool templateMode = false);
	String Serialize(Object enast, boolean serializeAnnotations, tangible.OutObject<AnnotationManager> annotMan, boolean templateMode);

	java.lang.Iterable<String> Morphology(java.lang.Iterable<String> col, String str, String form, boolean bigName);

	String GetEOLTag();

	String[] GetAllKeywords();

	boolean IsKeyword(String kw);

	boolean KeywordTagExists(String kw);

	void FindMark(SYMBOL smb, String mark, tangible.OutObject<String> kind, tangible.OutObject<String> form);

	String GetDefaultTagValue(String prop);

	boolean TagIsName(String prop);

	boolean TagIsDatatype(String prop);

	String[] GetTagSuffixes();

	boolean TagIsInstanceName(String prop);

	String GetSymbol(String prop);

	String GetKeyword(String prop);

	HashSet<String> GetAllMatchingKeywords(String kw);

	String GetKeywordTag(String wrd);

	String GetTooltipDesc(Map.Entry<String, String> kv);

	String GetKeywordTip(String kwtag);

	boolean LoadSmallestSentenceCache(HashMap<String, String> cache);

	void SaveSmallestSentenceCache(HashMap<String, String> cache);

	boolean ValidateSafeness(Object ast);
}