package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class facet_522c79e37a364067a3af29e0c191bfac extends facet
{
	public facet_522c79e37a364067a3af29e0c191bfac(Parser yyq)
	{
		super(yyq, (((COMPARER2)(yyq.StackAt(1).m_value)).getYytext().equals("lower-or-equal-to") ? "≤" : (((COMPARER2)(yyq.StackAt(1).m_value)).getYytext().equals("greater-or-equal-to") ? "≥" : (((COMPARER2)(yyq.StackAt(1).m_value)).getYytext().equals("lower-than") ? "<" : ">"))), ((dataval)(yyq.StackAt(0).m_value)));
	}
}