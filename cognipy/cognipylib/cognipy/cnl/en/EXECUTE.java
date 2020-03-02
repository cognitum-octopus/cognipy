package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%EXECUTE+66
public class EXECUTE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "EXECUTE";
	}
	@Override
	public int getYynum()
	{
		return 66;
	}
	public EXECUTE(Lexer yyl)
	{
		super(yyl);
	}
}