package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%DIFFERENTFROM+15
public class DIFFERENTFROM extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "DIFFERENTFROM";
	}
	@Override
	public int getYynum()
	{
		return 15;
	}
	public DIFFERENTFROM(Lexer yyl)
	{
		super(yyl);
	}
}