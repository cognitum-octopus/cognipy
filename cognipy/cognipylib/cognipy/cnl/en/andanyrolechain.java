package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+andanyrolechain+141
public class andanyrolechain implements iaccept, PartialSymbol
{
	public andanyrolechain(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<role> chain;
	public ArrayList<role> datachain;

	public andanyrolechain(Parser yyp, role r, boolean isDataRole)
	{
		super(yyp);
	chain = new ArrayList<role>();
	datachain = new ArrayList<role>();
	if (isDataRole)
	{
		datachain.add(r);
	}
	else
	{
		chain.add(r);
	}
	}
	public andanyrolechain(Parser yyp, andanyrolechain z, role r, boolean isDataRole)
	{
		super(yyp);
	chain = z.chain;
	datachain = z.datachain;
	if (isDataRole)
	{
		datachain.add(r);
	}
	else
	{
		chain.add(r);
	}
	}
	public Object accept(IVisitor v)
	{
		return null;
	}


	@Override
	public String getYynameEndl()
	{
		return "andanyrolechain";
	}
	@Override
	public int getYynumEndl()
	{
		return 141;
	}
}