package audio.abc;

import static audio.Constants.ABC_NOTES_FILE;
import static audio.Constants.ALPHABET_TOKENS;
import static audio.Constants.KEYS_FILE;
import static audio.Constants.PIPE_DELIM;
import static audio.Constants.TAB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import audio.Constants;
import audio.Util;

/**
 * Note: assumes that the original abc file does not contain double sharps or flats. The output file may contain double sharps or flats;
 */
public class Transposer {
	/** The log. */
	private static Logger log 						= Logger.getLogger(Transposer.class);	

	/* initialized at startup */
	public static Map<String, Integer> intervalMap 	= new HashMap<String, Integer>(); 	// map of all possible note values to interval
	public static List<List<String>> abcNotes		= new ArrayList<List<String>>();
	public static Map<String, Signature> signatures	= new HashMap<String, Signature>(); // map of key signatures, used for calculating note display values 
	public static Map<String, Integer> intVals 		= new HashMap<String, Integer>(); 	// map of all possible note and their int values
	public static String MIN_NOTE					= "G,";
	public static String MAX_NOTE					= "d'";
	public static int MIN_NOTE_VAL					= 0;
	public static int MAX_NOTE_VAL					= 0;

 	/* initialized and cleared at runtime */
	/** Transposition map, eg. 'Db' -> 'Bb'. Chordtype info is removed. */
	public Map<String, String> alphaMap 			= new HashMap<String, String>();	// map of all alpha 'from' notes -> alpha 'to' notes - 1 octave
	public Map<String, String> map 					= new HashMap<String, String>(); 	// map of all possible 'from' notes -> 'to' notes - 6 octaves
	public Map<String, String> chordMap 			= new HashMap<String, String>(); 	// map of all possible 'from' chords -> 'to' chords - 1 octave
	
	static {
		initSignatures();
		initAbcNotes();
	}

	private static void initSignatures() {
		List<String> lines = Util.getLines(KEYS_FILE);
		for (String line: lines) {
			line = line.trim();
			String[] arr = line.split(TAB);
			String key = arr[0].trim();
			int incidentals = Integer.parseInt(arr[1].trim());
			Signature signature = new Signature();
			signature.key = key;
			if (incidentals != 0) {
				signature.parity = (incidentals < 0) ? -1 : 1;
				if (signature.parity == -1) {
					for (int i = 0; i < (incidentals * -1); i++) {
						signature.notes += Constants.FLATS[i];
						signature.list.add(Constants.FLATS[i]);
					}
				} else {
					for (int i = 0; i < incidentals; i++) {
						signature.notes += Constants.SHARPS[i];
						signature.list.add(Constants.SHARPS[i]);
					}					
				}
			}
			signatures.put(key, signature);
			//log.debug(signature);
		}
	}
	
	private static void initAbcNotes() {
		log.debug("initAbcNotes()");
		List<String> lines = Util.getLines(ABC_NOTES_FILE);
		int i = 0;
		int val = 0;
		for (String line: lines) {
			line = line.trim();
			String[] arr = line.split(PIPE_DELIM);
			abcNotes.add(new ArrayList<String>());
			for (String s: arr) {
				String note = s.trim();
				abcNotes.get(i).add(note);
				intVals.put(note, val);
				//log.debug(note + " -> " + val);
				if (note.equals(MIN_NOTE)) {
					MIN_NOTE_VAL = val;
				}
				intervalMap.put(removeOctaveInfo(note), i);
			}
			i++;
			val++;
		}
		// generate 5 octaves
		for (i = 0; i < 5; i++) {
			for (int j = 0; j < 12; j++) {
				List<String> list = abcNotes.get(abcNotes.size() - 12);
				List<String> notes = new ArrayList<String>();
				for (String note: list) {
					note = raiseOctave(note);
					intVals.put(note, val);
					//log.debug(note + " -> " + val);	
					notes.add(note);
					if (note.equals(MIN_NOTE)) {
						MIN_NOTE_VAL = val;
					} else if (note.equals(MAX_NOTE)) {
						MAX_NOTE_VAL = val;
					} else {}
				}
				abcNotes.add(notes);
				val++;
			}
		}
		log.debug(MIN_NOTE_VAL + " -> " + MAX_NOTE_VAL);	
	}

	public void init(String from, String to) {
		alphaMap.clear();
		map.clear();
		chordMap.clear();

		// create map of all possible 'from' notes -> 'to' notes
		String fromAlpha = getKeyAlpha(from);
		String toAlpha = getKeyAlpha(to);
		int fromAlphaIndex = getKeyAlphaIndex(fromAlpha); 
		int toAlphaIndex = getKeyAlphaIndex(toAlpha); 

		for (int i = 0; i < ALPHABET_TOKENS.length; i++) {
			String key = ALPHABET_TOKENS[(i + fromAlphaIndex) % 7];
			String val = ALPHABET_TOKENS[(i + toAlphaIndex) % 7];
			//log.debug(key + " -> " + val);
			alphaMap.put(key, val);
		}
		
		int interval = getKeyInterval(to.replace("m", ""), from.replace("m", ""));
		//log.debug("interval=" + interval);

		int i = 0;
		for (List<String> fromNotes: abcNotes) {
			if (i % 12 == 0) {
				//log.debug(DIVIDER_40);
			}
			int j = i + interval;
			if (j > 0 && j < abcNotes.size()) {
				List<String> toNotes = abcNotes.get(j);
				if (toNotes == null) {
					//log.debug(i + " " + toDebug(fromNotes));
				} else {
					//log.debug(i + " " + toDebug(fromNotes) + " -> " + j + " " + toDebug(toNotes));	
					for (String fromNote: fromNotes) {
						fromAlpha = getUcAlpha(fromNote);
						String mapAlpha = alphaMap.get(fromAlpha);
						for (String toNote: toNotes) {
							toAlpha = getUcAlpha(toNote);
							if (toAlpha.equals(mapAlpha)) {
								map.put(fromNote, toNote);
								String fromChord = getChordVal(fromNote);
								if (!chordMap.containsKey(fromChord)) {
									String toChord = getChordVal(toNote);
									chordMap.put(fromChord, toChord);
									//log.debug("chordMap " + fromChord + "->" + toChord);
								}
								//log.debug(fromNote + "->" + toNote);
							}
						}
					}	
				}
			} else {
				//log.debug(i + " " + toDebug(fromNotes));
			}
			i++;
		}
	}
	
	/**
	 * @param chordName
	 * @return transposed chordName
	 */
	public String transposeChord(String chordName) throws Exception {
		//log.debug("chordName=" + chordName);
		Chord chord = new Chord(chordName);
		String abcRoot = standardToAbc(chord.root);
		//log.debug("abcRoot=" + abcRoot);
		String transposedAbcRoot = chordMap.get(abcRoot);
		//log.debug("transposedAbcRoot=" + transposedAbcRoot);
		String transposedRoot = abcToStandard(transposedAbcRoot);
		//log.debug("transposedRoot=" + transposedRoot);
		return transposedRoot + chord.type;
	}
	
	/**
	 * @param note
	 * @return transposed note, eg, c -> d ; _b' -> c'' 
	 * @throws Exception
	 */
	public String transposeAbcNote(String note) throws Exception {
		//log.debug("note=" + note);
		return map.get(note);
	}
	
	/**
	 * @param note
	 * @return note with octave info removed, eg. ^c' -> ^c
	 */
	private static String removeOctaveInfo(String note) {
		return note.replace(",", "").replace("'", "");
	}
	
	
	/**
	 * @param note
	 * @return note as a chord value, eg. ^c' -> ^C
	 */
	private static String getChordVal(String note) {
		return note.toUpperCase().replace(",", "").replace("'", "");
	}
	
	
	/**
	 * @param note
	 * @return uppercase alpha value, eg. ^c' -> C
	 */
	private static String getUcAlpha(String note) {
		return note.toUpperCase().replace("^", "").replace("_", "").replace(",", "").replace("'", "");
	}
	
	/**
	 * @param note
	 * @return the alpha value of the key signature, eg. C# -> C, Db -> D, Cm -> C
	 */
	private String getKeyAlpha(String note) {
		return note.substring(0, 1);
	}
	
	/**
	 * @param note
	 * @return the index of 'note' in the ALPHABET_TOKENS array
	 */
	private int getKeyAlphaIndex(String note) {
		for (int i = 0; i < ALPHABET_TOKENS.length; i++) {
			String token = ALPHABET_TOKENS[i];
			if (token.equals(note)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * @param to in standard notation, uppercase and without octave or maj/min info
	 * @param from in standard notation, uppercase and without octave or maj/min info
	 * @return interval in semi-tones between 'from' and 'to' key designation, eg. C# -> D# = 2
	 */
	private int getKeyInterval(String to, String from) {
		//log.debug(to + " -> " + from);
		return intervalMap.get(standardToAbc(to)) - intervalMap.get(standardToAbc(from));
	}
	
	/**
	 * c# -> ^c etc. Note: this method does not handle double flats and sharps. It is designed primarily for chord notations.
	 * 
	 * @param note
	 * @return note in abc notation
	 */
	private String standardToAbc(String note) {
		String abcNote = "";
		if (note.length() == 1) {
			abcNote = note;
		} else {
			if (note.endsWith("#")) {
				abcNote = "^" + note.replace("#", "");
			} else if (note.endsWith("b")) {
				abcNote = "_" + note.replace("b", "");
			} else {
			} 
		}
		return abcNote;
	}
	
	/**
	 * ^c -> c# etc. Note: this method does handle double flats and sharps.
	 * 
	 * @param abcNote
	 * @return abcNote in standard notation
	 */
	private String abcToStandard(String abcNote) {
		String note = "";
		if (abcNote.startsWith("__")) {
			note = abcNote.replace("__", "") + "bb"; 	
		} else if (abcNote.startsWith("_")) {
			note = abcNote.replace("_", "") + "b"; 	
		} else if (abcNote.startsWith("^^")) {
			note = abcNote.replace("^^", "") + "##"; 	
		} else if (abcNote.startsWith("^")) {
			note = abcNote.replace("^", "") + "#"; 	
		} else {
			note = abcNote;
		}
		return note;
	}
	
	/**
	 * @param note
	 * @return raise abcNote one octave
	 */
	public static String raiseOctave(String note) {
		if (note.endsWith(",")) {
			return note.substring(0, note.length() - 1);
		} else if (note.endsWith("'")) {
			return note + "'";
		} else if (note.equals(note.toLowerCase())) {	// already lowercase
			return note + "'";
		} else {
			return note.toLowerCase();
		}
	}

	/**
	 * @param abcNote
	 * @return raise abcNote one octave
	 */
	public static String lowerOctave(String note) {
		if (note.endsWith(",")) {
			return note + ",";
		} else if (note.endsWith("'")) {
			return note.substring(0, note.length() - 1);
		} else if (note.equals(note.toLowerCase())) {	// already lowercase
			return note.toUpperCase();
		} else {
			return note + ",";
		}
	}
}
