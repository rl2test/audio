package audio.abc;

import static audio.Constants.COMMA;
import static audio.Constants.DQ;
import static audio.Constants.NL;
import static audio.Constants.PIPE_DELIM;
import static audio.Constants.SQ;
import static audio.Constants.UNTRANSPOSABLE_ABC_ELEMENTS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import audio.ExtensionFilter;
import audio.Util;

public class Main {
	/** The log. */
	private Logger log = Logger.getLogger(getClass());
	public static final File JAZZ_DIR = (System.getProperty("os.version").equals("10.12"))
			? new File("/Users/rlowe/rob/music/jazz")			// wk
			: new File("/Volumes/IOMEGA-1000/Rob/music/jazz");	// hm
	public static final File ABC_DIR = new File(JAZZ_DIR, "abc/aebersold");
	public final File CHORDS_DIR = new File(JAZZ_DIR, "chords/Aebersold-Excersies");
	public final String[] majorKeys = {"C", "F", "Bb", "Eb", "Ab", "Db", "F#", "B",  "E",  "A",  "D", "G"}; // used for looping thru all maj keys
	public final String[] minorKeys = {"A", "D", "G",  "C",  "F",  "Bb", "Eb", "G#", "C#", "F#", "B", "E"}; // used for looping thru all min keys
	public Transposer transposer = new Transposer();
	public final int BARS_PER_LINE = 4;	
	
	
	/**
	 * Transpose an abc file, cycling thru all keys, writing each key to a separate .abc file
	 */
	public void run(String filename) throws Exception {
		log.debug("filename=" + filename);
		String dirName = filename.replace(".abc", "");
		File transposeDir = new File(ABC_DIR, dirName);
		if (!transposeDir.exists()) {
			transposeDir.mkdir();
		}	
		
		List<String> lines 	= Util.getLines(new File(ABC_DIR, filename));
		String fromKey 		= "";
		boolean isMinor 	= false;
		String abc 			= "";
		Header header 		= new Header();
		
		// parse abc file
		for (String line: lines) {
			if (line.startsWith("%")) {
				// comment 
			} else if (line.startsWith("X:")) {
				header.X = line.substring(2).trim(); 
			} else if (line.startsWith("T:")) {
				// generate title from filename 
			} else if (line.startsWith("S:")) {
				header.S = line.substring(2).trim();
			} else if (line.startsWith("M:")) {
				header.M = line.substring(2).trim(); 
			} else if (line.startsWith("L:")) {
				header.L = line.substring(2).trim(); 
			} else if (line.startsWith("Q:")) {
				header.Q = line.substring(2).trim(); 
			} else if (line.startsWith("K:")) {
				header.K = line.substring(2).trim();
				// get key, which may be minor
				fromKey = header.K;
				isMinor = (fromKey.contains("m"));
			} else {
				abc	+= line.trim();
			}
		}
		log.debug("fromKey=" + fromKey);
		
		Header newHeader = header.clone();
		
		// get list of Bars from abc String	
		List<Bar> bars = getBars(fromKey, abc);
		
		// write chords file
		StringBuffer chords = new StringBuffer();
		chords.append("@4|90-180|5" + NL); 
		chords.append("!K: " + header.K + NL);
		int barCount = 0;
		for (Bar bar: bars) {
			chords.append(bar.toChords());
			barCount++;
			if (barCount % 4 == 0) {
				chords.append(NL);
			}
			Util.writeToFile(new File(transposeDir, dirName + ".chords"), chords.toString());
		}
		
		// transpose for each key
		String[] keys = (isMinor) ? minorKeys : majorKeys;
		for (String toKey: keys) {
			if (isMinor) {
				toKey += "m"; 
			}
			log.debug("toKey=" + toKey);
			barCount = 1;
			newHeader.T = getTitle(filename, toKey);
			newHeader.K = toKey;
			StringBuffer sb = new StringBuffer(newHeader.toAbc());
			
			transposer.init(fromKey, toKey);
			for (Bar bar: bars) {
				List<Token> transposedTokens = new ArrayList<Token>();
				for (Token token: bar.tokens) {
					if (token.type == Token.CHORD) {
						transposedTokens.add(new AbcChord(transposer.transposeChord(token.absVal)));
					} else if (token.type == Token.NOTE) {
						AbcNote abcNote = new AbcNote();
						abcNote.setAbsVal(transposer.transposeAbcNote(token.absVal));
						transposedTokens.add(abcNote);
					} else {
						transposedTokens.add(new AbcElement(token.absVal));
					}
				}				
				Bar transposedBar = new Bar(toKey, transposedTokens);
				//log.debug("absolute: " + transposedBar.toAbcAbsolute());
				transposedBar.setDisplay();
				//log.debug(" display: " + transposedBar.toAbcDisplay());
					
				sb.append(transposedBar.toAbcDisplay());
				if (barCount % BARS_PER_LINE == 0) {
					sb.append(NL);
				}
				barCount++;
			}

			String newFilename = dirName + "-" + keyToText(toKey) + ".abc";
			
			Util.writeToFile(new File(transposeDir, newFilename), sb.toString());
			//break;
			//log.debug(DIVIDER_40);
		}
	}
	
	/**
	 * Get list of absolute Bars from abc String 
	 */
	public List<Bar> getBars(String key, String abc) {
		String[] abcBars = abc.split(PIPE_DELIM);
		List<Bar> bars = new ArrayList<Bar>();
		// convert each bar to a list of Tokens and add to bars array
		for (String abcBar: abcBars) {
			Bar bar = getBar(key, abcBar);
			//log.debug(" display: " + bar.toAbcDisplay());
			bar.setAbsolute();
			//log.debug("absolute: " + bar.toAbcAbsolute());
			//log.debug("========================================");
			bars.add(bar);
		}
		return bars;
	}
	
	/**
	 * get Bar from abcBar String 
	 */		
	public Bar getBar(String key, String abcBar) {
		// convert abcBar to an array of characters
		int len = abcBar.length();
		String[] arr = new String[len];
		for (int i = 0; i < len; i++) {
			arr[i] = "" + abcBar.charAt(i);
		}

		// parse array and extract tokens
		List<Token> tokens = new ArrayList<Token>();
		for (int i = 0; i < len; i++) {
			String s = arr[i];
			if (s.equals(DQ)) { // chord
				String chord = "";
				for (int j = 1; j < len - i; j++) {
					if (arr[i + j].equals(DQ)) {
						tokens.add(new AbcChord(chord));
						i +=j; 
						break;
					} else {
						chord += arr[i + j];
					}
				}
				//log.debug("chord=" + chord);
			} else if (UNTRANSPOSABLE_ABC_ELEMENTS.contains(s)) {
				tokens.add(new AbcElement(s));
			} else { // note element
				String note = "";
				if (isAltered(s)) { // accidental
					note += s + arr[i + 1];
					i++;
				} else {
					note = s;
				}
				if (i + 1 < len) {
					if (arr[i + 1].equals(COMMA) || arr[i + 1].equals(SQ)) {
						note += arr[i + 1];
						i++;
					}
				}		
				tokens.add(new AbcNote(note));
			}
		}
		Bar bar = new Bar(key, tokens);
		return bar;
	}
	
	
	public static boolean isAltered(String note) {
		return (note.contains("_") || note.contains("^") || note.contains("="));
	}

	/**
	 * @param key
	 * @return standard notation key to text, eg. C#m -> C-sharp-minor, Bbm -> B-flat-minor etc.
	 */
	public String keyToText(String key) {
		String text = "";
		if (key.contains("m")) {
			if (key.contains("#")) { // C#m
				text = key.substring(0, 1) + "-sharp-minor";
			} else if (key.contains("b")) { // Bbm
				text = key.substring(0, 1) + "-flat-minor";
			} else {
				text = key.substring(0, 1) + "-minor";
			}
		} else {
			if (key.contains("#")) { // C#
				text = key.substring(0, 1) + "-sharp";
			} else if (key.contains("b")) { // Bb
				text = key.substring(0, 1) + "-flat";
			} else {
				text = key;
			}
		}		
		return text;
	}
	
	/**
	 * @param filename
	 * @param toKey
	 * @return title derived from filename and toKey
	 */
	private String getTitle(String filename, String toKey) {
		filename = filename.replace(".abc", "");
		String exercise = "";
		String book = "";
		
		if (filename.startsWith("000")) {
			book = "Jazz-Handbook";
			exercise = filename.replace("000-", ""); 
		}
		else if (filename.startsWith("001")) {
			book = "Vol 001 - [How to Play and Improvise Jazz]";
			exercise = filename.replace("001-", ""); 
		}
		else if (filename.startsWith("003-supplement")) {
			book = "Vol 003 - [The II-V7-I Progression - Supplement]";
			exercise = filename.replace("003-supplement-", ""); 
		}
		else if (filename.startsWith("003")) {
			book = "Vol 003 - [The II-V7-I Progression]";
			exercise = filename.replace("003-", ""); 
		}
		else if (filename.startsWith("084")) {
			book = "Vol 084 - [Dominant Seventh Workout]";
			exercise = filename.replace("084-", "");
		}
		return book + NL + "T:" + exercise + " - " + toKey;
	}
	
	public static void main(String args[]) {
		Main main = new Main();
		List<String> filenames = Util.getFileNames(ABC_DIR, new ExtensionFilter("abc"));
		for(String filename: filenames) {
			try {
				if(!filename.startsWith(".")) {
					main.run(filename);	
				}	
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		System.out.println("ok");
	}	
}
