package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+STR+3
//%+ID+4
//%+NAT+5
//%+NUM+6
//%+DBL+7
//%+BOL+8
//%+CODE+9
//%+COMMENT+10
//%OPEN+11
public class OPEN extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "OPEN";
	}
	@Override
	public int getYynum()
	{
		return 11;
	}
	public OPEN(Lexer yyl)
	{
		super(yyl);
	}
}