package audio.abc;

import static audio.Constants.COMMA;
import static audio.Constants.DQ;
import static audio.Constants.EQUALS;
import static audio.Constants.PIPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.apache.log4j.Logger;

public class Bar {
	/** The log. */
	//private Logger log 						= Logger.getLogger(getClass());
	public String key 						= "";
	public List<Token> tokens 				= null;
	// a mapping of all notes matching a specific note, ie note->notes
	public Map<String, List<AbcNote>> map 	= new HashMap<String, List<AbcNote>>();
	public List<String> mapKeys 			= new ArrayList<String>(); // keys to map
	public Signature signature 				= null;
	
	public Bar(String key, List<Token>tokens) {
		this.key = key;
		this.tokens = tokens;
		for (Token token: tokens) {
			if (token.type == Token.NOTE) {
				AbcNote abcNote = (AbcNote) token; 
				if (!map.containsKey(abcNote.unalteredVal)) {
					map.put(abcNote.unalteredVal, new ArrayList<AbcNote>());
					mapKeys.add(abcNote.unalteredVal);
				}
				map.get(abcNote.unalteredVal).add(abcNote);
			}
		}
		signature = Transposer.signatures.get(key);	
	}
	
	
	/**
	 * Set the absVal for each note in the bar, based on signature and displayVal
	 */
	public void setAbsolute() {
		// convert each map to absolute values
		for (String mapKey: mapKeys) {
			List<AbcNote> abcNotes = map.get(mapKey);
			//log.debug(mapKey + " " + abcNotes.size());
			for (int i = 0, n = abcNotes.size(); i < n; i++) {
				AbcNote abcNote = abcNotes.get(i);
				String displayVal = abcNote.displayVal;
				if (Main.isAltered(displayVal)) {
					if (displayVal.startsWith(EQUALS)) {
						// remove '='
						abcNote.setAbsVal(abcNote.unalteredVal);
					} else {
						abcNote.setAbsVal(displayVal);
					}
				} else {
					if (i == 0) { 
						if (signature.notes.indexOf(abcNote.ucAlphaVal) != -1) {
							String val = (signature.parity == -1) ? "_" + displayVal : "^" + displayVal;	
							abcNote.setAbsVal(val);
						} else {
							abcNote.setAbsVal(displayVal);
						}
					} else {
						abcNote.setAbsVal(abcNotes.get(i - 1).absVal);
					}									
				}
			}
		}
	}
	
	/**
	 * Set the displayVal for each note in the bar, based on signature and absVal.
	 */
	public void setDisplay() {
		// convert each map to display values, checking for highest note (TODO)
		boolean upOctave = false;
		boolean downOctave = false;
		for (String mapKey: mapKeys) {
			List<AbcNote> abcNotes = map.get(mapKey);
			//log.debug(mapKey + " " + abcNotes.size());
			for (int i = 0, n = abcNotes.size(); i < n; i++) {
				AbcNote abcNote = abcNotes.get(i);
				int intVal = Transposer.intVals.get(abcNote.absVal);
				if (intVal > Transposer.MAX_NOTE_VAL) {
					downOctave = true;
				} else if (intVal < Transposer.MIN_NOTE_VAL) {
					upOctave = true;
				}

				if (i == 0) {
					if (signature.notes.indexOf(abcNote.ucAlphaVal) != -1) {
						if (signature.parity == abcNote.accidental) {	
							abcNote.displayVal = abcNote.unalteredVal;
						} else if (abcNote.accidental == 0) {
							abcNote.displayVal = EQUALS + abcNote.absVal;
						} else {
							abcNote.displayVal = abcNote.absVal;
						}
					} else {
						abcNote.displayVal = abcNote.absVal;
					}
					
				} else {
					if (abcNote.absVal.equals(abcNotes.get(i - 1).absVal)) {
						abcNote.displayVal = abcNote.unalteredVal;
					} else {
						if (abcNote.accidental == 0) {
							abcNote.displayVal = EQUALS + abcNote.absVal;
						} else {
							abcNote.displayVal = abcNote.absVal;
						}
					}
				}	
			}
		}

		// handle octave 
		if (upOctave && downOctave) {
		} else if (upOctave) {
			//log.debug("upOctave: " + toAbcDisplay());
			for (Token token: tokens) {
				if (token.type == Token.NOTE) {
					AbcNote abcNote = (AbcNote) token; 
					abcNote.displayVal = Transposer.raiseOctave(abcNote.displayVal);
				}
			}
			//log.debug("upOctave: " + toAbcDisplay());
		} else if (downOctave) {
			//log.debug("downOctave: " + toAbcDisplay());
			for (Token token: tokens) {
				if (token.type == Token.NOTE) {
					AbcNote abcNote = (AbcNote) token; 
					abcNote.displayVal = Transposer.lowerOctave(abcNote.displayVal);
				}
			}
			//log.debug("downOctave: " + toAbcDisplay());
		} else {
		}
	}
	
	/**
	 * @return bar with note display values
	 */
	public String toAbcDisplay() {
		String abc = "";
		for (Token token: tokens) {
			if (token.type == Token.CHORD) {	
				abc += DQ + token.absVal + DQ;
			} else if (token.type == Token.NOTE) {
				AbcNote abcNote = (AbcNote) token;
				abc += abcNote.displayVal;
			} else {
				abc += token.absVal;
			}
		}	
		return abc + PIPE;
	}
	
	/**
	 * @return bar with note absolute values
	 */
	public String toAbcAbsolute() {
		String abc = "";
		for (Token token: tokens) {
			if (token.type == Token.CHORD) {	
				abc += DQ + token.absVal + DQ;
			} else {
				abc += token.absVal;
			}
		}	
		return abc + PIPE;
	}
	
	/**
	 * @return bar in '.chords' notation
	 */
	public String toChords() {
		String chords = "";
		for (Token token: tokens) {
			if (token.type == Token.CHORD) {	
				chords += token.absVal + COMMA;
			}
		}	
		if (chords.length() > 0) {
			chords = chords.substring(0, chords.length() - 1);
		}
		
		return chords + PIPE;
	}
}
