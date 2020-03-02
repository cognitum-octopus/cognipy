package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%Y+58
public class Y extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "Y";
	}
	@Override
	public int getYynum()
	{
		return 58;
	}
	public Y(Lexer yyl)
	{
		super(yyl);
	}
}