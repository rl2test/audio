package audio.chords;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import audio.Util;

/*
-------------------------------------------------------------------------------- 
3. Information fields

The information fields are used to notate things such as composer, meter, etc, 
in fact anything that isn't music. 

Any line beginning with a letter in the range A-Z or a-z and immediately 
followed by a colon is to be interpreted as a field. Many of these field 
identifiers are currently unused, so programs that comply with this standard 
should ignore the occurrence of information fields not defined here. This will 
make it possible to extend the number of information fields in the future. 
Some fields are permitted only in the file or tune header and some only in the 
body, while others are allowed in both locations. Field identifiers A-G and a-g 
will not be permitted in the body to avoid confusion with note symbols. 

Note that if you are intending to use the notation solely for transcribing 
(rather than documenting) tunes, you can ignore most of of the information 
fields as all you really need are the X (reference number), T (title), 
M (meter), L (unit note length) and K (key) fields. If applicable you could add 
a C (composer), an A (author of lyrics) and a w (words) field. I have included 
a full list of fields however, for those who wish to understand tunes 
transcribed by other users. 

The table illustrates how the fields may be used in the tune header and whether 
they may also be used in the tune body (see section Use of fields within body 
for details) or in the file header (see section File structure). 

-------------------------------------------------------------------------------- 
Field name               header body file type Examples and notes 
----- ------------------ ------ ---- ---- ---- ---------------------------------
A:    author of lyrics   yes         yes  S    A:Walter Raleigh    
B:    book               yes         yes  S    B:O'Neills   
C:    composer           yes         yes  S    C:Robert Jones, C:Trad.  
D:    discography        yes         yes  S    D:Chieftains IV    
F:    file url           yes         yes  S    F:http://a.b.c/file.abc  
G:    group              yes         yes  S    G:flute      
H:    history            yes         yes  S    H:This tune said to...  
I:    instruction        yes    yes  yes  I    I:score (SA) (TB) 
K:    key                last   yes       I    K:G, K:Dm, K:AMix   
L:    unit note length   yes    yes       I    L:1/4, L:1/8    
M:    meter              yes    yes  yes  I    M:3/4, M:4/4    
m:    macro              yes    yes  yes  I    m: ~n2 = (3o/n/m/ n  
N:    notes (annotation) yes    yes  yes  S    N:see also O'Neills - 234 
O:    origin             yes         yes  S    O:UK, Yorkshire, Bradford 
P:    parts              yes    yes       I    P:A, P:ABAC, P:(A2B)3  
Q:    tempo              yes    yes       I    Q:"allegro" 1/4=120  
R:    rhythm             yes    yes  yes  S    R:R, R:reel    
r:    remark             yes    yes  yes  -    r:I love ABC    
S:    source             yes         yes  S    S:collected in Brittany  
s:    symbol line               yes       I    s: +pp+ ** +f+    
T:    title              second yes  yes  S    T:Paddy O'Rafferty  
U:    user defined       yes    yes  yes  I    U: T = +trill+    
V:    voice              yes    yes       I    V:4 clef=bass    
W:    words              yes    yes       S    lyrics after tune   
w:    words                     yes       S    lyrics aligned with tune  
X:    reference number   first            I    X:1, X:2      
Z:    transcriber        yes         yes  S    Z:John Smith, j.s@aol.com   
-------------------------------------------------------------------------------- 

Fields of type S accept free text in the form of an ABC string as argument. 
Fields of type I expect a special instruction syntax which will be detailed 
below. The contents of the remark field will be totally ignored. 

The following table indicates whether the field contents should be appended or 
replaced, if a certain field occurs multiple times in the same tune. Some fields 
have a special rule. 
A:   append                    Q:   replace
B:   append                    R:   append (if in header)
C:   append                    R:   replace (in body)
D:   append                    S:   append
F:   replace                   s:   append
G:   append                    T:   append  (if in header)
H:   append                    T:   begin new section (in body)
K:   replace                   U:   replace
L:   replace                   V:ID replace (if in header)
M:   replace                   V:ID switch to indicated voice (in body)
m:   replace                   W:   append
N:   append                    w:   append
O:   append                    X:   only once per tune
P:   replace (if in header)    Z:   append
P:ID only once (in body)
 */	

/**
 * This class contains the main() method for this package.
 * 
 * This class processes a '.chords' file and writes the result to an '.abc' file. 
 * Individual chords are translated into chordscales using a specified system. 
 * Currently Mymit ('A Beginner's Approach to Jazz Improvisation' ) and Aebersold 
 * ('THE SCALE SYLLABUS') are supported. It first parses the CHORD_TYPES_FILE 
 * and builds an array of chordType objects. These objects are then used 
 * when parsing the input file. The scales written out to the resulting '.abc' 
 * file may be used to improvise on the tune.
 * 
 * The .abc file is not intended for playback - it is primarily for reading. 
 * 
 * TODO generate an abc playback file - see 'audio.gui.Tune.saveAsAbc()'.
 */
public class ChordscaleWriter implements ChordsConstants {
	/** The log. */
	private Logger log 											= Logger.getLogger(getClass());
	/** The output dir for writing the resulting '.abc' file. */
	public static final File CHORDSCALES_DIR					= new File(MUSIC_DIR, "jazz/chordscales");
	/** Map of the chordscales, using chordStr as the key */
	public Map<String, Scale> chordscale1s 				= new HashMap<String, Scale>();
	/** X-count used when format is OUTPUT_TO_SINGLE_FILE. */
	public static int xCount									= 1;

	/**
	 * The file param may be either a file or a dir. If it is a dir, then all 
	 * files in the dir will be processed and the output written to the same 
	 * '.abc' file, otherwise an individual '.abc' file will be created.
	 * 
	 * @param chordsFileName
	 * @param file
	 * @param system  
	 */
	public void write(File file, int outputFormat) throws Exception {
		log.debug("file.getAbsolutePath()=" + file.getAbsolutePath());
		
		if (file.isDirectory()) {
			List<File> files = Util.getFiles(file, CHORDS_FILE_FILTER);
			for (File chordsFile: files) {
				// recursive call
				write(chordsFile, outputFormat);
			}
		} else {
			writeChordscales(file, outputFormat);
		}
	}
	
	/**
	 * @param inputFile the input '.chords' file
	 * @param outputFormat OUTPUT_TO_SINGLE_FILE | OUTPUT_TO_MULTIPLE_FILES 
	 */
	public void writeChordscales(File inputFile, int outputFormat) throws Exception {
		String inputFileName = inputFile.getName();
		log.debug("inputFileName=" + inputFileName);
		
		File inputParentDir = inputFile.getParentFile();
		String inputParentDirName = inputParentDir.getName();

		// Define the chordscalesDir, which may be either the top-level chordscalesDir
		// or a sub-dir.
		File chordscalesDir = null;
		if (outputFormat == OUTPUT_TO_SINGLE_ABC_FILE) {
			chordscalesDir = CHORDSCALES_DIR;
		} else {
			File dir = new File(CHORDSCALES_DIR, inputParentDirName);
			if (!dir.exists()) {
				dir.mkdir();
			}
			chordscalesDir = dir;
		}
		
		// define the chordscales file name
		String chordscalesFileName = (outputFormat == OUTPUT_TO_SINGLE_ABC_FILE)
				? inputParentDirName + EXT_ABC
				: inputFileName.replace(EXT_CHORDS, "") + EXT_ABC;
		
		// define the chordscales file
		File chordscalesFile = new File(
				chordscalesDir, 
				chordscalesFileName);
		
		// the abc file content buffer
		StringBuffer sb = new StringBuffer();

		/*
		T: Georgia On My Mind
		C: Hoagy Carmichael
		A: Stuart Gorell
		S: Dave Schiff
		L: 1/4
		M: 4/4
		Q: 1/4=70
		K: C
		C | | Am | Fm | C, A7 | Dm, G7 | {C | :2}
		*/
		
		sb.append("X:" + xCount + NL);
		if (outputFormat == OUTPUT_TO_SINGLE_ABC_FILE) {
			xCount++;
		}
		
		Tune tune = new Tune(inputFile);
//		for (String infoField: tune.infoFields) {
//			if (infoField.startsWith("T:")) {
//				infoField += " (chordscales)";
//			}
//			sb.append(infoField + NL);
//		}
		sb.append(tune.info.toAbc());
		
		for (Part part: tune.parts) {
			// TODO
			/*
			String chordSequence = part.chordSequence;
			String[] bars = ChordsUtil.getBars(chordSequence);
			log.debug("part.name=" + part.name + ", bars.length=" + bars.length);

			sb.append("P:" + part.name + NL);
			
			String lastBar = "";
			int barCount = 0;
			int numBars = bars.length;
			for (String bar: bars) {
				if (lastBar.equals(bar)) {
					sb.append(REST_4);
				} else {
					if (bar.length() > 0) {
						if (bar.contains(",")) {
							String[] chords = bar.split(",");
							for (String chord: chords) {
								sb.append(getChordscale(chord).abc);
							}
						} else {
							String chord = bar;
							sb.append(getChordscale(chord).abc);
						}
					} else {
						// empty bar, so add bar rest; note: empty bars must 
						// initially contain at least one space in the chords file  
						sb.append(REST_4);
					}
				}

				barCount++;
				
				if (barCount == numBars) {
					if (part.repeat) {
						sb.append(BARLINE_REPEAT);
					} else {
						sb.append(BARLINE_DOUBLE);
					}
				} else {
					sb.append(BARLINE);
				}
				
				if (barCount % 4 != 0 && barCount != numBars) {
					sb.append(BS);
				}
				
				sb.append(NL);
				lastBar = bar;
			}	
			*/			
		}
		
		// write file info to 'W' info field
		String w = (outputFormat == OUTPUT_TO_SINGLE_ABC_FILE)
				? inputParentDirName
				: inputParentDirName + " / " + chordscalesFileName;

		sb.append("W: " + w + NL);
		
		// Write the sb to the chordscales abc file. If outputFormat is OUTPUT_TO_SINGLE_FILE
		// then the isAppend and addNewline params will be set to true.
		Util.writeToFile(
				chordscalesFile,
				sb.toString(), 
				(outputFormat == OUTPUT_TO_SINGLE_ABC_FILE), 
				(outputFormat == OUTPUT_TO_SINGLE_ABC_FILE));		
	}

	/**
	 * @param chord
	 * @return a chordScale obj corresponding to the given chord - if the chordscales
	 *         map does not already contain the chordscale then a new one will be created,
	 *         otherwise the existing one will be returned 
	 */
	private Scale getChordscale1(String chord) {
		if (!chordscale1s.containsKey(chord)) {
			Scale chordscale1 = new Scale(chord);
			chordscale1s.put(chord, chordscale1);
		}

		return chordscale1s.get(chord);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ChordscaleWriter cw = new ChordscaleWriter();

			// use this block:
			
			//**********************************************************************
			// generate chordscales for an individual '.chords' file
			//File chordsFile = new File(JAZZ_CHORDS_DIR, "Beginner's Approach/Ch 5 - Ex 2" + EXT_CHORDS);
			//File chordsFile = new File(JAZZ_CHORDS_DIR, "Jazz Fake Book/Autumn Leaves" + EXT_CHORDS);
			//File chordsFile = new File(JAZZ_CHORDS_DIR, "jeffSetlist/Autumn Leaves" + EXT_CHORDS);
			//File chordsFile = new File(JAZZ_CHORDS_DIR, "The Real Book/Autumn Leaves" + EXT_CHORDS);

			//cw.write(chordsFile);   
			//**********************************************************************
			
			// or this block:

			//**********************************************************************
			// generate chordscales for all files in a given dir

			// use perl list_files_in_dir.pl to generate list of dirs in JAZZ_CHORDS_DIR
			String[] dirs = {
				////"Aebersold", // these books include chordscales
				//"Aebersold - FQBK-handbook - Blues",
				////"Aebersold - Vol 42 - Blues In All Keys", // these books include chordscales
				//"Aebersold - Vol 42 - Blues In All Keys - Intro",
				//"Beginner's Approach",
				//"Beginning Jazz Improvisation",
				//"Blues Progressions",
				//"Creative Jazz Improvisation",
				//"Dan Haerle",
				//"Googlebooks - Jazz Improvisation",
				//"Jazz Fake Book",
				//"Jeff Setlist",
				//"Misc",
				//"Test",
				"The Real Book",
			};

			// OUTPUT_TO_SINGLE_ABC_FILE writes all files in dir to a single '.abc' file
			//int outputFormat = OUTPUT_TO_SINGLE_ABC_FILE;  
			
			// OUTPUT_TO_INDIVIDUAL_ABC_FILES writes all files in dir to a single '.abc' file
			int outputFormat = OUTPUT_TO_INDIVIDUAL_ABC_FILES;
			
			for (String dir: dirs) {
				if (outputFormat == OUTPUT_TO_SINGLE_ABC_FILE) {
					File chordscalesFile = new File(CHORDSCALES_DIR, dir + EXT_ABC);
					if (chordscalesFile.exists()) {
						// delete the existing chordscalesFile, as output will be appended 
						chordscalesFile.delete();
					}
				}
				File chordsDir = new File(JAZZ_CHORDS_DIR, dir);	
				cw.write(chordsDir, outputFormat);
			}
			//**********************************************************************
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
