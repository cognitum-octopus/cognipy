package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class dlannotationassertion extends sentence
{
	public dlannotationassertion(Parser yyp)
	{
		super(yyp);
	}
	public dlannotationassertion(Parser yyp, String subject, String subjKind, W3CAnnotation w3cannot)
	{
		super(yyp);
	this.subject = subject;
	this.subjKind = subjKind;
	this.annotName = w3cannot.getType();
	this.value = (String)w3cannot.getValue();
	this.language = w3cannot.getLanguage();
	}
	public String subjKind;
	public String subject;
	public String annotName;
	public String value;
	public String language = null;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
}