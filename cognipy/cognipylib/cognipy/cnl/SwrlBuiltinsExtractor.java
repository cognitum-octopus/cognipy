package cognipy.cnl;

import cognipy.cnl.dl.*;
import cognipy.*;
import java.util.*;

public class SwrlBuiltinsExtractor extends GenericVisitor
{
	public final Tuple<Paragraph, Paragraph> Split(Paragraph e)
	{
		Object tempVar = e.accept(this);
		return tempVar instanceof Tuple<Paragraph, Paragraph> ? (Tuple<Paragraph, Paragraph>)tempVar : null;
	}

	private boolean builtinFound = false;
	@Override
	public Object Visit(Paragraph e)
	{
		ArrayList<Statement> swrlBuiltinsStatements = new ArrayList<Statement>();
		ArrayList<Statement> normalStatements = new ArrayList<Statement>();
		for (Statement x : e.Statements)
		{
			builtinFound = false;
			x.accept(this);
			if (builtinFound || x.modality != Statement.Modality.IS)
			{
				swrlBuiltinsStatements.add(x);
			}
			else
			{
				normalStatements.add(x);
			}
		}
		Paragraph tempVar = new Paragraph(null);
		tempVar.Statements = normalStatements;
		Paragraph tempVar2 = new Paragraph(null);
		tempVar2.Statements = swrlBuiltinsStatements;
		return Tuple.Create(tempVar, tempVar2);
	}

	@Override
	public Object Visit(SwrlStatement e)
	{
		builtinFound = true;
		return null;
	}

	@Override
	public Object Visit(SwrlBuiltIn e)
	{
		builtinFound = true;
		return null;
	}

	@Override
	public Object Visit(ExeStatement e)
	{
		builtinFound = true;
		return null;
	}

	@Override
	public Object Visit(SwrlIterate e)
	{
		builtinFound = true;
		return null;
	}

	@Override
	public Object Visit(CodeStatement e)
	{
		builtinFound = true;
		return null;
	}
}