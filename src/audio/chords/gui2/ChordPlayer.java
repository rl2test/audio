package audio.chords.gui2;
import static audio.Constants.ACOUSTIC_BASS;
import static audio.Constants.DEFAULT_BEATS_PER_BAR;
import static audio.Constants.DEFAULT_BEGIN_TEMPO;
import static audio.Constants.DEFAULT_END_TEMPO;
import static audio.Constants.DEFAULT_INCREMENT;
import static audio.Constants.ELECTRIC_PIANO_1;
import static audio.Constants.NYLON_STRING_GUITAR;
import static audio.Constants.V0;
import static audio.Constants.V2;
import static audio.Constants.V3;
import static audio.Constants.V4;
import static audio.Constants.V8;
import static audio.Constants.OCTAVE;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiChannel;

import org.apache.log4j.Logger;

import audio.Config;
import audio.MidiNote;
import audio.chords.Bar;
import audio.chords.Chord;
import audio.chords.Tune;

public class ChordPlayer extends Thread { 
	/** The log. */
	protected Logger log 					= Logger.getLogger(this.getClass());
	protected boolean runFlag 				= true;
	protected List<MidiNote> midiNotes		= new ArrayList<MidiNote>();
	protected List<MidiNote> endMidiNotes	= new ArrayList<MidiNote>();
	/** The number of beats per bar. */
	public int beatsPerBar 					= DEFAULT_BEATS_PER_BAR;
	/** The beginTempo. */
	private int beginTempo					= DEFAULT_BEGIN_TEMPO;
	/** The endTempo. */
	private int endTempo					= DEFAULT_END_TEMPO;
	/** The increment used to change from beginTempo to endTempo. */
	private int increment					= DEFAULT_INCREMENT;
	/** Flag indicating that default values are being used. */
	private boolean usingDefaults			= true;
	/** The genre. */
	private String genre					= "";
	/** The tune text. */
	private String text						= null;
	/** ChordPanel reference. */
	public ChordPanel chordPanel 			= null;
	/** StatusPanel reference. */
	public StatusPanel statusPanel 			= null;
	/** DisplayPanel reference. */
	public DisplayPanel displayPanel 		= null;

	/**
	 * Called from playButton.
	 * 
	 * @param beginTempo
	 * @param endTempo
	 * @param increment
	 * @param genre
	 * @param tuneFile
	 * @param chordFilePanel
	 */
	public ChordPlayer(
			int beginTempo,
			int endTempo,
			int increment,
			String genre,
			String text,
			ChordPanel chordPanel,
			DisplayPanel displayPanel) {
		if (beginTempo > 0) {
			// use gui settings, otherwise retain default values
			this.beginTempo		= beginTempo;
			this.endTempo 		= endTempo;
			this.increment 		= increment;
			usingDefaults		= false;
		}
		this.genre			= genre;
		this.text			= text;
		this.chordPanel 	= chordPanel;
		this.displayPanel 	= displayPanel;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			// channel definitions
			final int CHANNEL_BASS 	= 0;
			final int CHANNEL_CHORD = 1;

			final int BASS_VOL 		= V8;
			final int CHORD_VOL 	= V3;
			String style			= Config.properties.get("style");
			
			//bass
			MidiChannel bassChannel = GuiController.midiChannels[CHANNEL_BASS];
			bassChannel.controlChange(10, V0); // set pan
			bassChannel.programChange(ACOUSTIC_BASS);

			//chord
			MidiChannel chordChannel = GuiController.midiChannels[CHANNEL_CHORD];
			chordChannel.controlChange(10, V8); // set pan
			//if ((style.equals("gypsy"))) {
				chordChannel.programChange(NYLON_STRING_GUITAR);	
				//chordChannel.programChange(ELECTRIC_PIANO_1);
			//} else {
				//chordChannel.programChange(ELECTRIC_PIANO_1);	
			//}

			String transposeTo = chordPanel.getTransposeTo();
			
			Tune tune = new Tune(genre, text, transposeTo);
			//log.debug(tune);
			beatsPerBar	= tune.beatsPerBar;
			
			if (tune.transposed) {
				chordPanel.updateMessage("transposed from " + tune.transposeFrom + " to " + tune.transposeTo);
			}
			
			if (displayPanel != null) {
				displayPanel.init(tune.bars);
				displayPanel.repaint();
			}
			
			int tempo = 0;
			
			// increment feature
			boolean doIncrement = false;
			
			if (usingDefaults) {
				// override defaults with tune values (which may also be defaults)
				beginTempo	= tune.beginTempo;
				endTempo 	= tune.endTempo;
				increment 	= tune.increment;
			}

			if (endTempo > beginTempo) {
				doIncrement = true;
			}
			
			tempo = beginTempo;
			chordPanel.updateTempo("" + tempo);
			
			log.debug("usingDefaults=" + usingDefaults);
			log.debug("beatsPerBar=" + beatsPerBar);
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

				beatCount = pulseCount % beatsPerBar; 
				if (beatCount == 0) {
					// get next bar
					bar = tune.bars.get(barCount);
					
					if (displayPanel != null) {
						displayPanel.updateBars(barCount);
					}
					
					barCount++;
					if (barCount == tune.bars.size()) {
						barCount = 0; // reset to zero
					}
				}

				//log.debug("beatCount=" + beatCount + " / " + beatsPerBar);
				Chord chord = bar.chords.get(beatCount);
				Chord nextChord = (beatCount == beatsPerBar - 1) ? null : bar.chords.get(beatCount + 1);
				if (!chord.name.equals(lastChordName) || beatCount == 0) {
					chordBeatCount = 0;
				}
				//log.debug("chord=" + chord);
				//log.debug("nextChord=" + nextChord);
				
				// bass
				//int chordIntIndex = (chordBeatCount % 2 == 0) ? 0 : 2;
				//beginMidiNote(new MidiNote(CHANNEL_BASS, chord.chordIntegers[chordIntIndex] - OCTAVE, 1, BASS_VOL));
				if (chordBeatCount == 0) {
					if (nextChord != null) {
						int dur = (chord.name.equals(nextChord.name)) ? 2 : 1;
						beginMidiNote(new MidiNote(CHANNEL_BASS, chord.chordIntegers[0] - OCTAVE, dur, BASS_VOL));
					} else {
						beginMidiNote(new MidiNote(CHANNEL_BASS, chord.chordIntegers[0] - OCTAVE, 1, BASS_VOL));
					}
				} else if (chordBeatCount == 2) {
					if (nextChord != null) {
						int dur = (chord.name.equals(nextChord.name)) ? 2 : 1;
						beginMidiNote(new MidiNote(CHANNEL_BASS, chord.chordIntegers[2] - OCTAVE, dur, BASS_VOL));
					} else {
						beginMidiNote(new MidiNote(CHANNEL_BASS, chord.chordIntegers[0] - OCTAVE, 1, BASS_VOL));
					}
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
				if (pulseCount == tune.bars.size() * beatsPerBar) {
					// reached the end of the bars array, so reset pulseCount to 0
					pulseCount = 0;

					// check for doIncrement 
					if (doIncrement) {
						// increase tempo
						if (tempo < endTempo && (tempo + increment) <= endTempo) {
							tempo += increment;
							chordPanel.updateTempo("" + tempo);
							pulseLen = (int) (1000d * 60d / tempo);	
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			chordPanel.stop("Exception thrown in ChordPlayer.run(): " + e.toString());
		}
	}
	
	/**
	 * @param midiNote
	 */
	private void beginMidiNote(MidiNote midiNote) {
		midiNotes.add(midiNote);
		GuiController.midiChannels[midiNote.channel].noteOn(midiNote.pitch, midiNote.vol);
	}
	
	/**
	 * @param midiNote
	 */
	private void endMidiNote(MidiNote midiNote) {
		GuiController.midiChannels[midiNote.channel].noteOff(midiNote.pitch);
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
		log 		= null;
	}	
}


/*
if (style.equals("none")) {
	//log.debug("beatCount=" + beatCount);
	Beat beat = bar.pattern.beats[beatCount];
	
	// root
	if (beat.r > 0) {
		beginMidiNote(new MidiNote(CHANNEL_BASS, chord.rootValue, beat.r, BASS_VOL));
	}
	// fifth
	if (beat.f > 0) {
		if (chord.fifthValue > 0) {
			// For aug chords for example, fifthValue may not have 
			// been set, so substitute the root but use the fifth 
			// duration. 
			beginMidiNote(new MidiNote(CHANNEL_BASS, chord.fifthValue, beat.f, BASS_VOL));	
		} else {
			beginMidiNote(new MidiNote(CHANNEL_BASS, chord.rootValue, beat.f, BASS_VOL));
		}
	}
	// chord - chord duration is always either 0 or 1	
	if (beat.c > 0) {
		for (int chordInteger: chord.chordIntegers) {
			beginMidiNote(new MidiNote(CHANNEL_CHORD, chordInteger, 1, CHORD_VOL));	
		}
	}
} else {
//}
*/	