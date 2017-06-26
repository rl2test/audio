package audio.chords.gui;
import static audio.Constants.OCTAVE;
import static audio.Constants.TRANSPOSE_KEYS;
import static audio.Constants.V;

import java.util.HashMap;
import java.util.Map;

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

public class TunePlayer2 implements MetaEventListener { 
	final Logger log 								= Logger.getLogger(getClass());
	final int CONTROL 								= 176;
	final int PROGRAM 								= 192;
	final int BPM 									= 120;
	final int CHANNEL_BASS 							= 0;
	final int CHANNEL_CHRD	 						= 1;
	final int CHANNEL_PERC 							= 9;
	final int VOL_BASS 								= V[8];
	final int VOL_CHRD 								= V[6];
	final int VOL_PERC 								= V[5];
	final int defaultLoopCount 						= 1000;
	final Map<String, Integer> percMap				= new HashMap<String, Integer>();

	String text										= null; // tune text
	AudioController ac								= null;
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	Rhythm rhythm;
	boolean playing 								= false;
	int loopCount 									= defaultLoopCount; // default
	int bpm 										= BPM;
	int bpmInc 										= bpm; // init
	boolean accelerate 								= false; // default
	int inc 										= 1; // default

	/**
	 * Called from playButton.
	 * 
	 * @param beginTempo
	 * @param endTempo
	 * @param increment
	 * @param genre
	 * @param tuneFile
	 * @param filePanel
	 */
	public TunePlayer2(String text, AudioController ac) {
		this.text = text;
		this.ac = ac;
		
		percMap.put("Acoustic bass drum",	35);
	    percMap.put("Side stick", 			37);
	    percMap.put("Acoustic snare", 		38);
		percMap.put("Low floor tom", 		41);
		percMap.put("Closed hi-hat", 		42);
		percMap.put("High floor tom", 		43);
	    percMap.put("Low tom", 				45);
	    percMap.put("Open hi-hat", 			46);
	    percMap.put("Low-mid tom", 			47);
	    percMap.put("Hi-mid tom", 			48);

		openSequencer();
	}

	public void start() {
		try {
			String transposeTo = (ac.keyPanel.selectedKeyIndex != -1) ? TRANSPOSE_KEYS[ac.keyPanel.selectedKeyIndex] : "";
			log.debug("transposeTo=" + transposeTo);
	
			TimePanel timePanel =  ac.timePanel;
			log.debug("timePanel: set=" + timePanel.set + ", beginTempo=" + timePanel.beginTempo);
	
			Tune tune = new Tune(text, transposeTo); 
			int time		= (timePanel.set) ? timePanel.time : tune.time;
			int type		= (timePanel.set) ? timePanel.type : tune.type;
			int beginTempo	= (timePanel.set) ? timePanel.beginTempo : tune.beginTempo;
			int endTempo 	= (timePanel.set) ? timePanel.endTempo : tune.endTempo;
			int increment 	= (timePanel.set) ? timePanel.increment : tune.increment;	
	
			if (tune.transposed) {
				log.debug("transposed from " + tune.transposeFrom + " to " + tune.transposeTo);
			}
			
			if (ac.displayPanel != null) {
				ac.displayPanel.init(tune.bars);
				ac.displayPanel.repaint();
			}
			
			int tempo = beginTempo;
			log.debug(tempo);
			timePanel.setTempoValue(tempo);
			
			// increment feature
			boolean doIncrement = false;
	
			if (endTempo > beginTempo) {
				doIncrement = true;
			}
			
			log.debug("time=" + time);
			log.debug("beginTempo=" + beginTempo);
			log.debug("endTempo=" + endTempo);
			log.debug("increment=" + increment);
			log.debug("doIncrement=" + doIncrement);
			log.debug("tempo=" + tempo);

			
			bpm = beginTempo;
			bpmInc = bpm; // reset
            sequence = new Sequence(Sequence.PPQ, 1); // rhythm.subBeats
            log.debug("creating track");
            track = sequence.createTrack();

            createEvent(PROGRAM, CHANNEL_PERC, 0, 0, 0);
	        createEvent(PROGRAM, CHANNEL_PERC, 0, 0, tune.bars.size() * time);

	        createEvent(CONTROL, CHANNEL_BASS, 10, V[0], 0); // set pan
	        createEvent(CONTROL, CHANNEL_CHRD, 10, V[8], 0); // set pan
	        createEvent(CONTROL, CHANNEL_PERC, 10, V[4], 0); // set pan

            int i = 0;
			for (Bar bar: tune.bars) {
				for(Chord chord: bar.chords) {
    				if (i % time == 0) {
    					createNote(CHANNEL_BASS, chord.chordIntegers[0] - OCTAVE, VOL_BASS, i, 1);
    				} else if (i % time == 2) {
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
            sequencer.setTempoFactor((float) bpm / (float) BPM);
            log.debug("sequencer.getTempoFactor()=" + sequencer.getTempoFactor());
            
            sequencer.start();

		} catch (Exception e) {
			log.error(e);
			ac.filePanel.stop("exception thrown in TunePlayer2.start(): " + e.toString());
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
    	
    // required because this class implements MetaEventListener
    public void meta(MetaMessage message) {
    	log.debug(message.getType());
        if (message.getType() == 47) {  // 47 is end of track
        	if (accelerate) {
        		bpmInc += inc;
        		sequencer.setLoopCount(loopCount);
        		sequencer.setTempoFactor((float) bpmInc / (float) BPM);
            	log.debug("sequencer.getTempoFactor()=" + sequencer.getTempoFactor());
            	//labels.get("bpmValue").setText("" + bpmInc);
                sequencer.start();
        	} else {
                sequencer.stop();
                playing = false;
                //JLabel l = labels.get("playStop");
                //l.setText("Play");
                //set(l);
        	}
        }
    }
    
	public void destroyPlayer() {
		closeSequencer();
	}	
}
