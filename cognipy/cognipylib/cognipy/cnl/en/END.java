package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%END+101
public class END extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "END";
	}
	@Override
	public int getYynum()
	{
		return 101;
	}
	public END(Lexer yyl)
	{
		super(yyl);
	}
}