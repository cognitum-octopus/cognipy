package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%NAME+97
public class NAME extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "NAME";
	}
	@Override
	public int getYynum()
	{
		return 97;
	}
	public NAME(Lexer yyl)
	{
		super(yyl);
	}
}