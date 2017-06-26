package audio.chords.gui;

import static audio.Constants.GROOVES_FILE;
import static audio.Constants.PIPE_DELIM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import audio.Util;
import audio.chords.gui.GroovePanel.Voice;

public class RhythmUtil {
	final Logger log 							= Logger.getLogger(getClass());
	/** The singleton instance of this class. */    
	private static RhythmUtil rhythmUtil 		= null;
	final String voiceNames[]					= { 
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
	final int maxVoiveNameLen 					= voiceNames[0].length();
	//final List<Voice> voices 					= new ArrayList<Voice>();
	//final Map<String, Voice> voiceMap 			= new HashMap<String, Voice>();
	//final Map<Integer, Voice> voiceIdMap 		= new HashMap<Integer, Voice>();
	List<Rhythm> rhythms 						= new ArrayList<Rhythm>();
	String[] rhythmNames;

	
    /**
     * @return singleton instance of this class
     */
    public static RhythmUtil getInstance() throws Exception {
        if (rhythmUtil == null) {
        	rhythmUtil = new RhythmUtil();
    	}
    	return rhythmUtil;
    }
    
    private RhythmUtil() throws Exception {
    	loadRhythms();
    }
    
    private void loadRhythms() {
    	rhythms.clear();
    	
    	Rhythm r = null;
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
					r = new Rhythm();
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
}
