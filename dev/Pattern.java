package audio.chords;

import static audio.Constants.NL;
import static audio.Constants.UNDEF;

public class Pattern {
	/** The number of beats in this pattern. */
	public int numBeats = UNDEF;
	/** The beats array. */
	public Beat[] beats = null;
	
	/**
	 * @param numBeats
	 */
	public Pattern(int numBeats) {
		this.numBeats = numBeats; 
		beats = new Beat[numBeats];
		for (int i = 0; i < numBeats; i++) {
			beats[i] = new Beat();	
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[Pattern]" + NL);
		sb.append("numBeats=" + numBeats + NL);
		for (Beat beat: beats) {
			sb.append(beat + NL);
		}
		
		return sb.toString();
	}
}
