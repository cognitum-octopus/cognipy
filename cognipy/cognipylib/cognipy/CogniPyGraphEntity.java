package cognipy;

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct CogniPyGraphEntity
public final class CogniPyGraphEntity
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
	@Override
	public String toString()
	{
		return getName();
	}
}