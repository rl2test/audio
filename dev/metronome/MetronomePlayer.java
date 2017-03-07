package audio.metronome;

import static audio.Constants.V8;

import javax.sound.midi.MidiChannel;

import org.apache.log4j.Logger;

import audio.MidiNote;

public class MetronomePlayer extends Thread {
	/** The log. */
	protected Logger log 					= Logger.getLogger(this.getClass());
	protected boolean runFlag 				= true;
	private int PITCH 						= 72;
	private int CHANNEL 					= 0;
	private int PROGRAM 					= 113;
	private int beginTempo;
	private int endTempo;
	private int numBeats;
	private int increment;
	public MetronomePanel metronomePanel 	= null;
	
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
		MidiChannel channel = Metronome.midiChannels[CHANNEL];
		
		// set stereo r/l
		channel.controlChange(10, 127); 
		
		// set instrument
		channel.programChange(PROGRAM);
		
		// set initial tempo (bpm)
		int tempo = beginTempo;
		int beatLen = (int) (1000d * 60d / tempo);
		log.debug("beatLen=" + beatLen);
		
		// init tempo label
		metronomePanel.metronomeTempoLabel.setText("" + tempo);
		
		int beatCount = 1;
		MidiNote midiNote = new MidiNote(CHANNEL, PITCH, 1, V8);
		
		while(runFlag){
			metronomePanel.beatPanel.beatCount = beatCount;
			metronomePanel.beatPanel.repaint();

			beginMidiNote(midiNote);
			
			try {
				sleep(beatLen - 5);
			} catch(InterruptedException e) {
				log.error(e);
			}
			
			endMidiNote(midiNote);

			beatCount++;
			if (beatCount > numBeats) {
				beatCount = 1;
				if (tempo < endTempo) {
					tempo += increment;
					beatLen = (int) (1000d * 60d / tempo);
					log.debug(tempo);	
					metronomePanel.metronomeTempoLabel.setText("" + tempo);
				}
			}
		}
		
		endMidiNote(midiNote);
	}
	
	/**
	 * @param midiNote
	 */
	private void beginMidiNote(MidiNote midiNote) {
		Metronome.midiChannels[midiNote.channel].noteOn(midiNote.pitch, midiNote.vol);
	}
	
	/**
	 * @param midiNote
	 */
	private void endMidiNote(MidiNote midiNote) {
		Metronome.midiChannels[midiNote.channel].noteOff(midiNote.pitch);
	}

	/**
	 * 
	 */
	public void end() {
		runFlag 		= false;
		metronomePanel 	= null;	
		log 			= null;
	}	
}
