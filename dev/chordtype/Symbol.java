package audio.chords.parser.chordtype;

public class Symbol {
	String src = "";
	String val = "";

	public Symbol(String src, String val) {
		this.src = src;
		this.val = val;
	}
	
	public String toHtmlString() {
		return "<span class='" + src + "'>" + val + "</span>"; 
	}
	
	public String toString() {
		return src + " " + val; 
	}
}
