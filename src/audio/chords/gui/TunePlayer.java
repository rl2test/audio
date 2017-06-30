package audio.chords.gui;
import static audio.Constants.OCTAVE;
import static audio.Constants.PIPE_DELIM;
import static audio.Constants.TRANSPOSE_KEYS;
import static audio.Constants.V;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.apache.log4j.Logger;

import audio.chords.Bar;
import audio.chords.Chord;
import audio.chords.Tune;

public class TunePlayer implements MetaEventListener { 
	final Logger log 					= Logger.getLogger(getClass());
	final int CONTROL 					= 176;
	final int PROGRAM 					= 192;
	final int CHANNEL_BASS 				= 0;
	final int CHANNEL_CHRD	 			= 1;
	final int CHANNEL_PERC 				= 9;
	final int VOL_BASS 					= V[8];
	final int VOL_CHRD 					= V[5];
	final int VOL_PERC 					= V[4];
	final int defaultTempo 				= 120; // default beatsPerMinute
	final int defaultLoopCount 			= 4; // 1000
	String text							= null; // tune text
	AudioController ac					= null;
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	GrooveUtil gu						= GrooveUtil.getInstance();
	Groove groove;
	TimePanel timePanel;
	boolean playing 					= false;
	int beginTempo						= 0;
	int endTempo 						= 0;
	int increment 						= 0; // default	
	int tempo 							= 0;
	int loopCount 						= defaultLoopCount; // default
	List<Voice> percVoices				= new ArrayList<Voice>();
	List<Voice> chrdVoices				= new ArrayList<Voice>();

	 // called from play btn
	public TunePlayer(String text, AudioController ac) {
		this.text = text;
		this.ac = ac;
		timePanel = ac.timePanel;
		groove = gu.grooveMap.get(timePanel.grooveName);
		log.debug("groove.name=" + groove.name);
		
		// set pulses
     	for (String voiceStr: groove.voiceStrs) {
     		String[] arr = voiceStr.split(PIPE_DELIM);
			String name = arr[0].trim();
			String pulseStr = arr[1];
			int id = gu.voiceMap.get(name);
			Voice voice = new Voice(name, id);
			if (gu.chordVoiceNames.contains(name)) {
				chrdVoices.add(voice);
			} else {
				percVoices.add(voice);
			}
			
			voice.pulses = new boolean[groove.numPulses];
			if (pulseStr.length() > groove.numPulses) {
				log.warn("pulseStr.length() > groove.numPulses: " + pulseStr.length() + " " + groove.numPulses);
				pulseStr = pulseStr.substring(0, groove.numPulses);
			}
			for (int i = 0, n = pulseStr.length(); i < n; i++) {
				String s = pulseStr.substring(i, i + 1);
				if (s.equals("-")) {
					voice.pulses[i] = true;
				}
			}
     	}
		
		openSequencer();
	}

	public void start() {
		try {
			String transposeTo = (ac.keyPanel.selectedKeyIndex != -1) ? TRANSPOSE_KEYS[ac.keyPanel.selectedKeyIndex] : "";
			log.debug("transposeTo=" + transposeTo);
	
			log.debug("timePanel: set=" + timePanel.set + ", beginTempo=" + timePanel.beginTempo);
	
			Tune tune = new Tune(text, transposeTo); 
			beginTempo	= (timePanel.set) ? timePanel.beginTempo : tune.beginTempo;
			endTempo 	= (timePanel.set) ? timePanel.endTempo : tune.endTempo;
			increment 	= (timePanel.set) ? timePanel.increment : tune.increment;	
	
			if (tune.beatsPerBar != groove.beats) {
				throw new Exception("tune.beatsPerBar != groove.beats");
			}
			
			if (tune.transposed) {
				log.debug("transposed from " + tune.transposeFrom + " to " + tune.transposeTo);
			}
			
			if (ac.displayPanel != null) {
				ac.displayPanel.init(tune.bars);
				ac.displayPanel.repaint();
			}
			
			tempo = beginTempo;
			timePanel.setTempoValue(tempo);
			
			if (endTempo > beginTempo) {
				loopCount = 1;
				if (increment == 0) {
					increment = 1; // set a default
				}
			} else {
				increment = 0;
				loopCount = defaultLoopCount;
			}
			
			log.debug("beginTempo=" + beginTempo);
			log.debug("endTempo=" + endTempo);
			log.debug("tempo=" + tempo);
			log.debug("increment=" + increment);
			log.debug("loopCount=" + loopCount);

            sequence = new Sequence(Sequence.PPQ, groove.subBeats); 
            log.debug("creating track");
            track = sequence.createTrack();

            createEvent(PROGRAM, CHANNEL_PERC, 0, 0, 0);
	        createEvent(PROGRAM, CHANNEL_PERC, 0, 0, tune.bars.size() * tune.beatsPerBar * groove.subBeats);

	        createEvent(CONTROL, CHANNEL_BASS, 10, V[0], 0); // set pan
	        createEvent(CONTROL, CHANNEL_CHRD, 10, V[8], 0); // set pan
	        createEvent(CONTROL, CHANNEL_PERC, 10, V[4], 0); // set pan

	        if (chrdVoices.size() == 0) {
	        	// assume simple chord per beat pattern 
				int len = groove.subBeats;
	        	for (int bCount = 0, bNum = tune.bars.size(); bCount < bNum; bCount++) {
	        		Bar bar = tune.bars.get(bCount);
	        		String lastName = "";
	        		int lastBassNote = 0;
	        		for (int cCount = 0, cNum = bar.chords.size(); cCount < cNum; cCount++) {
		        		Chord chord = bar.chords.get(cCount);
		        		String name = chord.name;
		        		String nextName = (cCount == cNum - 1) ? "" : bar.chords.get(cCount + 1).name; 
	        			int tick = bCount * groove.numPulses + cCount * groove.subBeats;
	    	        	/*
						AAAA
						AAAB
						AABA
						AABB
						ABAA
						ABAB
						ABBA
						ABBB
	    	        	*/
		        		if (cCount == 0 || !name.equals(lastName)) {
		        			// bass plays root
		        			createNote(CHANNEL_BASS, chord.chordIntegers[0] - OCTAVE, VOL_BASS, tick, len);
		        			lastBassNote = 1;
		        		} else if (cCount % 2 == 0) {
		        			// play 5th if above condition not met
		        			createNote(CHANNEL_BASS, chord.chordIntegers[2] - OCTAVE, VOL_BASS, tick, len);
		        			lastBassNote = 5;
		        		} else if (name.equals(lastName) && !name.equals(nextName) && lastBassNote == 1) {
		        			// play 5th if above conditions not met
		        			createNote(CHANNEL_BASS, chord.chordIntegers[2] - OCTAVE, VOL_BASS, tick, len);
		        		} 
     					for (int j = 1, m = chord.chordIntegers.length; j < m; j++) {
         					createNote(CHANNEL_CHRD, chord.chordIntegers[j], VOL_CHRD, tick, len);
         				}
	     				lastName = chord.name;
					}	
				}	
	        } else {
	        	for (int bCount = 0, bNum = tune.bars.size(); bCount < bNum; bCount++) {
	        		Bar bar = tune.bars.get(bCount);
	        		String lastName = "";
	        		boolean rootPlayedOnBeat = false;
	        		for (int cCount = 0, cNum = bar.chords.size(); cCount < cNum; cCount++) {
		        		Chord chord = bar.chords.get(cCount);
		        		if (!chord.name.equals(lastName)) {
		        			rootPlayedOnBeat = false;
		        		}
						// groove assumes 1 bar per chord
			        	for (Voice voice: chrdVoices) {
			        		for (int i = 0, n = groove.subBeats; i < n; i++) {
			        			int pulse = (cCount * groove.subBeats) + i;
			        			int tick = (bCount * groove.numPulses) + pulse;
			        			if (voice.pulses[pulse]) {
			     					int len = groove.subBeats;
			     					if (pulse + len > groove.numPulses) {
			     						len = groove.numPulses - pulse;
			     					}
				     				if (voice.id == gu.ROOT) {
				     					createNote(CHANNEL_BASS, chord.chordIntegers[0] - OCTAVE, VOL_BASS, tick, len);
				     					if (i == 0) {
				     						rootPlayedOnBeat = true;
				     					}
				     				} else if (voice.id == gu.FIFTH) {
				     					if (!chord.name.equals(lastName)) {
				     						// if there has been a chord change, play root instead of fifth
				     						createNote(CHANNEL_BASS, chord.chordIntegers[0] - OCTAVE, VOL_BASS, tick, len);
						        			rootPlayedOnBeat = true;
						        		} else {
						        			createNote(CHANNEL_BASS, chord.chordIntegers[2] - OCTAVE, VOL_BASS, tick, len);	
						        		}
				     				} else if (voice.id == gu.CHORD) {
				     					for (int j = 1, m = chord.chordIntegers.length; j < m; j++) {
				         					createNote(CHANNEL_CHRD, chord.chordIntegers[j], VOL_CHRD, tick, len);
				         				}
				     				} 
				     			}			        			
			        		}
  
				        }
		        		if (!chord.name.equals(lastName) && rootPlayedOnBeat == false) {
		        			int tick = bCount * groove.numPulses + cCount * groove.subBeats;
		        			createNote(CHANNEL_BASS, chord.chordIntegers[0] - OCTAVE, VOL_BASS, tick, groove.subBeats);
		        		}
			        	lastName = chord.name;
					}	
				}	
	        }
	        
	        for (Voice voice: percVoices) {
 				for (int i = 0, n = voice.pulses.length; i < n; i++) {
	     			if (voice.pulses[i]) {
     					int len = groove.subBeats;
     					if (i + len > n) {
     						len = n - i;
     					}
     					for (int bCount = 0, bNum = tune.bars.size(); bCount < bNum; bCount++) {
     						int tick = bCount * n + i;
     						createNote(CHANNEL_PERC, voice.id, VOL_PERC, tick, 1);
     					}	
	     			}
	        	}     
	        }
			
	        // set and start the sequencer.
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(loopCount - 1); // note: first time through is not counted as being a loop
            sequencer.setTempoFactor((float) tempo / (float) defaultTempo);
            log.debug("sequencer.getTempoFactor()=" + sequencer.getTempoFactor());
            
            sequencer.start();

		} catch (Exception e) {
			log.error(e);
			ac.filePanel.stop("exception thrown in TunePlayer.start(): " + e);
		}
	}
		
	// create note
    private void createNote(int chan, int note, int vol, long tick, int len) {
    	createEvent(ShortMessage.NOTE_ON, chan, note, vol, tick);
		createEvent(ShortMessage.NOTE_OFF, chan, note, 0, tick + len);
    }
	
	// create event
    private void createEvent(int type, int chan, int id, int vol, long tick) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(type, chan, id, vol);
            MidiEvent event = new MidiEvent(message, tick);
            track.add(event);
		} catch (Exception e) { 
			log.error(e);
	    }
    }

    // required because this class implements MetaEventListener
    public void meta(MetaMessage message) {
    	log.debug(message.getType());
        if (message.getType() == 47) {  // 47 is end of track
        	if (increment > 0) {
        		if ((tempo + increment > endTempo)) {
        			increment = 0;
        			loopCount = defaultLoopCount;
            		sequencer.setLoopCount(loopCount);
        		} else {
            		tempo += increment;
            		sequencer.setLoopCount(loopCount);
            		sequencer.setTempoFactor((float) tempo / (float) defaultTempo);
                	log.debug("sequencer.getTempoFactor()=" + sequencer.getTempoFactor());
                	timePanel.setTempoValue(tempo);
        		}
                sequencer.start();
        	} else {
                sequencer.stop();
                playing = false;
                ac.filePanel.labels.get("playStop").setText("Play");
        	}
        }
    }
    
    // init sequencer
	public void openSequencer() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.addMetaEventListener(this);
        } catch (Exception e) { 
        	log.debug(e);
        }
        
    }

	// close sequencer
    public void closeSequencer() {
        if (sequencer != null) {
            sequencer.close();
        }
        sequencer = null;
    }
    	
	public void destroyPlayer() {
		closeSequencer();
	}	
}
