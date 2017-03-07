package audio.gui.player;

import javax.sound.midi.MidiChannel;

import audio.gui.Note;

public class CelticPlayer extends Player {
	String type = "";
	
	public CelticPlayer(
			MidiChannel[] midiChannels, 
			int root, 
			double bpm,
			String type) {
		super(midiChannels, root, bpm);
		this.type = type;
	}
	
	public void run() {
		int OCTAVE	= 12;
		int FIFTH	= 7;
		int o1		= R; // + OCTAVE
		int o2		= o1 + OCTAVE;
		int ffth2	= o2 + FIFTH;
		int o3		= o2 + OCTAVE;
		int ffth3	= o2 + FIFTH;

		// channels
		int DRONE 	= 0;
		int DRUM 	= 1;
		
		MidiChannel drone = midiChannels[DRONE];
		drone.controlChange(10, 0);
		drone.programChange(20);  // reed organ

		MidiChannel drum = midiChannels[DRUM];
		drum.controlChange(10, 127);
		drum.programChange(47);		
		
		beginNote(new Note(DRONE, o1, 		-1, V1));
		beginNote(new Note(DRONE, o2, 		-1, V1));
		beginNote(new Note(DRONE, ffth2,	-1, V1));
		beginNote(new Note(DRONE, o3, 		-1, V1));
		beginNote(new Note(DRONE, ffth3,	-1, V1));
		
		if (type.equals("Reel")) {
			int beat = (int) (1000d * 60d / (bpm * 2));
			Note one = new Note(DRUM, R, 1, V5);
			Note two = new Note(DRUM, R, 1, V8);

			boolean on = true;
			while(runFlag){
				if (on) {
					beginNote(one);	
				} else {
					beginNote(two);
				}
				
				try {
					sleep(beat);
				} catch(InterruptedException e) {
					log.error(e);
				}

				if (on) {
					endNote(one);
					notes.remove(one);
				} else {
					endNote(two);
					notes.remove(two);
				}

				on = !on; // toggle
			}
			
		} else {
			int beat = (int) (1000d * 60d / bpm);
			Note drumNote = new Note(DRUM, R, 1, V8);
			
			while(runFlag){
				//drum.noteOn(o1, 127);
				beginNote(drumNote);
				try {
					sleep(beat);
				} catch(InterruptedException e) {
					log.error(e);
				}
				endNote(drumNote);
				notes.remove(drumNote);
			}
			
		}
	}
}
