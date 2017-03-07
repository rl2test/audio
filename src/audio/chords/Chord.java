package audio.chords;

import static audio.Constants.ALPHABET_TOKENS;
import static audio.Constants.COMMA;
import static audio.Constants.OCTAVE;
import static audio.Constants.OCT_2;
import static audio.Constants.PERFECT_FIFTH;
import static audio.Constants.UNDEF;

import java.util.List;

import org.apache.log4j.Logger;

import audio.Maps;
import audio.Util;

/**
 * This class represents a specific chord, eg. Cm7.
 */
public class Chord {
	/** The log. */
	private Logger log = Logger.getLogger(this.getClass());
	/** The name of the chord eg. Cm7 */
	public String name			= "";
	/** The note value eg. C, F# */
	public String root 			= "";
	/** The chord type eg. m7 */
	public String type 			= "";
	/** The absolute root value. */
	public int rootValue		= UNDEF;
	/** The absolute fifth value. */
	//public int fifthValue		= UNDEF;
	/** 
	 * Array of integers representing the absolute note values of all notes in  
	 * the chord beginning one octave higher than the root
	 */
	public int[] chordIntegers;
	/** The chordType. */
	public ChordType chordType	= null;
	/** returned by toString() */
	public String string		= null;

	/**
	 * @param name
	 */
	public Chord(String name) throws Exception {
		this.name = name;
		//log.debug("name=" + name);

		int len = name.length();
		if (len == 1) { 
			root = name;
		} else if (len > 1) {
			if (name.substring(1, 2).equals("#") || name.substring(1, 2).equals("b")) {
				root = name.substring(0, 2);
				if (len > 2) {
					type = name.substring(2);
				}
			} else {
				root = name.substring(0, 1);
				type = name.substring(1);
			}
		} 
		
		// define rootValue
		//log.debug("root=" + root);
		rootValue = OCT_2 + Maps.noteToInterval.get(root);
		//log.debug("rootValue=" + rootValue);
		
		//log.debug("type=" + type);		
		
		if (!Maps.chordTypes.containsKey(type)) {
			throw new Exception("no chordType found for name=" + name + ", type=" + type);
		}
		
		chordType =  Maps.chordTypes.get(type);
		
		List<Integer> integers = chordType.integers;	
		chordIntegers = new int[integers.size()];
		int i = 0;
		for (int integer: integers) {
			chordIntegers[i++] = rootValue + OCTAVE + integer; 
		}

		//if (integers.contains(PERFECT_FIFTH)) {
		//	fifthValue = rootValue + PERFECT_FIFTH;
		//}

		string = "[Chord] " + name + ", " + root + ", " + type + ", " + rootValue + ", " + integers; //  + fifthValue + ", "
		//string = "[Chord] " + name + ", " + root + ", " + type;
		log.debug(this);
	}
	
	/**
	 * @param chordStr
	 * @return abc representation of this chord
	 */
	public String toAbc() {
		// the return string 
		String abcStr 				= "[";
		// Eb
		// E
		String rootNote 			= "";
		// b
		String rootModifier 		= "";
		// 3
		int rootRelativeInterval	= -1;

		int len = root.length();
		if (len == 1) { 
			rootNote = root;
		} else {
			rootNote = root.substring(0, 1);
			rootModifier = root.substring(1, 2);
		}
		
		// determine the relative interval for this root, using Map.intervalToNotes
		for (int i = 0; i < 12; i++) {
			List<String> notes = Maps.intervalToNotes.get(i);
			if (notes.contains(rootNote + rootModifier)) {
				rootRelativeInterval = i;
				break;
			}
		}
		//log.debug("rootNote=" + rootNote);
		//log.debug("rootModifier=" + rootModifier);
		//log.debug("rootRelativeInterval=" + rootRelativeInterval);

		// first get the numericDegree of the rootNote based on ALPHABET_TOKENS
		// eg. 3
		int rootNoteNumericDegree = Maps.alphabetTokenToNumericDegree.get(rootNote); 
		//log.debug("rootNoteNumericDegree=" + rootNoteNumericDegree);
		
		boolean lowerCase = false;
		// for each degree of the scale
		
		try {
			int i = 0;
			for (String intervalStr: chordType.intervalsStr.split(COMMA)) {
				// 1, b3, 5
				intervalStr = intervalStr.trim();

				if (!intervalStr.equals("")) {
					String modifier = intervalStr.startsWith("#") || intervalStr.startsWith("b")
							? intervalStr.substring(0, 1) : ""; 
					String numericIntervalStr = (modifier.equals("")) 
							? intervalStr : intervalStr.substring(1);  
					int numericInterval = Integer.parseInt(numericIntervalStr);
					
					if ((rootNoteNumericDegree + numericInterval - 1) > 7) {
						lowerCase = true;
					} 
					
					int integer = chordType.integers.get(i);

					// eg.
					// Ebm7				= Eb, Gb, Bb, Db
					// intervalStr 		= 1,  b3, 5,  b7  // chordType.intervalsStr
					// numericDegree	= 1,  3,  5,  7
					// degreeModifier	=  ,  b,   ,  b  
					// intervals		= 0,  3,  7,  10  // chordType.integers (List)

					String chordNoteAlphabetToken = ALPHABET_TOKENS[(rootNoteNumericDegree + numericInterval - 2) % 7];
					String chordNote = "";
					
					int relativeInterval = (rootRelativeInterval + integer) % 12;
					List<String> notes = Maps.intervalToNotes.get(relativeInterval);
					for (String note: notes) {
						if (note.contains(chordNoteAlphabetToken)) {
							chordNote = note;
							break;
						}
					}
					if (lowerCase) {
						chordNote = chordNote.toLowerCase();
					}
					
					String abcNote = Util.noteToAbc(chordNote);
					
					abcStr += abcNote + "2";

					i++;
				}
			}

			abcStr += "]";
			//String test = "";
			//for (int n: chordType.integers) {
			//	test += n + " ";
			//}
			
			//return chordType.intervalsStr;
			return abcStr;
		} catch (Exception e) {
			log.error(e);
			return "error for " + name;
		} 
	}
	
	public String toString() {
		return string;
	}
}
