package audio.chords.parser.jazzstandards;

import static audio.Constants.PIPE;
import audio.Util;

/**
 * This class represents a record from 'RealbookJazzLTDindex-out.txt' in the data dir.
 */
public class RealbookItem {
	public String title 		= "";
	public String pageNum 		= "";
	public String rbNum 		= "";
	public String titleKey		= "";
	public String rb 			= "";
	public boolean matchFound 	= false;
	
	public RealbookItem(String s) {
		// 52nd Street Theme|93|2
		String[] arr = Util.split(s, PIPE, 3);
		
		int i 		= 0;
		title 		= Util.formatTitle(arr[i++]); 
		pageNum		= arr[i++]; 
		rbNum 		= arr[i++]; 
		
		// key has no start or trailing 'The'
		titleKey = Util.getTitleKey(title);

		// rb is a concatenation of vol and page numbers
		rb = rbNum + "_" + pageNum; // 1-123  
	}
}
