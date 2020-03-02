package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Restriction+121
public class Restriction extends Expression
{
	public Restriction(Parser yyp)
	{
		super(yyp);
	}
	public Node R;
	@Override
	public int priority()
	{
		return 1;
	}


	@Override
	public String getYynameDl()
	{
		return "Restriction";
	}
	@Override
	public int getYynumDl()
	{
		return 121;
	}
}