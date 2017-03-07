package audio.synth;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/*
this class generates a midi sequence and then plays it
*/
public class Synthesizer1 extends JPanel {
	final int PROGRAM_CHANGE = 192;
	final int NOTE_ON = 144;
	final int NOTE_OFF = 128;

	Synthesizer synthesizer;
	Sequencer sequencer;
	Sequence sequence;
	Instrument instruments[];
	Track track;
	File file;
	ShortMessage message;
	MidiEvent event;
	MidiChannel midiChannels[];

	public Synthesizer1() {
		System.out.println("Synthesizer1()");

		setLayout(new FlowLayout());

		JButton composeB = new JButton("Compose");
		composeB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { compose(); }
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
		MidiChannel midiChannels[] = synthesizer.getChannels();
	}
	
	public void compose() {
		System.out.println("compose()");


		int instrument, channel, note, vol, noteInc;
		long noteLen, tick;


		int tempo = 12; //1=500ms
		int pieceLen = 60; //length of piece in seconds
		int numTicks = pieceLen * 2 * tempo; //num of ticks in piece
		int numOfNotes = 128; // num of notes that will sound

		//get sequence and track
		try{
	    sequence = new Sequence(Sequence.PPQ, tempo);
			track = sequence.createTrack();
    } catch (Exception ex) { ex.printStackTrace(); return; }


		//assign random instruments to the 16 channels
		tick=0; //set at beginning of track
		for(int i=0;i<16;i++){
			instrument = (int)(Math.random()*128);
			System.out.println("instrument " + instrument + " " + instruments[instrument].getName());
			//set instrument for ch i
			try {
				message = new ShortMessage();
				message.setMessage(PROGRAM_CHANGE + i, instrument, 127); //set instr
    	  event = new MidiEvent(message, tick);
      	track.add(event);
	    } catch (Exception ex) { ex.printStackTrace(); }
		}		


		//compose sequence
		for(int i=0;i<60;i++){ // i is number of phrases

			channel = (int)(Math.random()*16);
			note = (int)(Math.random()*numOfNotes);
			vol = 127;
			tick = (long)(Math.random()*numTicks); //place on track where this phrase will start
			noteInc = (int)(Math.random()*5)+1; //interval to increment each note in phrase by
			if(Math.random()>0.5d)noteInc *= -1; //up or down
			noteLen = (int)(Math.random()*7)+1; //length of each note in phrase, in ticks

			
			for(int j = 0;j<40; j++){
				try {
					message = new ShortMessage();
					message.setMessage(NOTE_ON+channel, note, vol); //on
	    	  event = new MidiEvent(message, tick);
	      	track.add(event);
					message = new ShortMessage();
					message.setMessage(NOTE_OFF+channel, note, vol); //off
	    	  event = new MidiEvent(message, tick + noteLen);
	      	track.add(event);
		    } catch (Exception ex) { ex.printStackTrace(); }
				tick+=noteLen;
				note+=noteInc;
				if((note>numOfNotes-1)||(note<0))note = (int)(Math.random()*128);
			}

		}
		System.out.println("");
		
		

		//System.out.println("microsecondLength " + sequence.getMicrosecondLength()); 
		System.out.println("millisecondLength " + sequence.getMicrosecondLength()/1000); 
		System.out.println("tickLength " + sequence.getTickLength()); 
           

		
		//pass sequence to sequencer
		try {sequencer.setSequence(sequence);}
		catch (Exception ex) { ex.printStackTrace(); }


		/*//save sequence to file
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
    }*/


		//play sequence
    sequencer.start();

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

    final Synthesizer1 s = new Synthesizer1();

		JFrame f = new JFrame("Synthesizer1");
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
/*//play rnd notes in rnd channels (w/ rnd instr)
		for(int i=0;i<40;i++){ // i is number of notes played
			//set instr and add on/off to track
			channel = (int)(Math.random()*16);
			note = (int)(Math.random()*81);
			noteLen = 1;
			vol = 127;
			tick = (long)(Math.random()*numTicks);
			try {
				message = new ShortMessage();
				message.setMessage(144+channel, note, vol); //on
    	  event = new MidiEvent(message, tick);
      	track.add(event);
				message = new ShortMessage();
				message.setMessage(128+channel, note, vol); //off
    	  event = new MidiEvent(message, tick + noteLen);
      	track.add(event);
	    } catch (Exception ex) { ex.printStackTrace(); }
		}*/

////////////////////////////////////////////////////////////////////////////////
		/*for(int i=0;i<200;i++){
			//set instr and add on/off to track
			channel = (int)(Math.random()*16);
			note = (int)(Math.random()*81);
			noteLen = (long)(Math.random()*40);
			vol = 127;
			tick = (long)(Math.random()*1000);
			try {
				message = new ShortMessage();
				message.setMessage(144+channel, note, vol); //on
    	  event = new MidiEvent(message, tick);
      	track.add(event);
				message = new ShortMessage();
				message.setMessage(128+channel, note, vol); //off
    	  event = new MidiEvent(message, tick + noteLen);
      	track.add(event);
	    } catch (Exception ex) { ex.printStackTrace(); }
		}*/
