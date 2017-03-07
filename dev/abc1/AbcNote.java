package audio.abc1;

import static audio.Constants.EQUALS;

import java.util.List;

import org.apache.log4j.Logger;

public class AbcNote extends Token {
	public static final int FLAT_2	= -2;
	public static final int FLAT	= -1;
	public static final int NATURAL	= 0;
	public static final int SHARP 	= 1;
	public static final int SHARP_2	= 2;
	public int incidental			= 0;
	// true if display value has no incidental
	public boolean isAltered		= false; 
	// unalteredValue is the value without any incidental, thus "_c'" -> "c'", "=c'" -> "c'"
	public String unalteredValue	= "";
	// alphaValue is the upper-cased alpha value, without any incidental or octave designation, thus "_c'" -> "C"
	public String alphaValue		= ""; 
	// the display value based on the key signature and previous notes within the same bar
	public String displayValue		= "";
	public int intValue				= 0;

	public AbcNote() {
		type = NOTE;
	}	
	
	public AbcNote(String note) {
		type = NOTE;
		displayValue = note;
		unalteredValue = Main.unalter(note);
		alphaValue = Main.getAlphaValue(note);
	}	
	
	public void setAbsValue(String absValue) {
		//log.debug("absValue=" + absValue);	
		this.absValue = absValue;
		if (absValue.startsWith("__")) {
			incidental = FLAT_2;
		} else if (absValue.startsWith("_")) {
			incidental = FLAT;
		} else if (absValue.startsWith("^^")) {
			incidental = SHARP_2;
		} else if (absValue.startsWith("^")) {
			incidental = SHARP;
		} else {
		}
		unalteredValue = Main.unalter(absValue);
		alphaValue = Main.getAlphaValue(absValue);
		isAltered = Main.isAltered(absValue);
		intValue = AbcMaps.noteToIntervalAbc.get(absValue);
	}
	
	/** transpose down an octave */
	/*
	public void transposeDownOctave() {
		intValue = intValue - 12;
		List<String> notes = AbcMaps.intervalToNotesAbc.get(intValue);
		for (String note: notes) {
			if (Main.getAlphaValue(note).equals(alphaValue)) {
				absValue = note;
				break;
			}
		}
		unalteredValue	= Main.unalter(absValue);
		// the display value does not need to be recalculated within the context of the bar - just transpose it as is
		log.debug("displayValue=" + displayValue);
		boolean naturalized = false;
		if (displayValue.startsWith(EQUALS)) {
			displayValue = Main.unalter(displayValue);
			naturalized = true;
		}
		int displayIntValue = AbcMaps.noteToIntervalAbc.get(displayValue) - 12;
		notes = AbcMaps.intervalToNotesAbc.get(displayIntValue);
		for (String note: notes) {
			if (Main.getAlphaValue(note).equals(alphaValue)) {
				displayValue = note;
				if (naturalized) {
					displayValue = EQUALS + displayValue;
				}
				break;
			}
		}
		log.debug("displayValue=" + displayValue);
	}
	*/
}
