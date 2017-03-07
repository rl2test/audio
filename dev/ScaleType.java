package audio.chords;

import java.util.List;

import org.apache.log4j.Logger;

import audio.Util;

public class ScaleType implements ChordsConstants {
	/** The log. */
	private Logger log 			= Logger.getLogger(getClass());	
	public String name 			= "";
	public String shortName		= "";
	public String desc 			= "";
	public String whIntervals 	= "";
	public String mode 			= "";
	public String intervals 	= "";
	public String cScale 		= "";
	public String symbol 		= "";
	public String otherSymbols	= "";

	/** Verify that intervals and cNotes match. */
	public boolean verify 		= true;
	
	public ScaleType(String line) {
		//log.debug(line);
		String[] arr = line.split(PIPE_DELIM);

		int i = 0;	
		name			= arr[i++].trim();
		shortName		= arr[i++].trim();
		desc			= arr[i++].trim();
		whIntervals		= arr[i++].trim();
		mode			= arr[i++].trim();
		intervals		= arr[i++].trim();
		cScale			= arr[i++].trim();
		symbol 			= arr[i++].trim();
		otherSymbols	= arr[i++].trim();

		if (verify) {
			String derivedIntervals = "";
			List<String> cNotes = Util.getList(cScale);
			
			int n = 0;
			for (String cNote: cNotes) {
				if (n != 0 && cNote.equals("C")) {
					cNote = "c";
				}
				derivedIntervals += Mapstore.cNotesToIntervals.get(cNote) + " ";
				n++;
			}
			derivedIntervals = derivedIntervals.substring(0, derivedIntervals.length() - 1);
			if (!derivedIntervals.equals(intervals)) {
				log.error(name + " intervals=" + intervals + ", derivedIntervals=" + derivedIntervals);	
			}
		}
	}
}
