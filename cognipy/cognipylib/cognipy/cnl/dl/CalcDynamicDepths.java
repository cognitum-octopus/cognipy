package cognipy.cnl.dl;

import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

public class CalcDynamicDepths extends GenericVisitor
{
	public HashSet<Integer> IntersectionDepth = new HashSet<Integer>();
	public HashSet<Integer> UnionDepth = new HashSet<Integer>();
	public HashSet<Integer> HasKeyDepth = new HashSet<Integer>();

	@Override
	public Object Visit(ConceptAnd e)
	{
		IntersectionDepth.add(e.Exprs.size());
		return super.Visit(e);
	}

	@Override
	public Object Visit(ConceptOr e)
	{
		UnionDepth.add(e.Exprs.size());
		return super.Visit(e);
	}
	@Override
	public Object Visit(HasKey e)
	{
		UnionDepth.add(e.DataRoles.size() + e.Roles.size());
		return super.Visit(e);
	}
}