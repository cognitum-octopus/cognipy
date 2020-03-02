package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+ConceptList+117
public class ConceptList extends Expression
{
	public ConceptList(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<Node> Exprs = null;


	@Override
	public String getYynameDl()
	{
		return "ConceptList";
	}
	@Override
	public int getYynumDl()
	{
		return 117;
	}
}