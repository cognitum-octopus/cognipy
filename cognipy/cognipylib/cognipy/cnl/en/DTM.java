package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%DTM+95
public class DTM extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "DTM";
	}
	@Override
	public int getYynum()
	{
		return 95;
	}
	public DTM(Lexer yyl)
	{
		super(yyl);
	}
}