package audio.chords.abc;

import static audio.Constants.*;

import org.apache.log4j.Logger;

import audio.Maps;
import audio.Util;
import audio.chords.Chord;


/**
 * This class represents a Tune object derived from a '.chordscales' file. Every 
 * tune has an array of bars. Each bar has an array of 0 or more chord symbols. 
 * 
 * Sample '.chordscales' file:
 * 
 *		@4|60-120|5
 *      ! C: Aebersold
 *		[{C7 | :2}
 *		F7 | Bb7 | :2]
 *		F#7 | B7 | E7 | A7 | {D7 | G7 | :2}
 */
public class ChordscalesTune {
	/** The log. */
	private Logger log 			= Logger.getLogger(getClass());
	/** The title of this tune. */
	private String title		= "";
	/** The text of this tune. */
	private String text			= "";
	private String[] barStrs 	=	null;
			
	/**
	 * @param genre
	 * @param text
	 */
	public ChordscalesTune(String title, String text) throws Exception {
		this.title 	= title;
		this.text	= text;

		String sequence = "";
		String[] lines = text.split(NL);
		
		for (String line: lines) {
			line = line.trim();
			sequence += line;
		}
		log.debug("sequence=" + sequence);
		
		String expandedSequence = expandSequence(sequence);
		log.debug("expandedSequence=" + expandedSequence);
		
		barStrs = expandedSequence.split(PIPE_DELIM, - 1);
		log.debug("barStrs.length=" + barStrs.length);
		
		//for (String barStr: barStrs) {
		//	log.debug("barStr=" + barStr);
		//}
	}
	
	/**
	 * Remove spaces, expand '{:n}' and '[:n]' sections (where n is optional
	 * and defaults to 2 if not present), remove start and end pipe if it exists.
	 * 
	 * @param sequence
	 * @return expanded sequence
	 */
	private String expandSequence(String sequence) {
		// remove spaces
		sequence = sequence.replace(SPACE, "");
		
		// expand inner repeats
		sequence = Util.expandSequence(sequence, "{", "}");
	
		// expand outer repeats
		sequence = Util.expandSequence(sequence, "[", "]");
	
		// remove pipe at start of sequence if it exists
		if (sequence.startsWith(PIPE)) {
			sequence = sequence.substring(1);
		}
		
		// remove pipe at end of sequence if it exists
		if (sequence.endsWith(PIPE)) {
			sequence = sequence.substring(0, sequence.length() - 1);
		}
		
		return sequence;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toAbc(String mode) {
		log.debug("mode=" + mode);
		final String REST	= " z4 "; 
		StringBuffer sb  	= new StringBuffer();
		
		sb.append("X:1" + NL);
		sb.append("T:" + title + NL);
		// just use defaults for these abc properties
		sb.append("M:4/4" + NL);	
		sb.append("L:1/4" + NL);
		sb.append("K:C" + NL);
		
		int count = 0;
		for (String barStr: barStrs) {
			// barStr could be empty str, one or more chords, with or without scale annotation
			//log.debug("barStr=" + barStr);
			
			if (barStr.equals("")) {
				sb.append(SPACE + REST + SPACE);
			} else {
				String[] chordStrs = null;
				if (barStr.contains(COMMA)) {
					chordStrs = barStr.split(COMMA); 
				} else {
					chordStrs = new String[1];
					chordStrs[0] = barStr; // maybe split would handle this - TODO - test
				}

				//mode: all|chords|scales|none
				for (String chordStr: chordStrs) {
					//log.debug("chordStr=" + chordStr);
					// the chordName without the scale definition
					String chordName = "";
					// the scale definition
					String scaleShortName = "";

					if (chordStr.contains(PARENS_OPEN)) {
						// chord has scale definition
						if (chordStr.contains(PARENS_CLOSE)) {
							int pos = chordStr.indexOf(PARENS_OPEN);
							// the chordName without the scale definition
							chordName = chordStr.substring(0, pos);
							// the scale definition
							scaleShortName = chordStr.substring(pos + 1, chordStr.length() - 1);
							
							//log.debug(chordName + " " + scaleShortName);
						} else {
							// unmatched parens	
							sb.append(DQ + chordStr + ": ERROR" + DQ + SPACE);
						}
					} else {
						if (chordStr.contains(PARENS_CLOSE)) {
							// unmatched parens	
							sb.append(DQ + chordStr + ": ERROR" + DQ + SPACE);
						} else {
							chordName = chordStr;
							//log.debug(chordName);
						}
					}
					
					// for all modes write the abc chord notation
					sb.append(DQ + chordName + DQ + SPACE);

					if (mode.equals("none")) {
						sb.append(SPACE + REST + SPACE);
					} else {
						Scale scale	= null;					
						if ((mode.equals("all") || mode.equals("scales")) && !scaleShortName.equals("")) {
							// write the abc text representation of the scale
							scale = Maps.scalesMap.get(scaleShortName);
							sb.append(DQ + "_" + scale.name + DQ + SPACE);
						}
						
						if (mode.equals("all") || mode.equals("chords")) {
							// write the abc notation of the chord
							Chord chord = Maps.chords.get(chordName);
							if (chord == null) {
								try {
									chord = new Chord(chordName);
									Maps.chords.put(chordName, chord);
								} catch (Exception e) {
									log.error(e);
								}
							}
							sb.append(chord.toAbc() + SPACE);
						}

						if ((mode.equals("all") || mode.equals("scales")) && scale != null) {
							// write the abc notation of the scale
							String scaleAbc = scale.toAbc(chordName);
							sb.append(scaleAbc + SPACE); 
						}						
					}
				}
			}

			sb.append(PIPE + SPACE);
			
			count++;
			if (count == 4) {
				sb.append(NL);
				count = 0;
			}
		}

		return sb.toString();
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb  = new StringBuffer();
		
		sb.append("text=" + text);

		return sb.toString();
	}
}

