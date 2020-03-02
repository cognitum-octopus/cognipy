package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+ID+4
public class ID extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "ID";
	}
	@Override
	public int getYynum()
	{
		return 4;
	}
	public ID(Lexer yyl)
	{
		super(yyl);
	}
}