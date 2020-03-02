package cognipy.executing.hermit;

import cognipy.cnl.dl.*;
import cognipy.configuration.*;
import org.apache.jena.graph.*;
import org.apache.jena.graph.impl.*;
import org.apache.jena.reasoner.rulesys.*;
import org.apache.jena.util.*;
import cognipy.*;
import java.util.*;
import java.io.*;

public class CreateDatetime extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "createDatetime";
	}

	@Override
	public int getArgLength()
	{
		return 8;
	}

	@Override
	public boolean bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		checkArgs(length, context);

		org.apache.jena.graph.Node n0 = getArg(0, args, context);
		org.apache.jena.graph.Node n1 = getArg(2, args, context);
		org.apache.jena.graph.Node n2 = getArg(3, args, context);
		org.apache.jena.graph.Node n3 = getArg(4, args, context);
		org.apache.jena.graph.Node n4 = getArg(5, args, context);
		org.apache.jena.graph.Node n5 = getArg(6, args, context);
		org.apache.jena.graph.Node n6 = getArg(7, args, context);

		if (!n0.isLiteral() && n1.isLiteral() && n2.isLiteral() && n3.isLiteral() && n4.isLiteral() && n5.isLiteral() && n6.isLiteral()) // bind to datetime
		{
			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1))
			{
				return false;
			}
			Object p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
			if (!RuleExtensions.isInteger(p2))
			{
				return false;
			}
			Object p3 = RuleExtensions.getValFromJenaLiteral(n3.getLiteralValue());
			if (!RuleExtensions.isInteger(p3))
			{
				return false;
			}
			Object p4 = RuleExtensions.getValFromJenaLiteral(n4.getLiteralValue());
			if (!RuleExtensions.isInteger(p4))
			{
				return false;
			}
			Object p5 = RuleExtensions.getValFromJenaLiteral(n5.getLiteralValue());
			if (!RuleExtensions.isInteger(p5))
			{
				return false;
			}
			Object p6 = RuleExtensions.getValFromJenaLiteral(n6.getLiteralValue());
			if (!RuleExtensions.isInteger(p6) && !RuleExtensions.isDouble(p6))
			{
				return false;
			}
			int y = (Integer)p1;
			int M = (Integer)p2;
			int d = (Integer)p3;
			int h = (Integer)p4;
			int m = (Integer)p5;
			int s;
			int ms;
			if (!RuleExtensions.isDouble(p6))
			{
				s = (Integer)p6;
				ms = 0;
			}
			else
			{
				double dbl = (Double)p6;
				s = (int)Math.Truncate(dbl);
				ms = (int)Math.Truncate((dbl - s) * 1000.0);
			}
			DateTimeOffset dt = new DateTimeOffset(y, M, d, h, m, s, ms, TimeSpan.Zero);
			org.apache.jena.graph.Node l = org.apache.jena.graph.NodeFactory.createLiteral(dt.toString("s") + "^^http://www.w3.org/2001/XMLSchema#dateTime");
			return context.getEnv().bind(n0, l);
		}
		else if (n0.isLiteral() && !n1.isLiteral() && !n2.isLiteral() && !n3.isLiteral() && !n4.isLiteral() && !n5.isLiteral() && !n6.isLiteral()) //split datetime into parts
		{
			Object val = n0.getLiteralValue();
			if (val instanceof org.apache.jena.datatypes.xsd.XSDDateTime)
			{
				org.apache.jena.datatypes.xsd.XSDDateTime jdtm = val instanceof org.apache.jena.datatypes.xsd.XSDDateTime ? (org.apache.jena.datatypes.xsd.XSDDateTime)val : null;
				return context.getEnv().bind(n1, Util.makeIntNode(jdtm.getYears())) && context.getEnv().bind(n2, Util.makeIntNode(jdtm.getMonths())) && context.getEnv().bind(n3, Util.makeIntNode(jdtm.getDays())) && context.getEnv().bind(n4, Util.makeIntNode(jdtm.getHours())) && context.getEnv().bind(n5, Util.makeIntNode(jdtm.getMinutes())) && context.getEnv().bind(n6, Util.makeDoubleNode(jdtm.getSeconds()));
			}
			else
			{
				return false;
			}
		}
		else if (n0.isLiteral() && n1.isLiteral() && n2.isLiteral() && n3.isLiteral() && n4.isLiteral() && n5.isLiteral() && n6.isLiteral()) //compare
		{
			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1))
			{
				return false;
			}
			Object p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
			if (!RuleExtensions.isInteger(p2))
			{
				return false;
			}
			Object p3 = RuleExtensions.getValFromJenaLiteral(n3.getLiteralValue());
			if (!RuleExtensions.isInteger(p3))
			{
				return false;
			}
			Object p4 = RuleExtensions.getValFromJenaLiteral(n4.getLiteralValue());
			if (!RuleExtensions.isInteger(p4))
			{
				return false;
			}
			Object p5 = RuleExtensions.getValFromJenaLiteral(n5.getLiteralValue());
			if (!RuleExtensions.isInteger(p5))
			{
				return false;
			}
			Object p6 = RuleExtensions.getValFromJenaLiteral(n6.getLiteralValue());
			if (!RuleExtensions.isInteger(p6) && !RuleExtensions.isDouble(p6))
			{
				return false;
			}
			int y = (Integer)p1;
			int M = (Integer)p2;
			int d = (Integer)p3;
			int h = (Integer)p4;
			int m = (Integer)p5;
			int s = (Integer)p5;
			int ms;
			if (!RuleExtensions.isDouble(p6))
			{
				s = (Integer)p6;
				ms = 0;
			}
			else
			{
				double dbl = (Double)p6;
				s = (int)Math.Truncate(dbl);
				ms = (int)Math.Truncate((dbl - s) * 1000.0);
			}

			Object val = n0.getLiteralValue();
			if (val instanceof org.apache.jena.datatypes.xsd.XSDDateTime)
			{
				org.apache.jena.datatypes.xsd.XSDDateTime jdtm = val instanceof org.apache.jena.datatypes.xsd.XSDDateTime ? (org.apache.jena.datatypes.xsd.XSDDateTime)val : null;
				return y == jdtm.getYears() && M == jdtm.getMonths() && d == jdtm.getDays() && h == jdtm.getHours() && m == jdtm.getMinutes() && s == (int)Math.Truncate(jdtm.getSeconds()) && ms == (int)Math.Truncate((jdtm.getSeconds() - s) * 1000.0);
				;
			}
			else
			{
				return false;
			}
		}

		return false;
	}
}