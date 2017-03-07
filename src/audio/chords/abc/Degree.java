package audio.chords.abc;

//import org.apache.log4j.Logger;

import audio.Maps;

public class Degree {
	/** The log. */
	//private Logger log 				= Logger.getLogger(getClass());
	/** The scale degree, eg. #4. */
	public String scaleDegree 		= "";
	/** The numeric degree, eg. 4. */
	public int numericDegree 		= 0;
	/** The degree modifier, eg. #. */
	public String degreeModifier	= "";

	/** The relative interval, eg. 6. */
	public int interval 			= 0;
	
	/**
	 * @param note
	 */
	public Degree(String note) {
		// String[] ALPHABET_TOKENS 		= {"C", "D", "E", "F", "G", "A", "B"};

		String alphabetToken 	= note.substring(0, 1);
		String modifier 		= (note.length() == 2) ? note.substring(1, 2) : "";
		scaleDegree				= modifier + Maps.alphabetTokenToNumericDegree.get(alphabetToken);
		//log.debug("scaleDegree=" + scaleDegree);
		interval 				= Maps.noteToInterval.get(note);
		
		int scaleDegreeLen = scaleDegree.length();
		if (scaleDegreeLen == 1) {
			numericDegree = Integer.parseInt(scaleDegree);
		} else {
			degreeModifier = scaleDegree.substring(0, 1);
			numericDegree = Integer.parseInt(scaleDegree.substring(1));
		}	 
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return scaleDegree + ", " + interval;  
	}
}
