package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%DONOT+28
public class DONOT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "DONOT";
	}
	@Override
	public int getYynum()
	{
		return 28;
	}
	public DONOT(Lexer yyl)
	{
		super(yyl);
	}
}