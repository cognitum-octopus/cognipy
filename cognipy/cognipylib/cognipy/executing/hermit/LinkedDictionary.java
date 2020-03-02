package cognipy.executing.hermit;

import cognipy.cnl.dl.*;
import cognipy.configuration.*;
import org.apache.jena.graph.*;
import org.apache.jena.graph.impl.*;
import org.apache.jena.reasoner.rulesys.*;
import org.apache.jena.util.*;
import cognipy.*;
import java.util.*;
import java.io.*;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// LinkedDictionary<K, V>
//
// Enhancement to a Dictionary<K,V> object which maintains an ordering of all Values such that they can be
// iterated in the forward or reverse direction, according to this ordering, in O(N). The type used for V
// must inherit from DoubleLink class, a doubly-linked list. The ordering is established by calling 
// InsertAtFront for element(s) which have been added to the associated Dictionary<K,V> but which are not yet 
// in the list. In a typical use, the list might be ordered by time of touch: the "current" element or element
// of interest is located by its key, unlinked from the list, and reinserted at the front, all in O(1) time.
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public class LinkedDictionary<K, V extends DoubleLink> extends HashMap<K, V>
{
	private final DoubleLink first = new DoubleLink();
	private final DoubleLink last = new DoubleLink();

	public LinkedDictionary()
	{
		first.setNext(last);
	}

	/** 
	 Enumerate in forward order
	*/
	public final java.lang.Iterable<V> getForward()
	{
		DoubleLink i = first.getNext();
		while (i != last)
		{
//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
			yield return (V)i;
			i = i.getNext();
		}
	}

	/** 
	 Enumerate in reverse order
	*/
	public final java.lang.Iterable<V> getReverse()
	{
		DoubleLink i = last.getPrev();
		while (i != first)
		{
//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
			yield return (V)i;
			i = i.getPrev();
		}
	}

	/** 
	 Truncates all element(s) beyond e in the ordering. They should already be removed from the dictionary, 
	 this only unlinks them
	*/
	public final void TruncateAt(V e)
	{
		e.setNext(last);
	}

	/** 
	 Element should already be removed from the dictionary, this only unlinks it
	*/
	public final void Unlink(V e)
	{
		e.Unlink();
	}

	/** 
	 Element should already be in dictionary, but not linked
	*/
	public final void LinkToFront(V e)
	{
		e.InsertAfter(first);
	}
}