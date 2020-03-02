package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class modality_2994985d0cdc4d74bde814570c298b31_51a1e4967eb4403ba2edffb6c4127ebd extends modality_2994985d0cdc4d74bde814570c298b31
{
	public modality_2994985d0cdc4d74bde814570c298b31_51a1e4967eb4403ba2edffb6c4127ebd(Parser yyq)
	{
		super(yyq);
		switch (((MODAL)(yyq.StackAt(0).m_value)).getYytext())
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