package audio.chords.gui;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiChannel;

import org.apache.log4j.Logger;


public class BluesPlayer extends Thread implements GuiConstants {
	/** The log. */
	protected Logger log 			= Logger.getLogger(this.getClass());
	protected int root;
	protected double bpm;
	protected boolean runFlag 		= true;
	protected List<Note> notes		= new ArrayList<Note>();
	protected List<Note> endNotes	= new ArrayList<Note>();
	
	/* interval definitions */
	public static final int i 	= 0;
	public static final int i2d = 1;
	public static final int i2  = 2;
	public static final int i2a = 3;
	
	public static final int i3d = 3;
	public static final int i3  = 4;		
	public static final int i3a = 5;
	
	public static final int i4d	= 4;
	public static final int i4 	= 5;
	public static final int i4a = 6;
	
	public static final int i5d = 6;
	public static final int i5 	= 7;
	public static final int i5a = 8;
	
	public static final int i6d = 8;
	public static final int i6 	= 9;
	public static final int i6a = 10;
	
	public static final int i7d = 10;
	public static final int i7 	= 11;
	public static final int i7a = 12;	
	public static final int i8 	= 12;
	
	public BluesPlayer(
			MidiChannel[] midiChannels, 
			int root, 
			double bpm) {
		this.root 	= root;
		this.bpm	= bpm;

	}
	
	public void run() {
		int BASS_1 = 0;
		int BASS_2 = 1;
		
		MidiChannel bass1 = GuiController.midiChannels[BASS_1];
		bass1.controlChange(10, 0);
		bass1.programChange(34);  // acoustic bass

		MidiChannel bass2 = GuiController.midiChannels[BASS_2];
		bass2.controlChange(10, 64);
		bass2.programChange(34);  // acoustic bass		

		int ppb 		= 3; // pulse per beat
		int pulseLen	= (int) (1000d * 60d / (bpm * ppb));
		
		log.debug("pulse=" + pulseLen);
		
		int pulse 		= 1; // pulse count
		int beat 		= 1; // beat count
		int bar			= 1; // bar count
		int verse		= 1; // verse count
		
		// set init values
		int r  			= root;
		int r3d			= r + i3d;
		int r3			= r + i3;
		int r5 			= r + i5;
		int r6 			= r + i6;		
		
		log.debug("verse=" + verse);

		while(runFlag){
			endNotes.clear(); 
			for (Note note: notes) {
				note.len--;
				if (note.len == 0) {
					endNote(note);
					endNotes.add(note);
				}
			}
			
			for (Note note: endNotes) {
				notes.remove(note);
			}
			
			// bars
			// 1  2  3  4
			// I  I  I  I
			//	
			// 5  6  7  8
			// IV IV I  I
			//	
			// 9  10 11 12
			// V  IV I  V
			
			// ////////////		// pc - pulse count
			// |  |  |  |		// bc - beat count
			// 1  2  3  4		// bc - beat count
			// bass2
			// 5 56 65  335		// interval
			//          d		// modifier
			// bass1
			// r rr rr rr r		// r - chord root

			// bass1
			if (pulse == 1) {
				beginNote(new Note(BASS_1, r, 2, V8));
			} else if (pulse == 3) {
				beginNote(new Note(BASS_1, r, 1, V6));
			}
			
			// bass2
			if (beat == 1) {
				if (pulse == 1) {
					beginNote(new Note(BASS_2, r5, 2, V3));
				}
				if (pulse == 3) {
					beginNote(new Note(BASS_2, r5, 1, V2));
				}
			} else if (beat == 2) {
				if (pulse == 1) {
					beginNote(new Note(BASS_2, r6, 2, V3));
				}
				if (pulse == 3) {
					beginNote(new Note(BASS_2, r6, 1, V2));
				}
			} else if (beat == 3) {
				if (pulse == 1) {
					beginNote(new Note(BASS_2, r5, 2, V3));
				}
			} else if (beat == 4) {
				if (pulse == 1) {
					beginNote(new Note(BASS_2, r3d, 1, V4));
				} else if (pulse == 2) {
					beginNote(new Note(BASS_2, r3, 1, V3));
				} else if (pulse == 3) {
					beginNote(new Note(BASS_2, r5, 1, V2));
				} else {
				}
			} else {
			}
			
			try {
				sleep(pulseLen);
			} catch(InterruptedException e) {
				log.error(e);
			}

			pulse++;
			if (pulse > 3) {
				pulse = 1;
				beat++;
				if (beat > 4) {
					beat = 1;
					bar++;
					if (bar > 12) {
						bar = 1;
						verse++;
						log.debug("verse=" + verse);
					}
					if (bar <= 4 || bar == 7 || bar == 8 || bar == 11) {
						r = root;
					} else if (bar == 5 || bar == 6 || bar == 10) {
						r = root + i4;
					} else {
						r = root + i5;
					}

					r3d	= r + i3d;
					r3	= r + i3;
					r5 	= r + i5;
					r6 	= r + i6;		
				}
			}
		}
	}

	protected void beginNote(Note note) {
		notes.add(note);
		GuiController.midiChannels[note.channel].noteOn(note.pitch, note.vol);
	}
	
	protected void endNote(Note note) {
		GuiController.midiChannels[note.channel].noteOff(note.pitch);
	}

	public void end() {
		runFlag = false;
		for (Note note: notes) {
			endNote(note);
		}
		notes.clear();
		log = null;
	}
}

//int PERC_1 = 3; // not used		
/*
MidiChannel perc1 = midiChannels[PERC_1];
perc1.controlChange(10, 127);
perc1.programChange(47);
*/
