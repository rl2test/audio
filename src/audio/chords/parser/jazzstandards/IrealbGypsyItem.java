package audio.chords.parser.jazzstandards;

import audio.Util;

public class IrealbGypsyItem {
	public String title 		= "";
	public String composer 		= "";
	public String titleKey		= "";
	public boolean matchFound	= false;

	public IrealbGypsyItem (String line) {
		line = line.trim();
		
		// - 52nd Street Theme
		
		// remove starting '- ' and ending ')
		line = line.substring(2);
		
		title = Util.formatTitle(line);
		titleKey = Util.getTitleKey(title);
	}
}