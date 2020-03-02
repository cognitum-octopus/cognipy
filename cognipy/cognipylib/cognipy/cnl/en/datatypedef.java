package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+datatypedef+108
public class datatypedef extends sentence
{
	public datatypedef(Parser yyp)
	{
		super(yyp);
	}
	public String name;
	public abstractbound db;

	public datatypedef(Parser yyp, String name, abstractbound db)
	{
		super(yyp);
	this.db = db.me();
	this.name = name;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "datatypedef";
	}
	@Override
	public int getYynumEndl()
	{
		return 108;
	}
}