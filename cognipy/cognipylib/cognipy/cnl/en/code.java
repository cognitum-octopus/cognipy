package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+CODE+9
public class CODE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "CODE";
	}
	@Override
	public int getYynum()
	{
		return 9;
	}
	public CODE(Lexer yyl)
	{
		super(yyl);
	}
}