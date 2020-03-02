package tools;

import java.util.*;
import java.io.*;

public class Null extends SYMBOL // fake up something that will evaluate to null but have the right yyname
{
	private int num;
	public Null(Parser yyp, int proxy)
	{
		super(yyp);
	num = proxy;
	}
	@Override
	public int getYynum()
	{
		return num;
	}
}