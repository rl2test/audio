package audio.chords;

import static audio.Constants.CSV;

/**
 * This class represents a normalized interval. If the interval is >= 8 it is
 * adjusted to fall within the range 1-7.
 */
public class Interval {
	/** The interval as notated in the CHORD_TYPES_FILE file, eg. b3. */
	public String interval 				= "";
	/** The numeric portion of the interval, eg. 3, 5 etc., referring to 3rd, 5th. */
	public int ordinalNum 				= 0;
	/** The modifier, ie. #|b. */
	public String modifier				= "";
	/** The normalized interval, eg. b13 -> b6. */
	public String normalizedInterval	= "";
	/** The relative interval, that is, the number of half-steps from the root, eg. b5 -> 6. */
	public int relInterval 				= 0;		

	/**
	 * @param intervalStr
	 */
	public Interval(String interval) { //'b3'
		this.interval = interval;
		
		if (interval.startsWith("bb") || interval.startsWith("##")) {
			modifier = interval.substring(0, 2);
			ordinalNum = Integer.parseInt(interval.substring(2));
		} else if (interval.startsWith("b") || interval.startsWith("#")) {
			modifier = interval.substring(0, 1);
			ordinalNum = Integer.parseInt(interval.substring(1));
		} else {
			ordinalNum = Integer.parseInt(interval);
		}

		relInterval = Maps.chordIntervalToChordInterger.get(interval);

		if (ordinalNum > 8) {
			ordinalNum 	-= 7;
			relInterval -= 12;
		}
		
		normalizedInterval = modifier + ordinalNum; 
	}

	/**
	 * @param intrvl
	 * @return boolean indicating that this interval is equal to the interval 
	 *         passed as a parameter
	 */
	public boolean equals(Interval intrvl) {
		return intrvl.interval.equals(interval);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("interval=" 				+ interval + CSV);
		sb.append("modifier=" 				+ modifier + CSV);
		sb.append("ordinalNum="				+ ordinalNum + CSV);
		sb.append("normalizedInterval="		+ normalizedInterval + CSV);
		sb.append("relInterval=" 			+ relInterval + CSV);

		return sb.toString();
	}
}
