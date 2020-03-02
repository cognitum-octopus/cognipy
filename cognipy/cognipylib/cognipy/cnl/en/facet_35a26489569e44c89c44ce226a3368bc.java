package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class facet_35a26489569e44c89c44ce226a3368bc extends facet
{
	public facet_35a26489569e44c89c44ce226a3368bc(Parser yyq)
	{
		super(yyq, "<-> " + (((COMPARER2)(yyq.StackAt(1).m_value)).getYytext().equals("lower-or-equal-to") ? "≤" : (((COMPARER2)(yyq.StackAt(1).m_value)).getYytext().equals("greater-or-equal-to") ? "≥" : (((COMPARER2)(yyq.StackAt(1).m_value)).getYytext().equals("lower-than") ? "<" : ">"))), ((NAT)(yyq.StackAt(0).m_value)).getYytext());
	}
}