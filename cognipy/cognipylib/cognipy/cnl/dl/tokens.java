package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class tokens extends Lexer
{
	public tokens()
	{
		super(new yytokens(new ErrorHandler(false)));
	}
	public tokens(ErrorHandler eh)
	{
		super(new yytokens(eh));
	}
	public tokens(YyLexer tks)
	{
		super(tks);
	}

	public String str;

}