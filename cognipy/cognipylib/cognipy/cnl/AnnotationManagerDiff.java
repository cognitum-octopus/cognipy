package cognipy.cnl;

import cognipy.*;
import java.util.*;
import java.time.*;

/** 
 A class containing the difference between two annotation managers.
*/
public class AnnotationManagerDiff
{
	private HashMap<Tuple<String, String>, ArrayList<W3CAnnotation>> _addedAnnotations = new HashMap<Tuple<String, String>, ArrayList<W3CAnnotation>>();
	public final HashMap<Tuple<String, String>, ArrayList<W3CAnnotation>> getAddedAnnotations()
	{
		return _addedAnnotations;
	}

	private HashMap<Tuple<String, String>, ArrayList<W3CAnnotation>> _removedAnnotations = new HashMap<Tuple<String, String>, ArrayList<W3CAnnotation>>();
	public final HashMap<Tuple<String, String>, ArrayList<W3CAnnotation>> getRemovedAnnotations()
	{
		return _removedAnnotations;
	}
}