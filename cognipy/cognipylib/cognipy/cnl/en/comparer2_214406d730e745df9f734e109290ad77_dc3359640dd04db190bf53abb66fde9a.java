package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class comparer2_214406d730e745df9f734e109290ad77_dc3359640dd04db190bf53abb66fde9a extends comparer2_214406d730e745df9f734e109290ad77
{
	public comparer2_214406d730e745df9f734e109290ad77_dc3359640dd04db190bf53abb66fde9a(Parser yyq)
	{
		super(yyq);
		setYytext((((COMPARER2)(yyq.StackAt(0).m_value)).getYytext().equals("lower-or-equal-to") ? "≤" : (((COMPARER2)(yyq.StackAt(0).m_value)).getYytext().equals("greater-or-equal-to") ? "≥" : (((COMPARER2)(yyq.StackAt(0).m_value)).getYytext().equals("lower-than") ? "<" : ">"))));
	}
}