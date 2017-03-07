package audio.chords.parser.irealb;

import static audio.Constants.COMMA;
import static audio.Constants.FS;
import static audio.Constants.SPACE;

import java.util.ArrayList;
import java.util.List;

import audio.Util;
//import org.apache.log4j.Logger;

public class Bar {
	/** The log. */
	//private Logger log 			= Logger.getLogger(getClass());
	/** The name of the section if this bar begins a section. */
	public String section 		= "";
	/** The iRealB string for this bar. */
	public String str 			= "";
	/** Boolean indicating that this bar begins a repeated section. */
	public boolean beginRepeat 	= false;
	/** Boolean indicating that this bar ends a repeated section. */
	public boolean endRepeat 	= false;
	/** List of chords in this bar. */
	public List<String> chords	= new ArrayList<String>();
	/** Boolean indicating that this is a ditto bar. */
	public boolean isDitto 		= false;
	/** Integer indicating that this bar begins a 1st or 2nd time indicator. */
	public int N				= 0;
	/** The number of beats in the bar. */
	public int beatsPerBar		= 0;
	/** The chord string for this bar. */
	public String chordStr 		= "";

	
	/**
	 * 
	 */
	public Bar() {
	}
	
	/**
	 * 
	 */
	public Bar(int beatsPerBar) {
		this.beatsPerBar = beatsPerBar;
	}
	
	/**
	 * @return
	 */
	public boolean hasSection() {
		return !section.equals("");
	}
	
	/**
	 * @return
	 */
	public boolean hasContent() {
		return (str.trim().length() > 0 || isDitto);
	}
	
	/**
	 * This method returns a String representing this bar in the '.chords' file 
	 * format. 
	 * 
	 * @return
	 */
	public String getChordString() {
		if (chordStr.equals("")) {
			StringBuffer sb = new StringBuffer();
			
			// sample '.chords' file - note that the '{ :}' and '[ :]' elements will not be used
			/*
			% A
			{Cmaj7 | :}{E7 | :}
			{A7 | :}{Dm | :}
			{E7 | :}{Am | :}
			{D7 | :}Dm7 | G7 |
			% B
			{Cmaj7 | :}{E7 | :}
			{A7 | :}{Dm | :}
			F | Fm | Cmaj7, Em7 | A7 |
			Dm7 | G7 | C6, Ebdim7 | Dm7, G7 |
			*/
			
			if (!isDitto) {
				// ditto bars will be written out as empty in the '.chords' file
				
				// parse the iRealB str

				// replace comma with space
				str = str.replace(COMMA, SPACE);

				// trim
				str = str.trim();
				
				// split on space
				String[] tokens = str.split(SPACE);
				
				for (String token: tokens) {
					if (!token.equals("")) {
						chords.add(translate(token.trim()));
					} 
				}

				// check if a chord needs to be added
				int size = chords.size();
				if ((beatsPerBar == 3 && size == 2) ||
					(beatsPerBar == 4 && size == 3)) {
					chords.add(0, chords.get(0));
				}
				
				int n = 0;
				for (String chord: chords) {
					sb.append(chord);
					if (n < chords.size() - 1) {
						sb.append(", ");
					}
					n++;	
				}
			}
			
			chordStr = sb.toString();
			
		}
		return chordStr;
	} 
	
	/**
	 * This method translates the iRealB chord symbol into '.chords' file format.
	 *  
	 * @param chord
	 * @return
	 */
	private String translate(String chord) {
		if (chord.contains(FS)) {
			int pos = chord.indexOf(FS);
			chord = chord.substring(0, pos);
		}
		
		chord = chord.replace("-", "m");
		chord = chord.replace("o", "dim");
		chord = chord.replace("^7", "maj7");
		chord = chord.replace("^9", "maj9");
		chord = chord.replace("h7", "m7-5");
		chord = chord.replace("69", "6add9");
		
		return chord;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(Util.padWithSpaces(section, 2));
		
		String n = (N > 0) ? N + "" : "";
		sb.append(Util.padWithSpaces(n, 2));

		String barStr = (isDitto) ? "x" : str;
		sb.append(Util.padWithSpaces(barStr, 20));
		
		String repeat  = "";
		if (beginRepeat) {
			repeat = "beginRepeat";
		} else if (endRepeat) {
			repeat = "endRepeat";
		} else {
			
		}
		sb.append(Util.padWithSpaces(repeat, 12));
		
		return sb.toString();
	}
}


