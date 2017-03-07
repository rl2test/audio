package audio.chords;

import org.apache.log4j.Logger;

import audio.Util;

/**
 *
 */
public class Scale implements ChordsConstants {
	/** The log. */
	private Logger log 			= Logger.getLogger(getClass());	
	public String chordName		= "";
	public ChordType chordType	= null;
	public ScaleType scaleType	= null;
	public String abc			= "";

	/**
	 * @param chordName
	 */
	public Scale(String chordName) {
		this.chordName = chordName; // Ab7
		StringBuffer sb = new StringBuffer();
		
		int len = chordName.length();
		String root 	= "";	// Ab7 -> Ab
		String alpha	= ""; 	// Ab7 -> A  // the alpha portion of the chordName, without the b|# modifier 
		String type 	= ""; 	// Ab7 -> 7
		
		alpha = chordName.substring(0,1);
		if (len > 1) {
			String subStr = chordName.substring(1,2);  
			if (subStr.equals("b") || subStr.equals("#")) {
				// chordName has a modifier
				root = chordName.substring(0,2);
				if (len > 2) {
					type = chordName.substring(2);					
				}
			} else {
				// chordName has no modifier
				root = chordName.substring(0,1);
				if (len > 1) {
					type = chordName.substring(1);
				}
			}
		} else {
			root = chordName;
		}
		
		if (type.equals("")) {
			type = "maj";
		}
		
		log.debug("chordName=" + chordName +
				", alpha=" + alpha +
				", root=" + root +
				", type=" + type);
		
		chordType = Mapstore.chordTypes.get(type);
		
		if (chordType == null) {
			log.error("chordType is null");
		} else {
			sb.append("\"^" + chordType.scaleTypeShortName + "\" ");	
		}
		
		sb.append("\"" + Util.padWithSpaces(chordName, 10) + "\" ");
		
		// get alpha arr index of alpha
		int alphaIndex = getAlphaIndex(alpha); // A7 -> 5

		// get pitches arr index of root tone 
		int pitchIndex = getPitchIndex(root);

		//log.debug("alphaIndex=" + alphaIndex + ", pitchIndex=" + pitchIndex);

		String lastIntervalAlpha = "";
		String lastIntervalAbc = "";
		for (Interval interval: chordType.intervals) {
			//log.debug(interval.toString());
			
			// get intervalAlpha; note: subtract 1 from interval.ordinalNum
			String intervalAlpha = ALPHAS[(alphaIndex + interval.ordinalNum - 1)] ;
			
			Pitch pitch = PITCHES[pitchIndex + interval.absInterval];

			//log.debug("intervalAlpha=" + intervalAlpha + ", pitch=" + pitch);

			String intervalAbc = pitch.getAbc(intervalAlpha);
			
			if (lastIntervalAlpha.equals(intervalAlpha) && 
					(lastIntervalAbc.contains("_") || lastIntervalAbc.contains("^")) &&
					!(intervalAbc.contains("_") || intervalAbc.contains("^"))) {
				// prepend natural sign
				intervalAbc = "=" + intervalAbc;
			}
			sb.append(intervalAbc);
			
			if (interval.type == SCALE) {
				// scale intervals are written as half-notes, chord intervals as quarter-notes
				sb.append("2");
			}
			
			sb.append(SPACE);
			
			lastIntervalAlpha	= intervalAlpha;			
			lastIntervalAbc 	= intervalAbc;
		}
		
		abc = sb.toString();
		log.debug("abc=" + abc);
	}

	/**
	 * TODO replace method with mapping
	 * 
	 * @param s
	 * @return the index to the PITCHES arr
	 */
	private int getPitchIndex(String s) {
		int index = 0;
		for (Pitch pitch: PITCHES) {
			// special case
			if (s.startsWith("Cb")) {
				s = s.toLowerCase();
			}
			if (pitch.contains(s)) {
				break;
			}
			index++;
		}
		return index;
	}
	
	/**
	 * TODO replace method with mapping
	 * 
	 * @param alpha
	 * @return the index to the ALPHA_STRS arr of s
	 */
	private int getAlphaIndex(String alpha) {
		int index = 0;
		for (String s: ALPHAS) {
			// {"C", "D", "E", "F", "G", "A", "B"}
			if (s.equals(alpha)) {
				break;
			}
			index++;
		}
		return index;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(NL);
		
		sb.append("chordName=" + chordName + NL);
		sb.append("chordType=" + chordType.toString());

		return sb.toString();
	}
}
