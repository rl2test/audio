package audio.chords.parser.jazzstandards;

import audio.Util;

public class IrealbItem {
	public String title 		= "";
	public String composer 		= "";
	public String titleKey		= "";
	public boolean matchFound	= false;

	public IrealbItem (String line) {
		line = line.trim();
		
		// - 52nd Street Theme (Thelonious Monk)
		
		// remove starting '- ' and ending ')
		line = line.substring(2, line.length() - 1);
		
		int pos = line.lastIndexOf("(");
		title = Util.formatTitle(line.substring(0, pos));
		composer = line.substring(pos + 1);
		
		titleKey = Util.getTitleKey(title);
	}
}