package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class StatementAttr extends Attribute
{
	private StatementType type = StatementType.values()[0];
	public final StatementType getType()
	{
		return type;
	}
	private void setType(StatementType value)
	{
		type = value;
	}
	public StatementAttr(StatementType type)
	{
		this.setType(type);
	}
}