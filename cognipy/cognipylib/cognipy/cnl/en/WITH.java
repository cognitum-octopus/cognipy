package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%WITH+72
public class WITH extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "WITH";
	}
	@Override
	public int getYynum()
	{
		return 72;
	}
	public WITH(Lexer yyl)
	{
		super(yyl);
	}
}