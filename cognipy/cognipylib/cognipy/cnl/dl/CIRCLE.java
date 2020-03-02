package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%CIRCLE+28
public class CIRCLE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "CIRCLE";
	}
	@Override
	public int getYynum()
	{
		return 28;
	}
	public CIRCLE(Lexer yyl)
	{
		super(yyl);
	}
}