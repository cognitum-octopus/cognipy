package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class comparer_5ecbaad0e39b422491fc9c98728523fc_ba645194d6924b0087226328f3ed2351 extends comparer_5ecbaad0e39b422491fc9c98728523fc
{
	public comparer_5ecbaad0e39b422491fc9c98728523fc_ba645194d6924b0087226328f3ed2351(Parser yyq)
	{
		super(yyq);
		switch (((COMPARER)(yyq.StackAt(0).m_value)).getYytext())
		{
			case "at-most":
				setYytext("≤");
				break;
			case "at-least":
				setYytext("≥");
				break;
			case "less-than":
				setYytext("<");
				break;
			case "more-than":
				setYytext(">");
				break;
			case "different-than":
				setYytext("≠");
				break;
			default:
				throw new IllegalStateException();
		}
	}
}