package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+rolesubsumption+112
public class rolesubsumption extends sentence
{
	public rolesubsumption(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<role> subChain;
	public roleWithXY superRole;


	public rolesubsumption(Parser yyp, role z_, roleWithXY s_)
	{
		super(yyp);
		subChain = new ArrayList<role>();
		subChain.add(z_);
		superRole = s_;
	}
	public rolesubsumption(Parser yyp, chain z_, roleWithXY s_)
	{
		super(yyp);
		subChain = z_.roles;
		superRole = s_;
	}

	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "rolesubsumption";
	}
	@Override
	public int getYynumEndl()
	{
		return 112;
	}
}