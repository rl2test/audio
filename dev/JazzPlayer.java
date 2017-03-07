package audio.gui.player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiChannel;

import org.apache.log4j.Logger;

import audio.Util;
import audio.gui.AudioController;
import audio.gui.Chord;
import audio.gui.JazzChord;
import audio.gui.Note;

public class JazzPlayer extends Player {
	/** The log. */
	private Logger log = Logger.getLogger(this.getClass());
	private static Map<String, JazzChord> chordMap 	= new HashMap<String, JazzChord>();
	private String sequence 					= null;
	private AudioController audioController		= null;
	
	public JazzPlayer(
			MidiChannel[] midiChannels, 
			double bpm,
			String tuneSelected) {
		super(midiChannels, bpm);
		sequence = getSequenceFromFile(tuneSelected);
		audioController = AudioController.getInstance();
	}

	public void run() {
		MidiChannel channel = midiChannels[0];
		channel.controlChange(10, 64);
		
//		public static final int AcousticGrand = 1;
//		public static final int ElectricPiano1 = 5;
//		public static final int ElectricPiano2 = 6;
//
//		public static final int RockOrgan = 19;
//		public static final int ReedOrgan = 21;
//
//		public static final int AcousticGuitarNylon = 25;		
		channel.programChange(1);	
		//channel.programChange(AcousticGrand);
		
		int pos = sequence.indexOf("|");
		String rhythm = sequence.substring(0, pos);
		// 3/4:90
		String[] arr = rhythm.split(":");
		
		int beatsPerBar = Integer.parseInt(arr[0].substring(0, 1));
		int bpm 		= Integer.parseInt(arr[1]); // beats/min

		int pulseLen	= (int) (1000d * 60d / bpm);
		
		log.debug("rhythm=" + rhythm);
		log.debug("bpm=" + bpm);
		log.debug("beatsPerBar=" + beatsPerBar);
		log.debug("pulseLen=" + pulseLen);
		log.debug("sequence=" + sequence);
		
		sequence = sequence.substring(pos + 1);
		log.debug("sequence=" + sequence);
		
		String[] bars = sequence.split(PIPE_DELIM); //.substring(2)
		log.debug("bars.length=" + bars.length);

		List<JazzChord> chords = new ArrayList<JazzChord>(); 

		int barCount = 1;
		try {
			String lastChordString = "";
			for (String bar: bars) {
				bar = bar.trim();
				log.debug(barCount + ". bar=" + bar);

				if (bar.equals("") && !lastChordString.equals("")) {
					// bar is empty so repeat last chord if present
					JazzChord chord = getJazzChord(lastChordString);
					for (int i = 0; i < beatsPerBar; i++) {
						chords.add(chord);
					}
				} else if (bar.indexOf("/") == -1 && bar.indexOf(" ") == -1) {
					// the bar contains a single chord, which will be repeated for the whole bar, one chord per beat
					String chordString = bar;
					JazzChord chord = getJazzChord(chordString);
					for (int i = 0; i < beatsPerBar; i++) {
						chords.add(chord);
					}
					lastChordString = chordString;
				} else {
					// the bar contains a multiple chords, which will be played one chord per beat
					String[] chordStrs = bar.split(" ");
					for(String chordString: chordStrs) {
						chordString = chordString.trim();
						log.debug("chordString=" + chordString);
						if (chordString.equals("/")) {
							chordString = lastChordString;
						}
						JazzChord chord = getJazzChord(chordString);
						chords.add(chord);
						lastChordString = chordString;
					}
				}
				barCount++;
			}
		} catch (Exception e) {
			log.error(e.toString());
		}
		
		int pulseCount = 0;

		while(runFlag){
			// handle note off events
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
			
			// break out of loop
			if (!runFlag) {
				break;
			}
	
			// otherwise continue
			JazzChord chord = chords.get(pulseCount);				
			log.debug(pulseCount + " " + chord);

			for (int c: chord.notes) {
				beginNote(new Note(0, c, 1, V4));	
			}
			
			try {
				sleep(pulseLen);
			} catch(InterruptedException e) {
				log.error(e);
			}

			pulseCount++;
			if (pulseCount > chords.size() - 1) {
				// reached the end of the cords array, so reset pulseCount to 0
				pulseCount = 0;
			}
		}
	}
	
	private JazzChord getJazzChord(String key) throws Exception {
		JazzChord chord = chordMap.get(key);
		if (chord == null) {
			chord = new JazzChord(key);
			chordMap.put(key, chord);
		}
		
		return chord;
	}
	
	/**
	 * @param tune
	 * @return
	 */
	private String getSequenceFromFile(String tune) {
		List<String> lines = Util.getLines(new File(JAZZ_CHORDS_DIR + FS + tune));
		String sequence = "";
		for(String line: lines) {
			line = line.trim();
			sequence += line;
		}
		//sequence = Util.removeSpaces(sequence);
		log.debug(sequence);

		//sequence = expandSequence(sequence, "{", "}");
		//sequence = expandSequence(sequence, "[", "]");
		
		return sequence;
	}
	
	/*
	private String expandSequence(String sequence, String start, String end) {
		if (sequence.indexOf(start) != -1) {
			// process repeats
			StringBuffer sb = new StringBuffer();
			int pos = 0;

			while (sequence.indexOf(end) != -1) {
				pos = sequence.indexOf(start);
				sb.append(sequence.substring(0, pos));
				sequence = sequence.substring(pos + 1); 
						
				pos = sequence.indexOf(end);
				String repeat = sequence.substring(0, pos);
				//log.debug("repeat=" + repeat);
				String[] info = repeat.split(":");
				String phrase = info[0];
				int num = Integer.parseInt(info[1]);
				for (int i = 0; i < num; i++) {
					sb.append(phrase);
				}
				sequence = sequence.substring(pos + 1);				
			}
			sb.append(sequence);

			sequence =  sb.toString();
		}
		return sequence;
	}
	*/
}

