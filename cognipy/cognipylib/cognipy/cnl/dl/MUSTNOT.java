package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%MUSTNOT+45
public class MUSTNOT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "MUSTNOT";
	}
	@Override
	public int getYynum()
	{
		return 45;
	}
	public MUSTNOT(Lexer yyl)
	{
		super(yyl);
	}
}