package audio.chords.parser;

import static audio.Constants.DATA_DIR;
import static audio.Constants.EXT_TXT;
import static audio.Constants.NL;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import audio.Util;

/**
 * This class parses the 'chord-types2.txt' file and sorts it by chordIntervals
 * or abcSymbols. 
 */
public class ChordTypes2Parser {
	/** The log. */
	private Logger log 							= Logger.getLogger(getClass());
	
	public void sortByChordIntervals(String fileName) {
		File inputFile	= new File(DATA_DIR, fileName + EXT_TXT);
		File outputFile	= new File(DATA_DIR, fileName + "-out" + EXT_TXT);
		
		try {
			List<String> lines = Util.getLines(inputFile);
			
			List<String> chordTypes = new ArrayList<String>();

			// 1 3 #5 b7         | 7#5, 7aug, 7+, 7+5+4            | 7+5         | WHOLE-TONE            | *
			
			for (String line: lines) {
				if (! (line.startsWith("//") || line.startsWith("%%") || line.startsWith("##"))) {
					chordTypes.add(line);
				}	
			}			

			Collections.sort(chordTypes);
			
			StringBuffer sb = new StringBuffer();
			for (String chordType: chordTypes) {
				sb.append("   " + chordType + NL);
			}
			
			Util.writeToFile(outputFile, sb.toString(), false, false);
		} catch (Exception e) {
			log.error(e);
		}
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ChordTypes2Parser().sortByChordIntervals("chord-types2");
	}
}
