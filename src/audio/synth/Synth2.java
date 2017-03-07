package audio.synth;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

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
generates a midi sequence

there was a problem with this script not writing instrument settings to the midi file - 
see Synthesizer0 -> for solution

problem now fixed
*/

public class Synth2 extends JFrame implements MetaEventListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Synthesizer synthesizer;
	MidiChannel midiChannels[] = null; //midiChannels[9] is percussion
	int tempo;
	Instrument[] instruments;
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	File file;

	//constructor
	public Synth2() {
		super("Synthesizer");
		System.out.println("Synth2()");

		addWindowListener(new WindowAdapter() {
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {
				closeSynth();
				System.exit(0);
			}  
		});		

		/*//assign default instruments
		for(int i = 0; i < 16; i++){
			//instruments[i] = i * 8;
			instruments[i] = (int)(Math.random()*128);
			System.out.print(instruments[i] + " ");
		}
		System.out.println("");*/
		
		openSynth();
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


		Soundbank sb = synthesizer.getDefaultSoundbank();
		if (sb != null) {
			System.out.println("got Soundbank");
			instruments = synthesizer.getDefaultSoundbank().getInstruments();
			System.out.println("instruments.length " + instruments.length); //RL
			System.out.println("");
			//synthesizer.loadInstrument(instruments[0]);
			synthesizer.loadAllInstruments(sb);
		}
		midiChannels = synthesizer.getChannels();
	}

	public void createSequence() {
		System.out.println("createSequence()");

		int pulsenum, note, lngth, state, channel, vol, i, j;

		tempo = 2;

		try{
			sequence = new Sequence(Sequence.PPQ, tempo);
			track = sequence.createTrack();
		} catch (Exception ex) { 
			ex.printStackTrace(); 
			return;
		}

		//assign default instruments to channels
		// - this doesn't work
		/*for(i=0;i<16;i++){
			midiChannels[i].programChange((int)(Math.random()*128));
		}*/

		long tick = 0;
		ShortMessage message;
		MidiEvent event;
		int instrNum[] = new int[16];
		for(i = 0; i < 16; i++){
			instrNum[i] = (int)(Math.random()*128);
			try {
				message = new ShortMessage();
				message.setMessage(192+i, instrNum[i], 127); //set instr
				event = new MidiEvent(message, tick);
				track.add(event);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		/*//test
		for(i=0;i<16;i++){
			pulsenum = i;
			note = 60;
			lngth = 1;
			channel = (int)(Math.random()*16);
			vol = 127;
			createShortEvent(144,note,pulsenum,channel,vol);
			createShortEvent(128,note,pulsenum + lngth,channel,vol);
		}*/



		for(i = 0; i < 16; i++){
			//channel = (int)(Math.random()*16);
			channel = i;
			System.out.println("" + i + " " + instrNum[i] + " " + instruments[instrNum[i]].getName() + " ");
			for(j = 0; j < 4; j++){
				pulsenum = i * 5 + j;
				note = 60 + j;
				lngth = 1;
				vol = 127;
				createShortEvent(144, note, pulsenum, channel, vol);
				createShortEvent(128, note, pulsenum + lngth, channel, vol);
			}
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
      }else{
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
    } catch (Exception ex) { ex.printStackTrace(); }
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

    final Synth2 synth = new Synth2();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    synth.setLocation(0, 0);
    //synth.setSize(screenSize.width, screenSize.height);
		synth.setSize(800, 600);
		//synth.pack();
    synth.setVisible(true);

	}
} 







