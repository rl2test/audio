package audio.chords.gui;

import static audio.Constants.BG_COLOR;
import static audio.Constants.DOUBLE_ROW_PANEL_HEIGHT;
import static audio.Constants.SINGLE_ROW_PANEL_HEIGHT;
import static audio.Constants.TOP_BAR_HEIGHT;
import static audio.Constants.DOCK_WIDTH;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
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
 * 
 * Note on java7 sound implementation:
 * 
 * @see http://www.oracle.com/technetwork/java/javase/compatibility-417013.html#incompatibilities
 * Area: Sound
 * Synopsis: Java Sound Synthesizer Implementation is Updated to use Open source Implementation
 * Description: The software synthesizer implementation in the Java Sound package has been replaced with an open sourced version. Due to the replacement, the following features were dropped:
 * GM soundbank support
 * RMF file playback support
 * OSS (Open Sound System) support on the Linux platform
 * The new synthesizer implementation supports soundbanks in DLS and SoundFont (SF2) formats.
 * Nature of Incompatibility: behavioral
 * RFE: 6702956
 * 
 */
public class GuiController extends JPanel {
	/** The generated serialVersionUID. */
	private static final long serialVersionUID	= 1L;
	/** The singleton instance of this class. */    
	private static GuiController guiController 	= null;
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
    public static GuiController getInstance() {
        if (guiController == null) {
        	guiController = new GuiController();
    	}
    	return guiController;
    }
	
    /** Private constructor */
    private GuiController() {
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

	    // tuner panel
	    int tunerPanelWidth = 290;
	    TunerPanel tunerPanel = TunerPanel.getInstance();
	    tunerPanel.setBounds(0, y, tunerPanelWidth, SINGLE_ROW_PANEL_HEIGHT);
	    add(tunerPanel);

	    DronePanel dronePanel = DronePanel.getInstance();
	    dronePanel.setBounds(tunerPanelWidth, y, w - tunerPanelWidth, SINGLE_ROW_PANEL_HEIGHT);
	    add(dronePanel);
	    
	    y += tunerPanel.getHeight();
	    
	    // metronome panel
	    MetronomePanel metronomePanel = MetronomePanel.getInstance();
	    metronomePanel.setBounds(0, y, w, SINGLE_ROW_PANEL_HEIGHT);
	    add(metronomePanel);
	    y += metronomePanel.getHeight();
	    
	    // chord file panel - set bounds height to h - the combined height of all the other panels
	    ChordFilePanel chordFilePanel = ChordFilePanel.getInstance();
	    chordFilePanel.setBounds(0, y, w, h - 3 * SINGLE_ROW_PANEL_HEIGHT - DOUBLE_ROW_PANEL_HEIGHT);
	    add(chordFilePanel);
	    y += chordFilePanel.getHeight();

	    // chord text panel
	    ChordTextPanel chordTextPanel = ChordTextPanel.getInstance();
	    chordTextPanel.setBounds(0, y, w, DOUBLE_ROW_PANEL_HEIGHT);
	    add(chordTextPanel);
	    y += chordTextPanel.getHeight();

	    // status panel
	    StatusPanel statusPanel = StatusPanel.getInstance();
	    statusPanel.setBounds(0, y, w, SINGLE_ROW_PANEL_HEIGHT);
	    add(statusPanel);

	    tunerPanel.statusPanel 		= statusPanel;
	    dronePanel.statusPanel 		= statusPanel;
	    metronomePanel.statusPanel 	= statusPanel;
	    chordFilePanel.statusPanel 	= statusPanel;
	    chordTextPanel.statusPanel 	= statusPanel;
	    
	    chordFilePanel.init();
    }

	public void close() {
		synthesizer = null;
	}
	
	public static void main(String args[]) {
		JFrame frame = new JFrame("GuiController");

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = screenSize.width - DOCK_WIDTH; 
		int height = screenSize.height - TOP_BAR_HEIGHT;

		//width = (width < 1900) ? 1500 : width;
		
		//Dimension frameDimension = new Dimension(
		//		screenSize.width, 
		//		screenSize.height - TASKBAR_HEIGHT);
		//frame.setSize(frameDimension.width, frameDimension.height);
		
		frame.setSize(width, height);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				guiController.close();
				System.exit(0);
			}
		});

		// call setVisible to get insets
		frame.setVisible(true);

		Insets insets = frame.getInsets();
		
//		int left 	= insets.left;
//		int right 	= insets.right;
//		int top 	= insets.top;
//		int bottom	= insets.bottom;
//		
//		System.out.println(left + " " + right + " " + top + " " + bottom);

		//w = frameDimension.width - insets.left - insets.right; 
		//h = frameDimension.height - insets.top - insets.bottom;
		w = width - insets.left - insets.right; 
		h = height - insets.top - insets.bottom;
		final GuiController guiController = GuiController.getInstance();

		frame.getContentPane().add("Center", guiController);

		// call setVisible to display gui
		frame.validate();
		frame.repaint();
	}
} 
