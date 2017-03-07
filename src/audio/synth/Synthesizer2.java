package audio.synth;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
this class plays midi in real time using threading
- derived from Synthesizer0
*/
public class Synthesizer2 extends JPanel {
	Synthesizer synthesizer;
	Sequencer sequencer;
	//Sequence sequence;
	Instrument instruments[];
	MidiChannel midiChannels[];
	Player players[];
	Percussionist percussionist;

	
	public Synthesizer2() {
		System.out.println("Synthesizer2()");

		final int numOfPlayers = 4;	
		setLayout(new FlowLayout());
				
		JButton composeB = new JButton("Play");
		composeB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//play();
				players = new Player[numOfPlayers];
				for(int i = 0;i<numOfPlayers; i++){
					players[i] = new Player(i);
				}
				//percussionist = new Percussionist();
			}
		});
		add(composeB);


		//init objects

		//get and open synthesizer and sequencer
		try {
			if (synthesizer == null) {
				if ((synthesizer = MidiSystem.getSynthesizer()) == null) {
					System.out.println("getSynthesizer() failed!");
					return;
				}
			} 
			synthesizer.open();
			sequencer = MidiSystem.getSequencer();
    	sequencer.open();			
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
		
		// get default soundbank
		Soundbank sb = synthesizer.getDefaultSoundbank();

		if (sb == null) {
			System.out.println("couldn't get soundbank");
		} else {
			System.out.println(sb.getDescription());
			System.out.println(sb.getName());			
			System.out.println(sb.getVendor());
			System.out.println(sb.getVersion());

			instruments = sb.getInstruments();
			System.out.println("instruments length " + instruments.length); //366 - med; 189 - delux
		}

		//get channels
		midiChannels = synthesizer.getChannels();
		int mod;
		for(int i = 0;i<midiChannels.length; i++){
			mod = i%2;
			midiChannels[i].controlChange(10, mod*127);
		}
	}
	

	//this task which runs in its own thread
	private class Player extends Thread {

		int channel;		


		Player(int ch) {
			channel = ch;
			start();
		}

		public void run() {
			System.out.println("player " + channel + " run()");

			int numOfNotes = 112; // num of notes on keyboard that will sound
			int	vol = 127;
			int numOfInstrs = 128;
			int numOfEvents = 20;
			int maxTempo = 16;	

			int eventType, pause, instrument; //all

			//scale
			int tempo, tick, note, scaleLen, noteInc;

			//chord			
			int maxChordNotes = 5;
			int chordNotes[] = new int[maxChordNotes];
			int numOfChordNotes; //num of notes in a particular chord
			int chordLen, nextNote ;

			//pattern
			int maxPatternNotes = 6;
			int patternNotes[] = new int[maxPatternNotes];
			int patternLen, repeatLen, thisNote;


			for(int j = 0;j<numOfEvents; j++){									//num of events

				//get kind of event
				eventType = (int)(Math.random() * 2); //TO DO - add phrase type
				pause = (int)(Math.random() * 5000);							//pause between events			
				instrument = (int)(Math.random() * numOfInstrs);
				midiChannels[channel].programChange(instrument);

				switch (eventType) {
					case 0:
						//scale
						tempo = (int)(Math.random() * maxTempo) + 1;			//1=500ms
						tick = 500/tempo;																	//length of each note in event
						note = (int)(Math.random() * numOfNotes);					//start note
						scaleLen = (int)(Math.random() * 40) + 1;					//num of notes in scale
						noteInc = (int)(Math.random() * 5) + 1;						//interval
						if(Math.random()<0.5d)noteInc *= -1;							//up or down

						System.out.println("player " + channel +
															" event " + j +
															" scale" + 
															" length " + scaleLen +
															" instrument " + instrument + " " + instruments[instrument].getName()
															);
		
						//play scale
						for(int i=0;i<scaleLen;i++){
							midiChannels[channel].noteOn(note,vol);
							try {
								sleep(tick);
							} catch(InterruptedException e) {
								System.err.println("Interrupted");
							}
							midiChannels[channel].noteOff(note,vol);
							note += noteInc;
							if((note>numOfNotes-1)||(note<0))break;
						}
						break;

					case 2:
						//chord
						chordLen = (int)(Math.random() * 4000) + 500;
						numOfChordNotes = (int)(Math.random() * maxChordNotes) + 1; //num of notes in this chord
						chordNotes[0] = (int)(Math.random() * numOfNotes);
						for(int i = 1;i<numOfChordNotes; i++){
							nextNote = chordNotes[i-1] + (int)(Math.random() * 6) + 1;
							if(nextNote>numOfNotes-1){
								numOfChordNotes = i;
								break;
							} else {
								chordNotes[i] = nextNote;
							}
						}
						
						System.out.println("player " + channel +
															" event " + j +
															" chord" + 
															" length " + chordLen + 
															" notes " + numOfChordNotes + 
															" instrument " + instrument + " " + instruments[instrument].getName()
															);

		
						//play chord
						for(int i=0;i<numOfChordNotes;i++){
							midiChannels[channel].noteOn(chordNotes[i],vol);
						}
						try {
							sleep(chordLen);
						} catch(InterruptedException e) {
							System.err.println("Interrupted");
						}
						for(int i=0;i<numOfChordNotes;i++){
							midiChannels[channel].noteOff(chordNotes[i],vol);
						}

						break;

					case 1:
						//pattern
						//int maxPatternNotes = 5;
						//int patternNotes[] = new int[maxPatternNotes];
						//int patternLen, repeatLen;
 
						tempo = (int)(Math.random() * maxTempo) + 1;			//1=500ms
						tick = 500/tempo;																	//length of each note in pattern
						note = (int)(Math.random() * numOfNotes);					//start note
						patternLen = (int)(Math.random() * maxPatternNotes) + 1;				//num of notes in pattern
						repeatLen = (int)(Math.random() * 12) + 1;				//num of times pattern is repeated
						noteInc = (int)(Math.random() * 5) + 1;						//increment after each repetition
						if(Math.random()<0.5d)noteInc *= -1;							//up or down

						System.out.println("player " + channel +
															" event " + j +
															" pattern" + 
															" notes " + patternLen +
															" repeats " + repeatLen +
															" instrument " + instrument + " " + instruments[instrument].getName()
															);
		
						for(int i = 0;i<patternLen; i++){
							patternNotes[i] = (int)(Math.random() * 6) + 1;
						}
		
						//play repeats
						thisNote = 0;
						for(int i=0;i<repeatLen;i++){
							for(int k=0;k<patternLen;k++){
								thisNote = note + patternNotes[k];
								if((thisNote>numOfNotes-1)||(thisNote<0))break;
								midiChannels[channel].noteOn(thisNote,vol);
								try {
									sleep(tick);
								} catch(InterruptedException e) {
									System.err.println("Interrupted");
								}
								midiChannels[channel].noteOff(thisNote,vol);
							} //k
							if((thisNote>numOfNotes-1)||(thisNote<0))break;
							note += noteInc;
						} //i
						break;				
		
				} //switch


				//pause between events
				try {
					sleep(pause);
				} catch(InterruptedException e) {
					System.err.println("Interrupted");
				}


			} //j
		} //run
	} //class Player



	//this task which runs in its own thread
	private class Percussionist extends Thread {

		int channel = 9;		

		Percussionist() {
			start();
		}

		public void run() {
			System.out.println("percussionist run()");
	
			int tempo, tick, instrument, note, eventLen, noteInc, pause;
			
			int numOfNotes = 128; // num of notes on keyboard that will sound
			int	vol = 127;
			int numOfEvents = 20;

			midiChannels[channel].programChange(0);
			for(int j = 0;j<numOfEvents; j++){ //num of phrases

				tempo = (int)(Math.random() * 8) + 1; //1=500ms
				tick = 500/tempo; //length of each note in event
				eventLen = (int)(Math.random() * 40) + 1;
				pause = (int)(Math.random() * 5000); //pause between events
		
				System.out.println("percussionist event " + j + " eventlength " + eventLen);

				//play event

				for(int i=0;i<eventLen;i++){
					note = (int)(Math.random() * 62)+26;
					midiChannels[channel].noteOn(note,vol);
					try {
						sleep(tick);
					} catch(InterruptedException e) {
						System.err.println("Interrupted");
					}
					midiChannels[channel].noteOff(note,vol);
				}
		
				//pause between events
				try {
					sleep(pause);
				} catch(InterruptedException e) {
					System.err.println("Interrupted");
				}

			} //j

		}

	}

	public void close() {
		if (synthesizer != null) {
			synthesizer.close();
		}
		if (sequencer != null) {
			sequencer.close();
		}
		sequencer = null;
		synthesizer = null;
		instruments = null;
	}
	
	
	
	public static void main(String args[]) {

    final Synthesizer2 s = new Synthesizer2();

		JFrame f = new JFrame("Synthesizer2");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				s.close();
				System.exit(0);
			}
		});
		//f.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		f.setLocation(screenSize.width/4, screenSize.height/4);
		f.setSize(screenSize.width/2, screenSize.height/2);



    f.getContentPane().add("Center", s);
		f.setVisible(true);

    //s.open();
	}


} 

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
