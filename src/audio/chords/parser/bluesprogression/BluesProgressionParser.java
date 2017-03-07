package audio.chords.parser.bluesprogression;

import static audio.Constants.DATA_DIR;
import static audio.Constants.EXT_CHORDS;
import static audio.Constants.FS;
import static audio.Constants.MUSIC_DIR;
import static audio.Constants.NL;
import static audio.Constants.PERIOD;
import static audio.Constants.PIPE_DELIM;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import audio.Util;

/**
 * This class uses the definitions in 
 * 		C:\rob\apps\audio\data\ 
 * to generate '.chord' files
 */
public class BluesProgressionParser {
	/** The log. */
	private Logger log = Logger.getLogger(getClass());
	private File jazzChordsDir = new File(MUSIC_DIR, "jazz/chords");
	
	public void generateBluesProgressions() {
		log.debug("begin");
		
		String dataFileName = "blues-progressions.txt";
		String outputFolder = "Blues Progressions";
		
		List<String> lines = Util.getLines(new File(DATA_DIR, dataFileName));
		List<String> progressions = new ArrayList<String>();
		
		String source = "";
		for (String line: lines) {
			line = line.trim();
			if (line.startsWith("//")) {
				if (line.startsWith("//S:")) {
					source = line.substring(4);	
					source = source.trim();
				}
			} else {
				//String[] arr = line.split(PIPE_DELIM);
				//A.  | F7       | Bb7       | F7            | Cmi, F7       | Bb7    | Bb7       | F7        | D7          | Gm9     | C7        | F7            | Gmi, C7   |
				int index = line.indexOf("|");
				String title = source + " - " + line.substring(0, index).trim();
				title = title.replace(PERIOD, ""); // remove period
				String chords = line.substring(index + 1).trim();
				chords = Util.collapseSpaces(chords);	
				
				chords = createPhrases(chords);
				
				StringBuffer sb = new StringBuffer();
				
//				T: Beginner's Approach - Ch 5 - Ex 1
//				S: A Beginner's Approach To Jazz Improvisation, Chapter 5
//				M: 4/4
//				Q: 1/4=80
//				L: 1/4
//				K: C
//				C6 | Dm7 | G7 | C6 |
				sb.append("T: " + title + NL);
				sb.append("M: 4/4" + NL);
				sb.append("Q: 1/4=80" + NL);
				sb.append("L: 1/4" + NL);
				sb.append("K: F" + NL);
				sb.append(chords + NL);
				
				log.debug(chords);
				progressions.add(chords + " " + title);
				
				File file = new File(jazzChordsDir, outputFolder + FS + title + EXT_CHORDS);
				Util.writeToFile(file, sb.toString(), false, false);				
			}				
		}
		Collections.sort(progressions);
		for (String progression: progressions) {
			log.debug(progression);
		}
	}
		
	public void generateBluesInAllKeys() {
		log.debug("begin");
		
		String dataFileName = "Vol42-Blues In All Keys.txt";
		String outputFolder = "Aebersold";
		String prefix 		= "Vol 42 - Blues In All Keys - ";
		
		List<String> lines = Util.getLines(new File(DATA_DIR, dataFileName));
		
		String source = "";
		for (String line: lines) {
			line = line.trim();
			if (line.startsWith("//")) {
				if (line.startsWith("//S:")) {
					source = line.substring(4);	
					source = source.trim();
				}
			} else {
				//01a. Bb | Bb7 | Eb7	| Bb7 | Fm7,  Bb7 | Eb7 | Eb7    | Bb7          | Ddim7,  G7+9  | Cm7  | F7  | Dm7,  G7  | Cm7,  F7  | Kenny'll Make It
				String[] arr = line.split(PIPE_DELIM);
				int len = arr.length;
				//01a. Bb
				String title = arr[0].trim();
				
				String key = title.substring(title.indexOf(PERIOD) + 1).trim(); 
				
				title = title.replace(PERIOD, "") + " Blues - " + arr[len - 1].trim();

				String chords = "";
				for (int i = 1; i < 13; i++) {
					chords += arr[i] + " | ";
				}
				
				chords = chords.substring(0, chords.length() - 2); // remove final "| " 
				log.debug(chords);
				
				chords = Util.collapseSpaces(chords);	
				
				chords = createPhrases(chords);
				
				StringBuffer sb = new StringBuffer();
				
//				T: Beginner's Approach - Ch 5 - Ex 1
//				S: A Beginner's Approach To Jazz Improvisation, Chapter 5
//				M: 4/4
//				Q: 1/4=80
//				L: 1/4
//				K: C
//				C6 | Dm7 | G7 | C6 |
				sb.append("T: " + title + NL);
				sb.append("M: 4/4" + NL);
				sb.append("Q: 1/4=80" + NL);
				sb.append("L: 1/4" + NL);
				sb.append("K: " + key + NL);
				sb.append(chords + NL);
				
				log.debug(chords);
				
				File file = new File(jazzChordsDir, outputFolder + FS + prefix + title + EXT_CHORDS);
				Util.writeToFile(file, sb.toString(), false, false);				
			}				
		}
	}

	/**
	 * @return chords split into 4 bar phrases
	 */
	private String createPhrases(String chords) {
		String[] arr = chords.split(PIPE_DELIM);
		StringBuffer sb = new StringBuffer();
		int count = 0;
		for (String s: arr) {
			sb.append(s.trim() + " | ");
			count++;
			if (count == 4) {
				sb.append(NL);
				count = 0;
			}
		}
		return sb.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//new Parser().generateBluesProgressions();
		new BluesProgressionParser().generateBluesInAllKeys();
	}

}
