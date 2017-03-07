package audio;

import static audio.Constants.ALPHABET_TOKENS;
import static audio.Constants.BRNL;
import static audio.Constants.CHORD_TYPES_FILE;
import static audio.Constants.COMMA;
import static audio.Constants.C_SCALES_FILE;
import static audio.Constants.DATA_DIR;
import static audio.Constants.EXT_HTML;
import static audio.Constants.EXT_TXT;
import static audio.Constants.INTEGER_NOTES_ABC_FILE;
import static audio.Constants.INTEGER_NOTES_FILE;
import static audio.Constants.KEYS_FILE;
import static audio.Constants.NL;
import static audio.Constants.PIPE_DELIM;
import static audio.Constants.TAB;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import audio.chords.Bar;
import audio.chords.Chord;
import audio.chords.ChordType;
import audio.chords.abc.Scale;

/**
 * This class contains multiple maps that can be accessed statically from any 
 * calling class.
 */
public class Maps {
	/** The log. */
	private static Logger log 											= Logger.getLogger(Maps.class);

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
	 * Map of ChordType objects using symbols as key. It is possible for more  
	 * than one symbol to refer to the same ChordType.  
	 */
	public static Map<String, ChordType> chordTypes 					= new HashMap<String, ChordType>();
	/** 
	 * Map of scale types, eg. lydian, ionian etc.  
	 */
	public static Map<String, Scale> scalesMap 							= new HashMap<String, Scale>();
	/** 
	 * Map of alphabet tokens -> numeric degrees of the scale, using C as the 
	 * base, eg. C -> 1, D -> 2.
	 */
	public static Map<String, Integer> alphabetTokenToNumericDegree		= new HashMap<String, Integer>(); 

	/* Populated at run-time. */
	/** Example: "Cmaj7" -> [Chord] */
	public static Map<String, Chord> chords								= new HashMap<String, Chord>();			
	/** Example: "C,C,G7,G7" -> [Bar] */
	public static Map<String, Bar> bars									= new HashMap<String, Bar>();

	/* Shared within this class. */
	/** 
	 * List of ChordType symbols.  
	 */
	private static List<String> symbols 								= new ArrayList<String>();
	
	/** 
	 * List of comments in the CHORD_TYPES_FILE.  
	 */
	private static List<String> comments 								= new ArrayList<String>();

	static {
		// init intervalNotes
		initIntervalNotes();		

		// init intervalNotes
		initIntervalNotesAbc();		
				
		// init chordTypes
		initChordTypes();
		
		// in Constants: String[] ALPHABET_TOKENS = {"C", "D", "E", "F", "G", "A", "B"};
		int i = 1;
		for (String token: ALPHABET_TOKENS) {
			alphabetTokenToNumericDegree.put(token, i++);
		}
		
		// initialize scalesMap
		//     - note: alphabetTokenToNumericDegree must be initialized prior to this
		initScalesMap();
		
		log.debug(Config.getBoolean("generateChordTypesHtmlFile"));
		if (Config.getBoolean("generateChordTypesHtmlFile")) {
			// generate chordTypes html file
			generateChordTypesHtmlFile();
		}
	}

	
	/**
	 * 
	 */
	private static void initIntervalNotesAbc() {
		List<String> lines = Util.getLines(INTEGER_NOTES_ABC_FILE);
	
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
		
		
		/*	
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
		*/
		
		/* for tesing input file for errors
		String header = "X:1" + NL;
		header += "T:Notes" + NL;
		header += "M:3/4" + NL;
		header += "L:1/4" + NL;
		header += "K:C" + NL;
		
		StringBuffer sb = new StringBuffer(header);

		for (String line: lines) {
			line = line.trim();
			if (line.length() > 0) {
				// b'
				// ____
				// ^B,,	|C,		|__D,
				if (line.contains(PIPE)) {
					log.debug(line);
					String[] arr = line.split(PIPE_DELIM);
					for (String s: arr) {
						sb.append(s.trim() + " ");	
					}
					if (arr.length == 2) {
						sb.append("z ");
					}
					sb.append("| ");
				} else if (line.equals("____")) {
				} else {
				}

			}
		}
		Util.writeToFile(new File("/Users/rlowe/test3.abc"), sb.toString());
		*/
	}	

	/**
	 * 
	 */
	private static void initIntervalNotes() {
		List<String> lines = Util.getLines(INTEGER_NOTES_FILE);
		
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
	
    /**
	 * 
	 */
	private static void initChordTypes() {
		List<String> lines = Util.getLines(CHORD_TYPES_FILE);
		
		/*
			// a comment line
			%% formatting - ignore this line completely
			## a column heading
			@@ a sub-heading
			-- ignore this record
		 */
		String group = "";
		for (String line: lines) {
			if (line.startsWith("//")) {
				// comment
				comments.add(line);
			} else if (line.startsWith("%%")) {
				// ignore formatting line
			} else if (line.startsWith("@@")) {
				// group heading line
				group = line.trim().substring(2);
			} else if (line.startsWith("##")) {
				// heading line
				// TODO use to generate headings
				// ##name                      | symbol  | notes    |
			} else if (line.startsWith("--")) {
				// ignore this record
			} else {
				// chord definition
				// 7th                         | 7       | C-E-G-Bb |                   
				
				ChordType chordType = new ChordType(line);
				chordType.group = group;
				String symbol = chordType.symbol;
				chordTypes.put(symbol, chordType);
				symbols.add(symbol);
			}
		}
		log.debug("chordTypes.size() = " + chordTypes.size());
	}	
	
	
	/**
	 * Initialize the scales map.
	 */
	private static void initScalesMap() {
		List<String> lines = Util.getLines(C_SCALES_FILE);

		for (String line: lines) {
			if (line.length() > 0 && !line.startsWith("#")) {
				//log.debug("line=" + line);
				Scale scale = new Scale(line); 
				scalesMap.put(scale.shortName, scale);
			}
		}
	}
	
	/**
	 * Generate a chordTypes html file. 
	 */
	private static void generateChordTypesHtmlFile() {
		try {
			log.debug("generateChordTypesHtmlFile");	
			String fileName 	= CHORD_TYPES_FILE.getName();
			String htmlFileName = fileName.replace(EXT_TXT, EXT_HTML);
			File htmlFile		= new File(DATA_DIR, htmlFileName);
			StringBuffer sb 	= new StringBuffer();

			sb.append(Util.getHtmlHeader(fileName));
			
			// comments
			for (String comment: comments) {
				sb.append(comment + BRNL);
			}
			
			sb.append("<table class='data'>" + NL);
			sb.append(ChordType.getHtmlTableHeaderRow());

			String group = "";
			for (String symbol: symbols) {
				ChordType chordType = chordTypes.get(symbol);
				
				// check if this is a new group
				if (!chordType.group.equals(group)) {
					group = chordType.group;
					sb.append("<tr class='subheading'><td colspan='" + ChordType.numberOfHtmlColumns + "'>" + group + "</td></tr>" + NL);
				}
			
				sb.append(chordType.getHtmlTableRow());
			}			

			sb.append("</table>" + NL);
			sb.append(Util.getHtmlFooter());

			// write to file
			Util.writeToFile(htmlFile, sb.toString(), false, false);
		} catch (Exception e) {
			log.error(e);
		}
	}
}
