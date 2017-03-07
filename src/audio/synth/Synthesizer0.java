package audio.synth;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
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
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Synthesizer0 extends JPanel {

	Synthesizer synthesizer;
	Sequencer sequencer;
	Sequence sequence;
	Instrument instruments[];
	Track track;
	File file;
	ShortMessage message;
	MidiEvent event;
	

	public Synthesizer0() {
		setLayout(new FlowLayout());
	}
	
	public void open() {

		System.out.println("open()");


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
			sequence = new Sequence(Sequence.PPQ, 10);
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
		

		//get soundbank
		Soundbank sb = synthesizer.getDefaultSoundbank();
		if (sb != null) {
			instruments = sb.getInstruments();
			System.out.println("instruments length " + instruments.length);
			//orig - 411; med - 366; delux - 189			
		} else {System.out.println("couldn't get soundbank");}

		//get channels
		MidiChannel midiChannels[] = synthesizer.getChannels();


		//get sequence and track
		int tempo = 10;
		try{
	    sequence = new Sequence(Sequence.PPQ, tempo);
			track = sequence.createTrack();
    } catch (Exception ex) { ex.printStackTrace(); return; }
		

		//play all notes
		int instrument = 0;
		midiChannels[0].programChange(instrument);

		try {
			message = new ShortMessage();
			message.setMessage(192, 0, 127); //set instr
    	event = new MidiEvent(message, 0);
      track.add(event);
    } catch (Exception ex) { ex.printStackTrace(); }			


		for(int i=0;i<127;i++){
			System.out.println("note " + i);
			midiChannels[0].noteOn(i,127);
			
			//set pan
			//midiChannels[i].controlChange(10, (int)(((double)value + 100.0) / 200.0 *  127.0));
			//above line from Juke.java pan control - i is between 0 and 127
			midiChannels[0].controlChange(10, i);

			try {
				Thread.sleep(50);
			} catch(InterruptedException e) {
				System.err.println("Interrupted");
			}
			midiChannels[0].noteOff(i,127);
			
			//set instr and add on/off to track
			try {
				//set pan for sequence - this doesn't quite work (it does something!)
				message = new ShortMessage();
				message.setMessage(176, 0, 10, i); //set instr
	   	  event = new MidiEvent(message, i);
  	   	track.add(event);

				message = new ShortMessage();
				message.setMessage(144 + 0, i, 127); //on
    	  event = new MidiEvent(message, (long)i);
      	track.add(event);
				message = new ShortMessage();
				message.setMessage(128 + 0, i, 127); //off
    	  event = new MidiEvent(message, (long)i + 1);
      	track.add(event);

	    } catch (Exception ex) { ex.printStackTrace(); }			
			
			
		}
		
		

		/*//play all notes on channel 10 (midiChannels[9])
		int instrument = 0;
		midiChannels[9].programChange(instrument);
		for(int i=0;i<128;i++){
			System.out.println("note " + i);
			//play sound on ch9
			midiChannels[9].noteOn(i,127);
			try {
				Thread.sleep(500);
			} catch(InterruptedException e) {
				System.err.println("Interrupted");
			}
			midiChannels[9].noteOff(i,127);
		}*/


		/*//play all instruments in the sound bank
		int instrument = 0;
		for(int i=0;i<instruments.length;i++){
			//instrument = (int)(Math.random()*instruments.length);
			//instrument = (int)(Math.random()*128);
			instrument = i;
			System.out.println("instrument " + instrument + " " + instruments[instrument].getName());
			//set instrument for ch0
			midiChannels[0].programChange(instrument); //this method works in real time but not with track object
			//play sound on ch0
			midiChannels[0].noteOn(60,127);
			try {
				Thread.sleep(500);
			} catch(InterruptedException e) {
				System.err.println("Interrupted");
			}
			midiChannels[0].noteOff(60,127);
			
			//set instr and add on/off to track
			try {
				message = new ShortMessage();
				message.setMessage(192, instrument, 127); //set instr
    	  event = new MidiEvent(message, (long)i);
      	track.add(event);
				message = new ShortMessage();
				message.setMessage(144+0, 60, 127); //on
    	  event = new MidiEvent(message, (long)i);
      	track.add(event);
				message = new ShortMessage();
				message.setMessage(128+0, 60, 127); //off
    	  event = new MidiEvent(message, (long)i + 1);
      	track.add(event);
	    } catch (Exception ex) { ex.printStackTrace(); }

		}*/
		
		
		//pass sequence to sequencer
		try {sequencer.setSequence(sequence);}
		catch (Exception ex) { ex.printStackTrace(); }
	

		//save sequence to file
		/*file = new File("test.mid");
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

    final Synthesizer0 s = new Synthesizer0();

		JFrame f = new JFrame("Synthesizer0");
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

    s.open();
	}


} 
