package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%AND+49
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
		return 49;
	}
	public AND(Lexer yyl)
	{
		super(yyl);
	}
}