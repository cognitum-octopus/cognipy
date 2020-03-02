package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%ISTHESAMEAS+32
public class ISTHESAMEAS extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "ISTHESAMEAS";
	}
	@Override
	public int getYynum()
	{
		return 32;
	}
	public ISTHESAMEAS(Lexer yyl)
	{
		super(yyl);
	}
}