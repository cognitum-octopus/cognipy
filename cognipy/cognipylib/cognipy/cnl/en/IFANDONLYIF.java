package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%IFANDONLYIF+22
public class IFANDONLYIF extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "IFANDONLYIF";
	}
	@Override
	public int getYynum()
	{
		return 22;
	}
	public IFANDONLYIF(Lexer yyl)
	{
		super(yyl);
	}
}