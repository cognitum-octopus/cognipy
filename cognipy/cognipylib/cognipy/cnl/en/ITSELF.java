package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%ITSELF+54
public class ITSELF extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "ITSELF";
	}
	@Override
	public int getYynum()
	{
		return 54;
	}
	public ITSELF(Lexer yyl)
	{
		super(yyl);
	}
}