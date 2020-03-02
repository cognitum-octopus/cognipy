package cognipy;

import java.util.*;

public class CogniPyStatement
{
	private String CnlStatement;
	public final String getCnlStatement()
	{
		return CnlStatement;
	}
	public final void setCnlStatement(String value)
	{
		CnlStatement = value;
	}

	private HashSet<String> Concepts;
	public final HashSet<String> getConcepts()
	{
		return Concepts;
	}
	public final void setConcepts(HashSet<String> value)
	{
		Concepts = value;
	}

	private HashSet<String> Instances;
	public final HashSet<String> getInstances()
	{
		return Instances;
	}
	public final void setInstances(HashSet<String> value)
	{
		Instances = value;
	}

	private HashSet<String> Roles;
	public final HashSet<String> getRoles()
	{
		return Roles;
	}
	public final void setRoles(HashSet<String> value)
	{
		Roles = value;
	}

	private HashSet<String> DataRoles;
	public final HashSet<String> getDataRoles()
	{
		return DataRoles;
	}
	public final void setDataRoles(HashSet<String> value)
	{
		DataRoles = value;
	}

	private StatementType Type = StatementType.values()[0];
	public final StatementType getType()
	{
		return Type;
	}
	public final void setType(StatementType value)
	{
		Type = value;
	}
}