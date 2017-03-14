package audio.chords.gui;

//import static audio.Constants.BANDONEON;
import static audio.Constants.CLARINET;
import static audio.Constants.O;
import static audio.Constants.TRANSPOSE_KEYS;
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
	private String key 						= "";
	
	public DronePlayer(String key) {
		this.key = key;
	}
	
	public void run() {
		log.debug("run()");
		
		// set channel
		MidiChannel channel = GuiController.midiChannels[0];
		
		// set stereo r/l
		channel.controlChange(10, 64); 
		
		// set instrument
		channel.programChange(CLARINET); //CLARINET
		
		MidiNote rootNote 	= null;
		MidiNote fifthNote 	= null;

		int gInterval		= getKeyInterval("G");
		int keyInterval 	= getKeyInterval(key);
		
		int oct = (keyInterval >= gInterval) ? O[2] : O[3];
		
		int root = oct + keyInterval;
		int fifth = root + 7;
		
		rootNote 			= new MidiNote(0, root, 1, V[8]);
		fifthNote 			= new MidiNote(0, fifth, 1, V[6]);
			
		beginMidiNote(rootNote);
		beginMidiNote(fifthNote);
	}
	
	/**
	 * @param key
	 * @return the numeric interval of this key relative to C
	 */
	private int getKeyInterval(String key) {
		for (int i = 0; i < TRANSPOSE_KEYS.length; i++) {
			if (TRANSPOSE_KEYS[i].equals(key)) {
				return TRANSPOSE_KEY_INTERVALS[i];
			}
		}
		return 0;
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
