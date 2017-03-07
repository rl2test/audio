package audio.abc1;

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

public class Main {
	/** The log. */
	private Logger log = Logger.getLogger(getClass());
	public final int BARS_PER_LINE = 4;	
	public final File JAZZ_DIR = (System.getProperty("os.version").equals("10.12"))
			? new File("/Users/rlowe/rob/music/jazz")			// wk
			: new File("/Volumes/IOMEGA-1000/Rob/music/jazz");	// hm
	public final File ABC_DIR = new File(JAZZ_DIR, "abc/aebersold");
	public final File CHORDS_DIR = new File(JAZZ_DIR, "chords/Aebersold-Excersies");
	public final String[] majorKeys = {"C", "F", "Bb", "Eb", "Ab", "Db", "F#", "B",  "E",  "A",  "D", "G"}; // used for looping thru all keys
	public final String[] minorKeys = {"A", "D", "G",  "C",  "F",  "Bb", "Eb", "G#", "C#", "F#", "B", "E"}; // used for looping thru all keys
	public AbcTransposer2 transposer = new AbcTransposer2();
	
	
	/**
	 * Transpose an abc file, cycling thru all keys, writing each key to a separate .abc file
	 */
	public void run(String filename) throws Exception {
		File transposeDir = new File(ABC_DIR, filename + "-transposed");
		if (!transposeDir.exists()) {
			transposeDir.mkdir();
		}	
		
		List<String> lines = AbcUtil.getLines(new File(ABC_DIR, filename + ".abc"));
		String fromKey = "C"; // default
		//String fromKey 		= "";
		boolean isMinor 	= false;
		String abc 			= "";
		Header header = new Header();
		
		//log.debug("MAX_INT_VALUE=" + AbcUtil.listToString(AbcMaps.intervalToNotesAbc.get(Bar.MAX_INT_VALUE)));
		
		// parse abc file
		for (String line: lines) {
			if (line.startsWith("%")) {
				// comment 
			} else if (line.startsWith("X:")) {
				header.X = line.substring(2).trim(); 
			} else if (line.startsWith("T:")) {
				header.T = line.substring(2).trim(); 
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
				if (fromKey.length() > 1 && fromKey.substring(1).equals("m")) {
					isMinor = true;
				}
			} else {
				abc	+= line.trim();
			}
		}
		
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
			AbcUtil.writeToFile(new File(transposeDir, filename + ".chords"), chords.toString());
		}
		
		// transpose for each key
		int keyCount = 1;
		String[] keys = (isMinor) ? minorKeys : majorKeys;
		for (String toKey: keys) {
			if (isMinor) {
				toKey += "m"; 
			}
			log.debug("fromKey=" + fromKey + ", toKey=" + toKey);
			barCount = 1;
			String newFilename = filename + "-" + AbcUtil.padInt(keyCount, 2) + "-" + toKey;
			newHeader.T = header.T + " - " + toKey;	
			newHeader.K = toKey;
			StringBuffer sb = new StringBuffer(newHeader.toAbc());
			
			transposer.init(fromKey, toKey);
			for (Bar bar: bars) {
				List<Token> transposedTokens = new ArrayList<Token>();
				for (Token token: bar.tokens) {
					if (token.type == Token.CHORD) {
						transposedTokens.add(new AbcChord(transposer.transposeChord(token.absValue)));
					} else if (token.type == Token.NOTE) {
						AbcNote abcNote = new AbcNote();
						abcNote.setAbsValue(transposer.transposeAbcNote(token.absValue));
						transposedTokens.add(abcNote);
					} else {
						transposedTokens.add(new AbcElement(token.absValue));
					}
				}				
				Bar transposedBar = new Bar(toKey, transposedTokens);
				log.debug(transposedBar.toAbcAbsolute());
				transposedBar.toDisplay();
				log.debug(transposedBar.toAbcDisplay());
					
				sb.append(transposedBar.toAbcDisplay());
				if (barCount % BARS_PER_LINE == 0) {
					sb.append(NL);
				}
				barCount++;
			}

			AbcUtil.writeToFile(new File(transposeDir, newFilename + ".abc"), sb.toString());
			keyCount++;
			//break;
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
			log.debug(" display: " + bar.toAbcDisplay());
			bar.toAbsolute();
			log.debug("absolute: " + bar.toAbcAbsolute());
			log.debug("========================================");
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
				log.debug("chord=" + chord);
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
	
	public static String unalter(String note) {
		return note.replace("_", "").replace("^", "").replace("=", ""); 
	}
	
	public static boolean isAltered(String note) {
		return (note.contains("_") || note.contains("^") || note.contains("="));
	}

	public static String getAlphaValue(String value) {
		return unalter(value).replace(",", "").replace("'", "").toUpperCase();
	}
	
	public static void main(String args[]) {
		String[] filenames = { 
			//"000-29-jazz-rhythms",
			//"001-39-blues",
			//"001-72-tenor-madness-pentatonic-roving-third",
			//"001-72-1-tenor-madness",
			//"001-72-2-pentatonic-blues",
			//"001-72-3-the-roving-third",
			//"003-supplement-03-ii-v-i-progression",
			//"003-supplement-04-ii-v-i-progression",
			"003-the-ii-v-1-progression-05-g-minor-blues",
			//"test",
			//"084-12",
		};
		
		Main main = new Main();
		for(String filename: filenames) {
			try {
				main.run(filename);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}	
}

/**
 * Transpose a list of abc phrases, cycling thru all keys, writing each phrase to a single abc file
 * TODO implement for revised structure 
 */
/*
public void cyclePhrase() {

	String[] abcs = {
		//"\"G7\"dfed gfed|\"C\"c4z4|",
		//"\"G7\"DFAF B_AGF|\"C\"E4z4|",
		//"\"C\"Bc2B- B_BA_A|\"Dm\"G_GFE \"G7\"_E^CD=C |\"C\"B,8-|B,8|",
	};
	String[] titles = {
		//"\"G7\"dfed gfed|\"C\"c4z4|",
		//"\"G7\"DFAF B_AGF|\"C\"E4z4|",
		"001-32a-Chromaticism",
	};
	String transposeFrom = "C"; // default
	
	String X = "1"; 
	String T = ""; 
	String S = "";
	String M = "4/4"; 
	String L = "1/8"; 
	String Q = "1/4=120"; 
	String K = transposeFrom; 
	Header header = new Header(X, T, S, M, L, Q, K);
	String chordsHeader = "@4|90-180|5" + NL + "!K: C" + NL;
	
	int count = 0;
	for (String abc: abcs) {
		String filename = titles[count];
		header.T = filename;

		//C|Dm,G7|C||
		StringBuffer chordsBuffer = new StringBuffer(chordsHeader);	
		
		
		// split into an array of bars	
		List<Bar> bars = getBars(transposeFrom, abc);
		
		int barCount = 1;
		StringBuffer sb = new StringBuffer(header.toAbc());
		
		// transpose for each key
		for (String transposeTo: keys) {
			Transposer transposer = new Transposer(transposeFrom, transposeTo);
			
			for (Bar bar: bars) {
				String chords = "";	
				List<Token> transposedTokens = new ArrayList<Token>();
				for (Token token: bar.tokens) {
					if (token.type == Token.CHORD) {
						String chord = transposer.transposeChord(token.value);
						//log.debug("chord=" + chord);
						transposedTokens.add(new Token(chord, Token.CHORD));
						chords += chord + COMMA + SPACE;
					} else if (token.type == Token.NOTE) {
						Token newToken = new Token();
						newToken.setValue(transposer.transposeAbcNote(token.value));
						transposedTokens.add(newToken);
					} else {
						transposedTokens.add(new Token(token.value));
					}
				}				
				Bar transposedBar = new Bar(transposeFrom, transposedTokens); // note: key is set to 'default "C"' instead of 'key' because there is no key signature in this context
				//log.debug(transposedBar.toAbcDeoptimized());
				transposedBar.optimize();
				//log.debug(transposedBar.toAbcOptimized());
					
				sb.append(transposedBar.toAbcOptimized());
					if (barCount % BARS_PER_LINE == 0) {
					sb.append(NL);
				}
				barCount++;
				if (chords.length() > 2) {
					chords = chords.substring(0, chords.length() - 2);	
				}
				log.debug("chords=" + chords);
				chordsBuffer.append(chords + PIPE);
			}
			chordsBuffer.append(NL);
			
		}
		Util.writeToFile(new File(ABC_DIR, filename + ".abc"), sb.toString());
		Util.writeToFile(new File(CHORDS_DIR, filename + ".chords"), chordsBuffer.toString());
	}
}
*/

//new AbcTransposer().cyclePhrase();

/* IN PROGRESS
public void scalePattern() {
	// a 1 -1 1 3 	
	// a 1 1L 1 3
	// a 1L 1 3 1
	// a (1 1L 1)3 (3 3L 3)3
	// d 1L 1 -1 -3
	//G, - d'
}
*/

/*
Properties p = System.getProperties();
Enumeration pKeys = p.keys();
while (pKeys.hasMoreElements()) {
    String pKey = (String) pKeys.nextElement();
    String value = (String) p.get(pKey);
    System.out.println(pKey + "=" + value);
}
log.debug(System.getProperty("os.version"));
*/

/*
java.runtime.name=Java(TM) SE Runtime Environment
sun.boot.library.path=/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/jre/lib
java.vm.version=23.25-b01
gopherProxySet=false
java.vm.vendor=Oracle Corporation
java.vendor.url=http://java.oracle.com/
path.separator=:
java.vm.name=Java HotSpot(TM) 64-Bit Server VM
file.encoding.pkg=sun.io
user.country=US
sun.java.launcher=SUN_STANDARD
sun.os.patch.level=unknown
java.vm.specification.name=Java Virtual Machine Specification
user.dir=/Users/rlowe/rob/apps/audio
java.runtime.version=1.7.0_25-b15
java.awt.graphicsenv=sun.awt.CGraphicsEnvironment
java.endorsed.dirs=/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/jre/lib/endorsed
os.arch=x86_64
java.io.tmpdir=/var/folders/0s/r9hj5gl919s9gtnxwlb71cc80000gs/T/
line.separator=

java.vm.specification.vendor=Oracle Corporation
os.name=Mac OS X
sun.jnu.encoding=US-ASCII
java.library.path=/Users/rlowe/Library/Java/Extensions:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:.
java.specification.name=Java Platform API Specification
java.class.version=51.0
sun.management.compiler=HotSpot 64-Bit Tiered Compilers
os.version=10.8.4
http.nonProxyHosts=local|*.local|169.254/16|*.169.254/16
user.home=/Users/rlowe
user.timezone=
java.awt.printerjob=sun.lwawt.macosx.CPrinterJob
file.encoding=UTF-8
java.specification.version=1.7
java.class.path=/Users/rlowe/rob/apps/audio/bin:/Users/rlowe/rob/apps/lib/commons-io-2.0.jar:/Users/rlowe/rob/apps/lib/log4j.jar
user.name=rlowe
java.vm.specification.version=1.7
sun.java.command=audio.abc.AbcTransposer
java.home=/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/jre
sun.arch.data.model=64
user.language=en
java.specification.vendor=Oracle Corporation
awt.toolkit=sun.lwawt.macosx.LWCToolkit
java.vm.info=mixed mode
java.version=1.7.0_25
java.ext.dirs=/Users/rlowe/Library/Java/Extensions:/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/jre/lib/ext:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java
sun.boot.class.path=/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/jre/lib/resources.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/jre/lib/sunrsasign.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/jre/lib/jsse.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/jre/lib/jce.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/jre/lib/charsets.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/jre/lib/jfr.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/jre/lib/JObjC.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/jre/classes
java.vendor=Oracle Corporation
file.separator=/
java.vendor.url.bug=http://bugreport.sun.com/bugreport/
sun.io.unicode.encoding=UnicodeBig
sun.cpu.endian=little
socksNonProxyHosts=local|*.local|169.254/16|*.169.254/16
ftp.nonProxyHosts=local|*.local|169.254/16|*.169.254/16
sun.cpu.isalist= 
 */
