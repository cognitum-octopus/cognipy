package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%ITERTHEN+50
public class ITERTHEN extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "ITERTHEN";
	}
	@Override
	public int getYynum()
	{
		return 50;
	}
	public ITERTHEN(Lexer yyl)
	{
		super(yyl);
	}
}