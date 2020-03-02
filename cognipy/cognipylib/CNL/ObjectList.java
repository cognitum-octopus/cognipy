import java.util.*;

public class ObjectList
{
	private static class Link
	{
		public Object it;
		public Link next;
		public Link(Object o, Link x)
		{
			it = o;
			next = x;
		}
	}
	private void Add0(Link a)
	{
		if (head == null)
		{
			head = last = a;
		}
		else
		{
			last = last.next = a;
		}
	}
	private Object Get0(Link a, int x)
	{
		if (a == null || x < 0) // safety
		{
			return null;
		}
		if (x == 0)
		{
			return a.it;
		}
		return Get0(a.next, x - 1);
	}
	private Link head = null, last = null;
	private int count = 0;
	public ObjectList()
	{
	}
	public final void Add(Object o)
	{
		Add0(new Link(o, null));
		count++;
	}
	public final void Push(Object o)
	{
		head = new Link(o, head);
		count++;
	}
	public final Object Pop()
	{
		Object r = head.it;
		head = head.next;
		count--;
		return r;
	}
	public final Object getTop()
	{
		return head.it;
	}
	public final int getCount()
	{
		return count;
	}
	public final Object get(int ix)
	{
		return Get0(head, ix);
	}
//C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the methods implemented will need adjustment:
	public static class OListEnumerator implements Iterator
	{
		private ObjectList list;
		private Link cur = null;
		public final Object getCurrent()
		{
			return cur.it;
		}
		public OListEnumerator(ObjectList o)
		{
			list = o;
		}
		public final boolean MoveNext()
		{
			if (cur == null)
			{
				cur = list.head;
			}
			else
			{
				cur = cur.next;
			}
			return cur != null;
		}
		public final void Reset()
		{
			cur = null;
		}
	}
	public final Iterator GetEnumerator()
	{
		return new OListEnumerator(this);
	}
}