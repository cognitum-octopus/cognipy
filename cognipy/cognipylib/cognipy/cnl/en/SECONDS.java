package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SECONDS+88
public class SECONDS extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SECONDS";
	}
	@Override
	public int getYynum()
	{
		return 88;
	}
	public SECONDS(Lexer yyl)
	{
		super(yyl);
	}
}