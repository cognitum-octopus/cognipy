package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+paragraph+102
public class paragraph implements iaccept, PartialSymbol
{
	public paragraph(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<sentence> sentences;

	public paragraph(Parser yyp, sentence S)
	{
		super(yyp);
	sentences = new ArrayList<sentence>();
	sentences.add(S);
	}
	public paragraph(Parser yyp, paragraph tu, sentence S)
	{
		super(yyp);
	sentences = tu.sentences;
	sentences.add(S);
	}

	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "paragraph";
	}
	@Override
	public int getYynumEndl()
	{
		return 102;
	}
}