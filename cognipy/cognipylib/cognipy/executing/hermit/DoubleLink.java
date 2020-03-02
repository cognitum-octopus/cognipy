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
// DoubleLink
//
// Base type for Values in LinkedDictionary<K, V>
//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [DebuggerDisplay("prev = {prev} next = {next}")] public class DoubleLink
public class DoubleLink
{
	private DoubleLink next;
	private DoubleLink prev;

	public final DoubleLink getNext()
	{
		return next;
	}
		// Rude setter: doesn't examine or patch existing links 
	public final void setNext(DoubleLink value)
	{
		value.prev = this;
		next = value;
	}

	public final DoubleLink getPrev()
	{
		return prev;
	}
		// Rude setter: doesn't examine or patch existing links 
	public final void setPrev(DoubleLink value)
	{
		value.next = this;
		prev = value;
	}

	// Remove this item from a list by patching over it
	public final void Unlink()
	{
		prev.next = next;
		next.prev = prev;
		next = prev = null;
	}

	// Insert this item into a list after the specified element
	public final void InsertAfter(DoubleLink e)
	{
		e.next.prev = this;
		next = e.next;
		prev = e;
		e.next = this;
	}
}