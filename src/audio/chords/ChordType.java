package audio.chords;

import static audio.Constants.ALPHABET_TOKENS;
import static audio.Constants.COMMA;
import static audio.Constants.NL;
import static audio.Constants.PIPE_DELIM;

import java.util.ArrayList;
import java.util.List;

//import org.apache.log4j.Logger;


import audio.Maps;
import audio.Util;

/**
 * This class represents a chord type, as defined in the CHORD_TYPES_FILE.
 */
public class ChordType {
	/** The log. */
	//private Logger log 				= Logger.getLogger(getClass());

	// ##name|altName|symbol|notes||||
	// values from file
	public String name 				= "";
	public String symbol 			= "";
	public String notesStr 			= "";
	
	// derived values
	public List<String> notes 		= new ArrayList<String>();
	public List<Integer> integers 	= new ArrayList<Integer>();
	
	public String intervalsStr 		= "";
	public List<Integer> intervals	= new ArrayList<Integer>();
	public String group 			= "";
	public static int numberOfHtmlColumns 
									= 5;
	
	/**
	 * The constructor using a CHORD_TYPES_FILE definition.
	 * 
	 * @param line
	 */
	public ChordType (String line) {
		/*
		##name                      | altName                     | symbol  | notes             ||||
		//7ths                      |                             |         |                   ||||                   
		7th                         | dominant seventh            | 7       | C-E-G-Bb          ||||                   
		*/
		
		//log.debug(line);
		String[] arr = line.split(PIPE_DELIM, -1);

		int i = 0;
		name 			= arr[i++].trim();
		symbol 			= arr[i++].trim();
		notesStr 		= arr[i++].trim();

		
		String[] notesArr = notesStr.split("-");
		for (String note: notesArr) {
			notes.add(note);
		}
		
		int lastInteger = 0;
		for (String note: notes) {
			int integer = Maps.noteToInterval.get(note);
			if (integer < lastInteger) {
				integer += 12;
			}
			lastInteger = integer;
			integers.add(integer);
			
			intervalsStr += noteToInterval(note) + ", ";
		}
		
		//log.debug(this);
	}	

	/**
	 * @param note
	 * @return
	 */
	private String noteToInterval(String note) {
		// Bbb -> bb7
		
		//public static final String[] ALPHBETIC_TOKENS 		= {"C", "D", "E", "F", "G", "A", "B"};
		String alphabetToken = note.substring(0,1);
		String modifier = ""; 
		if (note.length() > 1) {
			modifier = note.substring(1);
		}
		
		int alphabetIndex = Util.getIndex(ALPHABET_TOKENS, alphabetToken);
		int interval = alphabetIndex + 1; 
		
		int lastInterval = 0;
		if (intervals.size() > 0) {
			lastInterval = intervals.get(intervals.size() - 1);
		}

		//log.debug(interval + " " + lastInterval);
		if (interval < lastInterval) {
			interval += 7;
		}
		intervals.add(interval);
		
		return modifier + interval;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(NL);
		sb.append("name 			= " + name + NL);
		sb.append("symbol 			= " + symbol + NL);
		sb.append("notesStr 		= " + notesStr + NL);
		for (int integer: integers) {
			sb.append(integer + ", ");
		}
		return sb.toString();
	}	
	
	/**
	 * @return htmlTableHeaderRow
	 */
	public static String getHtmlTableHeaderRow() {
		StringBuffer sb = new StringBuffer(NL);

		sb.append("  <tr>" + NL);
		sb.append("    <th>name</th>" + NL);
		sb.append("    <th>symbol</th>" + NL);
		sb.append("    <th>notesStr</th>" + NL);
		sb.append("    <th>intervalsStr</th>" + NL);
		sb.append("    <th>integers</th>" + NL);
		sb.append("  </tr>" + NL);
		
		return sb.toString();
		
	}
	
	/**
	 * @return htmlTableRow
	 */
	public String getHtmlTableRow() {
		StringBuffer sb = new StringBuffer(NL);

		sb.append("  <tr>" + NL);
		sb.append("    <td>" + name + "</td>" + NL);
		sb.append("    <td>" + symbol + "</td>" + NL);
		sb.append("    <td>" + notesStr + "</td>" + NL);
		sb.append("    <td>" + intervalsStr + "</td>" + NL);
		sb.append("    <td>");
		for (int integer: integers) {
			sb.append(integer + ", ");
		}
		sb.append("    </td>" + NL);
		sb.append("  </tr>" + NL);
		
		return sb.toString();
	}
}
