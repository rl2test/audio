package audio.abc;


public class AbcNote extends Token {
	// accidental expressed as a negative or positive int, eg. __ (bb) -> -2; ^ (#) -> 1
	public int accidental			= 0;
	// true if absVal has accidental
	public boolean isAltered		= false; 
	// the value without any incidental, thus "_c'" -> "c'", "=c'" -> "c'"
	public String unalteredVal	= "";
	// the upper-cased alpha value, without any accidental or octave designation, thus "_c'" -> "C"
	public String ucAlphaVal		= ""; 
	// display value based on the key signature and previous notes within the same bar
	public String displayVal		= "";
	

	public AbcNote() {
		type = NOTE;
	}	
	
	public AbcNote(String note) {
		type = NOTE;
		displayVal = note;
		unalteredVal = unalter(note);
		ucAlphaVal = getUcAlphaVal(note);
	}	
	
	public void setAbsVal(String absVal) {
		//log.debug("absVal=" + absVal);	
		this.absVal = absVal;
		if (absVal.startsWith("__")) {
			accidental = -2;
		} else if (absVal.startsWith("_")) {
			accidental = -1;
		} else if (absVal.startsWith("^^")) {
			accidental = 2;
		} else if (absVal.startsWith("^")) {
			accidental = 1;
		} else {
		}
		unalteredVal = unalter(absVal);
		ucAlphaVal = getUcAlphaVal(absVal);
		isAltered = Main.isAltered(absVal);
	}
	
	/**
	 * @param note
	 * @return abc note with accidental portion removed, including natural indicator ("=")
	 */
	public String unalter(String note) {
		return note.replace("_", "").replace("^", "").replace("=", ""); 
	}
	
	/**
	 * @param val
	 * @return uppercased alpha value, ie. with abc accidental and octave portion removed
	 */
	public String getUcAlphaVal(String val) {
		return unalter(val).replace(",", "").replace("'", "").toUpperCase();
	}
}

