package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%EITHER+12
public class EITHER extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "EITHER";
	}
	@Override
	public int getYynum()
	{
		return 12;
	}
	public EITHER(Lexer yyl)
	{
		super(yyl);
	}
}