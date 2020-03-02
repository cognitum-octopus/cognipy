package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%EXETHEN+49
public class EXETHEN extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "EXETHEN";
	}
	@Override
	public int getYynum()
	{
		return 49;
	}
	public EXETHEN(Lexer yyl)
	{
		super(yyl);
	}
}