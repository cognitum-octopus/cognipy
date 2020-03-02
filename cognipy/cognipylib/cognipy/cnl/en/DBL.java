package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+DBL+7
public class DBL extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "DBL";
	}
	@Override
	public int getYynum()
	{
		return 7;
	}
	public DBL(Lexer yyl)
	{
		super(yyl);
	}
}