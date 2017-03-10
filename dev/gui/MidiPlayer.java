package audio.chords.gui;

import static audio.Constants.ROW_HEIGHT;
import static audio.Constants.SP;
import static audio.Constants.US;
import static audio.Constants.V;
import static audio.Constants.WIDTH_4;
import static audio.Constants.WIDTH_5;
import static audio.Constants.WIDTH_8;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import audio.MidiNote;
import audio.Util;

/**
 * This class plays midi in real time.
 */

public class MidiPlayer extends JPanel {
	/** The log. */
	protected Logger log 			= Logger.getLogger(this.getClass());
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Synthesizer synthesizer;
	Sequencer sequencer;
	Instrument instruments[];
	MidiChannel midiChannels[];
	
	final JButton playButton = new JButton("Play");
	final JButton stopButton = new JButton("Stop");
	
	protected List<MidiNote> midiNotes		= new ArrayList<MidiNote>();
	protected List<MidiNote> endMidiNotes	= new ArrayList<MidiNote>();
	
	public MidiPlayer() {
		setLayout(null);
		
		//get and open synthesizer and channels
		try {
			if ((synthesizer = MidiSystem.getSynthesizer()) == null) {
				log.debug("getSynthesizer() failed!");
				return;
			}
			synthesizer.open();
		} catch (Exception e) {
			log.error(e);
		}
		
		//get channels
		midiChannels = synthesizer.getChannels();
		
		Soundbank soundbank = null;
		
		// get the default soundbank
		soundbank = synthesizer.getDefaultSoundbank();
		
		// or
		
		// get the soundbank from a specific file
		/*
		File file = new File(SOUNDBANK);
	    try {
	    	soundbank = MidiSystem.getSoundbank(file);
			
			if (soundbank.getName().equals("Patches.hsb"))	{
				log.debug("soundbank - original");
			} else if (soundbank.getName().equals("Untitled Headspace Soundbank")) {
				log.debug("soundbank - med");
			} else{
				log.debug("soundbank - unrecognized soundbank");
			}
	    } catch(Exception e) {
	    	log.error(e);
			return;
		}
		*/
		
		log.debug("soundbank.description = " + soundbank.getDescription());
		log.debug("soundbank.name        = " + soundbank.getName());			
		log.debug("soundbank.vendor      = " + soundbank.getVendor());
		log.debug("soundbank.version     = " + soundbank.getVersion());
		
		int instrumentsLen = 0;
		
		instruments = soundbank.getInstruments();
		instrumentsLen = instruments.length;
		log.debug("instrumentsLen=" + instrumentsLen);
			
		// set channels to l or r
		midiChannels[0].controlChange(10, 127);

		int x = 0;
		int y = 0;
		
		playButton.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    add(playButton);
		playButton.setEnabled(true);
	    x += playButton.getWidth() + SP;
	    
	    // stop button, in class declaration
	    stopButton.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    add(stopButton);
	    stopButton.setEnabled(false);
	    x += stopButton.getWidth() + SP;
		
		String[] instrumentNames = new String[instrumentsLen];;
		int i = 0;
		for (Instrument instrument: instruments) {
			String instrumentName = instrument.getName().trim();

			//public static int ACOUSTIC_BASS 			= 32;
			//log.debug(instrumentName + " = " + i);
			log.debug("public static int " + instrumentName.toUpperCase().replace(" ", US) + " = " + i + ";");		
			
			instrumentNames[i] = Util.padInt(i, 3) + " - " + instrumentName;
			i++;
		}

	    // instrumentsBox
	    final JComboBox<String> instrumentsBox = new JComboBox<String>(instrumentNames);
	    instrumentsBox.setBounds(x, y, WIDTH_8, ROW_HEIGHT);
	    add(instrumentsBox);
	    x += instrumentsBox.getWidth() + SP;


	    String[] alphaNotes = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
		String[] notes = new String[128];;
		i = 0;
		for (int j = 0; j < 11; j++) {
			for (int k = 0; k < 12; k++) {
				notes[i] = Util.padInt(j, 2) + " - " + Util.padInt(i, 3) + " - " + alphaNotes[k];
				i++;
				if (i > 127) {
					break;
				}
			}
		}

	    // notesBox
	    final JComboBox<String> notesBox = new JComboBox<String>(notes);
	    notesBox.setBounds(x, y, WIDTH_5, ROW_HEIGHT);
	    add(notesBox);
	    x += notesBox.getWidth() + SP;
	    
		// buttons				
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String instrumentItem	= (String) instrumentsBox.getSelectedItem();
				String noteItem			= (String) notesBox.getSelectedItem();

				int program = getProgram(instrumentItem);
				int note = getNote(noteItem);
						
				midiChannels[0].programChange(program);
				
				endMidiNotes();
				beginMidiNote(new MidiNote(0, note, 1, V[8]));

				playButton.setEnabled(false);
				stopButton.setEnabled(true);
			}
		});
		add(playButton);

		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playButton.setEnabled(true);
				stopButton.setEnabled(false);

				endMidiNotes();
			}
		});
		add(stopButton);
	}
	
	public int getProgram(String instrumentItem) {
		// 000 - Piano
		String[] arr = instrumentItem.split(" - ");
		return Integer.parseInt(stripPadding(arr[0], "0", "start"));
	}
	
	public int getNote(String noteItem) {
		// 00 - 000 - C
		String[] arr = noteItem.split(" - ");

		return Integer.parseInt(stripPadding(arr[1], "0", "start"));
	}

	
	/**
	 * @param s
	 * @param chr
	 * @param pos
	 * @return
	 */
	public String stripPadding(String s, String chr, String pos) {
		// 000, "0", "start"
		
		if (pos.equals("start")) {
			while (s.startsWith(chr) && s.length() > 1) {
				s = s.substring(1);
			}
		} else {
			while (s.endsWith(chr) && s.length() > 1) {
				s = s.substring(0, s.length() - 1);
			}
		}
		
		return s;
	}
	
	/**
	 * @param midiNote
	 */
	private void beginMidiNote(MidiNote midiNote) {
		midiNotes.add(midiNote);
		midiChannels[midiNote.channel].noteOn(midiNote.pitch, midiNote.vol);
	}
	
	/**
	 * @param midiNote
	 */
	private void endMidiNote(MidiNote midiNote) {
		midiChannels[midiNote.channel].noteOff(midiNote.pitch);
	}
	
	/**
	 * 
	 */
	public void endMidiNotes() {
		for (MidiNote midiNote: midiNotes) {
			endMidiNote(midiNote);
		}
		midiNotes.clear();
	}
	
	/**
	 * 
	 */
	public void close() {
		if (synthesizer != null) {
			synthesizer.close();
		}
		synthesizer = null;
		instruments = null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String args[]) {

		final MidiPlayer mp = new MidiPlayer();

		JFrame f = new JFrame("Synthesizer3");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				mp.close();
				System.exit(0);
			}
		});

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		f.setLocation(screenSize.width/4, screenSize.height/4);
		f.setSize(screenSize.width/2, screenSize.height/2);

		f.getContentPane().add("Center", mp);
		f.setVisible(true);
	}
} 

