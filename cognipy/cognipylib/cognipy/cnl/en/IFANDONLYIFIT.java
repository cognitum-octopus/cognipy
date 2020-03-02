package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%IFANDONLYIFIT+23
public class IFANDONLYIFIT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "IFANDONLYIFIT";
	}
	@Override
	public int getYynum()
	{
		return 23;
	}
	public IFANDONLYIFIT(Lexer yyl)
	{
		super(yyl);
	}
}