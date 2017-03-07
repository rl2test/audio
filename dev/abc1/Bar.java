package audio.abc1;

import static audio.Constants.COMMA;
import static audio.Constants.DQ;
import static audio.Constants.EQUALS;
import static audio.Constants.PIPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Bar {
	/** The log. */
	private Logger log = Logger.getLogger(getClass());
	public String key = "";
	public List<Token> tokens = null;
	// a mapping of all notes matching a specific note, ie note->notes
	public Map<String, List<AbcNote>> map = new HashMap<String, List<AbcNote>>();
	public List<String> mapKeys = new ArrayList<String>(); // keys to map
	public Signature signature = null;
	public static final int MAX_INT_VALUE = 38; // d'
	
	public Bar(String key, List<Token>tokens) {
		this.key = key;
		this.tokens = tokens;
		for (Token token: tokens) {
			if (token.type == Token.NOTE) {
				AbcNote abcNote = (AbcNote) token; 
				if (!map.containsKey(abcNote.unalteredValue)) {
					map.put(abcNote.unalteredValue, new ArrayList<AbcNote>());
					mapKeys.add(abcNote.unalteredValue);
				}
				map.get(abcNote.unalteredValue).add(abcNote);
			}
		}
		signature = AbcMaps.signatures.get(key);	
	}
	
	public void toAbsolute() {
		// convert each map to absolute values
		for (String mapKey: mapKeys) {
			List<AbcNote> abcNotes = map.get(mapKey);
			//log.debug(mapKey + " " + abcNotes.size());
			for (int i = 0, n = abcNotes.size(); i < n; i++) {
				AbcNote abcNote = abcNotes.get(i);
				String displayValue = abcNote.displayValue;
				if (Main.isAltered(displayValue)) {
					if (displayValue.startsWith(EQUALS)) {
						// remove '='
						abcNote.setAbsValue(abcNote.unalteredValue);
					} else {
						abcNote.setAbsValue(displayValue);
					}
				} else {
					if (i == 0) { 
						if (signature.notes.indexOf(abcNote.alphaValue) != -1) {
							String value = (signature.parity == -1) ? "_" + displayValue : "^" + displayValue;	
							abcNote.setAbsValue(value);
						} else {
							abcNote.setAbsValue(displayValue);
						}
					} else {
						abcNote.setAbsValue(abcNotes.get(i - 1).absValue);
					}									
				}
			}
		}
	}
	
	public void toDisplay() {
		// convert each map to display values, checking for highest note
		int highestIntValue = 0;
		for (String mapKey: mapKeys) {
			List<AbcNote> abcNotes = map.get(mapKey);
			//log.debug(mapKey + " " + abcNotes.size());
			for (int i = 0, n = abcNotes.size(); i < n; i++) {
				AbcNote abcNote = abcNotes.get(i);
				if (abcNote.intValue > highestIntValue) {
					highestIntValue = abcNote.intValue;
				}
				if (i == 0) {
					if (signature.notes.indexOf(abcNote.alphaValue) != -1) {
						if (signature.parity == abcNote.incidental) {	
							abcNote.displayValue = abcNote.unalteredValue;
						} else if (abcNote.incidental == 0) {
							abcNote.displayValue = EQUALS + abcNote.absValue;
						} else {
							abcNote.displayValue = abcNote.absValue;
						}
					} else {
						abcNote.displayValue = abcNote.absValue;
					}
					
				} else {
					if (abcNote.absValue.equals(abcNotes.get(i - 1).absValue)) {
						abcNote.displayValue = abcNote.unalteredValue;
					} else {
						if (abcNote.incidental == 0) {
							abcNote.displayValue = EQUALS + abcNote.absValue;
						} else {
							abcNote.displayValue = abcNote.absValue;
						}
					}
				}	
			}
		}
		if (highestIntValue > MAX_INT_VALUE) {
			for (Token token: tokens) {
				if (token.type == Token.NOTE) {
					AbcNote abcNote = (AbcNote) token; 
					//abcNote.transposeDownOctave();
				}
			}
		}
	}
	
	public String toAbcDisplay() {
		String abc = "";
		for (Token token: tokens) {
			if (token.type == Token.CHORD) {	
				abc += DQ + token.absValue + DQ;
			} else if (token.type == Token.NOTE) {
				AbcNote abcNote = (AbcNote) token;
				abc += abcNote.displayValue;
			} else {
				abc += token.absValue;
			}
		}	
		return abc + PIPE;
	}
	
	public String toAbcAbsolute() {
		String abc = "";
		for (Token token: tokens) {
			if (token.type == Token.CHORD) {	
				abc += DQ + token.absValue + DQ;
			} else {
				abc += token.absValue;
			}
		}	
		return abc + PIPE;
	}
	
	public String toChords() {
		String chords = "";
		for (Token token: tokens) {
			if (token.type == Token.CHORD) {	
				chords += token.absValue + COMMA;
			}
		}	
		if (chords.length() > 0) {
			chords = chords.substring(0, chords.length() - 1);
		}
		
		return chords + PIPE;
	}
}
