package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class boundTotal_aece926e36334b22b0ccccd4a9967b6f extends boundTotal
{
	public boundTotal_aece926e36334b22b0ccccd4a9967b6f(Parser yyq)
	{
		super(yyq, ((DATATYPENAME)(yyq.StackAt(2).m_value)).getYytext().equals("integer") ? "NUM" : (((DATATYPENAME)(yyq.StackAt(2).m_value)).getYytext().equals("string") ? "STR" : (((DATATYPENAME)(yyq.StackAt(2).m_value)).getYytext().equals("real") ? "DBL" : (((DATATYPENAME)(yyq.StackAt(2).m_value)).getYytext().equals("duration") ? "DUR" : (((DATATYPENAME)(yyq.StackAt(2).m_value)).getYytext().equals("datetime") ? "DTM" : "BOL")))));
	}
}