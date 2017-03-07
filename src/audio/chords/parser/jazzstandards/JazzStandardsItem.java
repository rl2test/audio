package audio.chords.parser.jazzstandards;

import static audio.Constants.PIPE;
import static audio.Constants.TAB;
import static audio.Constants.Y;
import audio.Util;

/**
 * This class represents a record from 'JazzStandards.tsv' in the data dir.
 */
public class JazzStandardsItem {
	// fields from input file
	public String title 	= ""; 
	public String chrds 	= ""; 
	public String prnt		= ""; 
	public String mp3 		= ""; 
	public String rank 		= ""; 
	public String year 		= ""; 
	public String rB1 		= "";	// deprecated - see rb
	public String rB2 		= ""; 	// deprecated - see rb
	public String rB3		= ""; 	// deprecated - see rb
	public String nRB1 		= ""; 	// deprecated - see nrb
	public String nRB2 		= "";  	// deprecated - see nrb
	public String nRB3 		= "";  	// deprecated - see nrb
	public String srb 		= "";	// y - The Jazz Standards Real Book
	public String aebersold = "";
	// new fields
	public String jsc 		= "";	// y - jazzstandards.com
	public String rb 		= "";	// 1-123 - Real Book: vol-pageNum
	public String nrb 		= "";	// 1|2|3 - New Real Book: vol-pageNum
	public String irb 		= "";	// y - iRealB Jazz1200	
	public String irbg 		= "";	// y - iRealB GypsyJazz
	public String composer	= "";
	
	// map key derived from title
	public String titleKey	= "";
	
	int numInputFields 		= 14;
	
	/**
	 * Constructor taking a 'JazzStandards.tsv' record as a param.
	 * 
	 * @param s
	 */
	public JazzStandardsItem(String s) {
		String[] arr = Util.split(s, TAB, numInputFields);
		
		//int len = arr.length;
		//log.debug(len + "|" + s);
		
		int i 		= 0;
		title 		= Util.formatTitle(arr[i++]); 
		chrds 		= arr[i++]; 
		prnt 		= arr[i++]; 
		mp3 		= arr[i++]; 
		rank 		= arr[i++]; 
		year 		= arr[i++]; 
		rB1 		= arr[i++]; 	// deprecated field
		rB2 		= arr[i++]; 	// deprecated field
		rB3 		= arr[i++]; 	// deprecated field
		nRB1 		= arr[i++]; 	// deprecated field
		nRB2 		= arr[i++]; 	// deprecated field
		nRB3 		= arr[i++]; 	// deprecated field
		srb 		= arr[i++]; 
		aebersold 	= arr[i++];
		
		// populate new fields
		
		jsc 		= Y;
		
		if (rB3.equals(Y)) {
			rb = "3";
		}
		if (rB2.equals(Y)) {
			rb = "2";
		}
		if (rB1.equals(Y)) {
			rb = "1";
		}
		//log.debug(rB1 + "|" + rB2 + "|" + rB3 + ": " + rb);
		
		if (nRB3.equals(Y)) {
			nrb = "3";
		}
		if (nRB2.equals(Y)) {
			nrb = "2";
		}
		if (nRB1.equals(Y)) {
			nrb = "1";
		}
		
		titleKey = Util.getTitleKey(title);
	}
	
	/**
	 * Constructor taking a RealbookItem param.
	 * 
	 * @param realbookItem
	 */
	public JazzStandardsItem(RealbookItem realbookItem) {
		title 		= realbookItem.title; 
		rb 			= realbookItem.rb;
		titleKey 	= Util.getTitleKey(title);
	}
	
	/**
	 * Constructor taking a IrealbItem param.
	 * 
	 * @param irealbItem
	 */
	public JazzStandardsItem(IrealbItem irealbItem) {
		title 		= irealbItem.title; 
		irb 		= Y;
		composer	= irealbItem.composer;
		titleKey 	= Util.getTitleKey(title);
	}
	
	/**
	 * Constructor taking a IrealbGypsyItem param.
	 * 
	 * @param irealbGypsyItem
	 */
	public JazzStandardsItem(IrealbGypsyItem irealbGypsyItem) {
		title 		= irealbGypsyItem.title; 
		irbg 		= Y;
		titleKey 	= Util.getTitleKey(title);
	}

	/**
	 * @return a header row for records of this type
	 */
	public static String getHeader() {
		StringBuffer sb  = new StringBuffer();
		
		sb.append("title" 		+ PIPE);
		sb.append("composer" 	+ PIPE);
		sb.append("chrds" 		+ PIPE); 
		sb.append("prnt" 		+ PIPE); 
		sb.append("mp3" 		+ PIPE); 
		sb.append("rank" 		+ PIPE); 
		sb.append("year" 		+ PIPE); 
		sb.append("rb" 			+ PIPE);
		sb.append("nrb" 		+ PIPE);
		sb.append("srb" 		+ PIPE); 
		sb.append("aebersold"	+ PIPE);
		sb.append("irb" 		+ PIPE);
		sb.append("irbg" 		+ PIPE); // 13
		
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb  = new StringBuffer();
		
		sb.append(title 	+ PIPE);
		sb.append(composer 	+ PIPE);
		sb.append(chrds 	+ PIPE); 
		sb.append(prnt 		+ PIPE); 
		sb.append(mp3 		+ PIPE); 
		sb.append(rank 		+ PIPE); 
		sb.append(year 		+ PIPE); 
		sb.append(rb  		+ PIPE);
		sb.append(nrb 		+ PIPE);
		sb.append(srb 		+ PIPE); 
		sb.append(aebersold	+ PIPE);
		sb.append(irb 		+ PIPE);
		sb.append(irbg 		+ PIPE); // 13
		
		return sb.toString();
	}
}
