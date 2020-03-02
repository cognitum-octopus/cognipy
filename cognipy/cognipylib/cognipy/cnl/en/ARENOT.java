package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%ARENOT+31
public class ARENOT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "ARENOT";
	}
	@Override
	public int getYynum()
	{
		return 31;
	}
	public ARENOT(Lexer yyl)
	{
		super(yyl);
	}
}