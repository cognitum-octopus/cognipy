package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%NO+38
public class NO extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "NO";
	}
	@Override
	public int getYynum()
	{
		return 38;
	}
	public NO(Lexer yyl)
	{
		super(yyl);
	}
}