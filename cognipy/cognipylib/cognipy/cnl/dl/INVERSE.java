package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%INVERSE+57
public class INVERSE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "INVERSE";
	}
	@Override
	public int getYynum()
	{
		return 57;
	}
	public INVERSE(Lexer yyl)
	{
		super(yyl);
	}
}