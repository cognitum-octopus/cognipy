package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%TIME+65
public class TIME extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "TIME";
	}
	@Override
	public int getYynum()
	{
		return 65;
	}
	public TIME(Lexer yyl)
	{
		super(yyl);
	}
}