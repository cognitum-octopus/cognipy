package tools;

import java.util.*;
import java.io.*;

public class CatTest
{
	private byte cat = Byte.values()[0];
	public CatTest(byte c)
	{
		cat = c;
	}
	public final boolean Test(char ch)
	{
		return Character.getType(ch) == cat;
	}
}