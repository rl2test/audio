package audio.chords.gui;

import static audio.Constants.O;
import static audio.Constants.V;

import javax.sound.midi.MidiChannel;

import org.apache.log4j.Logger;

import audio.MidiNote;

public class MetronomePlayer extends Thread {
	/** The log. */
	protected Logger log 					= Logger.getLogger(this.getClass());
	protected volatile boolean runFlag 		= true;
	private int PITCH 						= 42; // 35|37|42
	private int CHANNEL 					= 9;
	public int beginTempo;
	public int endTempo;
	public int numBeats;
	public int increment;
	public TimePanel panel	= null;

    public MetronomePlayer(int beginTempo, int endTempo, int increment, int numBeats, TimePanel panel) {
    	this.beginTempo = beginTempo;
    	this.endTempo = endTempo;
    	this.numBeats = numBeats;
    	this.increment = increment;
    	this.panel = panel;
     }
	
	public void run() {
		log.debug("run()");
 		
		// set channel
		MidiChannel channel = AudioController.midiChannels[CHANNEL];
		
		// set stereo r/l
		channel.controlChange(10, 127); 
		
		// set initial tempo (bpm)
		int tempo = beginTempo;

		// the interval between beats
		int interval 			= (int) (1000d * 60d / tempo);
		log.debug("interval=" + interval);
		
		// the sleepInterval
		long sleepInterval = 0;
		
		// the interval in ms by which the thread overslept 
		long oversleptInterval = 0;
		
		// the expectedWakeUpTime
		long expectedWakeUpTime = 0;		
		
		// the beat count, used to track when the tempo should be incremented
		int beatCount = 1;

		// init tempo label
		panel.tempoValueLabel.setText("" + tempo);
		
		// the note to be played by the metronome
		MidiNote midiNote1 = new MidiNote(CHANNEL, PITCH, 1, V[5]);
		MidiNote midiNote2 = new MidiNote(CHANNEL, PITCH, 1, V[8]);
		MidiNote midiNote = null;
		while(runFlag){
			midiNote = (beatCount % 2 == 0) ? midiNote1 : midiNote2;
			//log.debug(midiNote.vol);
			
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
					panel.tempoValueLabel.setText("" + tempo);
				}
			}
			oversleptInterval = System.currentTimeMillis() - expectedWakeUpTime;
		}
		
		endMidiNote(midiNote);
	}
	
	/**
	 * @param midiNote
	 */
	private void beginMidiNote(MidiNote midiNote) {
		AudioController.midiChannels[midiNote.channel].noteOn(midiNote.pitch, midiNote.vol);
	}
	
	/**
	 * @param midiNote
	 */
	private void endMidiNote(MidiNote midiNote) {
		AudioController.midiChannels[midiNote.channel].noteOff(midiNote.pitch);
	}

	/**
	 * 
	 */
	public void end() {
		runFlag = false;
	}		
}
