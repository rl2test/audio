package audio.chords.gui;

import static audio.Constants.NL;
import static audio.Constants.PIPE;

import java.util.ArrayList;
import java.util.List;

/**
 * Rhythm data
 */
class Rhythm {
	String name = "";
	int beats = 0;
	int subBeats = 0;
	String src = "";
	List<String> voiceStrs = new ArrayList<String>();
	final int maxVoiceNameLen = "Acoustic bass drum".length();
	
	public Rhythm clone() {
		Rhythm r = new Rhythm();
		r.beats = beats;
		r.subBeats = subBeats;
    	for (String voiceStr: voiceStrs) {
    		r.voiceStrs.add(voiceStr);
    	}
		return r;
	}
	public int getNumPulses() {
		return beats * subBeats;
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
		s += "#" + getSpaces("#") + PIPE;
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
	
    public String getSpaces(String name) {
		String s = "";
		for (int i = 0, n = maxVoiceNameLen - name.length(); i < n; i++ ) {
			s += " ";
		}
		return s;
    }
    

}