package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SUBSTRING+70
public class SUBSTRING extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SUBSTRING";
	}
	@Override
	public int getYynum()
	{
		return 70;
	}
	public SUBSTRING(Lexer yyl)
	{
		super(yyl);
	}
}