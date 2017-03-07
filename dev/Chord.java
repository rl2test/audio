package audio.chords;

import org.apache.log4j.Logger;

/**
 * This class represents a specific chord, eg. Cm7, and all of its related 
 * properties, including its associated chord and scale.
 * 
 * TODO merge chord and chordscale objects. 
 */
public class Chord implements ChordsConstants {
	/** The log. */
	private Logger log = Logger.getLogger(this.getClass());
	/** The name of the chord eg. Cm7 */
	public String name;
	/** The note value eg. C, F# */
	public String root = "";
	/** The chord type eg. m7 */
	public String type = "";
	/** The absolute root value. */
	public int rootVal;
	/** The absolute fifth value. */
	public int fifthVal;
	/** 
	 * Arr of ints of absolute note values of all notes in the chord beginning 
	 * one octave higher than the root
	 */
	public int[] polyadVals;
	/** returned by toString() */
	public String string;

	/**
	 * @param name
	 */
	public Chord(String name) {
		this.name = name;
		log.debug("name=" + name);

		int len = name.length();
		if (len == 1) { 
			root = name;
		} else if (len > 1) {
			if (name.substring(1, 2).equals("#") || name.substring(1, 2).equals("b")) {
				root = name.substring(0, 2);
				if (len > 2) {
					type = name.substring(2);
				}
			} else {
				root = name.substring(0, 1);
				type = name.substring(1);
			}
		} 
		
		// define root
		log.debug("note=" + root);
		rootVal = Mapstore.cNotesToAbsIntervals.get(root);
		
		// check for maj
		if (type.equals("")) {
			type = "maj";
		}
		log.debug("type=" + type);		
		
		String chordIntervalsStr = Mapstore.chordTypes.get(type).chordIntervalsStr;
		String[] intervals	= chordIntervalsStr.split(SPACE); 

		polyadVals = new int[intervals.length];
		int i = 0;
		for (String interval: intervals) {
			int semitone = Mapstore.intervalsToAbsIntervals.get(interval);
			polyadVals[i++] = rootVal + OCTAVE + semitone;
			if (interval.contains("5")) {
				fifthVal = rootVal + semitone;
			}
		}

		string = name + ", " + root + "," + rootVal + ", " + fifthVal + ", ";

		for (int polyadVal: polyadVals) {
			// TODO translate to absolute note value 60 -> C (requires checking)
			string += polyadVal + " "; 
		}
		
		log.debug(this);
	}
	
	public String toString() {
		return string;
	}
}
