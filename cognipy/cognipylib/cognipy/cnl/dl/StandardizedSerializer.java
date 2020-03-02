package cognipy.cnl.dl;

import cognipy.*;
import cognipy.cnl.*;

// TODO [AnnotationForSentences]: this is a first (not optimal version) of a DL standardizer.
// this serializer should somehow create a unique DL string for each sentence independent on clauses position.....
public class StandardizedSerializer extends Serializer
{
	@Override
	public Object Visit(SwrlIVar e)
	{
		return "";
	}

	@Override
	public Object Visit(SwrlDVar e)
	{
		return "";
	}

	@Override
	public Object Visit(SwrlVarList e)
	{
		return "";
	}
}