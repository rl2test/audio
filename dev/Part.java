package audio.chords;

import java.util.ArrayList;
import java.util.List;

import audio.Util;

public class Part implements ChordsConstants {
	public String name 			= "";
	public String sequence 		=  "";
	public boolean repeat		= false;
	public int numRepeats		= 1;
	public List<Bar> bars		= new ArrayList<Bar>();
	
	/**
	 * @param sequence
	 * @throws Exception
	 */
	public void init(String sequence) throws Exception {
		this.sequence = sequence; // store the unparsed sequence

		// set the 'repeat' property
		if (sequence.contains("[") && sequence.contains("]")) {
			int pos = sequence.lastIndexOf(COLON);
			numRepeats = Integer.parseInt(sequence.substring(pos + 1, pos + 2));
			repeat = true;
		} 
		
		// normalize the sequence
		sequence = normalize(sequence);
		
		int len = sequence.length();
		if (len > 0) {

			String[] barStrs = sequence.split(PIPE_DELIM, - 1);

			for (String barStr: barStrs) {
				//if (!Mapstore.bars.containsKey(barStr)) { 
				//	Mapstore.bars.put(barStr, new Bar(barStr));
				//}
				bars.add(new Bar(barStr));
			}
		}
	}
	
	/**
	 * Remove spaces, expand '{}' sections, remove '[:n]' tokens, remove pipe at 
	 * start and end of sequence if it exists.
	 * 
	 * @param sequence
	 * @return normalized sequence
	 */
	private String normalize(String sequence) throws Exception {
		// remove spaces
		sequence = sequence.replace(SPACE, "");
		
		// expand inner repeats
		sequence = Util.expandSequence(sequence, "{", "}");

		// expand outer repeats
		//chordSequence = ChordsUtil.expandChordSequence(chordSequence, "[", "]");

		// remove outer repeats - boolean repeat will have been set to true for this part
		sequence = sequence.replace("[", "").replace(":" + numRepeats + "]", "");
		
		//if (sequence.contains("]")) {
		//	// currently only ':2]' is accepted
		//	throw new Exception("invalid repeat syntax in chordSequence=" + sequence);
		//}
		
		// remove pipe at start of sequence if it exists
		if (sequence.startsWith(PIPE)) {
			sequence = sequence.substring(1);
		}
		
		// trim final pipe
		if (sequence.endsWith(PIPE)) {
			sequence = sequence.substring(0, sequence.length() - 1);
		}
		
		return sequence;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[Part]" + 
				"name=" + name + CSV + 
				"sequence=" + sequence + CSV + 
				"repeat=" +  repeat + CSV +
				"numRepeats=" +  numRepeats + NL);
		for (Bar bar: bars) {
			sb.append(bar + NL);
		}
		
		return sb.toString();
	}
}
