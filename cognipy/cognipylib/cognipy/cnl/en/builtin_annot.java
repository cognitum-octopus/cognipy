package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+builtin_annot+245
public class builtin_annot extends builtin
{
	public objectr a;
	public datavaler prop;
	public datavaler lang;
	public datavaler b;

	public builtin_annot(Parser yyp)
	{
		super(yyp);
	}
	public builtin_annot(Parser yyp, objectr a, datavaler prop, datavaler lang, datavaler b)
	{
		super(yyp);
		this.a = a;
		this.b = b;
		this.prop = prop;
		this.lang = lang;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "builtin_annot";
	}
	@Override
	public int getYynumEndl()
	{
		return 245;
	}
}