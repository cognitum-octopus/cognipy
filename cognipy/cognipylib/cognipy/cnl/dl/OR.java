package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%OR+30
public class OR extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "OR";
	}
	@Override
	public int getYynum()
	{
		return 30;
	}
	public OR(Lexer yyl)
	{
		super(yyl);
	}
}