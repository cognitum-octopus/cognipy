package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%ISUNIQUEIF+21
public class ISUNIQUEIF extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "ISUNIQUEIF";
	}
	@Override
	public int getYynum()
	{
		return 21;
	}
	public ISUNIQUEIF(Lexer yyl)
	{
		super(yyl);
	}
}