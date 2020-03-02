package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class modality2_65c4f542ddbd4e18bcb08dfa7f89efc6_17d398d661fb4d528f09bb6bbab1450f extends modality2_65c4f542ddbd4e18bcb08dfa7f89efc6
{
	public modality2_65c4f542ddbd4e18bcb08dfa7f89efc6_17d398d661fb4d528f09bb6bbab1450f(Parser yyq)
	{
		super(yyq);
		switch (((MODAL)(yyq.StackAt(1).m_value)).getYytext())
		{
			case "must":
				setYytext("□");
				break;
			case "should":
				setYytext("◊");
				break;
			case "can":
				setYytext("◊◊");
				break;
			case "must-not":
				setYytext("~◊◊");
				break;
			case "should-not":
				setYytext("~◊");
				break;
			case "can-not":
				setYytext("~□");
				break;
			default:
				throw new IllegalStateException();
		}
	}
}