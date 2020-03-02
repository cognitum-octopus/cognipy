package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%EVERYTHING+39
public class EVERYTHING extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "EVERYTHING";
	}
	@Override
	public int getYynum()
	{
		return 39;
	}
	public EVERYTHING(Lexer yyl)
	{
		super(yyl);
	}
}