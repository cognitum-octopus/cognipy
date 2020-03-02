package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%BY+53
public class BY extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "BY";
	}
	@Override
	public int getYynum()
	{
		return 53;
	}
	public BY(Lexer yyl)
	{
		super(yyl);
	}
}