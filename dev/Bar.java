package audio.chords;

import java.util.ArrayList;
import java.util.List;

public class Bar implements ChordsConstants {
	/** The original sequence of the bar. */
	public String barStr 		=  "";
	/** A bar may contain 0, 1, 2 or 4 chords */
	public List<Chord> chords	= new ArrayList<Chord>();
	/** The number of chords in the bar. */
	public int numChords		= 0;
	
	public Bar(String barStr) {
		this.barStr = barStr;
		
		// accepted formats (spaces will have been removed prior to parsing): 
		// | |				-> expanded to previous chord
		// | C |			-> expanded to beatsPerBar, e.g. | C, C, C, C |
		// | C, F |			-> expanded to beatsPerBar, e.g. | C, C, F, F |
		// | C, F, Em, G |	-> as written, assuming 4/4
		
		String[] chordNames = barStr.split(",");
		
		for (String chordName: chordNames) {
			if (chordName.length() > 0) {
				if (!Mapstore.chords.containsKey(chordName)) {
					Mapstore.chords.put(chordName, new Chord(chordName));	
				}
				chords.add(Mapstore.chords.get(chordName));
			} // otherwise previous cord will be used by the player
		}
		
		numChords = chords.size(); 
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[Bar]" + "sequence=" + barStr + NL);

		for (Chord chord: chords) {
			sb.append(chord + NL);
		}
		
		return sb.toString();
	}	
}
