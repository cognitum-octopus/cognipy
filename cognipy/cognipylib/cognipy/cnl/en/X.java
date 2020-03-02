package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%X+57
public class X extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "X";
	}
	@Override
	public int getYynum()
	{
		return 57;
	}
	public X(Lexer yyl)
	{
		super(yyl);
	}
}