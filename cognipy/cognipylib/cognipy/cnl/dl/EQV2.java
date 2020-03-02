package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%EQV2+21
public class EQV2 extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "EQV2";
	}
	@Override
	public int getYynum()
	{
		return 21;
	}
	public EQV2(Lexer yyl)
	{
		super(yyl);
	}
}