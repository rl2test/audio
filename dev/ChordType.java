package audio.chords;

/**
 * This class represents a chordType as derived from the CHORDSCALE_TYPES_FILE.
 */
public class ChordType {
	public String intervalsStr	= "";
	public String[] intervals	= null;
	public String abcSymbol		= "";

	public ChordType(String intervalsStr, String abcType) {
		this.intervalsStr	= intervalsStr; 
		this.intervals 		= intervalsStr.split(" ");
		this.abcSymbol		= abcType; 
	}
	
	public String toString() {
		return abcSymbol + intervalsStr; 
	}
}
