package audio.abc1;

import java.util.*;

public class Signature {
	public String key = "";
	public int parity = 0;
	public String notes = "";
	public List<String> list = new ArrayList<String>();

	public boolean contains(String note) {
		return list.contains(note);
	}
	
	public String getAlteredNote(String note) {
		return (parity == -1) ? "_" + note : "^" + note ;
	}
	
	
	public String toString() {
		return key + " " + parity + " " + notes;
	}
}
