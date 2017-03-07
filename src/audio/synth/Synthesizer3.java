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
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/*
this class plays midi in real time using threading
- addition of panning from l to r
*/

public class Synthesizer3 extends JPanel {

	Synthesizer synthesizer;
	Sequencer sequencer;
	Sequence sequence;
	Instrument instruments[];
	MidiChannel midiChannels[];
	Player players[];
	Player percussionist; //uses ch 10
	Choir choir; // uses chs 11-16
	//Conductor conductor;
	
	final JButton playB;
	final JButton stopB;
	
	public Synthesizer3() {
		System.out.println("Synthesizer3()");

		setLayout(new FlowLayout());

		final int numOfPlayers = 3;	
		players = new Player[numOfPlayers];


		// buttons				
		playB = new JButton("Play");
		playB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int i = 0;i<numOfPlayers; i++){
					if(players[i]==null)players[i]=new Player(i);
				}
				//if(percussionist==null)percussionist=new Player(9);
				if(choir==null)choir=new Choir(10,15);
				playB.setEnabled(false);
				stopB.setEnabled(true);
			}
		});
		add(playB);

		stopB = new JButton("Stop");
		stopB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int i = 0;i<numOfPlayers; i++){
					if(players[i]!=null) {
						players[i].runFlag = false;
						players[i]=null;
					}
				}
				if(percussionist!=null) {
					percussionist.runFlag = false;
					percussionist=null;
				}
				if(choir!=null) {
					choir.runFlag = false;
					choir=null;
				}
				playB.setEnabled(true);
				stopB.setEnabled(false);
			}
		});
		add(stopB);


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
		

		//get soundbank
		Soundbank sb = synthesizer.getDefaultSoundbank();
		System.out.print("Soundbank " + sb.getName());
		if(sb.getName().equals("Patches.hsb")){System.out.println(" - original");}
		else if(sb.getName().equals("Untitled Headspace Soundbank")){System.out.println(" - med");}
		else{System.out.println();}
		if (sb != null) {
			instruments = sb.getInstruments();
			System.out.println("instruments length " + instruments.length);
			//orig - 411; med - 366; delux - 189
		} else {System.out.println("couldn't get soundbank");}


		//get channels
		midiChannels = synthesizer.getChannels();

		//set channels to l or r
		int mod;
		for(int i = 0;i<numOfPlayers; i++){
			mod = i%2;
			midiChannels[i].controlChange(10, mod*127);
		}

	}
	

	//this task which runs in its own thread
	private class Player extends Thread {

		int channel;
		public boolean runFlag = true;


		Player(int ch) {
			channel = ch;
			start();
		}

		public void run() {
			System.out.println("player " + channel + " run()");

			int numOfNotes = 128; // num of notes on keyboard that will sound
			int	vol = 127;
			int numOfInstrs = 128;
			int maxTempo = 16;	
			//all
			int eventType, pause, instrument, panDir;
			boolean pan = true;

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
			int patternLen, thisNote;


			while(runFlag){

				//get kind of event
				eventType = (int)(Math.random() * 2); //TO DO - add phrase type
				pause = (int)(Math.random() * 5000);							//pause between events			
				instrument = (int)(Math.random() * numOfInstrs);
				midiChannels[channel].programChange(instrument);
				pan = (Math.random()<0.5d)?true:false;
				panDir = (int)(Math.random() * 2);

				switch (eventType) {
					case 0:
						//scale
						tempo = (int)(Math.random() * maxTempo) + 1;			//1=500ms
						tick = 500/tempo;																	//length of each note in event
						note = (int)(Math.random() * numOfNotes);					//start note
						scaleLen = (int)(Math.random() * 40) + 2;					//num of notes in scale
						noteInc = (int)(Math.random() * 5) + 1;						//interval
						if(Math.random()<0.5d)noteInc *= -1;							//up or down

						System.out.println("player " + channel +
															" scale  " + 
															" length " + scaleLen +
															" pan " + pan + " " + panDir + 
															" instrument " + instrument + " " + instruments[instrument].getName()
															);
		
						//play scale
						for(int i=0;i<scaleLen;i++){
							if(pan){
								if(panDir==0){
									midiChannels[channel].controlChange(10,127 * i/(scaleLen-1));
								} else {
									midiChannels[channel].controlChange(10,127 - 127 * i/(scaleLen-1));								
								}
							}
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



					case 1:
						//pattern
 
						tempo = (int)(Math.random() * maxTempo) + 1;			//1=500ms
						tick = 500/tempo;																	//length of each note in pattern
						note = (int)(Math.random() * numOfNotes);					//start note
						patternLen = (int)(Math.random() * maxPatternNotes) + 1;				//num of notes in pattern
						scaleLen = (int)(Math.random() * 12) + 2;				//num of times pattern is repeated
						noteInc = (int)(Math.random() * 5) + 1;						//increment after each repetition
						if(Math.random()<0.5d)noteInc *= -1;							//up or down

						System.out.println("player " + channel +
															" pattern" + 
															" length " + scaleLen +
															" pan " + pan + " " + panDir + 
															" notes " + patternLen +
															" instrument " + instrument + " " + instruments[instrument].getName()
															);
		
						for(int i = 0;i<patternLen; i++){
							patternNotes[i] = (int)(Math.random() * 6) + 1;
						}
		
						//play repeats
						thisNote = 0;
						for(int i=0;i<scaleLen;i++){
							if(pan){
								if(panDir==0){
									midiChannels[channel].controlChange(10,127 * i/(scaleLen-1));
								} else {
									midiChannels[channel].controlChange(10,127 - 127 * i/(scaleLen-1));								
								}
							}
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
	


			} //while runflag
		} //run
	} //class Player




	//this task which runs in its own thread
	private class Choir extends Thread {

		int channel;
		int lower, upper;
		public boolean runFlag = true;


		Choir(int l, int u) {
			//channel = 10;
			lower = l;
			upper = u;
			start();
		}

		public void run() {
			System.out.println("choir run()");

			int numOfNotes = 128; // num of notes on keyboard that will sound
			int	vol = 127;
			int numOfInstrs = 128;
			int maxTempo = 16;	


			int eventType, pause, instrument, panDir;
			int pan;
			int noteLen;
			int maxChordNotes = upper - lower + 1;
			int chordNotes[] = new int[maxChordNotes];
			int numOfChordNotes; //num of notes in a particular chord
			int nextNote ;


			while(runFlag){

				//get kind of event
				eventType = (int)(Math.random() * 1); //chord, build chord, chord progression
				pause = (int)(Math.random() * 5000);							//pause between events			
				instrument = (int)(Math.random() * numOfInstrs);
				//pan = (int)(Math.random() *128);
				pan = (Math.random()<0.5d)?127:0;
				
				//get the notes for this chord
				numOfChordNotes = (int)(Math.random() * maxChordNotes) + 1; //num of notes to try to get for this chord
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


				//set instr and pan for req number of channels
				for(int i = 0;i<numOfChordNotes; i++){
					midiChannels[lower+i].programChange(instrument);
					midiChannels[lower+i].controlChange(10,pan);
				}



				switch (eventType) {
					case 0:
						//chord

						noteLen = (int)(Math.random() * 4500) + 500;

						System.out.println("chord " +
															" notes " + numOfChordNotes + 
															" len " + noteLen + 
															" instrument " + instrument + " " + instruments[instrument].getName()
															);
						for(int i = 0;i<numOfChordNotes; i++){
							System.out.print(chordNotes[i] + " ");
						}
						System.out.println("");		

						//play chord
						for(int i = 0;i<numOfChordNotes; i++){
							midiChannels[lower+i].noteOn(chordNotes[i],vol);
						}

						try {
							sleep(noteLen);
						} catch(InterruptedException e) {
							System.err.println("Interrupted");
						}
						for(int i = 0;i<numOfChordNotes; i++){
							midiChannels[lower+i].noteOff(chordNotes[i],vol);
						}
						break;



					case 1:
						//build chord
						break;				
		
					case 2:
						//chord progression
						break;				


				} //switch


				//pause between events
				try {
					sleep(pause);
				} catch(InterruptedException e) {
					System.err.println("Interrupted");
				}
	


			} //while runflag
		} //run
	} //class Choir




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
		players = null;
	}
	
	
	
	public static void main(String args[]) {

    final Synthesizer3 s = new Synthesizer3();

		JFrame f = new JFrame("Synthesizer3");
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

	}


} 

////////////////////////////////////////////////////////////////////////////////
	/*//this task which runs in its own thread
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

	}*/
////////////////////////////////////////////////////////////////////////////////
					/*case 2:
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

						break;*/
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
