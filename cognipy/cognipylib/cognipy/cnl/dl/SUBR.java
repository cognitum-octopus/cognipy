package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SUBR+22
public class SUBR extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SUBR";
	}
	@Override
	public int getYynum()
	{
		return 22;
	}
	public SUBR(Lexer yyl)
	{
		super(yyl);
	}
}