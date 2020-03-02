package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%BENOT+30
public class BENOT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "BENOT";
	}
	@Override
	public int getYynum()
	{
		return 30;
	}
	public BENOT(Lexer yyl)
	{
		super(yyl);
	}
}