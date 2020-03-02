package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+ConceptNot+120
public class ConceptNot extends Expression
{
	public ConceptNot(Parser yyp)
	{
		super(yyp);
	}
	public Node C;
	public ConceptNot(Parser yyp, Node c)
	{
		super(yyp);
	C = c.me();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	@Override
	public int priority()
	{
		return 5;
	}


	@Override
	public String getYynameDl()
	{
		return "ConceptNot";
	}
	@Override
	public int getYynumDl()
	{
		return 120;
	}
}