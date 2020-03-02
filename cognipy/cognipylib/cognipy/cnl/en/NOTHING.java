package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%NOTHING+40
public class NOTHING extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "NOTHING";
	}
	@Override
	public int getYynum()
	{
		return 40;
	}
	public NOTHING(Lexer yyl)
	{
		super(yyl);
	}
}