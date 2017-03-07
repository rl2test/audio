package audio.gui;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class Chord implements GuiConstants {
	/** The log. */
	private Logger log = Logger.getLogger(this.getClass());
	public String name;		// name of the chord eg. Cm 
	public int rootVal;		// absolute note value for root
	public int fifthVal;		// absolute note value for fifth
	public int[] polyadVals;	// arr of ints for absolute note values of all notes in the chord, not including the root
	public String string;
	
	static Map<String, Integer> map = new HashMap<String, Integer>();
	static final int OCT = 12;
	static final int BASE = 3 * OCT;
	
	static {
		String[] notes = {"C","D","E","F","G","A","B",};
		int[] values = {0,2,4,5,7,9,11};
		for(int i = 0; i < values.length; i++) {
			values[i] += BASE;
		}
		for(int i = 0; i < notes.length; i++) {
			map.put(notes[i], values[i]);
		}
	}
	
	/**
	 * The following modifiers are recognized:
	 * 		C, Cm, Caug, Cdim, C7, Cmaj7, Cm7, Cdim7, Caug7, Cm7b5, C6, Cm6, Csus2, Csus4
	 * This list may be added to in the future
	 * 
	 * This is a subset of the list defined in 
	 * 		http://lilypond.org/doc/v2.12/Documentation/user/lilypond/Common-chord-modifiers
	 * 
	 * @param name
	 */
	public Chord(String name) {
		this.name = name;
		log.debug("name=" + name);
		String modifier = "";

		String note = name.substring(0, 1); // eg. C, D, E etc
		rootVal = map.get(note);
		
		if (name.length() > 1) {
			if (name.substring(1, 2).equals("#") || name.substring(1, 2).equals("b")) {
				rootVal = (name.substring(1, 2).equals("#")) ? rootVal + 1 : rootVal - 1;
				if (name.length() > 2) {
					modifier = name.substring(2);
				}
			} else {
				modifier = name.substring(1);
			}
		} 
	
		// chords
		// C, Cm, Caug, Cdim, C7, Cmaj7, Cm7, Cdim7, Caug7, Cm7b5, C6, Cm6, Csus2, Csus4
		
		// intervals:
		// C        D        E        F        G        A        B
		// 0        2        4        5        7        9        11
		// C  C# Db D  D# Eb E  E# Fb F  F# Gb G  G# Ab A  A# Bb B  B# Cb 
		// 0  1  1  2  3  3  4  5  4  5  6  6  7  8  8  9  10 10 11 12 11 

		if (modifier.equals("")) {
			// major
			fifthVal = rootVal + 7;
			polyadVals = new int[3];
			polyadVals[0] = rootVal + OCT;
			polyadVals[1] = polyadVals[0] + 4;
			polyadVals[2] = polyadVals[0] + 7;
		} else if (modifier.equals("m")) {
			// minor
			fifthVal = rootVal + 7;
			polyadVals = new int[3];
			polyadVals[0] = rootVal + OCT;
			polyadVals[1] = polyadVals[0] + 3;
			polyadVals[2] = polyadVals[0] + 7;
		} else if (modifier.equals("aug")) {
			// augmented
			fifthVal = rootVal + 8;
			polyadVals = new int[3];
			polyadVals[0] = rootVal + OCT;
			polyadVals[1] = polyadVals[0] + 4;
			polyadVals[2] = polyadVals[0] + 8;
		} else if (modifier.equals("dim")) {
			// diminished
			fifthVal = rootVal + 6;
			polyadVals = new int[3];
			polyadVals[0] = rootVal + OCT;
			polyadVals[1] = polyadVals[0] + 3;
			polyadVals[2] = polyadVals[0] + 6;
		} else if (modifier.equals("7")) {
			// dominant seventh
			fifthVal = rootVal + 7;
			polyadVals = new int[4];
			polyadVals[0] = rootVal + OCT;
			polyadVals[1] = polyadVals[0] + 4;
			polyadVals[2] = polyadVals[0] + 7;
			polyadVals[3] = polyadVals[0] + 10;
		} else if (modifier.equals("maj7")) {
			// major 7th
			fifthVal = rootVal + 7;
			polyadVals = new int[4];
			polyadVals[0] = rootVal + OCT;
			polyadVals[1] = polyadVals[0] + 4;
			polyadVals[2] = polyadVals[0] + 7;
			polyadVals[3] = polyadVals[0] + 11;
		} else if (modifier.equals("m7")) {
			// minor 7th
			fifthVal = rootVal + 7;
			polyadVals = new int[4];
			polyadVals[0] = rootVal + OCT;
			polyadVals[1] = polyadVals[0] + 3;
			polyadVals[2] = polyadVals[0] + 7;
			polyadVals[3] = polyadVals[0] + 10;
		} else if (modifier.equals("dim7")) {
			// diminished 7th
			fifthVal = rootVal + 6;
			polyadVals = new int[4];
			polyadVals[0] = rootVal + OCT;
			polyadVals[1] = polyadVals[0] + 3;
			polyadVals[2] = polyadVals[0] + 6;
			polyadVals[3] = polyadVals[0] + 9;
		} else if (modifier.equals("aug7")) {
			// augmented 7th
			fifthVal = rootVal + 8;
			polyadVals = new int[4];
			polyadVals[0] = rootVal + OCT;
			polyadVals[1] = polyadVals[0] + 4;
			polyadVals[2] = polyadVals[0] + 8;
			polyadVals[3] = polyadVals[0] + 10;			
		} else if (modifier.equals("m7b5")) {
			// half diminished 7th
			fifthVal = rootVal + 7;
			polyadVals = new int[4];
			polyadVals[0] = rootVal + OCT;
			polyadVals[1] = polyadVals[0] + 3;
			polyadVals[2] = polyadVals[0] + 6;
			polyadVals[3] = polyadVals[0] + 10;
		} else if (modifier.equals("6")) {
			// major 6th
			fifthVal = rootVal + 7;
			polyadVals = new int[4];
			polyadVals[0] = rootVal + OCT;
			polyadVals[1] = polyadVals[0] + 4;
			polyadVals[2] = polyadVals[0] + 7;
			polyadVals[3] = polyadVals[0] + 9;
		} else if (modifier.equals("m6")) {
			// minor 6th
			fifthVal = rootVal + 7;
			polyadVals = new int[4];
			polyadVals[0] = rootVal + OCT;
			polyadVals[1] = polyadVals[0] + 3;
			polyadVals[2] = polyadVals[0] + 7;
			polyadVals[3] = polyadVals[0] + 9;			
		} else if (modifier.equals("sus2")) {
			// suspended 2nd
			fifthVal = rootVal + 7;
			polyadVals = new int[3];
			polyadVals[0] = rootVal + OCT;
			polyadVals[1] = polyadVals[0] + 2;
			polyadVals[2] = polyadVals[0] + 7;			
		} else if (modifier.equals("sus4")) {
			// suspended 4th
			fifthVal = rootVal + 7;
			polyadVals = new int[3];
			polyadVals[0] = rootVal + OCT;
			polyadVals[1] = polyadVals[0] + 5;
			polyadVals[2] = polyadVals[0] + 7;			
		//} else if (modifier.equals("")) {
			
		//} else if (modifier.equals("")) {
			
		} else {
			log.error("unrecognized modifier=" + modifier);
		}

		string = name + " " + rootVal + " " + fifthVal + ", ";

		for (int i: polyadVals) {
			string += i + " "; 
		}
	}
	
	public String toString() {
		return string;
	}
}
