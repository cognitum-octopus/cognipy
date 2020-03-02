package tools;

import java.util.*;
import java.io.*;

public class ErrorHandler
{
	public int counter = 0;
	public boolean throwExceptions = false;
	public ErrorHandler()
	{
	}
	public ErrorHandler(boolean ee)
	{
		throwExceptions = ee;
	}
	public void Error(CSToolsException e)
	{
		counter++;
		e.Handle(this);
	}
	public void Report(CSToolsException e)
	{
		//Console.WriteLine(e.Message); 
	}
}