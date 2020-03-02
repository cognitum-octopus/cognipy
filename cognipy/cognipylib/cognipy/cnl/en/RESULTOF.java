package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%RESULTOF+91
public class RESULTOF extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "RESULTOF";
	}
	@Override
	public int getYynum()
	{
		return 91;
	}
	public RESULTOF(Lexer yyl)
	{
		super(yyl);
	}
}