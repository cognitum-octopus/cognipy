package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%ISNOT+29
public class ISNOT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "ISNOT";
	}
	@Override
	public int getYynum()
	{
		return 29;
	}
	public ISNOT(Lexer yyl)
	{
		super(yyl);
	}
}