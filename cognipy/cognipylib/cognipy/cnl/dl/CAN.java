package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%CAN+44
public class CAN extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "CAN";
	}
	@Override
	public int getYynum()
	{
		return 44;
	}
	public CAN(Lexer yyl)
	{
		super(yyl);
	}
}