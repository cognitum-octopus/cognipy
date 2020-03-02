package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%MODAL+55
public class MODAL extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "MODAL";
	}
	@Override
	public int getYynum()
	{
		return 55;
	}
	public MODAL(Lexer yyl)
	{
		super(yyl);
	}
}