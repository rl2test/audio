package audio.chords.generator;

import static audio.Constants.SPACE;

import java.util.ArrayList;
import java.util.List;

import audio.Util;

/**
 * A class representing a specific musical pitch. The pitch may be represented by two or three 
 * different note notations, eg. 'B#  c  dbb'. By abc convention, the upper octave should use  
 * lowercase characters, as is in abc notation. 
 */
public class Pitch {
	/** 'B#  c  dbb' */
	public String notesStr 		= "";
	/** B#, c, dbb' */
	public List<String> notes 	= new ArrayList<String>();
	
	/**
	 * @param notesStr in the format 'B#  c  dbb'
	 */
	public Pitch(String notesStr) {
		this.notesStr = notesStr;
		notes = Util.getList(notesStr);
	}
	
	/**
	 * @param s
	 * @return boolean indicating that this pitch contains the note equal to s 
	 */
	public boolean contains (String s) {
		for (String note: notes) {
			if (note.equals(s)) {
				return true;
			}
		}
		return false;
	}

	// TODO link the following 2 methods
	
	/**
	 * @param noteAlpha - which is the alphabetic portion of a note definiton, 
	 *        eg. for the note F# the noteAlpha would be F
	 *         
	 * @return the note value corresponding to the 'noteAlpha' param, eg.
	 *         if the Pitch was instantiated using 'B#  c  dbb', and noteAlpha = 'B'
	 *         then return 'B#'   
	 */
	public String get(String noteAlpha) {
		for (String note: notes) {
			if (note.startsWith(noteAlpha)) {
				return note;
			}
		}
		return "error";
	}

	/**
	 * @param noteAlpha - which is the alphabetic portion of a note definiton, 
	 *        eg. for the note F# the noteAlpha would be F
	 *         
	 * @return the abc note value corresponding to the 'noteAlpha' param, eg.
	 *         if the Pitch was instantiated using 'B#  c  dbb', and noteAlpha = 'B'
	 *         then return '^B'   
	 */
	public String getAbc(String noteAlpha) {
		for (String note: notes) {
			if (note.startsWith(noteAlpha)) {
				return Util.noteToAbc(note);
			}
		}
		return "error";
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = "";
		for (String note: notes) {
			s += note + SPACE;
		}
		return s;
	}
}
