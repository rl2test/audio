package audio.abc;

public class AbcChord extends Token {

	public AbcChord(String val) {
		type = CHORD;	
		this.absVal = val;
	}	
}
