package cognipy.cnl.dl;

import cognipy.*;
import cognipy.cnl.*;
import java.io.*;

public class VisitingParam<T>
{
	private java.util.Stack<T> Stack = new java.util.Stack<T>();
	private T def;

	public VisitingParam(T def)
	{
		this.def = def;
	}

	private static class Lock implements Closeable
	{
		private java.util.Stack<T> Stack;
		public Lock(java.util.Stack<T> Stack, T val)
		{
			this.Stack = Stack;
			this.Stack.push(val);
		}
		public final void close() throws IOException
		{
			this.Stack.pop();
		}
	}

	private static class NullLock implements Closeable
	{
		public final void close() throws IOException
		{
		}
	}

	public final Closeable set(T val)
	{
		return new Lock(Stack, val);
	}

	public final Closeable setIf(boolean cond, T val)
	{
		if (cond)
		{
			return new Lock(Stack, val);
		}
		else
		{
			return new NullLock();
		}
	}

	public final T get()
	{
		if (Stack.empty())
		{
			return def;
		}
		else
		{
			return Stack.peek();
		}
	}
}