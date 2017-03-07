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

import org.apache.log4j.Logger;

/**
this class plays midi in real time using threading
- choir version
*/
public class Synthesizer4 extends JPanel {
	private static final long serialVersionUID = 1L;
	/** The log. */
	private Logger log = Logger.getLogger(this.getClass());
	private Synthesizer synthesizer;
	private Sequencer sequencer;
	private Instrument instruments[];
	private MidiChannel midiChannels[];
	private Player players[];
	private Conductor conductor;
	
	final JButton playButton;
	final JButton stopButton;
	
	public Synthesizer4() {
		setLayout(new FlowLayout());

		final int numOfPlayers = 8;	
		players = new Player[numOfPlayers];

		// buttons				
		playButton = new JButton("Play");
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				conductor = new Conductor(numOfPlayers);
				playButton.setEnabled(false);
				stopButton.setEnabled(true);
			}
		});
		add(playButton);

		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int i = 0;i<numOfPlayers; i++){
					if(players[i]!=null) {
						players[i].runFlag = false;
						players[i]=null;
					}
				}
				if(conductor!=null){
					conductor.runFlag = false;
					conductor = null;
				}
				playButton.setEnabled(true);
				stopButton.setEnabled(false);
			}
		});
		add(stopButton);


		//init objects

		//get and open synthesizer and sequencer
		try {
			if (synthesizer == null) {
				if ((synthesizer = MidiSystem.getSynthesizer()) == null) {
					log.warn("getSynthesizer() failed!");
					return;
				}
			} 
			synthesizer.open();
			sequencer = MidiSystem.getSequencer();
    	sequencer.open();			
		} catch (Exception e) {
			log.error(e);
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

		//set channels to l or r
		int mod;
		for(int i = 0; i < numOfPlayers; i++) {
			mod = i % 2;
			midiChannels[i].controlChange(10, mod * 127);
		}

	}
	
	/**
	 * This object runs in its own thread.
	 */
	private class Player extends Thread {
		int channel;
		boolean runFlag = true;
		int group = 0;

		Player(int ch) {
			channel = ch;
			log.debug("Player(" + channel + ")");
		}

		public void setInstruments(int g){
			log.debug("player " + channel + " setInstruments() group " + group);
			group = g;
		}

		public void run() {
			log.debug("player " + channel + " run()");

			int numOfNotes 	= 128; // num of notes on keyboard that will sound
			int	vol 		= 127;
			int numOfInstrs = 8;

			int instrument;
			int pan;
			int note;
			int noteLen;
			
			while(runFlag){

				instrument = (int)(Math.random() * numOfInstrs) + group * 8;
				midiChannels[channel].programChange(instrument);
				pan = (int)(Math.random() * 128);
				note = (int)(Math.random() * numOfNotes); // + 36;
				noteLen = (int)(Math.random() * 10000) + 1000;

				log.debug(
						"player " + channel +
						" note  " + note + 
						" length " + noteLen +
						" pan " + pan + 
						" instrument " + instrument + " " + instruments[instrument].getName()
				);

				midiChannels[channel].controlChange(10,pan);
				midiChannels[channel].noteOn(note,vol);
				try {
					sleep(noteLen);
				} catch(InterruptedException e) {
					log.error(e);
				}
				midiChannels[channel].noteOff(note,vol);

				//pause between events
				/*
				try {
					sleep(pause);
				} catch(InterruptedException e) {
					System.err.println("Interrupted");
				}
				*/
	


			} //while runflag
		} //run
	} //class Player


	//this task which runs in its own thread
	private class Conductor extends Thread {

		public boolean runFlag = true;
		int numOfPlayers = 0;
		int group = 0;

		Conductor(int n) {
			numOfPlayers = n;
			start();
		}

		public void run() {
			group = getGroup();
			for(int i = 0;i<numOfPlayers; i++){
				if(players[i]==null){
					players[i]=new Player(i);
					players[i].setInstruments(group);
					players[i].start();
				}
			}

			while(runFlag){

				try {
					sleep(20000);
				} catch(InterruptedException ex) {
					System.err.println("Interrupted");
				}

				group = getGroup();
				for(int i = 0;i<numOfPlayers; i++){
					if(players[i]!=null)players[i].setInstruments(group);
				}
	
	
			} //while runflag
		} //run
	} //class Conductor


	public int getGroup(){
		return (int)(Math.random() * 16); 
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
		players = null;
	}
	
	
	
	public static void main(String args[]) {

    final Synthesizer4 s = new Synthesizer4();

		JFrame f = new JFrame("Synthesizer4");
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
