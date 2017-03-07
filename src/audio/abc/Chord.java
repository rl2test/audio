package audio.abc;

public class Chord {
	String root = ""; // eg. C#
	String type = ""; // eg. m7
	
	public Chord(String name) {
		int len = name.length();
		if (len == 1) { 
			root = name;
		} else if (len > 1) {
			if (name.substring(1, 2).equals("#") || name.substring(1, 2).equals("b")) {
				root = name.substring(0, 2);
				if (len > 2) {
					type = name.substring(2);
				}
			} else {
				root = name.substring(0, 1);
				type = name.substring(1);
			}
		} 		
	}

	public String toString() {
		return "root=" + root + ", type=" + type;
	}
}
