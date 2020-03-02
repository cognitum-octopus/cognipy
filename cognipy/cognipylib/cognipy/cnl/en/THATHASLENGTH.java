package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%THATHASLENGTH+17
public class THATHASLENGTH extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "THATHASLENGTH";
	}
	@Override
	public int getYynum()
	{
		return 17;
	}
	public THATHASLENGTH(Lexer yyl)
	{
		super(yyl);
	}
}