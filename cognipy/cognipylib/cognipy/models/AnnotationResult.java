package cognipy.models;

import cognipy.*;

public class AnnotationResult
{
	//     Subject of the annotation
	private String Subject;
	public final String getSubject()
	{
		return Subject;
	}
	public final void setSubject(String value)
	{
		Subject = value;
	}

	//     type of the subject (one of Concept, Role, Instance, DataRole, Statement)
	private String SubjectType;
	public final String getSubjectType()
	{
		return SubjectType;
	}
	public final void setSubjectType(String value)
	{
		SubjectType = value;
	}

	//     name of the annotation
	private String Property;
	public final String getProperty()
	{
		return Property;
	}
	public final void setProperty(String value)
	{
		Property = value;
	}

	//     value of the annotation
	private Object Value;
	public final Object getValue()
	{
		return Value;
	}
	public final void setValue(Object value)
	{
		Value = value;
	}

	//     language of the annotation (can be empty or null!)
	private String Language;
	public final String getLanguage()
	{
		return Language;
	}
	public final void setLanguage(String value)
	{
		Language = value;
	}
}