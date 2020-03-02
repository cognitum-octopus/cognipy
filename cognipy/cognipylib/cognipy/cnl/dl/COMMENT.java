package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+COMMENT+10
public class COMMENT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "COMMENT";
	}
	@Override
	public int getYynum()
	{
		return 10;
	}
	public COMMENT(Lexer yyl)
	{
		super(yyl);
	}
}