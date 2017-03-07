package audio.chords.parser.chordtype;

public class Desc {
	String src = "";
	String val = "";
	
	public Desc(String src, String val) {
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
