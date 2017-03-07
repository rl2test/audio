package audio.jazz.chordscales;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO add mapping for C -> C#
 * 
 * A Tone represents a musical pitch, but without reference to a particular 
 * octave. The tone may be defined by either two or three note strings, eg.
 * "B#  C  Dbb"
 */
public class Tone implements ChordscaleConstants {
	public int index = 0;
	public List<String> noteStrs = new ArrayList<String>();
	
	/**
	 * @param str
	 */
	public Tone(String str, int index) {
		this.index = index;
		String[] arr = str.trim().split(" ");
		for (String s: arr) {
			if (!s.equals("")) {
				noteStrs.add(s);
			}
		}
	}
	
	/**
	 * @param s
	 * @return boolean indicating that this tone contains the noteStr equal to s 
	 */
	public boolean contains (String s) {
		for (String noteStr: noteStrs) {
			if (noteStr.equals(s)) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = index + " ";
		for (String noteStr: noteStrs) {
			s += noteStr + SPACE;
		}
		return s;
	}
}
