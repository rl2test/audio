package audio.tabs;

import static audio.Constants.*;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import audio.Transposer;
import audio.Util;

public class UltimateGuitar {
	/** The log. */
	private Logger log 					= Logger.getLogger(getClass());
	private final String BASE_URL_STR 	= "http://tabs.ultimate-guitar.com";
	private final static File TAB_DIR 	= new File("c:/rob/music/tabs");
	private final String SUFFIX			= "_crd.htm";
	private final String EXT			= ".crd"; // local file extension
	private final String CHORD_TOKEN	= "@"; 		
	private final String TEXT_TOKEN		= "%";
	
	static {
		if (!TAB_DIR.exists()) {
			TAB_DIR.mkdirs();
		}
	}

	public void run(String pathStr, String songName) {
		String fileName		= songName + SUFFIX;
		String urlStr 		= BASE_URL_STR + FS + pathStr + FS + fileName;
		File file 			= new File(TAB_DIR, songName + EXT);
		StringBuffer sb 	= new StringBuffer();
		List<String> lines 	= Util.getLinesFromUrl(urlStr, false, false);
		boolean parse 		= false;
		int preCount 		= 0;
		
		for (String line: lines) {
			if (line.trim().startsWith("<pre>")) {
				preCount++;
				if (preCount == 2) {
					parse = true;	
				}
			}
			if (line.trim().startsWith("</pre>") && parse == true) {
				parse = false;	
			}

			if (parse) {
				if (line.contains("<span>")) {
					line = CHORD_TOKEN + line.replaceAll("\\<.*?>","");	// strip tags
				} else {
					if (line.contains("<pre>")) {
						line = line.replaceAll("\\<.*?>","");	// strip tags
						line = line.trim();
					}
					line = TEXT_TOKEN + line;
				}
				log.debug(line);
				sb.append(line + NL);
			}
		}
		Util.writeToFile(file, sb.toString());
	}		
	
	public void transpose(String pathStr, String songName, String from, String to) {
		if (	!from.equals("") &&
				!to.equals("") && 
				!to.equals(from)) {
			List<String> lines 		= Util.getLines(new File(TAB_DIR, songName + EXT));
			StringBuffer sb 		= new StringBuffer();
			File file				= new File(TAB_DIR, songName + DASH + to + EXT);
			Transposer transposer	= new Transposer(from, to);
			
			for (String line: lines) {
				line = line.replace(TAB, "    ");
				String newLine = "";
				if (line.startsWith(CHORD_TOKEN)) {
					line = line.substring(1); // remove token
					// parse line, retain spaces and extract and transpose chord symbols
					String spaces = "";
					String chord = "";
					for (int i = 0; i < line.length(); i++) {
						String s = line.substring(i, i + 1);
						if (s.equals(" ")) {
							if (!chord.equals("")) {
								newLine += transposer.transposeChord(chord);
								chord = ""; // reset
							}
							spaces += " ";
						} else {
							newLine += spaces;
							spaces = ""; //reset
							chord += s;
						}
					}
					if (!chord.equals("")) {
						newLine += chord;
					}
					if (!spaces.equals("")) {
						newLine += spaces;
					}
					
				} else if (line.startsWith(TEXT_TOKEN)) {
					newLine = line.substring(1); // remove token
				} else {
					newLine = line;
				}
				sb.append(newLine + NL);
			}
			Util.writeToFile(file, sb.toString());
		}
	}	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UltimateGuitar ug = new UltimateGuitar();
		
		//tabs.ultimate-guitar.com/r/ray_charles/georgia_on_my_mind_crd.htm?
		//ug.run("r/ray_charles", "georgia_on_my_mind");
		
		ug.transpose("r/ray_charles", "georgia_on_my_mind", "F", "C");

	}	
}