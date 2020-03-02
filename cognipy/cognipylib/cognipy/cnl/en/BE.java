package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%BE+56
public class BE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "BE";
	}
	@Override
	public int getYynum()
	{
		return 56;
	}
	public BE(Lexer yyl)
	{
		super(yyl);
	}
}