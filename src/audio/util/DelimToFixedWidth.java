package audio.util;

import static audio.Constants.DATA_DIR;
import static audio.Constants.EXT_TXT;
import static audio.Constants.NL;
import static audio.Constants.PIPE;
import static audio.Constants.PIPE_DELIM;
import static audio.Constants.SPACE;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import audio.Util;

/**
 * This generic class takes a pipe-delim .txt file and formats the columns into equal widths. 
 * 		abc|def|ghi
 *      jklm|nopq|rstu
 *          ->
 * 		abc |def |ghi
 *      jklm|nopq|rstu
 */
public class DelimToFixedWidth {
	/** The log. */
	private Logger log = Logger.getLogger(getClass());
	
	public void run(String fileName, int numColumns) {
		String msg 		= "";
		File inputFile	= new File(DATA_DIR, fileName + EXT_TXT);
		File outputFile	= new File(DATA_DIR, fileName + "-out" + EXT_TXT);
		
		try {
			List<String> lines = Util.getLines(inputFile);
			int[] colWidths = new int[numColumns];

			// init
			for (int i = 0; i < numColumns; i++) {
				colWidths[i] = 0;
			}
			
			for (String line: lines) {
				// C | Major | W W H W W W H | C D E F G A B C | C E G B D
				
				line = line.trim();
				log.debug(line);
				
				String[] arr = line.split(PIPE_DELIM, -1);
				int arrLen = arr.length;  
				if (arrLen != numColumns) {
					msg = "invalid arrLen=" + arrLen + ": " + line;
					throw new Exception(msg);
				}
					
				for (int i = 0; i < numColumns; i++) {
					String s = arr[i].trim();
					int sLen = s.length();
					if (sLen > colWidths[i]) {
						colWidths[i] = sLen;
					}
				}
			}			

			StringBuffer sb = new StringBuffer();
			for (String line: lines) {
				String[] arr = line.split(PIPE_DELIM, -1);
					
				for (int i = 0; i < numColumns; i++) {
					String s = Util.padWithSpaces(arr[i].trim(), colWidths[i]);
					sb.append(s);
					if (i < numColumns - 1) {
						sb.append(SPACE + PIPE + SPACE);
					}
				}
				sb.append(NL);
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
		String fileName = "chord-types";
		int numColumns = 7;

		//String fileName = "scale-types";
		//int numColumns = 9;

		new DelimToFixedWidth().run(fileName, numColumns);
	}

}
