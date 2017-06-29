package audio.chords.gui;

import static audio.Constants.NL;
import static audio.Constants.PIPE;

import java.util.ArrayList;
import java.util.List;

/**
 * A representation of a single set of groove data in GROOVES_FILE
 */
class Groove {
	String name = "";
	int beats = 0;
	int subBeats = 0;
	int numPulses = 0;
	String src = "";
	List<String> voiceStrs = new ArrayList<String>();
	final int maxVoiceNameLen = "Acoustic bass drum".length();
	
	public Groove clone() {
		Groove g = new Groove();
		g.beats = beats;
		g.subBeats = subBeats;
		g.numPulses = numPulses; 
    	for (String voiceStr: voiceStrs) {
    		g.voiceStrs.add(voiceStr);
    	}
		return g;
	}
	
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
	public String toString() {
		String s = "@" + name + NL;
		s += "$" + src + NL;
		s += "% " + beats + PIPE + subBeats + NL;
		s += "#" + GrooveUtil.getSpaces("#") + PIPE;
		for (int i = 0; i < beats; i++) {
			s += (i + 1);
			for (int j = 1; j < subBeats; j++) {
				s += "-";
			}
		}
		s += NL;
		for (String voiceStr: voiceStrs) {
			s += voiceStr + NL;	
		}
		return s;
	}
	
}