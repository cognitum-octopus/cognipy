package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%QUOTATION+56
public class QUOTATION extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "QUOTATION";
	}
	@Override
	public int getYynum()
	{
		return 56;
	}
	public QUOTATION(Lexer yyl)
	{
		super(yyl);
	}
}