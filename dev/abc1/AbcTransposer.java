package audio.abc1;

import static audio.Constants.ALPHABET_TOKENS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class AbcTransposer {
	/** The log. */
	private Logger log 					= Logger.getLogger(getClass());
	int alphabeticInterval 				= 0; 
	int absoluteInterval 				= 0; 
	/** Transposition map, eg. 'Db' -> 'Bb'. Chordtype info is removed. */
	public Map<String, String> chordTranspositionMap = new HashMap<String, String>();
	public Map<String, String> abcTranspositionMap = new HashMap<String, String>();
	
	public AbcTransposer(String from, String to) {
		log.debug(from + " -> " + to);
		
		String alphbeticalTokenFrom = getAlphbeticalToken(from); 
		String alphbeticalTokenTo 	 = getAlphbeticalToken(to);
		log.debug(alphbeticalTokenFrom + " -> " + alphbeticalTokenTo);
		
		// eg. C -> D = 1, B -> Bb = 0
		alphabeticInterval = 
				AbcUtil.getIndex(ALPHABET_TOKENS, alphbeticalTokenTo) - AbcUtil.getIndex(ALPHABET_TOKENS, alphbeticalTokenFrom);
		
		// eg. C -> D = 2, B -> Bb = -1
		absoluteInterval = 
				AbcMaps.noteToInterval.get(to.replace("m", "")) - AbcMaps.noteToInterval.get(from.replace("m", ""));

		log.debug("alphabeticalInterval=" + alphabeticInterval);
		log.debug("absoluteInterval=" + absoluteInterval);
	}
	
	/**
	 * @param chordName
	 * @param alphabeticInterval
	 * @param absoluteInterval
	 * @return
	 */
	public String transposeChord(String chordName) {
		//log.debug("chordName, alphabeticInterval, absoluteInterval=" + chordName + ", " + alphabeticInterval + ", " + absoluteInterval);
		
		String pitch 				= ""; // eg. C#
		String type 				= ""; // eg. m7
		String transposedPitch		= ""; // eg. G
		
		int len = chordName.length();
		if (len == 1) { 
			pitch = chordName;
		} else if (len > 1) {
			if (chordName.substring(1, 2).equals("#") || chordName.substring(1, 2).equals("b")) {
				pitch = chordName.substring(0, 2);
				if (len > 2) {
					type = chordName.substring(2);
				}
				//modifier = chordName.substring(1, 2);
			} else {
				pitch = chordName.substring(0, 1);
				type = chordName.substring(1);
			}
		} 		
		//log.debug("pitch, modifier, type=" + pitch + ", " + modifier + ", " + type);
		
		if (chordTranspositionMap.containsKey(pitch)) {
			transposedPitch = chordTranspositionMap.get(pitch); 
		} else {
			String alphabeticPitch 				= pitch.substring(0, 1);
			//log.debug("alphabeticPitch=" + alphabeticPitch);
			
			int alphabeticIndex 				= AbcUtil.getIndex(ALPHABET_TOKENS, alphabeticPitch);
			//log.debug("alphabeticIndex=" + alphabeticIndex);
			
			int transposedAlphabeticIndex 		= AbcUtil.getRemainder((alphabeticIndex + alphabeticInterval), 7);
			//log.debug("transposedAlphabeticIndex=" + transposedAlphabeticIndex);
			String transposedAlphabeticPitch 	= ALPHABET_TOKENS[transposedAlphabeticIndex];
			//log.debug("transposedAlphabeticPitch=" + transposedAlphabeticPitch);
			
			int intervalIndex 					= AbcMaps.noteToInterval.get(pitch);
			//log.debug("intervalIndex=" + intervalIndex);
			int transposedIntervalIndex			= AbcUtil.getRemainder((intervalIndex + absoluteInterval), 12);
			//log.debug("transposedIntervalIndex=" + transposedIntervalIndex);
			List<String> notes					= AbcMaps.intervalToNotes.get(transposedIntervalIndex);
			//log.debug(Util.arrToString(arr));
			for (String note: notes) {
				if (note.startsWith(transposedAlphabeticPitch)) {
					transposedPitch = note;
					break;
				}
			}
			
			// eg. if transposedPitch = 'Dbb', then substitute the middle value from 
			//     the arr 'B#,  C,  Dbb"
			if (transposedPitch.length() == 3) {
				transposedPitch = notes.get(1);
			}
			
			// add to transpositionMap 
			chordTranspositionMap.put(pitch, transposedPitch);
			//log.debug("added " + pitch + "->" + transposedPitch + " to chordTranspositionMap");
		}
 
		//log.debug("transposedPitch=" + transposedPitch);
		return transposedPitch + type;
	}
	
	public String transposeAbcNote(String note) {
		//log.debug("note=" + note);
		String transposedNote = "";
		
		if (abcTranspositionMap.containsKey(note)) {
			transposedNote = abcTranspositionMap.get(note); 
			//log.debug("map: transposedNote=" + transposedNote);
		} else {
			String alphabeticNote 				= note.replace("_", "").replace("^", "").replace(",", "").replace("'", "").toUpperCase();
			int alphabeticIndex 				= AbcUtil.getIndex(ALPHABET_TOKENS, alphabeticNote);
			int transposedAlphabeticIndex 		= AbcUtil.getRemainder((alphabeticIndex + alphabeticInterval), 7);
			String transposedAlphabeticNote 	= ALPHABET_TOKENS[transposedAlphabeticIndex];
			//log.debug("alphabeticNote -> transposedAlphabeticNote=" + alphabeticNote + " -> " + transposedAlphabeticNote);
			
			int intervalIndex 					= AbcMaps.noteToIntervalAbc.get(note);
			int transposedIntervalIndex			= intervalIndex + absoluteInterval;
			List<String> notes					= AbcMaps.intervalToNotesAbc.get(transposedIntervalIndex);
			//log.debug(Util.listToString(notes));
			for (String noteStr: notes) {
				if (noteStr.toUpperCase().contains(transposedAlphabeticNote)) {
					transposedNote = noteStr;
					break;
				}
			}
			
			// eg. if transposedPitch = 'Dbb', then substitute the middle value from 
			//     the arr 'B#,  C,  Dbb"
			//if (transposedPitch.length() == 3) {
			//	transposedPitch = notes.get(1);
			//}
			
			// add to transpositionMap 
			abcTranspositionMap.put(note, transposedNote);
			//log.debug("added " + note + "->" + transposedNote + " to noteTranspositionMap");
		}
 
		
		return transposedNote;
	}
	
	/**
	 * @param s
	 * @return C# -> C, Db -> D, Cm -> C
	 */
	private String getAlphbeticalToken(String s) {
		return s.replace("b", "").replace("#", "").replace("m", "");
	}
}
