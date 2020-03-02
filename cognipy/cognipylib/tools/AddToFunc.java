package tools;

import java.util.*;
import java.io.*;

@FunctionalInterface
public interface AddToFunc
{
	void invoke(Transition a, SymbolSet s);
}