package audio.gui;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class JazzChord implements GuiConstants {
	/** The log. */
	private Logger log = Logger.getLogger(this.getClass());
	/* identifier definitions */
	public static final int C 	= 0;
	public static final int D  	= 2;
	public static final int E  	= 4;		
	public static final int F 	= 5;
	public static final int G 	= 7;
	public static final int A 	= 9;
	public static final int B 	= 11;
	public static final String IDENTIFIERS = "CDEFGAB";
	/** The base for all roots */
	public static final int BASE = 48;

	private String symbol		= ""; 
	private String identifier	= "";
	private String modifier		= "";	
	private String components	= "";
	private int root;
	public int[] notes;
	private String string		= "";

	static Map<String, Integer> identifierMap = new HashMap<String, Integer>();
	
	static {
		String[] identifiers = {"C", "D", "E", "F", "G", "A", "B"};
		int[] values = {C, D, E, F, G, A, B};
		for(int i = 0; i < identifiers.length; i++) {
			identifierMap.put(identifiers[i], values[i] + BASE);
		}
	}

	/**
	 * The following modifiers are recognized:
	 *		m		 	Cm / Cmin / C-
	 *		aug		 	Caug / C+
	 *		dim			Cdim / Cm(b5) / Cmin(b5) / C-(b5)
	 *		maj7		Cmaj7 / CM7
	 *		mmaj7		Cmmaj7 / CmM7 / CminM7 / Cminmaj7 / C-M7 / C-maj7
	 *		maj7#5		Cmaj7#5 / C+maj7 / CaugM7 / Caugmaj7 / CM7#5 / C+M7
	 *		7			C7
	 *		m7			Cm7 / Cmin7 / C-7 / C-7
	 *		aug7		Caug7 / C+7 / C7#5
	 *		m7b5		Cm7b5 / Cmin7b5 / C-7b5
	 *		dim7		Cdim7
	 * This list may be added to in the future
	 * 
	 * This is a subset of the list defined in 
	 * 		http://lilypond.org/doc/v2.12/Documentation/user/lilypond/Common-chord-modifiers
	 * 
	 * @param name
	 */
	public JazzChord(String symbol) throws Exception {
		this.symbol = symbol;
		//log.debug("symbol=" + symbol);
		
		// C#maj7+5
		// identifier	: C
		// modifier		: #
		// components	" maj7+5 

		identifier = symbol.substring(0, 1); // eg. C, D, E etc
		//log.debug("identifier=" + identifier);
		
		if (!IDENTIFIERS.contains(identifier)) {
			throw new Exception("invalid identifier");
		}
		
		root = identifierMap.get(identifier);
		
		if (symbol.length() > 1) {
			if (symbol.substring(1, 2).equals("#") || symbol.substring(1, 2).equals("b")) {
				modifier = symbol.substring(1, 2);
				//log.debug("modifier=" + modifier);
				root = (modifier.equals("#")) ? root + 1 : root - 1;
				if (symbol.length() > 2) {
					components = symbol.substring(2);
				} else {
					components = "";
				}
			} else {
				components = symbol.substring(1);
			}
		} else {
			components = "";
		} 
		//log.debug("components=" + components);
														// name					symbol(s)				
														// intervals			example
														// --------------------	---------------------------------------------------
		// triads 
		if (components.equals("")) {					// major				C
			int[] ints = {0, 4, 7};						// {0, 4, 7}			C-E-G
			setNotes(ints);
		} else if (components.equals("m")) {			// minor				Cm / Cmin / C-
			int[] ints = {0, 3, 7};						// {0, 3, 7}			C-Eb-G
			setNotes(ints);
		} else if (components.equals("aug")) {			// augmented			Caug / C+
			int[] ints = {0, 4, 8};						// {0, 4, 8}			C-E-G#
			setNotes(ints);
		} else if (components.equals("dim")) {			// diminished			Cdim / Cm(b5) / Cmin(b5) / C-(b5)
			int[] ints = {0, 3, 6};						// {0, 3, 6}			C-Eb-Gb
			setNotes(ints);
		
		// seventh chords
		} else if (components.equals("maj7")) {			// Major 7th			Cmaj7 / CM7
			int[] ints = {0, 4, 7, 10};					// {0, 4, 7, 11}		C-E-G-B
			setNotes(ints);
		} else if (components.equals("mmaj7")) {		// Minor-Major 7th		Cmmaj7 / CmM7 / CminM7 / Cminmaj7 / C-M7 / C-maj7
			int[] ints = {0, 3, 7, 11};					// {0, 3, 7, 11}		C-Eb-G-B
			setNotes(ints);
		} else if (components.equals("maj7#5")) {		// Augmented-Major 7th	Cmaj7#5 / C+maj7 / CaugM7 / Caugmaj7 / CM7#5 / C+M7
			int[] ints = {0, 4, 8, 11};					// {0, 4, 8, 11}		C-E-G#-B
			setNotes(ints);
		} else if (components.equals("7")) {			// Dominant 7th			C7
			int[] ints = {0, 4, 7, 10};					// {0, 4, 7, 10}		C-E-G-Bb
			setNotes(ints);
		} else if (components.equals("m7")) {			// Minor 7th			Cm7 / Cmin7 / C-7 / C-7
			int[] ints = {0, 3, 7, 10};					// {0, 3, 7, 10}		C-Eb-G-Bb
			setNotes(ints);
		} else if (components.equals("aug7")) {			// Augmented 7th		Caug7 / C+7 / C7#5
			int[] ints = {0, 4, 8, 10};					// {0, 4, 8, 10}		C-E-G#-Bb
			setNotes(ints);
		} else if (components.equals("m7b5")) {			// Half-diminished 7th	Cm7b5 / Cmin7b5 / C-7b5
			int[] ints = {0, 3, 6, 10};					// {0, 3, 6, 10}		C-Eb-Gb-Bb
			setNotes(ints);
		} else if (components.equals("dim7")) {			// Diminished 7th		Cdim7
			int[] ints = {0, 3, 6, 9};					// {0, 3, 6, 9}			C-Eb-Gb-Bbb
			setNotes(ints);

		/*
		// this chord is not included in the wikipedia chord_music article, though
		// it is present in the chord_notation article   
		} else if (components.equals("7b5")) {			// Dominant 7th b five	C7b5
			int[] ints = {0, 4, 6, 10};					// {0, 4, 6, 10}		C-E-Gb-Bb
			setNotes(ints);
		*/
		
		//} else if (components.equals("")) {			// suspended 4th
			//	int[] ints = {};
			//	setNotes(ints);
		//} else if (modifier.equals("")) {
			//	int[] ints = {};
			//	setNotes(ints);
		//} else if (modifier.equals("")) {
			//	int[] ints = {};
			//	setNotes(ints);
		} else {
			log.error("unrecognized modifier=" + identifier);
		}	
		
		log.debug(this);
	}
	
	/**
	 * @param integers
	 */
	private void setNotes(int[] integers) {
		notes = new int[integers.length];
		int i = 0;
		for (int integer: integers) {
			if (i == 0) {
				integer -= 12;
			}
			notes[i++] = root + integer; 
		}
	}
	
	public String toString() {
		if (string.equals("")) {
			String delimiter = " | ";
			String notesStr = "";
			if (notes != null) {
				for (int note: notes) {
					notesStr += note + " ";	
				} 
			}
			
			string = symbol + delimiter + 
					identifier + delimiter +
					modifier + delimiter +
					components + delimiter +
					//root + delimiter +
					notesStr;			
		}
		return string;
	}
}
