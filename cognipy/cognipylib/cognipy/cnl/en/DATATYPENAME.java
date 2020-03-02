package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%DATATYPENAME+63
public class DATATYPENAME extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "DATATYPENAME";
	}
	@Override
	public int getYynum()
	{
		return 63;
	}
	public DATATYPENAME(Lexer yyl)
	{
		super(yyl);
	}
}