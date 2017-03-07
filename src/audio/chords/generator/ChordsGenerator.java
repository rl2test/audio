package audio.chords.generator;

import static audio.Constants.ALPHABET;
import static audio.Constants.BARLINE;
import static audio.Constants.EXT_CHORDS;
import static audio.Constants.MUSIC_DIR;
import static audio.Constants.NL;
import static audio.Constants.UNDEF;

import java.io.File;

import org.apache.log4j.Logger;

import audio.Util;

/**
 * This class generates '.chords' files.
 */
public class ChordsGenerator {
	/** The log. */
	private Logger log 								= Logger.getLogger(getClass());

	public Pitch[] pitches 							= {
			new Pitch("B#  C  Dbb"),
			new Pitch("B## C# Db "),
			new Pitch("C## D  Ebb"),
			new Pitch("D#  Eb Fbb"),
			new Pitch("D## E  Fb "),
			new Pitch("E#  F  Gbb"),
			new Pitch("E## F# Gb "),
			new Pitch("F## G  Abb"),
			new Pitch("G#     Ab "),
			new Pitch("G## A  Bbb"),
			new Pitch("A#  Bb Cbb"),
			new Pitch("A## B  Cb ") };	

	public String[] alphaValues 					= {
			"C", "D", "E", "F", "G", "A", "B" };
	
	private static String composer = "Aebersold";
	
	private static String[] letters = new String[26];
	
	static {
		for (int i = 0; i < 26; i++) {
			letters[i] = ALPHABET.substring(i, i + 1);
		}
	}
	private File jazzChordsDir = new File(MUSIC_DIR, "jazz/chords");
	
	/**
	 */
	public void generate003_1(String source) {
		String title 	= "01 - II-V7-I All Major Keys";

		String[] roots = {"C", "Bb", "Ab", "Gb", "E", "D", "Db", "B", "A", "G", "F", "Eb"};

		StringBuffer sb = new StringBuffer();
		
		sb.append(getHeader(title, source));
		
		for (String root: roots) {
			String rootAlpha = root.substring(0, 1);
			int rootAlphaPitchIndex = getAlphaValueIndex(rootAlpha);
			String twoAlpha = alphaValues[(rootAlphaPitchIndex + 1) % 7]; 
			String fiveAlpha = alphaValues[(rootAlphaPitchIndex + 4) % 7];
			
			log.debug(rootAlpha + " " + twoAlpha + " " + fiveAlpha);
			
			int rootPitchIndex = getPitchIndex(root);
			Pitch twoPitch = pitches[(rootPitchIndex + 2) % 12];
			Pitch fivePitch = pitches[(rootPitchIndex + 7) % 12];
			
			String two = twoPitch.get(twoAlpha);
			String five = fivePitch.get(fiveAlpha);
			
			log.debug(root + " " + two + " " + five);
			
			sb.append(two + "m" + BARLINE + five + "7" + BARLINE + "{" + root + "maj7" + BARLINE + ":2}" + NL);
		}
		
		// get dir to write to
		File dir 		= new File(jazzChordsDir, composer + " - " + source);
		
		// get file to write to
		File file 		= new File(dir, title + EXT_CHORDS);

		// write to file
		Util.writeToFile(file, sb.toString());
	}
	
	/**
	 */
	public void generate003_2(String source) {
		String title 	= "02 - Random II-V7 Progressions";

		String[] twos = {"C", "Bb", "E", "D", "Ab", "B", "A", "G", "Eb", "F#", "F", "C#"};

		StringBuffer sb = new StringBuffer();
		
		sb.append(getHeader(title, source));
		
		for (String two: twos) {
			String twoAlpha = two.substring(0, 1);
			int twoAlphaPitchIndex = getAlphaValueIndex(twoAlpha);
			String fiveAlpha = alphaValues[(twoAlphaPitchIndex + 3) % 7];
			
			log.debug(twoAlpha + " " + fiveAlpha);
			
			int twoPitchIndex = getPitchIndex(two);
			Pitch fivePitch = pitches[(twoPitchIndex + 5) % 12];
			
			String five = fivePitch.get(fiveAlpha);
			
			log.debug(two + " " + five);
			
			sb.append("{" + two + "m" + BARLINE + five + "7" + BARLINE + ":2}" + NL);
		}

		// get dir to write to
		File dir 		= new File(jazzChordsDir, composer + " - " + source);
		
		// get file to write to
		File file 		= new File(dir, title + EXT_CHORDS);

		// write to file
		Util.writeToFile(file, sb.toString());
	}
	
	/**
	 */
	public void generate003_3(String source) {
		String title 	= "03 - V7+9-I All Keys";

		String rootsStr = "A, Fm, Abm, G, Bb, Em, F#, Cm, B, C#m, Dm, Ebm";
		String[] roots = rootsStr.split(", ");

		StringBuffer sb = new StringBuffer();
		
		sb.append(getHeader(title, source));
		
		int i = 0;
		for (String root: roots) {
			sb.append("P:" + letters[i] + NL);
			String rootMode = root.endsWith("m") ? "m" : "";
			
			if (rootMode.equals("m")) {
				root = root.substring(0, root.length() - 1); 
			}
			
			String rootAlphaValue = root.substring(0, 1);
			int rootAlphaValueIndex = getAlphaValueIndex(rootAlphaValue);
			String fiveAlphaValue = alphaValues[(rootAlphaValueIndex + 4) % 7];
			
			log.debug(rootAlphaValue + " " + fiveAlphaValue);
			
			int rootPitchIndex = getPitchIndex(root);
			Pitch fivePitch = pitches[(rootPitchIndex + 7) % 12];
			
			String five = fivePitch.get(fiveAlphaValue);
			
			log.debug(root + " " + five);
			
			sb.append("[{" + five + "7+9" + BARLINE + ":2}{" + root + rootMode + BARLINE + ":2} :4]" + NL);
			i++;
		}
		
		// get dir to write to
		File dir 		= new File(jazzChordsDir, composer + " - " + source);
		
		// get file to write to
		File file 		= new File(dir, title + EXT_CHORDS);

		// write to file
		Util.writeToFile(file, sb.toString());
	}	
	
	/**
	 */
	public void generate003_4(String source) {
		String title 	= "04 - II-V7+9-I All Minor Keys";

		String[] roots = {"C", "Bb", "Ab", "F#", "E", "D", "C#", "B", "A", "G", "F", "Eb"};

		StringBuffer sb = new StringBuffer();
		
		sb.append(getHeader(title, source));
		sb.append("% half-diminished to diminished whole-tone resolving to tonic" + NL);
		
		int i = 0;
		for (String root: roots) {
			sb.append("P:" + letters[i] + NL);
			String rootAlpha = root.substring(0, 1);
			int rootAlphaPitchIndex = getAlphaValueIndex(rootAlpha);
			String twoAlpha = alphaValues[(rootAlphaPitchIndex + 1) % 7]; 
			String fiveAlpha = alphaValues[(rootAlphaPitchIndex + 4) % 7];
			
			log.debug(rootAlpha + " " + twoAlpha + " " + fiveAlpha);
			
			int rootPitchIndex = getPitchIndex(root);
			Pitch twoPitch = pitches[(rootPitchIndex + 2) % 12];
			Pitch fivePitch = pitches[(rootPitchIndex + 7) % 12];
			
			String two = twoPitch.get(twoAlpha);
			String five = fivePitch.get(fiveAlpha);
			
			log.debug(root + " " + two + " " + five);
			
			sb.append("[" + two + "m7b5" + BARLINE + five + "7+9" + BARLINE + "{" + root + "m" + BARLINE + ":2} :2]" + NL);
			i++;
		}
		
		// get dir to write to
		File dir 		= new File(jazzChordsDir, composer + " - " + source);
		
		// get file to write to
		File file 		= new File(dir, title + EXT_CHORDS);

		// write to file
		Util.writeToFile(file, sb.toString());
	}	
	
	/**
	 * @param title
	 * @param source
	 * @return the .chords file header
	 */
	private String getHeader(String title, String source) {
		StringBuffer sb = new StringBuffer();
		/*
T: 01 II-V7-I All Major Keys
C: Aebersold
S: Vol 003 - The II-V7-I Progression
L: 1/4
M: 4/4
Q: 1/4=90
K: Ab
Am7 | D7 | Gmaj7 | Cmaj7 | F#m7b5 | B7 | Em | Em | 
		 */
		
		sb.append("T:" + title 		+ NL);
		sb.append("C:" + composer 	+ NL);
		sb.append("S:" + source 	+ NL);
		sb.append("L: 1/4" 			+ NL);
		sb.append("M: 4/4" 			+ NL);
		sb.append("Q: 1/4=60" 		+ NL);
		sb.append("K: C" 			+ NL);		

		return sb.toString();
	}
	

	/**
	 * @param s
	 * @return alphaValueIndex of alphaValue 
	 */
	private int getAlphaValueIndex(String s) {
		int i = 0;
		for (String alphaPitch: alphaValues) {
			if (alphaPitch.equals(s)) {
				return i;
			}
			i++;
		}
		return UNDEF;
	}
	
	/**
	 * @param s
	 * @return pitchIndex of s
	 */
	private int getPitchIndex(String s) {
		int i = 0;
		for (Pitch pitch: pitches) {
			if (pitch.contains(s)) {
				return i;
			}
			i++;
		}
		return UNDEF;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ChordsGenerator cg = new ChordsGenerator();
		
		String source 	= "Vol 003 - The II-V7-I Progression";

		cg.generate003_1(source); 
		cg.generate003_2(source);
		cg.generate003_3(source);
		cg.generate003_4(source);
	}
}
