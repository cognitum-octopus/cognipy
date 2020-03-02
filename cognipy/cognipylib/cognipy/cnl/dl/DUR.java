package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%DUR+61
public class DUR extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "DUR";
	}
	@Override
	public int getYynum()
	{
		return 61;
	}
	public DUR(Lexer yyl)
	{
		super(yyl);
	}
}