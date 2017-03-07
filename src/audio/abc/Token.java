package audio.abc;

import org.apache.log4j.Logger;

public class Token {
	/** The log. */
	public Logger log = Logger.getLogger(getClass());	
	public static final int CHORD	= 0;
	public static final int NOTE	= 1;
	public static final int OTHER	= 2;
	public String absVal 			= "";
	public int type					= -1;
}
