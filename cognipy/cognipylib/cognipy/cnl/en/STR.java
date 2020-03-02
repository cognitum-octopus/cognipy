package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+STR+3
public class STR extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "STR";
	}
	@Override
	public int getYynum()
	{
		return 3;
	}
	public STR(Lexer yyl)
	{
		super(yyl);
	}
}