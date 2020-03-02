package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%OR+50
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
		return 50;
	}
	public OR(Lexer yyl)
	{
		super(yyl);
	}
}