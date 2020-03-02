package tools;

import java.util.*;
import java.io.*;

@FunctionalInterface
public interface Func
{
	SymbolSet invoke(Transition a);
}