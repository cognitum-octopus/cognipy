package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%EQ+34
public class EQ extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "EQ";
	}
	@Override
	public int getYynum()
	{
		return 34;
	}
	public EQ(Lexer yyl)
	{
		super(yyl);
	}
}