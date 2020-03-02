package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+NUM+6
public class NUM extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "NUM";
	}
	@Override
	public int getYynum()
	{
		return 6;
	}
	public NUM(Lexer yyl)
	{
		super(yyl);
	}
}