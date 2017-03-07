package audio.chords.parser.chordtypes;

import static audio.Constants.NL;

import java.util.ArrayList;
import java.util.List;

public class ChordType {
	/*
	%% ------------------|---------------------------------|-------------|-----------------------|------
	## chordIntervals    | symbols                         | abcSymbol   | scaleType             | notes
	%% ------------------|---------------------------------|-------------|-----------------------|------
	   1 2 5             |                                 | sus2        |                       |
	   1 3 #5            | #5, +                           | aug         | WHOLE-TONE            |
	 */
	public String integerStr = "";
	public String symbols = "";
	public String abcSymbol = "";

	public ChordType(String integerStr, String symbols, String abcSymbol) {
		this.integerStr = integerStr;
		this.symbols = symbols;
		this.abcSymbol = abcSymbol;
	}

	public String toHtml(int count, String abcSymbol) {
		StringBuffer sb  		= new StringBuffer();
		
		sb.append("<tr valign='top'>" + NL);
		sb.append("<td>" + integerStr + "</td>" + NL);
		sb.append("<td>" + symbols + "</td>" + NL);
		sb.append("<td>" + abcSymbol + "</td>" + NL);
		sb.append("</tr>" + NL);
		
		return sb.toString();
	}
}
