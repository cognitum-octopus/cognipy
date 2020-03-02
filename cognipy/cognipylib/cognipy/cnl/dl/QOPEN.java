package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%QOPEN+16
public class QOPEN extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "QOPEN";
	}
	@Override
	public int getYynum()
	{
		return 16;
	}
	public QOPEN(Lexer yyl)
	{
		super(yyl);
	}
}