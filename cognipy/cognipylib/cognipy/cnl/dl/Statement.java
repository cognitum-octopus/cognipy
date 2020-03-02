package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Statement+62
public class Statement extends PartialSymbol implements IAccept
{
	public enum Modality
	{
		IS,
		MUST,
		SHOULD,
		CAN,
		MUSTNOT,
		SHOULDNOT,
		CANNOT;

		public static final int SIZE = java.lang.Integer.SIZE;

		public int getValue()
		{
			return this.ordinal();
		}

		public static Modality forValue(int value)
		{
			return values()[value];
		}
	}
	public Modality modality = Modality.values()[0];
	public Object accept(IVisitor v)
	{
		return null;
	}
	public Statement(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameDl()
	{
		return "Statement";
	}
	@Override
	public int getYynumDl()
	{
		return 62;
	}
}