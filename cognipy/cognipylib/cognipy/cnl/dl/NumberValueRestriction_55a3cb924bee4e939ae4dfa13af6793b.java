package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class NumberValueRestriction_55a3cb924bee4e939ae4dfa13af6793b extends NumberValueRestriction
{
	public NumberValueRestriction_55a3cb924bee4e939ae4dfa13af6793b(Parser yyq)
	{
		super(yyq, ">", ((Node)(yyq.StackAt(1).m_value)), ((NAT)(yyq.StackAt(2).m_value)).getYytext(), ((AbstractBound)(yyq.StackAt(0).m_value)));
	}
}