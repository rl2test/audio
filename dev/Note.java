package audio.jazz.chordscales;


/**
 * A representation of a specific note and its notation.
 */
public class Note implements ChordscaleConstants {
	public String noteStr 					= "";	// C#
	public String abcStr 					= "";	// ^C|_C - function of noteStr and octave
	//public int octave 						= 0;	// 0|1|2
	public String alphaStr 					= "";	// C|D|E|F|G|A
	public String accidentalStr 			= "";	// #|##|b|bb
	public int type							= CHORD; // CHORD|SCALE 
	
	/**
	 * @param noteStr
	 * @param octave
	 * @param type
	 */
	public Note(String noteStr, int type) { //, int octave
		this.noteStr	= noteStr;
		//this.octave		= octave;
		this.type		= type;

		alphaStr 		= noteStr.substring(0,1);
		accidentalStr 	= noteStr.substring(1);
		
		String abcAccidentalStr = accidentalStr.replace("b", "_").replace("#", "^");
		//String abcAlphaStr		= (octave > 0) ? alphaStr.toLowerCase() : alphaStr;  
		String abcAlphaStr		= alphaStr;
		
		abcStr 			= abcAccidentalStr + abcAlphaStr; 
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("noteStr=" 		+ noteStr + SPACE);
		//sb.append("octave=" 		+ octave + SPACE);
		sb.append("alphaStr=" 		+ alphaStr + SPACE);
		sb.append("accidentalStr=" 	+ accidentalStr + SPACE);

		return sb.toString();
	}
}
