package audio.synth;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.sound.midi.Instrument;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.swing.JFrame;

/*
generates a midi sequence from data in a txt file

the txt file was generated using a vba script in an excel file containing the score

cf Synth.java - removed extraneous graphic elements
- still problen w/ channel -> instrument allocation
*/

public class Synth1 extends JFrame implements MetaEventListener { 
	private static final long serialVersionUID = 1L;
	Synthesizer synthesizer;
	MidiChannel midiChannels[] = null;
	int tempo;
	Instrument[] instruments;
	List<int[]> intArrs = new ArrayList<int[]>(); //list of arrays
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	File file;
	String[] channels = new String[16];
	
	//constructor
	public Synth1() {
		super("My Synthesizer");
		System.out.println("Synth1()");

		addWindowListener(new WindowAdapter() {
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {
				closeSynth();
				System.exit(0);
			}  
		});		

		openSynth();
		selectFile();
		try{
			getData();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		createSequence();
		saveSequence();
		
		startSequence();
	}


	//initialize synthesizer
	public void openSynth() {
		System.out.println("openSynth()");
	    try {
	    	if (synthesizer == null) {
	    		if ((synthesizer = MidiSystem.getSynthesizer()) == null) {
	    			System.out.println("getSynthesizer() failed!");
	    			return;
	    		}
	    	} 
	    	synthesizer.open();
	    	sequencer = MidiSystem.getSequencer();
			sequencer.addMetaEventListener(this);
	    	sequencer.open();
	    } catch(Exception ex) {
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

		midiChannels = synthesizer.getChannels();
	}


	public void selectFile() {
		System.out.println("selectFile()");
		file = new File("c:/rob/apps/audio/media/2partinvention1.txt");
	}
	
	//get data
	public void getData() throws IOException, FileNotFoundException {
		System.out.println("getData()");

		String s;
		int i;
		int j;
		
		StringTokenizer st;

		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		//int pulseNum, note, len, channel;

		//first line is tempo
		s = br.readLine();
		tempo = Integer.parseInt(s);
		//second line is instruments
		//s = br.readLine();
		//st = new StringTokenizer(s,"|");
		//for(i=0;i<16;i++){
		//	instruments[i] = Integer.parseInt(st.nextToken());
		//}

		//use stringTokenizer to get data
		while((s = br.readLine()) != null){
			st = new StringTokenizer(s,"|");
			int[] a = new int[5];
			for (i = 0; i < 5; i++) {
				a[i] = Integer.parseInt(st.nextToken());
			}
			intArrs.add(a);
		}

		if (!(br == null)) { br.close(); }
		if (!(fr == null)) { fr.close(); }
	}

	public void createSequence() {
		System.out.println("createSequence()");
		int pulsenum	= 0;
		int note 		= 0;
		int lngth 		= 0;
		int state 		= 0;
		int channel 	= 0;
		int vol 		= 0;
		int i 			= 0;
		int j 			= 0;

		try {
			sequence = new Sequence(Sequence.PPQ, tempo);
			track = sequence.createTrack();
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}

		// assign default instruments to channels - this doesn't work because
		// the values have to be set to the track
		/*
		for(i = 0; i < 16; i++){
			midiChannels[i].programChange((int)(Math.random()*128));
		}
		*/

		long tick = 0;
		ShortMessage message;
		MidiEvent event;
		int instrNum;
		for(i = 0; i < 16; i++){
			instrNum = (int) (Math.random()*128);
			System.out.println(i + " " + instruments[instrNum].getName());
			try {
				message = new ShortMessage();
				message.setMessage(192 + i, instrNum, 127); //set instr
				event = new MidiEvent(message, tick);
				track.add(event);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		for(int[] intArr: intArrs) {
			pulsenum	= intArr[0];
			note 		= intArr[1];
			lngth		= intArr[2];
			channel		= intArr[3];
			vol			= 127;
			createShortEvent(144, note, pulsenum, channel, vol);
			createShortEvent(128, note, pulsenum + lngth, channel, vol);
		}

		try {
			sequencer.setSequence(sequence);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void startSequence() {
		System.out.println("startSequence()");
		sequencer.start();
	}

	public void stopSequence() {
		System.out.println("stopSequence()");
		sequencer.stop();
	}

	public void saveSequence() {
		System.out.println("saveSequence()");

		file = new File("test.mid");
		try {
			int[] fileTypes = MidiSystem.getMidiFileTypes(sequence);
			if (fileTypes.length == 0) {
				System.out.println("Can't save sequence");
			} else {
				if (MidiSystem.write(sequence, fileTypes[0], file) == -1) {
					throw new IOException("Problems writing to file");
				} 
			}
		} catch (Exception ex) { 
			ex.printStackTrace(); 
		}
	}

	public void createShortEvent(int type, int num, int tick, int channel, int vol) {
		//used for recording - type designates noteOn/noteOff (144/128)
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(type+channel, num, vol); 
			MidiEvent event = new MidiEvent(message, (long)tick);
			track.add(event);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void closeSynth() {
		System.out.println("closeSynth()");
		if (synthesizer != null) {
			synthesizer.close();
		}
		synthesizer = null;
		if (sequencer != null) {
			sequencer.close();
		}
		sequencer = null;
	}

	public void meta(MetaMessage message) {
		System.out.println("meta(MetaMessage message)");
		if (message.getType() == 47) {  // 47 is end of track
			//startB.setText("Start");
			//status.setText("meta(MetaMessage message)");
		}
	}

	public static void main(String args[]) {
		System.out.println("main()");

		final Synth1 synth = new Synth1();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		synth.setLocation(0, 0);
		//synth.setSize(screenSize.width, screenSize.height);
		synth.setSize(800, 600);
		//synth.pack();
		synth.setVisible(true);
	}
} 







