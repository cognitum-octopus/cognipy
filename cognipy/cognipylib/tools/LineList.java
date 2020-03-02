package tools;

import java.util.*;
import java.io.*;

public class LineList
{
	public int head;
	public CommentList comments = null;
	public LineList tail; // previous line!
	public LineList(int h, LineList t)
	{
		head = h;
		comments = null;
		tail = t;
	}
	public final int getpos(int pos)
	{
		int n = pos - head;
		for (CommentList c = comments; c != null; c = c.tail)
		{
			if (pos > c.spos)
			{
				n += c.len;
			}
		}
		return n;
	}
}