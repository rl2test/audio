package audio.chords.parser.guitarcodex;

//import static audio.Constants.CHORD_INTERVALS;
//import static audio.Constants.CHORD_INTEGERS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbcDefinition {
	public String symbol	= "";	// 6/9
	public String abcSymbol	= "";	// 6add9, or empty if same as symbol
	public String intervals	= "";	// 1 3 5 6
	public String semitones	= "";	// 0 4 7 9
	public String desc		= "";	// major 6th
	/** for midi definitions - 'b5' -> '6' */
	public static Map<String, String> intervalSemitoneMap 	= new HashMap<String, String>();
	/** for abc - 'b5' -> '_G' */
	public static Map<String, String> intervalCNoteMap 		= new HashMap<String, String>();
	/** abc C instance of chord C7 -> C E G _B  */
	public List<String> cNotes = new ArrayList<String>();
	
	static {
		/** A list of notes in abc format corresponding to the INTERVALS list, eg. 'b5' -> '_G'. */
//		String[] abcCNotes = {"C", "^C", "_D", "D", "^D", "_E", "E", "F", "^F", "_G", "G", "^G", "_A", "A", "__B", "^A", "_B", "B", "cb", "c", "_d", "d",  "^d", "f",  "^f",  "_a",  "a"};
		
//		for (int i = 0, n = CHORD_INTERVALS.length; i < n; i++) {
//			intervalSemitoneMap.put(CHORD_INTERVALS[i], "" + CHORD_INTEGERS[i]);
//			intervalCNoteMap.put(CHORD_INTERVALS[i], abcCNotes[i]);
//		}
	}
	
	/**
	 * @return an abc representation of this chord definition
	 */
	public String toAbc() {
		//"%^maj7 major 7th 1 3 5 7" "Cmaj7" z4 | G B d ^f | z4 |
		
		String outputAbcSymbol = (abcSymbol.equals("")) ? symbol : abcSymbol; 
		
		String abc = 
					"\"^" + symbol + "    " + desc + "    " + intervals + "\" " + 
					"\"C" + outputAbcSymbol + "\" ";
		
		for (String cNote: cNotes) {
			abc += cNote + " ";
		}
		
		abc += "| ";
		
		return abc;
	}	
	
	/**
	 * @return a playable abc representation of this chord definition
	 */
	public String toPlayableAbc() {
		//"%^maj7 major 7th 1 3 5 7" "Cmaj7" z4 | G B d ^f | z4 |
		
		String outputAbcSymbol = (abcSymbol.equals("")) ? symbol : abcSymbol;
		
		String abc = 
					"% \"^" + symbol + "    " + desc + "    " + intervals + "\" " + 
					"\"C" + outputAbcSymbol + "\" " + "z4 | ";
		
		int noteCount = 0;
		for (String cNote: cNotes) {
			abc += cNote + " ";
			noteCount++;
			if (noteCount == 4) {
				abc += "| ";
			}
		}
		
		if (noteCount < 4) {
			int rest = 4 - noteCount;
			if (rest == 1) {
				abc += "z | "; //z4 | 
			} else {
				abc += "z" + rest + " | "; //z4 | 
			}
		} else if (noteCount == 4) {
			//abc += "z4 | ";
		} else {
			int rest = 8 - noteCount;
			if (rest == 1) {
				abc += "z | ";
			} else {
				abc += "z" + rest + " | ";
			}
		}
		 
		return abc;
	}

	
	public String toMidiString() {
		String outputAbcSymbol = (abcSymbol.equals("")) ? symbol : abcSymbol;
		
		return  "%%MIDI chordname " + 
				outputAbcSymbol + " " +  
				semitones;
	}
}
