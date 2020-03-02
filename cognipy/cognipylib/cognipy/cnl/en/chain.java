package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+chain+139
public class chain implements iaccept, PartialSymbol
{
	public chain(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<role> roles;

	public chain(Parser yyp, role s)
	{
		super(yyp);
	roles = new ArrayList<role>();
	roles.add(s);
	}

	public chain(Parser yyp, role s, role r)
	{
		super(yyp);
	roles = new ArrayList<role>();
	roles.add(s);
	roles.add(r);
	}
	public chain(Parser yyp, chain z, role r)
	{
		super(yyp);
	roles = z.roles;
	roles.add(r);
	}
	public Object accept(IVisitor v)
	{
		return null;
	}


	@Override
	public String getYynameEndl()
	{
		return "chain";
	}
	@Override
	public int getYynumEndl()
	{
		return 139;
	}
}