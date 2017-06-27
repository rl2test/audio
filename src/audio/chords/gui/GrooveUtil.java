package audio.chords.gui;

import static audio.Constants.GROOVES_FILE;
import static audio.Constants.NL;
import static audio.Constants.PIPE_DELIM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import audio.Util;


public class GrooveUtil {
	final Logger log 						= Logger.getLogger(getClass());
	/** The singleton instance of this class. */    
	private static GrooveUtil rhythmUtil 	= null;
	private final String voiceNames[]				= { 
    		"Acoustic bass drum", 
    		"_Bass drum 1", // same as Acoustic bass drum
    		"Side stick", 
    		"Acoustic snare",
	        "_Hand clap", // same as Side stick
	        "_Electric snare", // same as Acoustic snare 
	        "Low floor tom", 
	        "Closed hi-hat",
	        "High floor tom", 
	        "_Pedal hi-hat", // same as Closed hi-hat
	        "Low tom", 
	        "Open hi-hat", 
	        "Low-mid tom", 
	        "Hi-mid tom",
	        "Chord",
	        "Fifth",
	        "Root"
	};
	static int maxVoiceNameLen 				= 0;
	final Map<String, Integer> voiceMap 	= new HashMap<String, Integer>();
	final List<String> voiceKeys 			= new ArrayList<String>();
	List<Groove> rhythms 					= new ArrayList<Groove>();
	String[] rhythmNames;

	
    /**
     * @return singleton instance of this class
     */
    public static GrooveUtil getInstance() throws Exception {
        if (rhythmUtil == null) {
        	rhythmUtil = new GrooveUtil();
    	}
    	return rhythmUtil;
    }
    
    private GrooveUtil() throws Exception {
		// create voices
        int id = 35;
     	for (String voiceName: voiceNames) {
     		if (!voiceName.startsWith("_")) {
     			voiceMap.put(voiceName, id);
     			voiceKeys.add(voiceName);
     		}	
	     	id++;
     	}

     	maxVoiceNameLen = voiceNames[0].length();

     	loadRhythms();
    }
    
    // populate rhythms list from file
    public void loadRhythms() {
    	rhythms.clear();
    	
    	Groove r = null;
		List<String> lines 	= Util.getLines(GROOVES_FILE);
		for (String line: lines) {
			/*
				@ basic-jazz-pattern
				$ http://www.freedrumlessons.com/drum-lessons/basic-jazz-pattern.php
				% 4|3
				#                 |1--2--3--4--
				Acoustic bass drum|-       -
				Acoustic snare    |   - -   - -
				Pedal hi-hat      |   -     -
				Open hi-hat       |-  - --  - -
			*/
			if (!line.startsWith("#")) {
				String[] arr = line.split(PIPE_DELIM);
				if (line.startsWith("@")) {
					r = new Groove();
					r.name = arr[0].substring(1).trim();
					rhythms.add(r);
				} else if (line.startsWith("$")) {
					r.src = arr[0].substring(1).trim();
				} else if (line.startsWith("%")) {
					r.beats = Integer.parseInt(arr[0].substring(1).trim());
					r.subBeats = Integer.parseInt(arr[1].trim());
				} else {
					r.voiceStrs.add(line);
				}				
			}
		}
		log.debug(rhythms.size());
        rhythmNames = new String[rhythms.size()];
        for (int i = 0, n = rhythms.size(); i < n; i++) {
        	log.debug(rhythms.get(i).name);
        	rhythmNames[i] = rhythms.get(i).name;
        }
    }
    
    // save rhythms list to file
    public void saveRhythms(boolean refresh) {
    	if (refresh) { // save as - resort 
    		Collections.sort(rhythms, new Comparator<Groove>() {
    	        @Override
    	        public int compare(Groove r2, Groove r1) {
    	            return  r2.name.compareTo(r1.name);
    	        }
    	    });
    	}
    	for (Groove r: rhythms) {
    		log.debug(r.name);
    	}
    	String s = "";
    	for (Groove rhythm: rhythms) {
    		s += rhythm.toString() + NL;
    	}
    	Util.writeToFile(GROOVES_FILE, s);
    	if (refresh) {
    		loadRhythms();
    	}
    }
    
    public static String getSpaces(String name) {
		String s = "";
		for (int i = 0, n = GrooveUtil.maxVoiceNameLen - name.length(); i < n; i++ ) {
			s += " ";
		}
		return s;
    }
}
