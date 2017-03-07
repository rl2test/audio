package audio.util;

import static audio.Constants.CHORD_TYPES_FILE;
import static audio.Constants.DATA_DIR;
import static audio.Constants.EXT_HTML;
import static audio.Constants.EXT_TXT;
import static audio.Constants.NL;
import static audio.Constants.PIPE_DELIM;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import audio.Util;

/**
 * This generic class takes a pipe-delim .txt file and formats to html. 
 */
public class DelimToHtml {
	/** The log. */
	private Logger log = Logger.getLogger(getClass());
	
	public void run(String fileName) {
		File inputFile	= new File(DATA_DIR, fileName + EXT_TXT);
		File outputFile	= new File(DATA_DIR, fileName + EXT_HTML);
		StringBuffer sb = new StringBuffer();
		
		try {
			List<String> lines = Util.getLines(inputFile);
		
			sb.append(Util.getHtmlHeader(fileName));
			sb.append("<table class='data'>" + NL);
			
			// get max number of columns
			int maxNumberOfColumns = 0;
			for (String line: lines) {
				line = line.trim();

				String[] arr = line.split(PIPE_DELIM, -1);
				int len = arr.length;
				if (len > maxNumberOfColumns) maxNumberOfColumns = len;
			}			
			
			for (String line: lines) {
				line = line.trim();
				log.debug(line);

				String[] arr = line.split(PIPE_DELIM, -1);
				int len = arr.length;
				
				sb.append("<tr>" + NL);
				if (len == 1) {
					sb.append("<td colspan='" + maxNumberOfColumns + "'>" + arr[0].trim() + "</td>" + NL);
				} else {
					for (int i = 0; i < len; i++) {
						sb.append("<td>" + arr[i].trim() + "</td>" + NL);						
					}
				}

				sb.append("</tr>" + NL);
			}			

			sb.append("</table>" + NL);
			sb.append(Util.getHtmlFooter());

			Util.writeToFile(outputFile, sb.toString(), false, false);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String fileName = "aebersold-nomenclature";

		//String fileName = "chordscale-types";

		String fileName = CHORD_TYPES_FILE.getName().replace(EXT_TXT, "");
		
		new DelimToHtml().run(fileName);
	}

}
