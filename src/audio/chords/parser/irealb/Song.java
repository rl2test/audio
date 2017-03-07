package audio.chords.parser.irealb;

import static audio.Constants.EXT_CHORDS;
import static audio.Constants.NL;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import audio.Util;

public class Song {
	/** The log. */
	private Logger log 					= Logger.getLogger(getClass());
	public String title 				= "";
	public String composer 				= "";
	public String style 				= "";
	public String key 					= "";
	public String token 				= "";
	public String tuneStr 				= "";
	public boolean hasRepeats			= false;
	public String source				= "";	
	
	/**
	 * Constructor taking string param from irealb file.
	 * 
	 * @param s
	 */
	public Song(String s, String source) {
		//26-2=Coltrane John=Medium Up Swing=F=n=*A[T44F^7 Ab7 |Db^7 E7 |A^7 C7 |C-7 F7 |Bb^7 Db7 |Gb^7 A7 |D-7 G7 |G-7 C7 ]*A[F^7 Ab7 |Db^7 E7 |A^7 C7 |C-7 F7 |Bb^7 Ab7 |Db^7 E7 |A^7 C7 |F^7   ]*B[C-7 F7 |E-7 A7 |D^7 F7 |Bb^7   |Eb-7   |Ab7   |Db^7   |G-7 C7 ]*A[F^7 Ab7 |Db^7 E7 |A^7 C7 |C-7 F7 |Bb^7 Ab7 |Db^7 E7 |A^7 C7 |F^7   Z
		//500 Miles High=Corea Chick=Bossa Nova=E-=n=[T44E-7   | x  |G-7   | x  |Bb^7   | x  |Bh7   |E7#9   |A-7   | x  |F#h7   | x  |F-7   | x Q |C-7   | x  |B7#9   | x  Z        Y{QC-7   | x  |Ab^7   | x  }

		String[] arr = s.split("=");
		int i = 0;
		this.title 		= arr[i++];
		this.composer 	= arr[i++];
		this.style 		= arr[i++];
		this.key 		= arr[i++];
		this.token 		= arr[i++];
		this.tuneStr 	= arr[i++];
		this.source 	= source;
		
		title = title.replace("?", "");
		
		String[] cArr = composer.split(" ");
		int len = cArr.length; 
		if (len > 1) {
			this.composer = cArr[1] + " " + cArr[0];  
		}
	}
	
	/**
	 * Save irealb song as '.chords' file.
	 * 
	 * @param dir
	 */
	public void saveAsChordFile(File dir) {
		File file = new File(dir, title + EXT_CHORDS);
		StringBuffer sb = new StringBuffer();
		
		// 26-2                                 *A[T44F^7 Ab7 |Db^7 E7 |A^7 C7 |C-7 F7 |Bb^7 Db7 |Gb^7 A7 |D-7 G7 |G-7 C7 ]*A[F^7 Ab7 |Db^7 E7 |A^7 C7 |C-7 F7 |Bb^7 Ab7 |Db^7 E7 |A^7 C7 |F^7   ]*B[C-7 F7 |E-7 A7 |D^7 F7 |Bb^7   |Eb-7   |Ab7   |Db^7   |G-7 C7 ]*A[F^7 Ab7 |Db^7 E7 |A^7 C7 |C-7 F7 |Bb^7 Ab7 |Db^7 E7 |A^7 C7 |F^7   Z
		// 500 Miles High                       [T44E-7   | x  |G-7   | x  |Bb^7   | x  |Bh7   |E7#9   |A-7   | x  |F#h7   | x  |F-7   | x Q |C-7   | x  |B7#9   | x  Z        Y{QC-7   | x  |Ab^7   | x  }
		// 502 Blues                            {T34A-7   |Db^7   |Bh7   |E7#9   |A-7   |Db^7   |Bh7   |E7#9   |C-7   |F7b9   |Bb^7   |Ab-7 Db7 |N1F#h7   |B7b9   |E^7#5   |E^7#5 E7 }|N2F#h7   |B7b9   |E-7   | x  Z
		// 52nd Street Theme                    {*AT44C A-7 |D-7 G7 |C A-7 |D-7 G7 |C A-7 |D-7 G7 |C G7 |C   }[*BC7,   | x  |F6   | x  |D7,   | x  |G7   | x  ][*AC, A-7 |D-7 G7 |C A-7 |D-7 G7 |C A-7 |D-7 G7 |C G7 |C   Z 
		// 9.20 Special                         {*AT44C9,   |Eb-6,   |C9,   |Eb-6   |Bb,   |sBb7,A7,Ab7,G7|N1lC9, F#o7, |C9, sAb7,G7}        |N2lC9, F#o7 |lC9, sF7,Bb][*BlBb,   |Eb, Eb6 |Eb6,   | x  |G9,   |F, F6, |F9,   |  F7 ][*AC9,   |Eb-6,   |C9,   |Eb-6,   |Bb,   |sBb7,A7,Ab7,G7|lC9, F#o7 ,|sC6,F7,Bb,D9Z 
		// A Felicidade                         *A{T44A-7(C^7)   | x  |C^7   | x  |E-7   |B7b9   |E-7 <(Repeat Optional)>A7 |D-7 G7 }[*BC^7   | x  |Bh7   |E7b9   |A-7   | x (Ab-7) |G-7   |C7   |F^7   |D-7   |A-7   |D7   |A-7   |Bh7 E7b9 |A-7   |G7   ]*C[C^7 |F7 |C^7 |x |G-7 |C7 |F^7 |x |D-7 |G7 |C^7 |x |F#h7 |B7b9 |sE-7,A7,|D-7,G7,]*D[lA-7   |A-7/G   |D7/F#   |D-7/F   |A-7   |Bh7 E7b9 |A-7   | x  Z 
		// Ac-Cent-Tchu-Ate The Positive        {*AT44F^7 F+ |F6 F^7 |G-7   |C7   ||F^7 F+ |F6 F^7 |G-7 C7 |F6 C7 }[*BF6   |F7   |Bb^7 sG-7,C7,|lF6 C7 |F6   |D7b9   |G7#5   |C7   ][*AF^7 F+ |F6 F^7 |G-7   |C7   ||F^7 F+ |F6 F^7 |G-7 C7, |F6 C7 Z 
		// Across The Valley From The Alamo     {*AT44F^7,   | x  | x  | x  |C7,   | x  |N1G-7, C7, |F^7,   }        |N2G-7, C7, |F^7,   ]Y[*BBb^7,   |F^7,   |Bb^7, Bb-7, |F^7,   |A7,   |D-7, G7, |G7,   |C7,   ]Y[*AF^7,   | x  | x  | x  |C7,   | x  |G-7, C7, |F^7   Z 
		// Adam's Apple                         [T44Ab7   | x  | x  | x  | x  | x  | x  | x  |Gb7   | x  | x  | x  |Ab7   | x  | x  | x  |Ab-7   |Db7   |Bb-7   |Eb7#9   |Ab7   | x  | x  | x  Z
		// Afro Blue                            [T34F-7   |G-7   |Ab^7 G-7 |F-7   |F-7   |G-7   |Ab^7 G-7 |F-7   |Eb   | x  |Db Eb |F-7   |Eb   | x  |Db Eb |F-7   Z

		String section = "";	
		int beatsPerBar = 0;
		List<Bar> bars = new ArrayList<Bar>();
		Bar bar	= new Bar();
		
		//for (int i = 0, n = tuneStr.length(); i < n; i++) {
		String t = tuneStr;
		while (t.length() > 0) {
			String s = t.substring(0, 1);
			
			if (s.equals("*")) {
				// denotes a new section
				section = t.substring(1, 2);
				t = t.substring(2);
			} else if (s.equals("[") || s.equals("{") || s.equals("|")) {
				// denotes a new bar
				if (bar != null && bar.hasContent()) {
					bar.section = section;
					section = ""; // section has now been added to this bar, so clear section

					// add this bar to the array
					bars.add(bar);

					// now create a new bar
					bar = new Bar(beatsPerBar);
				}
				if (s.equals("{")) {
					bar.beginRepeat = true;
					hasRepeats = true;
				}
				t = t.substring(1);
			} else if (s.equals("T")) {
				beatsPerBar = Integer.parseInt(t.substring(1, 2)); // only use the first int of the time signature string, eg. 44 -> 4
				t = t.substring(3);
				if (bar != null) {
					bar.beatsPerBar = beatsPerBar; 
				}
			} else if (s.equals("x")) {
				bar.isDitto = true;
				t = t.substring(1);
			} else if (s.equals("Z")) {
				if (bar.hasContent()) {
					// add the final bar to the array
					bars.add(bar);
				}
				// break out of loop, ignoring coda if there is one
				break;
			} else if (s.equals("l") || s.equals("s") || s.equals("Q") || s.equals("Y")) {
				// ignore these for now - meaning is not known or not used
				t = t.substring(1);
			} else if (s.equals("]") || s.equals("}")) {
				// likely denotes a new bar, although it could be an empty bar
				if (bar != null && bar.hasContent()) {
					// add the previous bar to the array
					bars.add(bar);
					if (s.equals("}")) {
						bar.endRepeat = true;
					}
				}
				bar = new Bar();
				t = t.substring(1);
			} else if (s.equals("N")) {
				bar.N = Integer.parseInt(t.substring(1, 2)); 
				t = t.substring(2);
//			} else if (s.equals("")) {
//				
//			} else if (s.equals("")) {
				
			} else {
				bar.str += s;
				t = t.substring(1);
			}			
		}
			
		/*
		public static String[] ANNOTATION_STRINGS	= {
			"@4|80-120|10" + NL,		// tempo info
			"! C: ",					// composer
			"! A: ",					// author of lyrics	
			"! S: ",					// source
			"! R: ",					// rhythm
			"! K: ",					// key
			"% " + NL,					// new part
			"% A" + NL,					// A part 
			"% B" + NL,					// B part 
			"% C" + NL,					// C part
			"% D" + NL,					// D part
			"% 2nd time repeat" + NL,	// 2nd time repeat part
		}; 
		*/

		sb.append("@" + beatsPerBar + "|80-120|10" + NL);
		sb.append("! C: " + composer + NL);
		sb.append("! S: " + source + NL);
		sb.append("! R: " + style + NL);
		sb.append("! K: " + key + NL);

		int barCounter = 1;
		int toggleRepeatNum = 1;
		
		// testing block 1
		/*
		barCounter = 1;
		for (Bar br: bars) {
			log.debug(Util.padInt(barCounter, 2) + ". " + br.toString());
			barCounter++;
		}
		*/
		
		if (hasRepeats) {
			try {
				bars = expandRepeats(bars);
			} catch (Exception e) {
				log.error("unable to expand bars for" + title);	
				e.printStackTrace();
			}
		}
		
		// testing block 2
		/*
		barCounter = 1;
		log.debug("-----------------------------------------------------" + NL);
		for (Bar br: bars) {
			log.debug(Util.padInt(barCounter, 2) + ". " + br.toString());
			barCounter++;
		}
		*/
		
		barCounter = 1;
		toggleRepeatNum = 1;
		for (Bar br: bars) {
			if (br.hasSection() && toggleRepeatNum == 1) {
				if (barCounter % 4 != 1) {
					sb.append(NL);
					barCounter = 1; // set back to 1 to denote new line
				}
				sb.append("% " + br.section + NL);	
			} 
			
			if (br.beginRepeat) {
				if (toggleRepeatNum == 2) {
					if (barCounter % 4 != 1) {
						sb.append(NL);
						barCounter = 1; // set back to 1 to denote new line
					}
					if (br.hasSection()) {
						sb.append("% " + br.section + " 2nd time repeat" + NL);
					} else {
						sb.append("% 2nd time repeat" + NL);	
					}
						
				}
				toggleRepeatNum = (toggleRepeatNum == 1) ? 2 : 1;
			}
			
			sb.append(br.getChordString() + " | ");
			if (barCounter % 4 == 0) {
				sb.append(NL);
			}
			barCounter++;
		}
		
		Util.writeToFile(file, sb.toString());
	} 
	
	/**
	 *
	 */
	public class Repeat {
		List<Bar> bars 				= new ArrayList<Bar>();
		List<Bar> firstTimeBars 	= new ArrayList<Bar>();
		List<Bar> secondTimeBars 	= new ArrayList<Bar>();
		boolean hasFirstAndSecondTimeBars = false;
		
		public List<Bar> getAllBars() {
			List<Bar> allBars = new ArrayList<Bar>();
			
			allBars.addAll(bars);
			allBars.addAll(firstTimeBars);
			allBars.addAll(bars);
			
			return allBars;
		}
	}
	
	/**
	 * @param bars
	 * @return list of bars with repeats, including first- and second-time repeats, expanded 
	 */
	private List<Bar> expandRepeats(List<Bar> bars) {
		List<Bar> expandedBars = new ArrayList<Bar>();
		
		boolean inRepeat 			= false;
		boolean inFirstTimeRepeat 	= false;
		
		Repeat repeat = null;
		
		for (Bar bar: bars) {
			if (bar.beginRepeat) {
				inRepeat = true;
				repeat = new Repeat();
			}

			if (inRepeat) {
				if (bar.N == 1) {
					inFirstTimeRepeat = true;
					repeat.hasFirstAndSecondTimeBars = true;
				}
				
				if (inFirstTimeRepeat) {
					repeat.firstTimeBars.add(bar);
				} else {
					repeat.bars.add(bar);					
				}
			} else {
				expandedBars.add(bar);
			}
			
			if (bar.endRepeat) {

				// add repeat bars to expandedBars
				expandedBars.addAll(repeat.getAllBars());

				inRepeat = false;
				repeat = null;
			}
		}
		
		return expandedBars;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("title     = " + title + NL);
		sb.append("composer  = " + composer + NL);
		sb.append("style     = " + style + NL);
		sb.append("key       = " + key + NL);
		sb.append("token     = " + token + NL);
		sb.append("tune      = " + tuneStr + NL);
		
		return sb.toString();
	}
}

