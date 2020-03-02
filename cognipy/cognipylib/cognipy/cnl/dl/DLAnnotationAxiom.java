package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+DLAnnotationAxiom+69
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Annotation)] public partial class DLAnnotationAxiom: Statement
public class DLAnnotationAxiom extends Statement
{
	public DLAnnotationAxiom(Parser yyp)
	{
		super(yyp);
	}
	public DLAnnotationAxiom(Parser yyp, String subject, String subjectKind, String annotName, String language, String value)
	{
		super(yyp);
		this.setSubject(subject);
		this.annotName = annotName;
		this.language = language;
		this.value = value;
		this.setSubjKind(subjectKind);
	}
	public DLAnnotationAxiom(Parser yyp, String subject, String subjectKind, String annotName, String value)
	{
		super(yyp);
		this.setSubject(subject);
		this.annotName = annotName;
		this.language = "";
		this.value = value;
		this.setSubjKind(subjectKind);
	}

	private String _subject;
	public final String getSubject()
	{
		if (_subjKind == cognipy.ars.EntityKind.Statement && _subject.startsWith("\"") && _subject.endsWith("\"") && !tangible.StringHelper.isNullOrWhiteSpace(_subject))
		{
			return _subject.substring(1, 1 + _subject.length() - 2);
		}
		else
		{
			return _subject;
		}
	}
	public final void setSubject(String value)
	{
		_subject = value;
	}

	private cognipy.ars.EntityKind _subjKind = cognipy.ars.EntityKind.values()[0];
	public final String getSubjKind()
	{
		return _subjKind.toString();
	}
	public final void setSubjKind(String value)
	{
		_subjKind = cognipy.cnl.AnnotationManager.ParseSubjectKind(value);
	}

	public String annotName;
	public String value;
	public String language;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "DLAnnotationAxiom";
	}
	@Override
	public int getYynumDl()
	{
		return 69;
	}
}