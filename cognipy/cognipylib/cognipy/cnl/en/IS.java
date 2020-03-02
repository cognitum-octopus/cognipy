package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%IS+46
public class IS extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "IS";
	}
	@Override
	public int getYynum()
	{
		return 46;
	}
	public IS(Lexer yyl)
	{
		super(yyl);
	}
}