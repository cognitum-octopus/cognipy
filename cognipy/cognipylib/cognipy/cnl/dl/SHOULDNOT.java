package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SHOULDNOT+46
public class SHOULDNOT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SHOULDNOT";
	}
	@Override
	public int getYynum()
	{
		return 46;
	}
	public SHOULDNOT(Lexer yyl)
	{
		super(yyl);
	}
}