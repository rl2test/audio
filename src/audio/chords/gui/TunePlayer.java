package audio.chords.gui;
import static audio.Constants.ACOUSTIC_BASS;
import static audio.Constants.NYLON_STRING_GUITAR;
import static audio.Constants.OCTAVE;
import static audio.Constants.PATTERNS;
import static audio.Constants.PATTERN_STRS;
import static audio.Constants.TRANSPOSE_KEYS;
import static audio.Constants.V;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiChannel;

import org.apache.log4j.Logger;

import audio.MidiNote;
import audio.chords.Bar;
import audio.chords.Chord;
import audio.chords.Tune;

public class TunePlayer extends Thread { 
	/** The log. */
	protected Logger log 					= Logger.getLogger(this.getClass());
	protected boolean runFlag 				= true;
	protected List<MidiNote> midiNotes		= new ArrayList<MidiNote>();
	protected List<MidiNote> endMidiNotes	= new ArrayList<MidiNote>();
	/** The tune text. */
	private String text						= null;
	private AudioController ac				= null;

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
	public TunePlayer(String text, AudioController ac) {
		this.text = text;
		this.ac = ac;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			// channel definitions
			final int CHANNEL_BASS 	= 0;
			final int CHANNEL_CHORD = 1;

			final int BASS_VOL 		= V[8];
			final int CHORD_VOL 	= V[3];
			
			//bass
			MidiChannel bassChannel = ac.midiChannels[CHANNEL_BASS];
			bassChannel.controlChange(10, V[0]); // set pan
			bassChannel.programChange(ACOUSTIC_BASS);

			//chord
			MidiChannel chordChannel = ac.midiChannels[CHANNEL_CHORD];
			chordChannel.controlChange(10, V[8]); // set pan
			chordChannel.programChange(NYLON_STRING_GUITAR);	

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

			int patternKey = (time < 5) ? time : time * 10 + type;
			Integer[] pattern = PATTERNS.get(patternKey);
			log.debug("patternStr=" + PATTERN_STRS.get(patternKey));
			ac.setMsg(PATTERN_STRS.get(patternKey));
			
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
			
			// set the initial pulse length
			int pulseLen = (int) (1000d * 60d / tempo);
			
			log.debug("pulseLen=" + pulseLen);
			
			Bar bar 		= null;
			int pulseCount 	= 0; // init using zero as base
			int barCount 	= 0; // init using zero as base
			int beatCount 	= 0; // init using zero as base
			int chordBeatCount 	= 0; // init using zero as base
			String lastChordName = "";
			
			while(runFlag){
				// handle note off events
				endMidiNotes.clear(); 
				for (MidiNote midiNote: midiNotes) {
					midiNote.len--;
					if (midiNote.len == 0) {
						endMidiNote(midiNote);
						endMidiNotes.add(midiNote);
					}
				}
				for (MidiNote midiNote: endMidiNotes) {
					midiNotes.remove(midiNote);
				}
				
				// break out of loop
				if (!runFlag) {
					break;
				}

				beatCount = pulseCount % time; 
				if (beatCount == 0) {
					// get next bar
					bar = tune.bars.get(barCount);
					
					if (ac.displayPanel != null) {
						ac.displayPanel.updateBars(barCount);
					}
					
					barCount++;
					if (barCount == tune.bars.size()) {
						barCount = 0; // reset to zero
					}
				}

				Chord chord = bar.chords.get(beatCount);
				Chord nextChord = (beatCount == time - 1) ? null : bar.chords.get(beatCount + 1);
				if (!chord.name.equals(lastChordName) || beatCount == 0) {
					chordBeatCount = 0;
				}
				
				// bass
				int patternVal = pattern[beatCount];
				if (patternVal > 0) {
					int chordInt = (patternVal == 1 || chordBeatCount == 0) ? chord.chordIntegers[0] : chord.chordIntegers[2];
					int dur = (nextChord != null && chord.name.equals(nextChord.name)) ? 2 : 1;
					beginMidiNote(new MidiNote(CHANNEL_BASS, chordInt - OCTAVE, dur, BASS_VOL));
				}

				// chord
				for (int i = 1, n = chord.chordIntegers.length; i < n; i++) {
					beginMidiNote(new MidiNote(CHANNEL_CHORD, chord.chordIntegers[i], 1, CHORD_VOL));	
				}

				// sleep till the next pulse
				sleep(pulseLen);

				pulseCount++;
				chordBeatCount++;
				lastChordName = chord.name;
				if (pulseCount == tune.bars.size() * time) {
					// reached the end of the bars array, so reset pulseCount to 0
					pulseCount = 0;

					// check for doIncrement 
					if (doIncrement) {
						// increase tempo
						if (tempo < endTempo && (tempo + increment) <= endTempo) {
							tempo += increment;
							timePanel.setTempoValue(tempo);
							pulseLen = (int) (1000d * 60d / tempo);	
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ac.filePanel.stop("Exception thrown in TunePlayer.run(): " + e.toString());
		}
	}
	
	/**
	 * @param midiNote
	 */
	private void beginMidiNote(MidiNote midiNote) {
		midiNotes.add(midiNote);
		ac.midiChannels[midiNote.channel].noteOn(midiNote.pitch, midiNote.vol);
	}
	
	/**
	 * @param midiNote
	 */
	private void endMidiNote(MidiNote midiNote) {
		ac.midiChannels[midiNote.channel].noteOff(midiNote.pitch);
	}

	/**
	 * 
	 */
	public void destroyPlayer() {
		runFlag = false;
		for (MidiNote midiNote: midiNotes) {
			endMidiNote(midiNote);
		}
		midiNotes.clear();
		endMidiNotes.clear();
		
		midiNotes		= null;
		endMidiNotes	= null;
		log 			= null;
	}	
}
