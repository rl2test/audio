package audio.chords.gui2;

import static audio.Constants.O;
import static audio.Constants.V;
import static audio.Constants.WOODBLOCK;

import javax.sound.midi.MidiChannel;

import org.apache.log4j.Logger;

import audio.MidiNote;

public class MetronomePlayer extends Thread {
	/** The log. */
	protected Logger log 					= Logger.getLogger(this.getClass());
	protected boolean runFlag 				= true;
	private int PITCH 						= O[5];
	private int CHANNEL 					= 0;
	private int PROGRAM 					= WOODBLOCK;
	private int beginTempo;
	private int endTempo;
	private int numBeats;
	private int increment;
	public MetronomePanel metronomePanel	= null;
	
	/**
	 * @param beginTempo
	 * @param endTempo
	 * @param numBeats the number of beats before the tempo is incremented, if increment is > 0
	 * @param increment
	 * @param metronomePanel a reference to the metronomePanel to be updated 
	 */
	public MetronomePlayer(
			int beginTempo,
			int endTempo,
			int numBeats,
			int increment,
			MetronomePanel metronomePanel) {
		
		this.beginTempo		= beginTempo;
		this.endTempo 		= endTempo;
		this.numBeats 		= numBeats;
		this.increment 		= increment;
		this.metronomePanel = metronomePanel;
		
		log.debug("constructed");
	}

	public void run() {
		log.debug("run()");
		
		// set channel
		MidiChannel channel = GuiController.midiChannels[CHANNEL];
		
		// set stereo r/l
		channel.controlChange(10, 127); 
		
		// set instrument
		channel.programChange(PROGRAM);
		
		// set initial tempo (bpm)
		int tempo 				= beginTempo;

		// the interval between beats
		int interval 			= (int) (1000d * 60d / tempo);
		log.debug("interval=" + interval);
		
		// the sleepInterval
		long sleepInterval 		= 0;
		
		// the interval in ms by which the thread overslept 
		long oversleptInterval 	= 0;
		
		// the expectedWakeUpTime
		long expectedWakeUpTime	= 0;		
		
		// the beat count, used to track when the tempo should be incremented
		int beatCount 			= 1;

		// init tempo label
		metronomePanel.metronomeTempoLabel.setText("" + tempo);
		
		// the note to be played by the metronome
		MidiNote midiNote1 = new MidiNote(CHANNEL, PITCH, 1, V[4]);
		MidiNote midiNote2 = new MidiNote(CHANNEL, PITCH, 1, V[8]);
		MidiNote midiNote = null;
		while(runFlag){
			//metronomePanel.beatPanel.beatCount = beatCount;
			//metronomePanel.beatPanel.repaint();
			
			midiNote = (beatCount % 2 == 0) ? midiNote1 : midiNote2;
			log.debug(midiNote.vol);
			
			beginMidiNote(midiNote);
			
			sleepInterval = interval - oversleptInterval; 
			expectedWakeUpTime = System.currentTimeMillis() + sleepInterval;
			
			try {
				sleep(sleepInterval);
			} catch(InterruptedException e) {
				log.error(e);
			}
			
			endMidiNote(midiNote);

			beatCount++;
			if (beatCount > numBeats) {
				beatCount = 1;
				if (tempo < endTempo) {
					tempo += increment;
					interval = (int) (1000d * 60d / tempo);
					log.debug(tempo);	
					metronomePanel.metronomeTempoLabel.setText("" + tempo);
				}
			}

			oversleptInterval = System.currentTimeMillis() - expectedWakeUpTime;
			//log.debug(oversleptInterval);
		}
		
		endMidiNote(midiNote);
	}
	
	/**
	 * @param midiNote
	 */
	private void beginMidiNote(MidiNote midiNote) {
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
	public void end() {
		runFlag 		= false;
	}		
}
