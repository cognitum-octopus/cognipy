package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+Paragraph+63
public class Paragraph extends PartialSymbol implements IAccept
{
	public Paragraph(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<Statement> Statements;
	public Paragraph(Parser yyp, Statement S)
	{
		super(yyp);
	Statements = new ArrayList<Statement>();
	Statements.add(S);
	}
	public Paragraph(Parser yyp, Paragraph tu, Statement S)
	{
		super(yyp);
	Statements = tu.Statements;
	Statements.add(S);
	}
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "Paragraph";
	}
	@Override
	public int getYynumDl()
	{
		return 63;
	}
}