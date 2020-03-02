package cognipy.models;

import cognipy.*;
import java.util.*;

public class InstanceDescription
{
	//
	// Summary:
	//     Instance for which the description was asked
	private String Instance;
	public final String getInstance()
	{
		return Instance;
	}
	public final void setInstance(String value)
	{
		Instance = value;
	}
	//
	// Summary:
	//     ObjectProperties of the instance (e.g. John has-friend Marta., RelatedInstances
	//     will contain {has-friend,Marta})
	private HashMap<String, java.lang.Iterable<String>> RelatedInstances;
	public final HashMap<String, java.lang.Iterable<String>> getRelatedInstances()
	{
		return RelatedInstances;
	}
	public final void setRelatedInstances(HashMap<String, java.lang.Iterable<String>> value)
	{
		RelatedInstances = value;
	}
	//
	// Summary:
	//     DataProperties of the instance (e.g. Dog has-legs equal-to 4., AttributeValues
	//     will contain {has-legs,4})
	private HashMap<String, java.lang.Iterable<Object>> AttributeValues;
	public final HashMap<String, java.lang.Iterable<Object>> getAttributeValues()
	{
		return AttributeValues;
	}
	public final void setAttributeValues(HashMap<String, java.lang.Iterable<Object>> value)
	{
		AttributeValues = value;
	}
}