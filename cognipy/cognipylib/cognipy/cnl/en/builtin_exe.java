package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+builtin_exe+246
public class builtin_exe extends builtin
{
	public String name;
	public datavaler a;
	public exeargs ea;

	public builtin_exe(Parser yyp)
	{
		super(yyp);
	}
	public builtin_exe(Parser yyp, String name, exeargs ea, datavaler a)
	{
		super(yyp);
		this.name = name;
		this.a = a;
		this.ea = ea;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "builtin_exe";
	}
	@Override
	public int getYynumEndl()
	{
		return 246;
	}
}