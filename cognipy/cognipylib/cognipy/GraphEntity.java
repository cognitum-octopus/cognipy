package cognipy;

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct GraphEntity
public final class GraphEntity
{
	private String Name;
	public String getName()
	{
		return Name;
	}
	public void setName(String value)
	{
		Name = value;
	}
	private String Kind;
	public String getKind()
	{
		return Kind;
	}
	public void setKind(String value)
	{
		Kind = value;
	}
	@Override
	public String toString()
	{
		return getName();
	}
}