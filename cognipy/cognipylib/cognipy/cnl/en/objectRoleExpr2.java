package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+objectRoleExpr2+129
public class objectRoleExpr2 extends objectRoleExpr
{
	public objectRoleExpr2(Parser yyp)
	{
		super(yyp);
	}
	public boolean Negated = false;
	public oobject s;
	public role r;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public objectRoleExpr2(Parser yyp, boolean Negated_, oobject s_, role r_)
	{
		super(yyp);
		Negated = Negated_;
		s = s_;
		r = r_;
	}

	public objectRoleExpr2(Parser yyp, boolean Negated_, oobject s_, String rn)
	{
		this(yyp, Negated_, s_, rn, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public objectRoleExpr2(Parser yyp, bool Negated_, oobject s_, string rn, bool Inversed_ = false)
	public objectRoleExpr2(Parser yyp, boolean Negated_, oobject s_, String rn, boolean Inversed_)
	{
		super(yyp);
		Negated = Negated_;
		s = s_;
		r = new role(yyp, rn, Inversed_);
	}


	@Override
	public String getYynameEndl()
	{
		return "objectRoleExpr2";
	}
	@Override
	public int getYynumEndl()
	{
		return 129;
	}
}