package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%CMP+78
public class CMP extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "CMP";
	}
	@Override
	public int getYynum()
	{
		return 78;
	}
	public CMP(Lexer yyl)
	{
		super(yyl);
	}
}