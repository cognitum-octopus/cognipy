package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%GT+39
public class GT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "GT";
	}
	@Override
	public int getYynum()
	{
		return 39;
	}
	public GT(Lexer yyl)
	{
		super(yyl);
	}
}