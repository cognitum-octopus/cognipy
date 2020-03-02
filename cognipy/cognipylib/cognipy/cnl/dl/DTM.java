package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%DTM+60
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
		return 60;
	}
	public DTM(Lexer yyl)
	{
		super(yyl);
	}
}