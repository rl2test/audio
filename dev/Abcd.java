package audio.gui.player;

import java.util.List;

import org.apache.log4j.Logger;

public class Abcd {
	/** The log. */
	protected Logger log 					= Logger.getLogger(this.getClass());
	public String L = "";
	public String K = "";
	double defaultNoteLength;	// as a fraction or multiple of a beat
	int pulsesPerDefaultNote = 1; // TODO get this from W: field in abcd file 
		
	public Abcd (List<String> lines) {
		/*
X:1
T:Khasid u Rabina
M:2/4
L:1/16
K:Gm
Q:1/4=60
|:D6^FE|D6^FE|D2^FED2^FE|D8|DBAG^F2G2|ABA6|DBAG^F2G2|AG^F6|DBAG^F2G2|ABC'BA3G|^FCDEFGFE|EDD6:| 
|:D    |D    |D         |D |Gm       |D   |Gm       |D    |D        |D       |D        |D   :| 
		 */
		
		for(String line: lines) {
			if (line.startsWith("X:")) {
				
			} else if (line.startsWith("T:")) {
				
			} else if (line.startsWith("M:")) {

			} else if (line.startsWith("L:")) {
				L = line.substring(2).trim();
				String[] arr = L.split("/");
				int num = Integer.parseInt(arr[0]);
				int denom = Integer.parseInt(arr[1]);
				// L is expressed as a fraction of a common time, but defaultNoteLength is expressed as a 
				// fraction or multiple of a beat, so the numeric value of L must be multiplied by 4
				defaultNoteLength = 4d * (double) num / (double) denom;
				log.debug("defaultNoteLength=" + defaultNoteLength);
			} else if (line.startsWith("K:")) {
				K = line.substring(2).trim();
				log.debug("K=" + K);				
			} else if (line.startsWith("Q:")) {
				
			} else if (line.startsWith("W:")) {
				// TODO use for pulsesPerDefaultNote definition 
			} else {
				// tune
				log.debug("line=" + line);
			} 
			
		}
		
	}
}
