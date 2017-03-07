package audio.abc1;

import org.apache.log4j.Logger;

public class Token {
	/** The log. */
	public Logger log = Logger.getLogger(getClass());	
	public static final int CHORD	= 0;
	public static final int NOTE	= 1;
	public static final int OTHER	= 2;
	public final String[] types		= {"chord", "note", "other"};
	String absValue = "";
	int type;
	
	public String getType() {
		return types[type]; 
	}
}
