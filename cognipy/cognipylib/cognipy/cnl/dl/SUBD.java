package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SUBD+25
public class SUBD extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SUBD";
	}
	@Override
	public int getYynum()
	{
		return 25;
	}
	public SUBD(Lexer yyl)
	{
		super(yyl);
	}
}