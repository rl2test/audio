package audio.chords.gui;

import static audio.Constants.CLARINET;
import static audio.Constants.GUITAR_STRINGS;
import static audio.Constants.INSTRUMENT_VIOLIN;
import static audio.Constants.V8;
import static audio.Constants.VIOLIN_STRINGS;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiChannel;

import org.apache.log4j.Logger;

import audio.MidiNote;

public class NotePlayer extends Thread {
	/** The log. */
	protected Logger log 					= Logger.getLogger(this.getClass());
	protected boolean runFlag 				= true;
	protected List<MidiNote> midiNotes		= new ArrayList<MidiNote>();
	private String instrument 				= "";
	
	public NotePlayer(String instrument) {
		this.instrument = instrument;
	}
	
	public void run() {
		log.debug("run()");
		
		// set channel
		MidiChannel channel = GuiController.midiChannels[0];
		
		// set stereo r/l
		channel.controlChange(10, 127); 
		
		// set instrument
		channel.programChange(CLARINET);
		
		MidiNote midiNote = null;
		int noteLen = 4000;
		int numNotes = instrument.equals(INSTRUMENT_VIOLIN) 
				? 4 
				: 6;
		int count = 0;
		while(runFlag){
			midiNote = (instrument.equals(INSTRUMENT_VIOLIN))
					? new MidiNote(0, VIOLIN_STRINGS[count], 1, V8)
					: new MidiNote(0, GUITAR_STRINGS[count], 1, V8);
			beginMidiNote(midiNote);
			
			try {
				sleep(noteLen);
			} catch(InterruptedException e) {
				log.error(e);
			}
			
			endMidiNote(midiNote);
			if (midiNotes != null) {
				midiNotes.remove(midiNote);
			}
			
			count++;
			if (count == numNotes) {
				count = 0;
			}
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
