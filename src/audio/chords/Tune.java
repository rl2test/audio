package audio.chords;

import static audio.Constants.PARENS_CLOSE;
import static audio.Constants.NL;
import static audio.Constants.PARENS_OPEN;
import static audio.Constants.PIPE;
import static audio.Constants.PIPE_DELIM;
import static audio.Constants.SPACE;
import static audio.Constants.US;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import audio.Maps;
import audio.Transposer;
import audio.Util;

/**
 * This class represents a Tune object derived from a '.chords' file. Every tune
 * has an array of bars. Each bar has an array of 0 or more chord symbols. If 
 * the length of the chord symbols array is 0, the bar will be populated with 
 * the last defined chord. Note: if the previously defined bar contains more 
 * than one chord, only the last defined chord will be used to populate the 
 * empty bar: eg. (assuming 4 beatsPerBar)
 * 
 * 		'C, G | |' -> 'C, G | G |' -> 'C C G G | G G G G |'   
 * 
 * Each chord references a chordType and a scaleType, which are stored and 
 * retrieved from maps. In addition, bars and chords are also stored and 
 * retrieved from maps.
 * 
 * Sample '.chords' file:
 * 
 *		@4|60-120|5
 *      ! C: Aebersold
 *		[{C7 | :2}
 *		F7 | Bb7 | :2]
 *		F#7 | B7 | E7 | A7 | {D7 | G7 | :2}
 */
public class Tune {
	/** The log. */
	private Logger log 				= Logger.getLogger(getClass());
	//public String genre				= "";
	/** The text of this tune. */
	public String text 				= "";
	public String errorMsg 			= "";
	public int time 				= 0;
	public int type					= 0;
	public int beginTempo			= 0;
	public int endTempo				= 0;
	public int increment    		= 0;    
	public List<Bar> bars			= new ArrayList<Bar>();
	/** The actual key, if specified in the tune info section. Empty if not specified. */
	public String key 				= "";
	/** 
	 * The key to transpose from, equal to the 'key' property without any 
	 * maj/m/min information. Empty if key property is not specified.
	 */
	public String transposeFrom 	= "";
	/** 
	 * The key to transpose to. Empty if transposition is not specified.
	 */
	public String transposeTo 	= "";
	/** Transposition map, eg. 'Db' -> 'Bb'. Chordtype info is removed. */
	public Map<String, String> transpositionMap = new HashMap<String, String>();
	/** Flag indicating that this tune has been transposed. */
	public boolean transposed = false;
	
	/**
	 * @param genre
	 * @param text
	 */
	public Tune(String text, String transposeTo) throws Exception { //String genre, 
		Maps.bars.clear();
		
		//this.genre 			= genre;
		this.text 			= text;
		this.transposeTo 	= transposeTo;

		String sequence = "";
		String[] lines = text.split(NL);
		
		for (String line: lines) {
			line = line.trim();
		
			if (line.length() > 0)  {
				if (line.startsWith("@"))  {
					// directive, eg. @4|60-120|5
					line = line.substring(1);
					String[] tokens = line.split(PIPE_DELIM);
					int len = tokens.length;

					int timeType = Integer.parseInt(tokens[0]);

					if (time > 9) {
						type = time % 10;
						time = time / 10;
					} else {
						time = timeType;
						type = 1;
					}
					
					if (len > 1) {
						String tempo = tokens[1];
						if (tempo.contains("-")) {
							String[] tempos = tempo.split("-");
							beginTempo	= Integer.parseInt(tempos[0]);
							endTempo	= Integer.parseInt(tempos[1]);
						} else {
							beginTempo	= Integer.parseInt(tempo);
							endTempo	= beginTempo;
						}
					}
					if (len > 2) {
						increment = Integer.parseInt(tokens[2]);
					}

				} else if (line.startsWith("!"))  {
					// info
					// remove spaces
					line = line.replace(" ", "");
					// ! K: F
					if (line.startsWith("!K:"))  {
						key = line.substring(3);
						transposeFrom = key.replace("maj", "").replace("min", "").replace("m", "");
					}
				}  else if (line.startsWith("%"))  {
					// annotation
					String annotation = (line.length() > 1) 
							? line.substring(1).trim()
							: "";
					log.debug("annotation=" + annotation);
					annotation = annotation.replace(SPACE, US);
					sequence += "<" + annotation + ">";	
				} else {
					line = stripChordscales(line);
					sequence += line;
				}
			}
		}
		log.debug("sequence=" + sequence);
		
		String expandedSequence = expandSequence(sequence);
		log.debug("expandedSequence=" + expandedSequence);
		
		String[] barStrs = expandedSequence.split(PIPE_DELIM, - 1);
		log.debug("barStrs.length=" + barStrs.length);

		if (	!transposeFrom.equals("") &&
				!transposeTo.equals("") && 
				!transposeTo.equals(transposeFrom)) {
			Transposer transposer = new Transposer(transposeFrom, transposeTo);
			barStrs = transpose(barStrs, transposer);
			log.debug("transposedExpandedSequence=" + Util.arrToString(barStrs, PIPE));
			transposed = true;
		}

		setBars(time, barStrs);
	}
	
	/**
	 * @param expandedSequence
	 * @return expandedSequence transposed
	 */
	private String[] transpose(
			String[] barStrs,
			Transposer transposer) {
		String[] transposedBarStrs = new String[barStrs.length];
		
		// expandedSequence=<A>Cmaj7||E7|E7|A7|A7|Dm|Dm|E7|E7|Am|Am|D7|D7|Dm7|G7|<B>Cmaj7|Cmaj7|E7|E7|A7|A7|Dm|Dm|F|Fm|Cmaj7,Em7|A7|Dm7|G7|C6,Ebdim7|Dm7,G7
		// note: expandedSequence may contain annotations ('<A>') and empty bars ('||')\
		
		int i = 0;
		for (String barStr: barStrs) {
			transposedBarStrs[i++] = transposeBarStr(
					barStr, 
					transposer); 
		}
		
		return transposedBarStrs;
	} 
	
	private String transposeBarStr(
			String barStr, 
			Transposer transposer) {
		// accepted formats: 
		//   'C'		-> expanded if beatsPerBar = 4 to, e.g. 'C,C,C,C'
		//   'C,F'		-> expanded if beatsPerBar = 4, e.g. 'C,C,F,F' 
		//   'C,F,Em'	-> as written if beatsPerBar = 3
		//   'C,F,Em,G'	-> as written if beatsPerBar = 4
		//   any of the preceding preceded by an annotation:
		//   '<A>'		-> A 
		String transposedBarStr	= "";
		String annotationStr 	= "";
		
		if (barStr.startsWith("<") && barStr.contains(">")) {
			int pos =  barStr.indexOf(">");
			annotationStr = barStr.substring(0, pos + 1);
			barStr = barStr.substring(pos + 1);
		}
		
		if (barStr.trim().equals("")) {
		} else if (barStr.contains(",")) {
			// multiple chords
			String[] chordNames = barStr.split(",");

			for(String chordName: chordNames) {
				//transposedBarStr += transposeChordName(chordName, alphabeticalInterval, absoluteInterval) + ","; 
				transposedBarStr += transposer.transposeChord(chordName) + ",";
			}
			transposedBarStr = transposedBarStr.substring(0, transposedBarStr.length() - 1); 
		} else {
			String chordName = barStr;
			//transposedBarStr += transposeChordName(chordName, alphabeticalInterval, absoluteInterval);
			transposedBarStr += transposer.transposeChord(chordName);
		}
		
		return annotationStr + transposedBarStr;
	}
	
	/**
	 * @param beatsPerBar
	 * @param barStrs
	 */
	private void setBars(int beatsPerBar, String[] barStrs) throws Exception {
		for (String barStr: barStrs) {
			Bar bar = null;
			if (barStr.equals("")) {
				Bar precedingBar = bars.get(bars.size() - 1);
				String precedingChordName = precedingBar.chords.get(precedingBar.chords.size() - 1).name; 
				barStr = precedingChordName;
			}
			
			bar = new Bar(beatsPerBar, barStr);
			// Note that at this point the new bar is uninitialized, only the 
			// sequence has been set.

			// A tune will typically contain more the one instance of a bar, as 
			// defined by the barStr (eg. 'C,G7'), so bars are stored in 
			// a map, referenced by the barStr. If an instance of a particular 
			// bar already exists in the map, then that instance will be retrieved 
			// and added to this tune's 'bars' array. If no existing instance is 
			// found, the new bar will be initialized and added to the map, and 
			// then added to this tune's 'bars' array 
			if (!Maps.bars.containsKey(barStr)) {
				bar.init();
				Maps.bars.put(barStr, bar);
			}
			bars.add(Maps.bars.get(barStr));
		}
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
	
	private String stripChordscales(String s) {
		//log.debug(s);
		String ret 			= "";
		
		while (s.contains(PARENS_OPEN)) {
			ret += s.substring(0, s.indexOf(PARENS_OPEN));
			//log.debug("ret=" + ret);
			s = s.substring(s.indexOf(PARENS_CLOSE) + 1);
			//log.debug("s=" + s);
		}
		ret += s;
		
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb  = new StringBuffer();
		
		sb.append(NL + "[Tune]" + NL);
		//sb.append("genre       = " + genre + NL);
		sb.append("text        = " + NL + text + NL);
		sb.append("beatsPerBar = " + time + NL);
		sb.append("beginTempo  = " + beginTempo + NL);
		sb.append("endTempo    = " + endTempo + NL);
		sb.append("increment   = " + increment + NL);    

		for (Bar bar: bars) {
			sb.append(bar + NL);
		}
		
		return sb.toString();
	}
}

