package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+CardinalRestriction+127
public class CardinalRestriction extends Restriction
{
	public CardinalRestriction(Parser yyp)
	{
		super(yyp);
	}
	public String N;
	public String Kind;


	@Override
	public String getYynameDl()
	{
		return "CardinalRestriction";
	}
	@Override
	public int getYynumDl()
	{
		return 127;
	}
}