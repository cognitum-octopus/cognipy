package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%AND+29
public class AND extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "AND";
	}
	@Override
	public int getYynum()
	{
		return 29;
	}
	public AND(Lexer yyl)
	{
		super(yyl);
	}
}