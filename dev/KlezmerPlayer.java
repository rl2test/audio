package audio.gui.player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiChannel;

import audio.Util;
import audio.gui.AudioController;
import audio.gui.Chord;
import audio.gui.Note;

public class KlezmerPlayer extends Player {
	private static Map<String, Chord> chordMap 	= new HashMap<String, Chord>();
	private String sequence 					= null;
	private AudioController audioController		= null;
	private final int INC						= 10;
	
	public KlezmerPlayer(
			MidiChannel[] midiChannels, 
			double bpm,
			String tuneSelected) {
		super(midiChannels, bpm);
		sequence = getSequenceFromFile(tuneSelected);
		audioController = AudioController.getInstance();
	}

	public void run() {
		/* channel definitions */
		int BASS_1 	= 0;
		int CHORD 	= 1;
		
		MidiChannel bass1 = midiChannels[BASS_1];
		bass1.controlChange(10, 0);
		bass1.programChange(34);  // acoustic bass

		MidiChannel bass2 = midiChannels[CHORD];
		bass2.controlChange(10, 64);
		bass2.programChange(1);  // piano		
		
		int pos = sequence.indexOf("|");
		String rhythm = sequence.substring(0, pos);
		//3/4:1:90-120
		String[] arr = rhythm.split(":");
		int len = arr.length;
		
		int beatsPerBar = Integer.parseInt(arr[0].substring(0, 1));
		int pulse 		= Integer.parseInt(arr[1]); // pulses per beat
		int limit 		= -1; // if tempo should get faster after each repeat, then this will have a positeve value > bpm

		if (bpm == 0) {
			if (len == 3) {
				String tempo = arr[2]; 
				int index = tempo.indexOf("-");
				if (index != -1) {
					// range
					bpm =  Integer.parseInt(tempo.substring(0, index));
					limit =  Integer.parseInt(tempo.substring(index + 1));
				} else {
					bpm = Integer.parseInt(tempo);	
				}
				
			} else {
				bpm = 90; // default
			}
			audioController.setTempo((int) bpm);
		}
		int pulseLen	= (int) (1000d * 60d / (bpm * pulse));
		
		sequence = sequence.substring(pos + 1);
		
		log.debug("rhythm=" + rhythm);
		log.debug("bpm=" + bpm);
		log.debug("beatsPerBar=" + beatsPerBar);
		log.debug("pulse=" + pulse);
		log.debug("pulseLen=" + pulseLen);
		log.debug("sequence=" + sequence);
		
		String[] bars = sequence.split("\\|"); //.substring(2)
		log.debug("bars.length=" + bars.length);

		Chord chord = null;
		List<Chord> chords = new ArrayList<Chord>(); 
		
		for (String bar: bars) {
			//log.debug(bar);
			if (bar.indexOf(",") == -1) {
				// the bar contains a single chord, which will be repeated for the whole bar
				chord = getChord(bar);
				for (int i = 0; i < beatsPerBar; i++) {
					chords.add(chord);
				}
			} else {
				// the bar contains a multiple chords, which will be played as written, one chord per beat
				String[] barChords = bar.split(",");
				for(String barChord: barChords) {
					chord = getChord(barChord);
					chords.add(chord);
				}
			}
		}
		
		int pulseCount = 0;
		Chord lastChord = null;		

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
			
			if (beatsPerBar == 2) {
				if (pulse == 2) {
					if (pulseCount % 2 == 0) {
						// get the next chord
						chord = chords.get(pulseCount / 2);				
						//log.debug(pulseCount + " " + chord);
					}
					
					// example: 2 beatsPerBar: pulse = 2
					// pulse   0 1 2 3 4...
					// beat    1   2   1...
					// polyad    |   | 
					// fifth       .   
					// root    .       .
					// % 4     0 1 2 3 0...  
					
					int mod = pulseCount % 4;
					if (mod == 0) {
						beginNote(new Note(BASS_1, chord.rootVal, 2, V8));
					} else if (mod == 1 || mod == 3) {
						for (int c: chord.polyadVals) {
							beginNote(new Note(CHORD, c, 1, V4));	
						}
					} else { // mod == 2
						if (lastChord == chord) {
							beginNote(new Note(BASS_1, chord.fifthVal, 2, V8));
						} else {
							beginNote(new Note(BASS_1, chord.rootVal, 2, V8));
						}
					}
				} else {
					log.error("Unhandled pulse=" + pulse);
				}
			} else if (beatsPerBar == 3) {
				if (pulse == 1) {
					// example: 3 beatsPerBar
					// pulse   0 1 2 0 1 2 0...
					// beat    1 2 3 1 2 3 1...
					// polyad    | |   | |
					// fifth         .
					// root    .
					// % 3     0 1 2 0 1 2 0...  
					chord = chords.get(pulseCount);				
					//log.debug(pulseCount + " " + chord);

					int mod = pulseCount % 3;
					if (mod == 0) {
						beginNote(new Note(BASS_1, chord.rootVal, 1, V8));
						// TODO logic for alternating root and fifth
						/* 		
						if (lastChord == chord) {
							beginNote(new Note(BASS_1, chord.fifth, 1, v8));
						} else {
							beginNote(new Note(BASS_1, chord.root, 1, v8));
						}
						*/
					} else if (mod == 1 || mod == 2) {
						for (int c: chord.polyadVals) {
							beginNote(new Note(CHORD, c, 1, V4));	
						}
					}				
				} else {
					log.error("Unhandled pulse=" + pulse);
				}
			} else if (beatsPerBar == 4) {	
				if (pulse == 1) {
					// example: 4 beatsPerBar
					// pulse   0 1 2 3 0...
					// beat    1 2 3 4 1...
					// polyad    |   |   
					// fifth       .
					// root    .       .
					// % 4     0 1 2 3 0...  
					chord = chords.get(pulseCount);				
					//log.debug(pulseCount + " " + chord);

					int mod = pulseCount % 4;
					if (mod == 0) {
						beginNote(new Note(BASS_1, chord.rootVal, 1, V8));
					} else if (mod == 1 || mod == 3) {
						for (int c: chord.polyadVals) {
							beginNote(new Note(CHORD, c, 1, V4));	
						}
					} else { // mod == 3
						if (lastChord == chord) {
							beginNote(new Note(BASS_1, chord.fifthVal, 1, V8));
						} else {
							beginNote(new Note(BASS_1, chord.rootVal, 1, V8));
						}
					}				
				} else if (pulse == 2) {
					if (pulseCount % 2 == 0) {
						// get the next chord
						chord = chords.get(pulseCount / 2);				
						//log.debug(pulseCount + " " + chord);
					}
					
					// example: 4 beatsPerBar
					// pulse   0 1 2 3 4 5 6 7 8...
					// beat    1   2   3   4   1...
					// polyad    |   |   |   |
					// fifth       .       .
					// root    .       .       .
					// % 2     0 1 0 1 0 1 0 1 0
					// % 4     0 1 2 3 0 1 2 3 0  
					
					int mod = pulseCount % 4;
					if (mod == 0) {
						beginNote(new Note(BASS_1, chord.rootVal, 2, V8));
					} else if (mod == 1 || mod == 3) {
						for (int c: chord.polyadVals) {
							beginNote(new Note(CHORD, c, 1, V4));	
						}
					} else { // mod == 2
						if (lastChord == chord) {
							beginNote(new Note(BASS_1, chord.fifthVal, 2, V8));
						} else {
							beginNote(new Note(BASS_1, chord.rootVal, 2, V8));
						}
					}	
				}
			} else {
				log.error("Unhandled beatsPerBar=" + beatsPerBar);
			}
			
			try {
				sleep(pulseLen);
			} catch(InterruptedException e) {
				log.error(e);
			}

			pulseCount++;
			if ((pulseCount / pulse) > chords.size() - 1) {
				// reached the end of the cords array, so reset pulseCount to 0
				pulseCount = 0;
				
				// increase tempo
				if (limit != -1 && ((int) bpm) < limit) {
					bpm += INC;
					pulseLen = (int) (1000d * 60d / (bpm * pulse));	
					audioController.setTempo((int) bpm);
				}
			}

			lastChord = chord;
		}

	}
	
	private Chord getChord(String key) {
		Chord chord = chordMap.get(key);
		if (chord == null) {
			chord = new Chord(key);
			chordMap.put(key, chord);
		}
		
		return chord;
	}
	
	/**
	 * Sample data:
	 * 
	 * 		4
	 * 		|Dm|Dm,Gm,Dm,Dm|Dm|Dm,A7,Dm,C7
	 * 		|F|F,Bf,F,F|F|Dm,A7,Dm,Dm|Dm,Dm
	 * 		[|A7|Dm:7]|A7|Dm,A7,Dm,A7
	 * 
	 * Note: the format for a repeated phrase is:
	 * 
	 * 		[|A7|Dm:7]
	 * 
	 * @param tune
	 * @return
	 */
	private String getSequenceFromFile(String tune) {
		List<String> lines = Util.getLines(new File(KLEZMER_CHORDS_DIR + FS + tune));
		String sequence = "";
		for(String line: lines) {
			line = line.trim();
			sequence += line;
		}
		sequence = sequence.replace(SPACE, "");
		log.debug(sequence);

		sequence = expandSequence(sequence, "{", "}");
		sequence = expandSequence(sequence, "[", "]");
		
		return sequence;
	}
	
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
}

/**
 * @param s
 * @param n
 * @return s repeated n times
 */
//private String repeat(String s, int n) {
//	String ret = "";
//	for (int i = 0; i < n; i++) {
//		ret += s;
//	}
//	return ret;
//}



/*
sequence  = "4"; 
a = "|Dm|Dm,Gm,Dm,Dm|Dm|Dm,A7,Dm,C7";			// a section
a += "|F|F,Bf,F,F|F|Dm,A7,Dm,Dm|Dm,Dm";
b = repeat("|A7|Dm", 4);						// b 1st time
b += repeat("|A7|Dm", 3) + "|A7|Dm,A7,Dm,A7";	// b 2nd time
sequence += a + b;
*/

//if (tuneSelected.equals("Ale Brider")) {
/*
} else if (tuneSelected.equals("Khasid U Rabina")) {

	sequence = "2";
	a = repeat("|D", 4) + repeat("|Gm|D", 3) + repeat("|D", 2);
	b = repeat("|Gm", 4) + repeat("|F", 2) + repeat("|D", 2);
	c = repeat("|D", 5) + "|D,Cm|Cm|D";
	sequence += repeat(a, 2) + repeat(b, 2) + repeat(c, 2);   

} else if (tuneSelected.equals("Galitzyaner Tanz")) {

	sequence = "2";
	a = repeat("|C,Fm", 2) + repeat("|C,Bfm", 2) + repeat("|C,Fm", 2) + "|C,Bfm" + "|C";
	b = "|Cm|Bfm|Ef|Bf7|Ef|Ef|G7|Cm";
	c = "|Fm,Ef|Bf,Cm|Ef|Ef,Bf|Fm,Cm|G7,Cm|Fm,G7|Cm";
	sequence += repeat(a, 2) + repeat(b, 2) + repeat(c, 2);

} else {

}
*/

//String a 		= "";	// a section
//String b 		= "";	// b section
//String c 		= "";	// c section
//String d 		= "";	// d section

/*
		if (data.indexOf("[") != -1) {
			// process repeats
			StringBuffer sb = new StringBuffer();
			int pos = 0;

			while (data.indexOf("]") != -1) {
				pos = data.indexOf("[");
				sb.append(data.substring(0, pos));
				data = data.substring(pos + 1); 
						
				pos = data.indexOf("]");
				String repeat = data.substring(0, pos);
				//log.debug("repeat=" + repeat);
				String[] info = repeat.split(":");
				String phrase = info[0];
				int num = Integer.parseInt(info[1]);
				for (int i = 0; i < num; i++) {
					sb.append(phrase);
				}
				data = data.substring(pos + 1);				
			}
			sb.append(data);

			sequence =  sb.toString();
		} else {
			sequence = data;
		}
*/
/*
if (bar.equals("")) {
				// the bar contains no chord, in which case the last chord will be used
				chord = lastChord;
				for (int i = 0; i < beatsPerBar; i++) {
					chords.add(chord);
				}
			} else 
*/
