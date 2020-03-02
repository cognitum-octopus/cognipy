package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%EQ+77
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
		return 77;
	}
	public EQ(Lexer yyl)
	{
		super(yyl);
	}
}