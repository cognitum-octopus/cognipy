package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class NumberValueRestriction_204ccdc951d04d68bd4c5ebfb3407ecd extends NumberValueRestriction
{
	public NumberValueRestriction_204ccdc951d04d68bd4c5ebfb3407ecd(Parser yyq)
	{
		super(yyq, "≤", ((Node)(yyq.StackAt(1).m_value)), ((NAT)(yyq.StackAt(2).m_value)).getYytext(), ((AbstractBound)(yyq.StackAt(0).m_value)));
	}
}