package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%EQV+20
public class EQV extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "EQV";
	}
	@Override
	public int getYynum()
	{
		return 20;
	}
	public EQV(Lexer yyl)
	{
		super(yyl);
	}
}