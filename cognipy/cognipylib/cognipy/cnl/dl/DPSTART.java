package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%DPSTART+53
public class DPSTART extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "DPSTART";
	}
	@Override
	public int getYynum()
	{
		return 53;
	}
	public DPSTART(Lexer yyl)
	{
		super(yyl);
	}
}