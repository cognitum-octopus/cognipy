package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%FROM+71
public class FROM extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "FROM";
	}
	@Override
	public int getYynum()
	{
		return 71;
	}
	public FROM(Lexer yyl)
	{
		super(yyl);
	}
}