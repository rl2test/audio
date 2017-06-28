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
	private static GrooveUtil grooveUtil 	= null;
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
	List<Groove> grooves 					= new ArrayList<Groove>();
	String[] grooveNames;
	Map<String, Groove>grooveMap			= new HashMap<String, Groove>();
	
    /**
     * @return singleton instance of this class
     */
    public static GrooveUtil getInstance() {
        if (grooveUtil == null) {
        	grooveUtil = new GrooveUtil();
    	}
    	return grooveUtil;
    }
    
    private GrooveUtil() {
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

     	loadGrooves();
    }
    
    // populate grooves list from file
    public void loadGrooves() {
    	grooves.clear();
    	
    	Groove groove = null;
    	
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
					groove = new Groove();
					groove.name = arr[0].substring(1).trim();
					grooves.add(groove);
					grooveMap.put(groove.name, groove);
				} else if (line.startsWith("$")) {
					groove.src = arr[0].substring(1).trim();
				} else if (line.startsWith("%")) {
					groove.beats = Integer.parseInt(arr[0].substring(1).trim());
					groove.subBeats = Integer.parseInt(arr[1].trim());
					groove.numPulses = groove.beats * groove.subBeats;  
				} else {
					groove.voiceStrs.add(line);
				}				
			}
		}
		log.debug(grooves.size());
        grooveNames = new String[grooves.size()];
        for (int i = 0, n = grooves.size(); i < n; i++) {
        	log.debug(grooves.get(i).name);
        	grooveNames[i] = grooves.get(i).name;
        }
    }
    
    // save grooves list to file
    public void saveGrooves(boolean refresh) {
    	if (refresh) { // save as - resort 
    		Collections.sort(grooves, new Comparator<Groove>() {
    	        @Override
    	        public int compare(Groove r2, Groove r1) {
    	            return  r2.name.compareTo(r1.name);
    	        }
    	    });
    	}
    	for (Groove r: grooves) {
    		log.debug(r.name);
    	}
    	String s = "";
    	for (Groove groove: grooves) {
    		s += groove.toString() + NL;
    	}
    	Util.writeToFile(GROOVES_FILE, s);
    	if (refresh) {
    		loadGrooves();
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
