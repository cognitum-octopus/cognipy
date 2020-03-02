package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%PAT+40
public class PAT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "PAT";
	}
	@Override
	public int getYynum()
	{
		return 40;
	}
	public PAT(Lexer yyl)
	{
		super(yyl);
	}
}