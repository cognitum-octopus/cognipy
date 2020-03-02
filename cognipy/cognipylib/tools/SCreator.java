package tools;

import java.util.*;
import java.io.*;

// Support for runtime object creation

@FunctionalInterface
public interface SCreator
{
	Object invoke(Parser yyp);
}