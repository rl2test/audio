package audio.chords.gui2;

import static audio.Constants.ENV;
import static audio.Constants.RUNTIME_VERSION;
import static audio.Constants.TOP_BAR_HEIGHT;
import static audio.Constants.W;
import static audio.Constants.WK;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
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
    /**
     * 
     */
    private GuiController() {
    	this.setBackground(Color.black);
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
		int x = 0;
	    int y = 1;

	    DronePanel dronePanel = DronePanel.getInstance();
	    dronePanel.setBounds(x, y, w, W[1]);
	    add(dronePanel);
	    y += dronePanel.getHeight() + 1;
	    
	    // metronome panel
	    MetronomePanel metronomePanel = MetronomePanel.getInstance();
	    metronomePanel.setBounds(0, y, w, W[1]);
	    add(metronomePanel);
	    y += metronomePanel.getHeight() + 1;
	    
	    // chord file panel - set bounds height to h - the combined height of all the other panels
	    ChordFilePanel chordFilePanel = ChordFilePanel.getInstance();
	    chordFilePanel.setBounds(0, y, w, h - (2 * (W[1] + 1)));
	    add(chordFilePanel);
	    y += chordFilePanel.getHeight() + 1;

	    chordFilePanel.init();
    }

	public void close() {
		synthesizer = null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		GraphicsEnvironment ge 	= GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gds[] 	= ge.getScreenDevices();
        int len 				= gds.length;
        boolean isDual 			= (len > 1);
        int useScreen			= (isDual) ? 2 : 1;
		int screenW 			= 0;
		int screenH 			= 0;
		int screenX				= 0;
		int screenY				= 0;

        System.out.println("RUNTIME_VERSION=" + RUNTIME_VERSION);
        System.out.println("ENV=" + ENV);
        int scale = (ENV.equals(WK)) ? 1 : 1;

        System.out.println(len + " screens detected: isDual=" + isDual + ", useScreen=" + useScreen);
        int screenNum = 1;
        for(GraphicsDevice gd: gds) {
        	 GraphicsConfiguration	graphicsConfiguration = gd.getDefaultConfiguration();
        	 Rectangle r = graphicsConfiguration.getBounds();
        	 if (useScreen == screenNum) {
        		screenW = r.width / scale;
        		screenH = r.height / scale;
        		screenX = r.x;
        		screenY = screenW;
       		 	System.out.println("screenNum" + screenNum + ":" + screenW + ", " + screenH + ", " + screenX + ", " + screenY);
        	 }
        	 screenNum++;
         }
        
		JFrame frame = new JFrame("GuiController");

		//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//int width = screenSize.width - DOCK_WIDTH; 
		//int height = screenSize.height - TOP_BAR_HEIGHT;

		//Dimension frameDimension = new Dimension(
		//		screenSize.width, 
		//		screenSize.height - TASKBAR_HEIGHT);
		//frame.setSize(frameDimension.width, frameDimension.height);
		
		frame.setSize(screenW, screenH - TOP_BAR_HEIGHT);
		frame.setLocation(screenX, screenY);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				guiController.close();
				System.exit(0);
			}
		});

		// call setVisible to get insets
		frame.setVisible(true);

		Insets insets = frame.getInsets();
		
		int left 	= insets.left;
		int right 	= insets.right;
		int top 	= insets.top;
		int bottom	= insets.bottom;
		
		System.out.println(left + " " + right + " " + top + " " + bottom);

		//w = frameDimension.width - insets.left - insets.right; 
		//h = frameDimension.height - insets.top - insets.bottom;
		w = screenW - insets.left - insets.right; 
		h = screenH - TOP_BAR_HEIGHT - insets.top - insets.bottom;
		final GuiController guiController = GuiController.getInstance();

		frame.getContentPane().add("Center", guiController);

		// call setVisible to display gui
		frame.validate();
		frame.repaint();
	}
} 
