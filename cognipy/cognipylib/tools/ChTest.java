package tools;

import java.util.*;
import java.io.*;

//#endif
// support for Unicode character sets

@FunctionalInterface
public interface ChTest
{
	boolean invoke(char ch);
}