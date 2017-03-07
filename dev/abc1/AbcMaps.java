package audio.abc1;

import static audio.Constants.ABC_NOTES_FILE;
import static audio.Constants.ALPHABET_TOKENS;
import static audio.Constants.COMMA;
import static audio.Constants.DIVIDER_40;
import static audio.Constants.INTEGER_NOTES_ABC_FILE;
import static audio.Constants.INTEGER_NOTES_FILE;
import static audio.Constants.KEYS_FILE;
import static audio.Constants.PIPE_DELIM;
import static audio.Constants.TAB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This class contains multiple maps that can be accessed statically from any 
 * calling class.
 */
public class AbcMaps {
	/** The log. */
	private static Logger log 											= Logger.getLogger(AbcMaps.class);

	/* Populated at initialization using data files. */
	/** Map of intervals to notes, eg. 0 -> C, 1 -> B##,C#,Db, used for transposition. */
	public static Map<Integer, List<String>> intervalToNotes 			= new HashMap<Integer, List<String>>();
	/** Map of notes to intervals, eg C -> 0, C# -> 1, Db -> 1, used for transposition. */	
	public static Map<String, Integer> noteToInterval 					= new HashMap<String, Integer>();

	/** Map of intervals to notes, eg. 0 -> ^B,C__D,; 1 -> ^^B,^C_D used for transposition. */
	public static Map<Integer, List<String>> intervalToNotesAbc 		= new HashMap<Integer, List<String>>();
	/** Map of notes to intervals, eg C -> 0; ^C -> 1; _Db -> 1 used for transposition. */	
	public static Map<String, Integer> noteToIntervalAbc				= new HashMap<String, Integer>();
	/** 
	 * Map of alphabet tokens -> numeric degrees of the scale, using C as the 
	 * base, eg. C -> 1, D -> 2.
	 */
	public static Map<String, Integer> alphabetTokenToNumericDegree		= new HashMap<String, Integer>(); 

	/* Populated at run-time. */
	/** Example: "C,C,G7,G7" -> [Bar] */
	public static Map<String, Bar> bars									= new HashMap<String, Bar>();
	public static Map<String, Signature> signatures						= new HashMap<String, Signature>();
	public static final String[] SHARPS									= {"F", "C", "G", "D", "A", "E", "B"};
	public static final String[] FLATS									= {"B", "E", "A", "D", "G", "C", "F"};
	public static List<List<String>> abcNotes							= new ArrayList<List<String>>();
	
	static {
		// init intervalNotes
		initIntervalNotes();		

		// init intervalNotes
		initIntervalNotesAbc();		
				
		// in Constants: String[] ALPHABET_TOKENS = {"C", "D", "E", "F", "G", "A", "B"};
		int i = 1;
		for (String token: ALPHABET_TOKENS) {
			alphabetTokenToNumericDegree.put(token, i++);
		}
		
		initSignatures();
		initAbcNotes();
	}

	private static void initAbcNotes() {
		List<String> lines = AbcUtil.getLines(ABC_NOTES_FILE);
		int i = 0;
		for (String line: lines) {
			line = line.trim();
			String[] arr = line.split(PIPE_DELIM);
			abcNotes.add(new ArrayList<String>());
			for (String s: arr) {
				abcNotes.get(i).add(s.trim());
			}
			i++;
		}
		// generate 5 octaves
		for (i = 0; i < 5; i++) {
			for (int j = 0; j < 12; j++) {
				List<String> list = abcNotes.get(abcNotes.size() - 12);
				List<String> notes = new ArrayList<String>();
				for (String s: list) {
					notes.add(raiseOctave(s));
				}
				abcNotes.add(notes);
			}
			
		}
		/*
		i = 0;
		for (List<String> list: abcNotes) {
			if (i % 12 == 0) {
				log.debug(DIVIDER_40);
			}
			String notes =  "";
			for (String s: list) {
				notes += s + TAB;
			}
			log.debug(notes);

			i++;
		}
		*/
	}

	private static String raiseOctave(String s) {
		if (s.endsWith(",")) {
			return s.substring(0, s.length() - 1);
		} else if (s.endsWith("'")) {
			return s + "'";
		} else if (s.equals(s.toLowerCase())) {	// already lowercase
			return s + "'";
		} else {
			return s.toLowerCase();
		}
	}
	
	
	private static void initSignatures() {
		List<String> lines = AbcUtil.getLines(KEYS_FILE);
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
						signature.notes += AbcMaps.FLATS[i];
						signature.list.add(AbcMaps.FLATS[i]);
					}
				} else {
					for (int i = 0; i < incidentals; i++) {
						signature.notes += AbcMaps.SHARPS[i];
						signature.list.add(AbcMaps.SHARPS[i]);
					}					
				}
			}
			signatures.put(key, signature);
			//log.debug(signature);
		}
	}
	
	private static void initIntervalNotesAbc() {
		List<String> lines = AbcUtil.getLines(INTEGER_NOTES_ABC_FILE);
	
		int n = 0;
		for (String line: lines) {
			line = line.trim();
			if (line.length() > 0) {
				// ^B,,	|C,		|__D,
				//log.debug(line);
				String[] notes = line.split(PIPE_DELIM);
				for (String note: notes) {
					note = note.trim();
					// eg. B# -> 0
					noteToIntervalAbc.put(note, n);
					
					if (!intervalToNotesAbc.containsKey(n)) {
						intervalToNotesAbc.put(n, new ArrayList<String>());
					}
					// eg. 0 -> [B#,  C,  Dbb]
					intervalToNotesAbc.get(n).add(note);	
				}
				n++;
			}
		}
		
		
	}	
	private static void initIntervalNotes() {
		List<String> lines = AbcUtil.getLines(INTEGER_NOTES_FILE);
		
		for (String line: lines) {
			if (line.length() > 0) {
				// 0,  B#,  C,  Dbb
				
				String[] tokens = line.split(COMMA);
				int i = 0;
				int interval = -1;
				for (String token: tokens) {
					token = token.trim();

					if (i == 0) {
						interval = Integer.parseInt(token);	
					} else {
						String note = token;

						// eg. B# -> 0
						noteToInterval.put(note, interval);
						
						if (!intervalToNotes.containsKey(interval)) {
							intervalToNotes.put(interval, new ArrayList<String>());
						}
						// eg. 0 -> [B#,  C,  Dbb]
						intervalToNotes.get(interval).add(note);					
					}
					i++;
				}
			}
		}
	}
}
