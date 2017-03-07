package audio.chords.parser.scalesyllabus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import audio.Constants;
import audio.Util;

/**
 * This class uses the aebersold chordscale definitions in 
 * 		C:\rob\apps\audio\data\the-scale-syllabus.txt 
 * to generate 'scaleSyllabus.txt' and 'scaleSyllabus.html' files.
 */
public class ScaleSyllabusParser implements Constants {
	/** The log. */
	private Logger log 							= Logger.getLogger(getClass());
	/** Map of 'chordTypes' using intervalStr as key. */
	private List<Chordscale> chordscales 		= new ArrayList<Chordscale>();
	private final File inputFile				= new File(DATA_DIR, "the-scale-syllabus.txt");
	private final File outputFile				= new File(DATA_DIR, "scale-syllabus.txt");
	
	public void run() {
		log.debug("begin");
		
		List<String> lines = Util.getLines(inputFile);

		for (String line: lines) {
			// // CHORD/SCALE SYMBOL | SCALE NAME | WHOLE & HALF STEP CONSTRUCTION | SCALE IN KEY OF C | BASIC CHORD IN KEY OF C
			// //------------------------------------------------------------------------------
			// // FIVE BASIC CATEGORIES
			// //------------------------------------------------------------------------------
			// C | Major | W W H W W W H | C D E F G A B C | C E G B D
			
			line = line.trim();
			log.debug(line);
			
			if (line.startsWith("//")) {
				chordscales.add(new Chordscale(line));
			} else {
				String[] arr = line.split(PIPE_DELIM);
				
				chordscales.add(new Chordscale(
						arr[0].trim(),
						arr[1].trim(),
						arr[2].trim(),
						arr[3].trim(),
						arr[4].trim()));
			}
		}			

		StringBuffer sb = new StringBuffer();
		for (Chordscale chordscale: chordscales) {
			if (chordscale.isText) {
				sb.append(chordscale.text);
			} else {
				sb.append(chordscale.toString());
			}
			sb.append(NL);
		}

		
		Util.writeToFile(outputFile, sb.toString(), false, false);
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ScaleSyllabusParser().run();
	}
}
