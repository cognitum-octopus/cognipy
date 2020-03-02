package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+NAT+5
public class NAT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "NAT";
	}
	@Override
	public int getYynum()
	{
		return 5;
	}
	public NAT(Lexer yyl)
	{
		super(yyl);
	}
}