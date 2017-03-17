package audio.chords.gui;

//import static audio.Constants.BANDONEON;
import static audio.Constants.CLARINET;
import static audio.Constants.O;
import static audio.Constants.PERFECT_FIFTH;
import static audio.Constants.TRANSPOSE_KEY_INTERVALS;
import static audio.Constants.V;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiChannel;

import org.apache.log4j.Logger;

import audio.MidiNote;

public class DronePlayer extends Thread {
	/** The log. */
	protected Logger log 					= Logger.getLogger(this.getClass());
	protected boolean runFlag 				= true;
	protected List<MidiNote> midiNotes		= new ArrayList<MidiNote>();
	private int interval 					= 0;
	
	public DronePlayer(int keyIndex) {
		interval = TRANSPOSE_KEY_INTERVALS[keyIndex];
	}
	
	public void run() {
		log.debug("run()");
		
		// set channel
		MidiChannel channel = AudioController.midiChannels[0];
		
		// set stereo r/l
		channel.controlChange(10, 64); 
		
		// set instrument
		channel.programChange(CLARINET); //CLARINET
		
		MidiNote rootNote = null;
		MidiNote fifthNote = null;

		int root = (interval >= PERFECT_FIFTH) ? O[2] + interval: O[3] + interval;
		int fifth = root + PERFECT_FIFTH;
		
		rootNote = new MidiNote(0, root, 1, V[8]);
		fifthNote = new MidiNote(0, fifth, 1, V[6]);
			
		beginMidiNote(rootNote);
		beginMidiNote(fifthNote);
	}
	
	/**
	 * @param midiNote
	 */
	private void beginMidiNote(MidiNote midiNote) {
		midiNotes.add(midiNote);
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
		for (MidiNote midiNote: midiNotes) {
			endMidiNote(midiNote);
		}
		midiNotes.clear();
		midiNotes	= null;
		log 		= null;
	}	
}
