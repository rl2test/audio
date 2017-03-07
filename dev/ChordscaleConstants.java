package audio.chords;

import java.util.Comparator;


public interface ChordscaleConstants extends ChordsConstants {
	/* 
	 * Integer used to denote whether a note is a part of the chord or the scale 
	 * in a chordscale context.
	 */
	public final int CHORD			= 0;
	public final int SCALE 			= 1;

	/** 
	 * Conventional notation of unmodified notes, corresponding to the white keys, using  
	 * the upper/lowercase abc convention to specify the upper/lower octave. All chordscales
	 * can be described within this 2-octave range. 
	 */
	public static final String[] ALPHAS			= 
			{"C", "D", "E", "F", "G", "A", "B", "c", "d", "e", "f", "g", "a", "b"};
	
	// TODO at some point it may be necessary to add an octave parameter to make 
	//      this class more generic 
	/** 
	 * Array of Strings representing the conventional notation aliases for each 
	 * individual pitch within a 2-octave range, using the abc convention of 
	 * lowercase for the upper octave.
	 */
	public static Pitch[] PITCHES 	= {
			// lower octave
			new Pitch("B#  C  Dbb"),
			new Pitch("B## C# Db "),
			new Pitch("C## D  Ebb"),
			new Pitch("D#  Eb Fbb"),
			new Pitch("D## E  Fb "),
			new Pitch("E#  F  Gbb"),
			new Pitch("E## F# Gb "),
			new Pitch("F## G  Abb"),
			new Pitch("G#     Ab "),
			new Pitch("G## A  Bbb"),
			new Pitch("A#  Bb cbb"),
			new Pitch("A## B  cb "),
			// upper octave
			new Pitch("B#  c  dbb"),
			new Pitch("B## c# db "),
			new Pitch("c## d  ebb"),
			new Pitch("d#  eb fbb"),
			new Pitch("d## e  fb "),
			new Pitch("e#  f  gbb"),
			new Pitch("e## f# gb "),
			new Pitch("f## g  abb"),
			new Pitch("g#  ab    "),
			new Pitch("g## a  bbb"),
			new Pitch("a#  bb cbb"),
			new Pitch("a## b  cb "),
	};
	
	/** Comparator used to order a list of Interval objects. */
	public static final Comparator<Interval> INTERVAL_COMPARATOR 
									= new Comparator<Interval>() {

		public int compare(Interval i1,  Interval i2) {
			return i1.absInterval - i2.absInterval;
		}
		
	};
}
