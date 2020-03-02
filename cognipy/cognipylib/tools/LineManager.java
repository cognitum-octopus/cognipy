package tools;

import java.util.*;
import java.io.*;

public class LineManager
{
	public int lines = 1; // for error messages etc
	public int end = 0; // high water mark of positions
	public LineList list = null;
	public LineManager()
	{
	}
	public final void newline(int pos)
	{
		lines++;
		backto(pos);
		list = new LineList(pos, list);
	}
	public final void backto(int pos)
	{
		if (pos > end)
		{
			end = pos;
		}
		while (list != null && list.head >= pos)
		{
			list = list.tail;
			lines--;
		}
	}
	public final void comment(int pos, int len)
	{ // only for C-style comments not C++
		if (pos > end)
		{
			end = pos;
		}
		if (list == null)
		{
			list = new LineList(0, list);
			lines = 1;
		}
		list.comments = new CommentList(pos, len, list.comments);
	}
}