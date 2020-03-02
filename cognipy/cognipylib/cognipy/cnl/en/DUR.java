package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%DUR+96
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
		return 96;
	}
	public DUR(Lexer yyl)
	{
		super(yyl);
	}
}