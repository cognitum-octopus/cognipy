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

public class CreateDuration extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "createDuration";
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
		//var n1 = getArg(2, args, context);
		//var n2 = getArg(3, args, context);
		org.apache.jena.graph.Node n3 = getArg(4, args, context);
		org.apache.jena.graph.Node n4 = getArg(5, args, context);
		org.apache.jena.graph.Node n5 = getArg(6, args, context);
		org.apache.jena.graph.Node n6 = getArg(7, args, context);

		if (!n0.isLiteral() && n3.isLiteral() && n4.isLiteral() && n5.isLiteral() && n6.isLiteral()) // bind to datetime
		{
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
			TimeSpan dt = new TimeSpan(d, h, m, s, ms);
			org.apache.jena.graph.Node l = org.apache.jena.graph.NodeFactory.createLiteral(System.Xml.XmlConvert.toString(dt) + "^^http://www.w3.org/2001/XMLSchema#dayTimeDuration");
			return context.getEnv().bind(n0, l);
		}
		else if (n0.isLiteral() && !n3.isLiteral() && !n4.isLiteral() && !n5.isLiteral() && !n6.isLiteral()) //split datetime into parts
		{
			Object val = n0.getLiteralValue();
			final String durSuf = "^^http://www.w3.org/2001/XMLSchema#dayTimeDuration";
			if (val instanceof String && (val instanceof String ? (String)val : null).endsWith(durSuf))
			{
				String tm = (val instanceof String ? (String)val : null).substring(0, (val instanceof String ? (String)val : null).Length - durSuf.length());
				System.TimeSpan jdtm = System.Xml.XmlConvert.ToTimeSpan(tm);
				return context.getEnv().bind(n3, Util.makeIntNode(jdtm.Days)) && context.getEnv().bind(n4, Util.makeIntNode(jdtm.Hours)) && context.getEnv().bind(n5, Util.makeIntNode(jdtm.Minutes)) && context.getEnv().bind(n6, Util.makeDoubleNode((double)jdtm.Seconds + ((double)jdtm.Milliseconds) / 1000.0));
			}
			else
			{
				return false;
			}
		}
		else if (n0.isLiteral() && n3.isLiteral() && n4.isLiteral() && n5.isLiteral() && n6.isLiteral()) //compare
		{

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
			final String durSuf = "^^http://www.w3.org/2001/XMLSchema#dayTimeDuration";
			if (val instanceof String && (val instanceof String ? (String)val : null).endsWith(durSuf))
			{
				String tm = (val instanceof String ? (String)val : null).substring(0, (val instanceof String ? (String)val : null).Length - durSuf.length());
				System.TimeSpan jdtm = System.Xml.XmlConvert.ToTimeSpan(tm);

				return d == jdtm.Days && h == jdtm.Hours && m == jdtm.Minutes && s == jdtm.Seconds && ms == jdtm.Milliseconds;
			}
			else
			{
				return false;
			}
		}

		return false;
	}
}