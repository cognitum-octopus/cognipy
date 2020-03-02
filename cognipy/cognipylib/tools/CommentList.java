package tools;

import java.util.*;
import java.io.*;

public class CommentList
{
	public int spos, len;
	public CommentList tail = null;
	public CommentList(int st, int ln, CommentList t)
	{
		spos = st;
		len = ln;
		tail = t;
	}
}