package audio.chords;

import static audio.Constants.NL;
import static audio.Constants.UNDEF;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import audio.Maps;

public class Bar {
	/** The log. */
	private Logger log = Logger.getLogger(this.getClass());	
	/** The original string representing this bar. */
	public String barStr 				=  "";
	/** The clean version of the barStr, with any annotation removed. */
	public String cleanBarStr 			=  "";
	/** The chord sequence of the bar. */
	public String sequence 				=  "";
	/** The number of beats per bar. */
	public int beatsPerBar				= UNDEF;
	/** The list of chords in the bar. */
	public List<Chord> chords			= new ArrayList<Chord>();
	/** 
	 * The annotation associated with this bar, if any. For example, a bar may 
	 * be preceded with a line such as '%', '% A' or '% B', any one of 
	 * which may indicate a new section. 
	 */
	public String annotation			= null;
	
	/**
	 * Constructor, which sets the sequence.
	 * 
	 * @param beatsPerBar
	 * @param barStr
	 */
	public Bar(int beatsPerBar, String barStr) {
		this.beatsPerBar	= beatsPerBar;
		this.barStr 		= barStr;
	}

	/**
	 * Set the expanded list of chords for this bar.
	 */
	public void init() throws Exception {
		cleanBarStr = barStr;

		if (cleanBarStr.startsWith("<") && cleanBarStr.contains(">")) {
			int pos =  cleanBarStr.indexOf(">");
			annotation = cleanBarStr.substring(1, pos);
			cleanBarStr = cleanBarStr.substring(pos + 1);
		}
		
		// accepted formats: 
		//   'C'		-> expanded if beatsPerBar = 4 to, e.g. 'C,C,C,C'
		//   'C,F'		-> expanded if beatsPerBar = 4, e.g. 'C,C,F,F' 
		//   'C,F,Em'	-> as written if beatsPerBar = 3
		//   'C,F,Em,G'	-> as written if beatsPerBar = 4
		//   any of the preceding preceded by an annotation:
		//   '<A>'		-> A 
		
		if (cleanBarStr.contains(",")) {
			// multiple chords
			String[] chordNames = cleanBarStr.split(",");
			int len = chordNames.length;

			if (len == beatsPerBar) {
				sequence = cleanBarStr;
			} else if (beatsPerBar == 4 && len == 2) {
				for (int i = 0; i < 2; i++) {
					for (int j = 0; j < 2; j++) {
						sequence += chordNames[i] + ",";
					}	
				}
				// remove final ','
				sequence = sequence.substring(0, sequence.length() - 1);
			} else if (beatsPerBar == 6 && len == 2) {
				for (int i = 0; i < 2; i++) {
					for (int j = 0; j < 3; j++) {
						sequence += chordNames[i] + ",";
					}	
				}
				// remove final ','
				sequence = sequence.substring(0, sequence.length() - 1);
			} else {
				log.error("invalid beatsPerBar and/or tempBarStr: beatsPerBar=" + beatsPerBar + ", cleanBarStr=" + cleanBarStr);
			}	
		} else {
			// single chord
			String chordName = cleanBarStr;

			for (int i = 0; i < beatsPerBar; i++) {
				if (i < beatsPerBar - 1) {
					sequence += chordName + ",";	
				} else {
					sequence += chordName;
				}
			}
		}
		
		String[] chordNames = sequence.split(",");

		for (String chordName: chordNames) {
			// add to maps
			if (!Maps.chords.containsKey(chordName)) {
				Maps.chords.put(chordName, new Chord(chordName));	
			}
			chords.add(Maps.chords.get(chordName));
		}
		
		String patternKey = "0";
		int len = chordNames.length;
		for (int i = 1; i < len; i++) {
			String prevChordName = chordNames[i - 1];
			String nextChordName = chordNames[i];
			int keyLen = patternKey.length();
			if (prevChordName.equals(nextChordName)) {
				patternKey += patternKey.substring(keyLen - 1, keyLen); 
			} else {
				String lastChar = patternKey.substring(keyLen - 1, keyLen);
				patternKey += (lastChar.equals("0")) ? "1" : "0";
			}
		}
		
		log.debug(sequence + " -> " + patternKey);
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("[Bar]" + NL);
		sb.append("barStr      =" + barStr + NL);
		sb.append("sequence    =" + sequence + NL);
		sb.append("beatsPerBar =" + beatsPerBar + NL);
		
		for (Chord chord: chords) {
			sb.append(chord + NL);
		}
		
		return sb.toString();
	}	
}
