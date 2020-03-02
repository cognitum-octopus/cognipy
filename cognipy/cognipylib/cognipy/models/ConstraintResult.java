package cognipy.models;

import cognipy.*;
import java.util.*;

public class ConstraintResult
{
	/** 
	 Concept for which the constraint was asked
	*/
	private String Concept;
	public final String getConcept()
	{
		return Concept;
	}
	public final void setConcept(String value)
	{
		Concept = value;
	}

	private HashMap<cognipy.cnl.dl.Statement.Modality, ArrayList<String>> Relations;
	public final HashMap<cognipy.cnl.dl.Statement.Modality, ArrayList<String>> getRelations()
	{
		return Relations;
	}
	public final void setRelations(HashMap<cognipy.cnl.dl.Statement.Modality, ArrayList<String>> value)
	{
		Relations = value;
	}

	private HashMap<cognipy.cnl.dl.Statement.Modality, ArrayList<String>> ThirdElement;
	public final HashMap<cognipy.cnl.dl.Statement.Modality, ArrayList<String>> getThirdElement()
	{
		return ThirdElement;
	}
	public final void setThirdElement(HashMap<cognipy.cnl.dl.Statement.Modality, ArrayList<String>> value)
	{
		ThirdElement = value;
	}
}