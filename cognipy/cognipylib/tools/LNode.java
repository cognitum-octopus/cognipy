package tools;

import java.util.*;

public abstract class LNode
{
	public int m_state;
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public TokensGen m_tks;
	public LNode(TokensGen tks)
	{
		m_tks = tks;
		m_state = tks.NewState();
	}
//#endif
	protected LNode()
	{
	}
}