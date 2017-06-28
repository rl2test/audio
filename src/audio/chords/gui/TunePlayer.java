package audio.chords.gui;
import static audio.Constants.OCTAVE;
import static audio.Constants.TRANSPOSE_KEYS;
import static audio.Constants.V;

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
	final int VOL_CHRD 					= V[6];
	final int VOL_PERC 					= V[5];
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

	 // called from play btn
	public TunePlayer(String text, AudioController ac) {
		this.text = text;
		this.ac = ac;
		timePanel = ac.timePanel;
		groove = gu.grooveMap.get(timePanel.grooveName);
		log.debug("groove.name=" + groove.name);
		
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

            sequence = new Sequence(Sequence.PPQ, 1); // groove.subBeats
            log.debug("creating track");
            track = sequence.createTrack();

            createEvent(PROGRAM, CHANNEL_PERC, 0, 0, 0);
	        createEvent(PROGRAM, CHANNEL_PERC, 0, 0, tune.bars.size() * tune.beatsPerBar);

	        createEvent(CONTROL, CHANNEL_BASS, 10, V[0], 0); // set pan
	        createEvent(CONTROL, CHANNEL_CHRD, 10, V[8], 0); // set pan
	        createEvent(CONTROL, CHANNEL_PERC, 10, V[4], 0); // set pan

            int i = 0;
			for (Bar bar: tune.bars) {
				for(Chord chord: bar.chords) {
    				if (i % tune.beatsPerBar == 0) {
    					createNote(CHANNEL_BASS, chord.chordIntegers[0] - OCTAVE, VOL_BASS, i, 1);
    				} else if (i % tune.beatsPerBar == 2) {
    					createNote(CHANNEL_BASS, chord.chordIntegers[2] - OCTAVE, VOL_BASS, i, 1);
    				} else {
    					for (int j = 1, n = chord.chordIntegers.length; j < n; j++) {
         					createNote(CHANNEL_CHRD, chord.chordIntegers[j], VOL_CHRD, i, 1);
         				}
    				}
     				//createNote(CHANNEL_PERC, instrument.id, VOL_PERC, i, 1);
					i++;
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
