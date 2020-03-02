package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%AN+47
public class AN extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "AN";
	}
	@Override
	public int getYynum()
	{
		return 47;
	}
	public AN(Lexer yyl)
	{
		super(yyl);
	}
}