package audio.chords.abc;

import static audio.Constants.ALPHABET_TOKENS;
import static audio.Constants.COMMA;
import static audio.Constants.PIPE_DELIM;

import java.util.ArrayList;
import java.util.List;

//import org.apache.log4j.Logger;


import audio.Maps;
import audio.Util;

/**
 */
public class Scale {
	/** The log. */
	//private Logger log 				= Logger.getLogger(getClass());
	/** The scale name. */
	public String name 				= null;
	/** The scale short name. */
	public String shortName 		= null;
	/** The list of degrees of this scale */
	private List<Degree> degrees 	= new ArrayList<Degree>();
	
	/**
	 * Constructor.
	 * 
	 * @param s a line from the c-scales definition file
	 */
	public Scale(String s) {
		//eg. Lydian		= C, D,  E,  F#, G,  A,  B
		
		s 				= s.replace("\t", "").replace(" ", "");
		
		String[] arr 	= s.split(PIPE_DELIM, - 1);
		
		name 			= arr[0];
		shortName 		= arr[1];
		String notesStr	= arr[2];

		String[] notes 	= notesStr.split(COMMA);
 
		for (String note: notes) {
			//log.debug("note=" + note);
			degrees.add(new Degree(note));
		}
	}
	
	public String toAbc(String chord) {
		// the abc string to return
		String abcStr 		= "";
		
		// Ebm7
		// E
		String chordNote 			= "";
		// b
		String chordModifier 		= "";
		// 4
		int chordRelativeInterval	= -1;

		int len = chord.length();
		if (len == 1) { 
			chordNote = chord;
		} else if (len > 1) {
			if (chord.substring(1, 2).equals("#") || chord.substring(1, 2).equals("b")) {
				chordNote = chord.substring(0, 1);
				chordModifier = chord.substring(1, 2);
			} else {
				chordNote = chord.substring(0, 1);
			}
		}
		
		// determine the relative interval for this chord's root, using Map.intervalToNotes
		for (int i = 0; i < 12; i++) {
			List<String> notes = Maps.intervalToNotes.get(i);
			if (notes.contains(chordNote + chordModifier)) {
				chordRelativeInterval = i;
				break;
			}
		}
		//log.debug("chordNote=" + chordNote);
		//log.debug("chordModifier=" + chordModifier);
		//log.debug("chordRelativeInterval=" + chordRelativeInterval);

		// first get the numericDegree of the chordNote based on ALPHABET_TOKENS
		// eg. 3
		int chordNoteNumericDegree = Maps.alphabetTokenToNumericDegree.get(chordNote); 
		//log.debug("chordNoteNumericDegree=" + chordNoteNumericDegree);
		
		int i = 0;
		boolean lowerCase = false;
		// for each degree of the scale
		for (Degree degree: degrees) {
			//log.debug("i=" + i + " degree=" + degree);

			// eg.
			// Lydian			= C, D, E, F#, G, A, B
			// degrees 			= 1, 2, 3, #4, 5, 6, 7  
			// numericDegree	= 1, 2, 3, 4,  5, 6, 7
			// degreeModifier	= ,  ,  ,  #,  ,  ,  
			// intervals		= 0, 2, 4, 6,  7, 9, 11

			String scaleNoteAlphabetToken = ALPHABET_TOKENS[(chordNoteNumericDegree + degree.numericDegree - 2) % 7];
			String scaleNote = "";
			
			int relativeInterval = (chordRelativeInterval + degree.interval) % 12;
			List<String> notes = Maps.intervalToNotes.get(relativeInterval);
			for (String note: notes) {
				if (note.contains(scaleNoteAlphabetToken)) {
					scaleNote = note;
					break;
				}
			}
			
			if (!chordNote.equals("C") && scaleNoteAlphabetToken.equals("C")) {
				// upper octave, so lc note
				lowerCase = true;
			}
			
			if (lowerCase) {
				scaleNote = scaleNote.toLowerCase();
			}
			
			String abcNote = Util.noteToAbc(scaleNote);
			
			abcStr += abcNote + " ";
			
			i++;
		}

		return abcStr;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = name + " = ";
		
		for (Degree degree: degrees) {
			s += degree.toString() + "; ";
		}

		return s.substring(0, s.length() - 2);
	}
}


	// /** List of fully defined degrees of this scale, eg. 1, 2, 3, #4, 5, 6, 7. */
	// private List<String> degrees 	= new ArrayList<String>();
	// /** List of relative intervals for this scale, eg. 0, 2, 4, 6, 7, 9, 11. */
	// public int[] intervals 			= null;
 
// intervals = new int[notes.length];
// String[] ALPHABET_TOKENS 		= {"C", "D", "E", "F", "G", "A", "B"};
//String alphabetToken 	= note.substring(0, 1);
//String modifier 		= (note.length() == 2) ? note.substring(1, 2) : "";
//String degree			= modifier + alphabetTokenToNumericDegree.get(alphabetToken);  
//degrees.add(degree);
//intervals[i++] = maps.noteToInterval.get(note);
