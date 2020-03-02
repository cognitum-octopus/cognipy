package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%ALPHA+89
public class ALPHA extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "ALPHA";
	}
	@Override
	public int getYynum()
	{
		return 89;
	}
	public ALPHA(Lexer yyl)
	{
		super(yyl);
	}
}