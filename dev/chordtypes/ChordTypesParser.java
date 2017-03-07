package audio.chords.parser.chordtypes;

import static audio.Constants.BR;
import static audio.Constants.CHORD_TYPES_FILE;
import static audio.Constants.DATA_DIR;
import static audio.Constants.EXT_HTML;
import static audio.Constants.EXT_TXT;
import static audio.Constants.NL;
import static audio.Constants.PIPE_DELIM;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import audio.Util;

/**
 * This class generates an html file using the definitions in CHORD_TYPES_FILE.
 */
public class ChordTypesParser {
	/** The log. */
	private Logger log 							= Logger.getLogger(getClass());
	/** Map of 'chordTypes' using intervalStr as key. */
	private Map<String, ChordType> chordTypes 	= new HashMap<String, ChordType>();
	/** Sorted list of intervalStrs used as keys to 'chordTypes' map. */
	private List<String> integerStrs			= new ArrayList<String>();
	
	public void run() {
		log.debug("begin");
		
		StringBuffer html = new StringBuffer();
		
		html.append("<html>" + NL);
		html.append("<head>" + NL);
		html.append("	<title>Untitled</title>" + NL);
		html.append("<style type='text/css'>" + NL);
		html.append("	body 		{font	: normal 8pt verdana, arial, sans-serif;}" + NL);
		html.append("	td	 		{font	: normal 8pt verdana, arial, sans-serif;}" + NL);
		html.append("   table.data {" + NL);
		html.append("		border-collapse: collapse;" + NL);
		html.append("	}");
		html.append("	table.data tr th {" + NL);
		html.append("	 	background-color : #88f;" + NL);
		html.append("	    border		: 1px solid #ccc;" + NL);
		html.append("		padding		: 3px;" + NL);
		html.append("	    font		: bold 8pt verdana, arial, sans-serif;" + NL);
		html.append("	    white-space	: nowrap;" + NL);
		html.append("	}" + NL);
		html.append("	table.data tr td {" + NL);
		html.append("		font	: normal 8pt verdana, arial, sans-serif;" + NL);
		html.append("		border	: 1px solid #ccc;" + NL);
		html.append("		padding	: 3px;" + NL);
		html.append("	}" + NL);
		html.append("	span.gray {color: #888;}" + NL);
		html.append("</style>" + NL);
		html.append("</head>" + NL);
		html.append("<body>" + NL);

		html.append("<table border='1' class='data'>" + NL);
		html.append("<tr valign='top'>" + NL);
		html.append("<th>integerStr</th>" + NL);
		html.append("<th>symbols</th>" + NL);
		html.append("<th>abcSymbol</th>" + NL);
		html.append("</tr>" + NL);
		
		List<String> lines = Util.getLines(CHORD_TYPES_FILE);

		/*	
		//
		%% ------------------|---------------------------------|-------------|-----------------------|------
		## chordIntervals    | symbols                         | abcSymbol   | scaleType             | notes
		%% ------------------|---------------------------------|-------------|-----------------------|------
		   1 2 5             |                                 | sus2        |                       |
		   1 3 #5            | #5, +                           | aug         | WHOLE-TONE            |
		*/
		for (String line: lines) {
			line = line.trim();
			
			if (	line.startsWith("//") || 
					line.startsWith("%%") ||
					line.startsWith("##")) {
				continue;
			}

			String[] arr = line.split(PIPE_DELIM);
			
			String integerStr	= arr[0].trim();
			String symbols 		= arr[1].trim();
			String abcSymbol 	= arr[2].trim();
					
			if (chordTypes.containsKey(integerStr)) {
				log.error("chordTypes.containsKey(" + integerStr+ ")");
			} else {
				integerStrs.add(integerStr);
				chordTypes.put(integerStr, new ChordType(integerStr, symbols, abcSymbol));
			}

			html.append("<tr valign='top'>" + NL);
			html.append("<td>" + integerStr	+ "</td>" + NL);
			html.append("<td>" + symbols 	+ "</td>" + NL);
			html.append("<td>" + abcSymbol 	+ "</td>" + NL);
			html.append("</tr>" + NL);
		}			
		
		html.append("</table>" + NL);
		
		html.append("</body>" + NL);
		html.append("</html>" + NL);

		String htmlFileName = CHORD_TYPES_FILE.getName().replace(EXT_TXT, EXT_HTML);
		File htmlFile = new File(DATA_DIR, htmlFileName); 	
		Util.writeToFile(htmlFile, html.toString(), false, false);
		
	}
		
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ChordTypesParser().run();
	}

}
