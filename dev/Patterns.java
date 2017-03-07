package audio.chords;

import static audio.Constants.DATA_DIR;
import static audio.Constants.UNDEF;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import audio.Config;
import audio.Util;

/**
 * This class is responsible for loading the pre-determined patterns stored in 
 * the BEATS_FILE. The patterns represent the combinations of root, fifth and 
 * chord to be played on each beat of the bar, given the number of beats in a  
 * bar and the particular chord progression of the bar.
 * 
 * A sample pattern for a 3/4 bar (with added comments) is in the following format: 
 * 
 * 	#3#    // this line indicates the number of beats per bar
 *	
 *	000    // this line indicates the chord pattern, eg. C,C,C
 *	-11    // this line indicates the duration of the chord for each beat of the bar
 *	---    // this line indicates the duration of the fifth for each beat of the bar
 *	3--    // this line indicates the duration of the root for each beat of the bar
 *	
 *	001    // this line indicates the chord pattern, eg. C,C,G7
 *	-11
 *	---
 *	2-1 
 *
 * Note that when the file is loaded, empty lines are removed by the 
 * Util.getLines() method.
 */
public class Patterns {
	/** The logger.	*/
	private static Logger log 					= Logger.getLogger(Patterns.class);
	//private static final File BEATS_FILE 		= new File(DATA_DIR, "beats.txt"); // beats-gypsy.txt
	private static final File BEATS_FILE 		= Config.properties.get("style").equals("gypsy")
			? new File(DATA_DIR, "beats-gypsy.txt")
			: new File(DATA_DIR, "beats.txt");
	/** The map of all patterns in the file.	*/	
	public static Map<String, Pattern> patterns	= new HashMap<String, Pattern>();
	
	static {
		List<String> lines = Util.getLines(BEATS_FILE);
		log.debug("lines.size()=" + lines.size());
		
		int beatsPerBar = UNDEF;
		int count = 0;
		Pattern pattern = null;

		for (String line: lines) {
			if (line.startsWith("#")) {
				beatsPerBar = Integer.parseInt(line.replace("#", ""));
				//log.debug("beatsPerBar=" + beatsPerBar);
				count = 0;
			} else {
				if (count == 0) {
					pattern = new Pattern(beatsPerBar);
					//log.debug("line=" + line);
					patterns.put(line, pattern);
				} else {
					for (int i = 0; i < beatsPerBar; i++) {
						String s = line.substring(i, i + 1);
						if (count == 1) { // c - chord
							pattern.beats[i].c = (s.equals("-")) ? 0 : Integer.parseInt(s);
						} else if (count == 2) { // f - fifth
							pattern.beats[i].f = (s.equals("-")) ? 0 : Integer.parseInt(s);
						} else { // count == 3, r - root
							pattern.beats[i].r = (s.equals("-")) ? 0 : Integer.parseInt(s);
							count = -1; // to reset to 0
						}
					}
				} 
				count++;
			}	
		}
	}
}
