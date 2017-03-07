package audio.chords.parser.chordtype;

import static audio.Constants.NL;

import java.util.ArrayList;
import java.util.List;

public class ChordType {
	public String intervalStr	= "";
	public List<Symbol> symbols	= new ArrayList<Symbol>();
	public List<Desc> descs		= new ArrayList<Desc>();

	public ChordType(String intervalStr) {
		this.intervalStr = intervalStr;
	}

	public String toHtml(int count, String abcSymbol) {
		StringBuffer sb  		= new StringBuffer();
		List<String> symbolVals = new ArrayList<String>();
		List<String> descVals 	= new ArrayList<String>();
		
		sb.append("<tr valign='top'>");
		sb.append("<td align='right'><span class='gray'>" + count + "</span></td>");
		sb.append("<td>" + intervalStr + "</td>");
		sb.append("<td>" + abcSymbol + "</td>" + NL);
		
		
		sb.append("<td>");
		for (Symbol symbol: symbols) {
			String val = symbol.val;
			if (!symbolVals.contains(val)) {
				symbolVals.add(val);
				sb.append(symbol.toHtmlString() + "<br />");
			}
		}
		sb.append("</td>");

		sb.append("<td>");
		for (Desc desc: descs) {
			String val = desc.val;
			if (!descVals.contains(val)) {
				descVals.add(val);
				sb.append(desc.toHtmlString() + "<br />");
			}
		}
		sb.append("</td>");
		
		sb.append("</tr>" + NL);
		
		return sb.toString();
	}
}
