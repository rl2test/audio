package audio.chords.abc;

import static audio.Constants.*;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import audio.Util;

/**
 * This class parses a .chords file with scale-type annotations and generates an 
 * .abc file rendering the scales for each chord symbol in the file
 */
public class ChordsToAbcScales {
	/** The log. */
	private Logger log = Logger.getLogger(getClass());
	
	/**
	 * 
	 */
	public void run(File chordscalesFile, String mode) {
		try {
			List<String> tuneLines = Util.getLines(chordscalesFile);
			StringBuffer sb = new StringBuffer();
			for (String line: tuneLines) {
				if (!line.startsWith("@") && !line.startsWith("!")) {
					sb.append(line);					
				}
			}
			ChordscalesTune chordscalesTune = new ChordscalesTune(
					chordscalesFile.getName().replace(".chords", ""),
					sb.toString());
			
			
			//new File("C:/rob/music/rlp-abc/files/CourseraIntroToImprov/Memories Of Tomorrow.abc")
			String parentDirName = chordscalesFile.getParentFile().getName();
			String abcDirName = parentDirName.replace(" ", ""); 
			File abcDir = new File(ABC_DIR, abcDirName);
			abcDir.mkdirs();
			
			File abcFile = new File(abcDir, chordscalesFile.getName().replace(EXT_CHORDS, EXT_ABC));
			Util.writeToFile(abcFile, chordscalesTune.toAbc(mode));
			
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ChordsToAbcScales().run(
				//new File("C:/rob/music/jazz/chordscales/500 Miles High.chordscales"),
				//new File("C:/rob/music/rlp-abc/files/courseraIntroToImprov/500 Miles High.abc"));
				new File("C:/rob/music/jazz/chords/Coursera Intro To Improv/Memories Of Tomorrow.chords"), "all");
	}

}
