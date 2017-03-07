package audio.synth;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

/* play all notes/instruments in real time */
public class Synthesizer5 extends JPanel {
	private static final long serialVersionUID = 1L;
	/** The log. */
	private Logger log 							= Logger.getLogger(this.getClass());
	private Synthesizer synthesizer;
	private Instrument instruments[];
	
	public Synthesizer5() {
		setLayout(new FlowLayout());
	}
	
	public void open() {
		//get and open synthesizer and sequencer
		try {
			if (synthesizer == null) {
				if ((synthesizer = MidiSystem.getSynthesizer()) == null) {
					log.error("getSynthesizer() failed!");
					return;
				}
			} 
			synthesizer.open();
		} catch (Exception e) {
			log.error(e);
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

		//play all notes
		
		int instrument = 0;
		midiChannels[0].programChange(instrument);
		midiChannels[0].controlChange(10, 127);
		for(int i = 0; i < 128; i++){
			log.debug("note " + i);
			midiChannels[0].controlChange(10, 127 - i);
			midiChannels[0].noteOn(i, 127);
			
			try {
				Thread.sleep(20);
			} catch(InterruptedException e) {
				log.error(e);
			}
			midiChannels[0].noteOff(i, 127);
		}
		/**/
		

		//play all instruments in the sound bank
		/*
		int instrument = 0;
		for(int i = 0; i < instruments.length; i++) {
			instrument = i;
			log.debug("instrument " + instrument + " " + instruments[instrument].getName());
			int pan = (i % 2 == 0) ? 0 : 127; 
			midiChannels[0].controlChange(10, pan);
			midiChannels[0].programChange(instrument);
			midiChannels[0].noteOn(60,127);
			try {
				Thread.sleep(500);
			} catch(InterruptedException e) {
				log.error(e);
			}
			midiChannels[0].noteOff(60,127);
			
		}
		*/

		//play all notes on channel 10 (midiChannels[9])
		/*
		int instrument = 0;
		midiChannels[9].programChange(instrument);
		for(int i = 0; i < 128; i++){
			log.debug("note " + i);
			//play sound on ch9
			int pan = (i % 2 == 0) ? 0 : 127; 
			midiChannels[9].controlChange(10, pan);
			midiChannels[9].noteOn(i, 127);
			try {
				Thread.sleep(500);
			} catch(InterruptedException e) {
				log.error(e);
			}
			midiChannels[9].noteOff(i, 127);
		}
				*/
	}
	
	public void close() {
		synthesizer = null;
	}
	
	public static void main(String args[]) {
		final Synthesizer5 s = new Synthesizer5();
		JFrame f = new JFrame("Synthesizer5");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				s.close();
				System.exit(0);
			}
		});

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		f.setLocation(screenSize.width/4, screenSize.height/4);
		f.setSize(screenSize.width/2, screenSize.height/2);
		f.getContentPane().add("Center", s);
		f.setVisible(true);

		s.open();
	}
} 
