package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%QCLOSE+17
public class QCLOSE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "QCLOSE";
	}
	@Override
	public int getYynum()
	{
		return 17;
	}
	public QCLOSE(Lexer yyl)
	{
		super(yyl);
	}
}