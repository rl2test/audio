package audio.metronome;

import static audio.Constants.BG_COLOR;
import static audio.Constants.SINGLE_ROW_PANEL_HEIGHT;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;


/**
 * Play '.chords' files or '.chords' text in real time in addition to providing 
 * tuner and metronome functions.
 */
public class Metronome extends JPanel {
	/** The generated serialVersionUID. */
	private static final long serialVersionUID	= 1L;
	/** The singleton instance of this class. */    
	private static Metronome metronome 			= null;
	/** The log. */
	private Logger log 							= Logger.getLogger(this.getClass());
	/** The synthesizer. */
	private Synthesizer synthesizer 			= null;
	/** The midiChannels. */
	public static MidiChannel[] midiChannels 	= null;
	/** The width of the gui, not including the frame objects. */
	public static int w							= 0;
	/** The height of the gui, not including the frame objects. */
	public static int h							= 0;
	
    /**
     * @return singleton instance of this class
     */
    public static Metronome getInstance() {
        if (metronome == null) {
        	metronome = new Metronome();
    	}
    	return metronome;
    }
	
    /** Private constructor */
    private Metronome() {
    	this.setBackground(BG_COLOR);
		this.setSize(w, h);
		
		log.debug("gui w x h = " + w + " x " + h);

		// get and open the synthesizer
		try {
			if ((synthesizer = MidiSystem.getSynthesizer()) == null) {
				log.debug("getSynthesizer() failed!");
				return;
			}
			synthesizer.open();
		} catch (Exception e) {
			log.error(e);
		}
		
		// get the synthesizer channels
		midiChannels = synthesizer.getChannels();
		
		setLayout(null);
	    int y = 0;

	    // metronome panel
	    MetronomePanel metronomePanel = MetronomePanel.getInstance();
	    metronomePanel.setBounds(0, y, w, SINGLE_ROW_PANEL_HEIGHT * 3);
	    add(metronomePanel);
    }

	public void close() {
		synthesizer = null;
	}
	
	public static void main(String args[]) {
		JFrame frame = new JFrame("Metronome");

//		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		Dimension frameDimension = new Dimension(
//				screenSize.width, 
//				screenSize.height - TASKBAR_HEIGHT);

		Dimension frameDimension = new Dimension(
				675, 
				200);		
		
		frame.setSize(frameDimension.width, frameDimension.height);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				metronome.close();
				System.exit(0);
			}
		});

		// call setVisible to get insets
		frame.setVisible(true);

		Insets insets = frame.getInsets();
		
		w = frameDimension.width - insets.left - insets.right; 
		h = frameDimension.height - insets.top - insets.bottom;
		
		final Metronome metronome = Metronome.getInstance();

		frame.getContentPane().add("Center", metronome);

		// call setVisible to display gui
		frame.validate();
		frame.repaint();
	}
} 
