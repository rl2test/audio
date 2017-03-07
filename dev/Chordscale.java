package audio.chords.parser.scalesyllabus;

import java.util.HashMap;
import java.util.Map;

import audio.Constants;
import audio.Util;

public class Chordscale implements Constants {
	public String text 									= "";
	// CHORD/SCALE SYMBOL | SCALE NAME | WHOLE & HALF STEP CONSTRUCTION | SCALE IN KEY OF C | BASIC CHORD IN KEY OF C
	// chord/scale symbol | scale name | whole & half step construction | scale in key of c | basic chord in key of c
	public String chordScaleSymbol 						= "";
	public String scaleName 							= "";
	public String wholeHalfstepConstruction 			= "";
	public String scaleInKeyOfC 						= "";
	public String basicChordInKeyOfC 					= "";

	public static int chordScaleSymbolLen 				= 0;
	public static int scaleNameLen 						= 0;
	public static int wholeHalfstepConstructionLen		= 0;
	public static int scaleInKeyOfCLen 					= 0;
	public static int basicChordInKeyOfCLen 			= 0;
	
	public boolean isText								= false;
	public static Map<String, String> notesToIntervals	= new HashMap<String, String>(); 
	public String[] intervals 							= null;
	public String intervalStr							= "";
	public static int intervalStrLen					= 0;
	
	static {
		for (int i = 0, n = C_NOTES.length; i < n; i++) {
			notesToIntervals.put(C_NOTES[i], INTERVALS[i]);
		}
	}

	public Chordscale(String text) {
		this.text	= text;
		isText		= true;
	}
	
	public Chordscale(
			String chordScaleSymbol,
			String scaleName,
			String wholeHalfstepConstruction,
			String scaleInKeyOfC,
			String basicChordInKeyOfC) {
		this.chordScaleSymbol 			= chordScaleSymbol;
		this.scaleName 					= scaleName;
		this.wholeHalfstepConstruction	= wholeHalfstepConstruction;
		this.scaleInKeyOfC 				= scaleInKeyOfC;
		this.basicChordInKeyOfC 		= basicChordInKeyOfC;
		
		int len = 0;
		
		len = chordScaleSymbol.length();
		if (len > chordScaleSymbolLen) {
			chordScaleSymbolLen = len;
		}
		len = scaleName.length();
		if (len > scaleNameLen) {
			scaleNameLen = len;
		}
		len = wholeHalfstepConstruction.length();
		if (len > wholeHalfstepConstructionLen) {
			wholeHalfstepConstructionLen = len;
		}
		len = scaleInKeyOfC.length();
		if (len > scaleInKeyOfCLen) {
			scaleInKeyOfCLen = len;
		}
		len = basicChordInKeyOfC.length();
		if (len > basicChordInKeyOfCLen) {
			basicChordInKeyOfCLen = len;
		}
		
		String[] arr = scaleInKeyOfC.split(" ");
		intervals = new String[arr.length];
		int i = 0;
		for (String s: arr) {
			String interval = notesToIntervals.get(s); 
			if (i < 2 && interval.equals("8")) {
				interval = "1";
			}
			intervals[i++] = interval;
			intervalStr += interval + " ";
		}
		len = intervalStr.length();
		if (len > intervalStrLen) {
			intervalStrLen = len;
		}
	}
	
	public String toString() {
		String delimiter = " | ";
		if (isText) {
			return text;
		} else {
			return 
					Util.padWithSpaces(chordScaleSymbol,			chordScaleSymbolLen + 1) 			+ delimiter + 
					Util.padWithSpaces(scaleName, 					scaleNameLen + 1) 					+ delimiter + 
					Util.padWithSpaces(wholeHalfstepConstruction,	wholeHalfstepConstructionLen + 1) 	+ delimiter + 
					Util.padWithSpaces(scaleInKeyOfC, 				scaleInKeyOfCLen + 1) 				+ delimiter + 
					Util.padWithSpaces(basicChordInKeyOfC, 			basicChordInKeyOfCLen + 1) 			+ delimiter + 
					Util.padWithSpaces(intervalStr, 				intervalStrLen + 1) 				+ delimiter;					
		} 
	}
}
