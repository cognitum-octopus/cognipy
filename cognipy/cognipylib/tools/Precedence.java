package tools;

import java.util.*;
import java.io.*;

public class Precedence
{
	public enum PrecType
	{
		left,
		right,
		nonassoc,
		before,
		after;

		public static final int SIZE = java.lang.Integer.SIZE;

		public int getValue()
		{
			return this.ordinal();
		}

		public static PrecType forValue(int value)
		{
			return values()[value];
		}
	}
	public PrecType m_type = PrecType.values()[0];
	public int m_prec;
	public Precedence m_next;
	public Precedence(PrecType t, int p, Precedence next)
	{
		if (CheckType(next, t, 0) != 0)
		{
			System.out.println("redeclaration of precedence");
		}
		m_next = next;
		m_type = t;
		m_prec = p;
	}
	private static int CheckType(Precedence p, PrecType t, int d)
	{
		if (p == null)
		{
			return 0;
		}
		if (p.m_type == t || (p.m_type.getValue() <= PrecType.nonassoc.getValue() && t.getValue() <= PrecType.nonassoc.getValue()))
		{
			return p.m_prec;
		}
		return Check(p.m_next, t, d + 1);
	}
	public static int Check(Precedence p, PrecType t, int d)
	{
		if (p == null)
		{
			return 0;
		}
		if (p.m_type == t)
		{
			return p.m_prec;
		}
		return Check(p.m_next, t, d + 1);
	}
	public static int Check(CSymbol s, Production p, int d)
	{
		if (s.m_prec == null)
		{
			return 0;
		}
		int a = CheckType(s.m_prec, PrecType.after, d + 1);
		int b = CheckType(s.m_prec, PrecType.left, d + 1);
		if (a > b)
		{
			return a - p.m_prec;
		}
		else
		{
			return b - p.m_prec;
		}
	}
	public static void Check(Production p)
	{
		int efflen = p.m_rhs.getCount();
		while (efflen > 1 && ((CSymbol)p.m_rhs.get(efflen - 1)).IsAction())
		{
			efflen--;
		}
		if (efflen == 3)
		{
			CSymbol op = (CSymbol)p.m_rhs.get(1);
			int b = CheckType(op.m_prec, PrecType.left, 0);
			if (b != 0 && ((CSymbol)p.m_rhs.get(2)) == p.m_lhs)
			{
			 // allow operators such as E : V = E here
				p.m_prec = b;
			}
		}
		else if (efflen == 2)
		{
			if ((CSymbol)p.m_rhs.get(0) == p.m_lhs)
			{
				int aft = Check(((CSymbol)p.m_rhs.get(1)).m_prec, PrecType.after, 0);
				if (aft != 0)
				{
					p.m_prec = aft;
				}
			}
			else if ((CSymbol)p.m_rhs.get(1) == p.m_lhs)
			{
				int bef = Check(((CSymbol)p.m_rhs.get(0)).m_prec, PrecType.before, 0);
				if (bef != 0)
				{
					p.m_prec = bef;
				}
			}
		}
	}
}