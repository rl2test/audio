package audio.gui.player;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiChannel;

import org.apache.log4j.Logger;

import audio.gui.GuiConstants;
import audio.gui.Note;


public class Player extends Thread implements GuiConstants {
	/** The log. */
	protected Logger log 					= Logger.getLogger(this.getClass());
	public String name 						= "";
	protected MidiChannel[] midiChannels	= null;
	protected int R;
	protected double bpm;
	protected boolean runFlag 				= true;
	protected List<Note> notes				= new ArrayList<Note>();
	protected List<Note> endNotes			= new ArrayList<Note>();
	
	public Player(
			MidiChannel[] midiChannels) {
		this.midiChannels = midiChannels;
	}

	public Player(
			MidiChannel[] midiChannels, 
			double bpm) {
		this.midiChannels = midiChannels;
		this.bpm	= bpm;
	}
	
	public Player(
			MidiChannel[] midiChannels, 
			int root, 
			double bpm) {
		this.midiChannels = midiChannels;
		this.R 	= root;
		this.bpm	= bpm;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
	}

	protected void beginNote(Note note) {
		notes.add(note);
		midiChannels[note.channel].noteOn(note.pitch, note.vol);
	}
	
	protected void endNote(Note note) {
		midiChannels[note.channel].noteOff(note.pitch);
	}

	public void end() {
		runFlag = false;
		for (Note note: notes) {
			endNote(note);
		}
		notes.clear();
	}
}
