package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%ISNOTTHESAMEAS+33
public class ISNOTTHESAMEAS extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "ISNOTTHESAMEAS";
	}
	@Override
	public int getYynum()
	{
		return 33;
	}
	public ISNOTTHESAMEAS(Lexer yyl)
	{
		super(yyl);
	}
}