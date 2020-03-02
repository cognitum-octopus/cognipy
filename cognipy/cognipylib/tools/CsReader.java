package tools;

import java.util.*;
import java.io.*;

// the following class gets rid of comments for us

public class CsReader
{
	public String fname = "";
	private TextReader m_stream;
	public LineManager lm = new LineManager();
	private int back; // one-char pushback
	private enum State
	{
		copy,
		sol,
		c_com,
		cpp_com,
		c_star,
		at_eof,
		transparent;

		public static final int SIZE = java.lang.Integer.SIZE;

		public int getValue()
		{
			return this.ordinal();
		}

		public static State forValue(int value)
		{
			return values()[value];
		}
	}
	private State state = State.values()[0];
	private int pos = 0;
	private boolean sol = true;
	public CsReader(String data)
	{
		m_stream = new StringReader(data);
		state = State.copy;
		back = -1;
	}
	public CsReader(String fileName, Encoding enc)
	{
		fname = fileName;
		FileInputStream fs = new FileInputStream(fileName);
//C# TO JAVA CONVERTER WARNING: The java.io.InputStreamReader constructor does not accept all the arguments passed to the System.IO.StreamReader constructor:
//ORIGINAL LINE: m_stream = new StreamReader(fs, enc);
		m_stream = new InputStreamReader(fs);
		state = State.copy;
		back = -1;
	}
	public CsReader(CsReader inf, Encoding enc)
	{
		fname = inf.fname;
		if (inf.m_stream instanceof StreamReader)
		{
//C# TO JAVA CONVERTER WARNING: The java.io.InputStreamReader constructor does not accept all the arguments passed to the System.IO.StreamReader constructor:
//ORIGINAL LINE: m_stream = new StreamReader(((StreamReader)inf.m_stream).BaseStream, enc);
			m_stream = new InputStreamReader(((StreamReader)inf.m_stream).BaseStream);
		}
		else
		{
			m_stream = new InputStreamReader(inf.m_stream.ReadToEnd());
		}
		state = State.copy;
		back = -1;
	}
	public final boolean Eof()
	{
		return state == State.at_eof;
	}
	public final int Read(char[] arr, int offset, int count)
	{
		int c, n;
		for (n = 0; count > 0; count--, n++)
		{
			c = Read();
			if (c < 0)
			{
				break;
			}
			arr[offset + n] = (char)c;
		}
		return n;
	}
	public final String ReadLine()
	{
		int c = 0, n;
		char[] buf = new char[1024];
		int count = 1024;
		for (n = 0; count > 0; count--)
		{
			c = Read();
			if (((char)c) == '\r')
			{
				continue;
			}
			if (c < 0 || ((char)c) == '\n')
			{
				break;
			}
			buf[n++] = (char)c;
		}
		if (c < 0)
		{
			state = State.at_eof;
		}
		return new String(buf, 0, n);
	}
	public final int Read()
	{
		int c, comlen = 0;
		if (state == State.at_eof)
		{
			return -1;
		}
		while (true)
		{
			// get a character
			if (back >= 0)
			{ // back is used only in copy mode
				c = back;
				back = -1;
			}
			else if (state == State.at_eof)
			{
				c = -1;
			}
			else
			{
				c = m_stream.Read();
			}
			if (c == '\r')
			{
				continue;
			}
			while (sol && c == '#') // deal with #line directive
			{
				while (c != ' ')
				{
					c = m_stream.Read();
				}
				lm.lines = 0;
				while (c == ' ')
				{
					c = m_stream.Read();
				}
				while (c >= '0' && c <= '9')
				{
					lm.lines = lm.lines * 10 + (c - '0');
					c = m_stream.Read();
				}
				while (c == ' ')
				{
					c = m_stream.Read();
				}
				if (c == '"')
				{
					fname = "";
					c = m_stream.Read();
					while (c != '"')
					{
						fname += c;
						c = m_stream.Read();
					}
				}
				while (c != '\n')
				{
					c = m_stream.Read();
				}
				if (c == '\r')
				{
					c = m_stream.Read();
				}
			}
			if (c < 0)
			{ // at EOF we must leave the loop
				if (state == State.sol)
				{
					c = '/';
				}
				state = State.at_eof;
				pos++;
				return c;
			}
			sol = false;
			// otherwise work through a state machine
			switch (state)
			{
				case copy:
					if (c == '/')
					{
						state = State.sol;
					}
					else
					{
						if (c == '\n')
						{
							lm.newline(pos);
							sol = true;
						}
						pos++;
						return c;
					}
					continue;
				case sol: // solidus '/'
					if (c == '*')
					{
						state = State.c_com;
					}
					else if (c == '/')
					{
						comlen = 2;
						state = State.cpp_com;
					}
					else
					{
						back = c;
						state = State.copy;
						pos++;
						return '/';
					}
					continue;
				case c_com:
					comlen++;
					if (c == '\n')
					{
						lm.newline(pos);
						comlen = 0;
						sol = true;
					}
					if (c == '*')
					{
						state = State.c_star;
					}
					continue;
				case c_star:
					comlen++;
					if (c == '/')
					{
						lm.comment(pos, comlen);
						state = State.copy;
					}
					else if (c == '*') // 4.7j
					{
						state = State.c_star; // 4.7j
					}
					else
					{
						state = State.c_com;
					}
					continue;
				case cpp_com:
					if (c == '\n')
					{
						state = State.copy;
						sol = true;
						pos++;
						return c;
					}
					else
					{
						comlen++;
					}
					continue;
			}
		}
		/* notreached */
	}
}