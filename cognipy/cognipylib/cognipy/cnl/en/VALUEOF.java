package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%VALUEOF+62
public class VALUEOF extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "VALUEOF";
	}
	@Override
	public int getYynum()
	{
		return 62;
	}
	public VALUEOF(Lexer yyl)
	{
		super(yyl);
	}
}