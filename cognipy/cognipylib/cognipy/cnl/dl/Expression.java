package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Expression+65
public class Expression extends Node
{
	public Expression(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameDl()
	{
		return "Expression";
	}
	@Override
	public int getYynumDl()
	{
		return 65;
	}
}