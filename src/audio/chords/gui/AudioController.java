package audio.chords.gui;

import static audio.Constants.BARS_PER_LINE;
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
public class AudioController extends JPanel {
	/** The generated serialVersionUID. */
	private static final long serialVersionUID	= 1L;
	/** The singleton instance of this class. */    
	private static AudioController controller 	= null;
	/** The log. */
	private Logger log 							= Logger.getLogger(this.getClass());
	/** The synthesizer. */
	private Synthesizer synthesizer 			= null;
	/** The midiChannels. */
	public MidiChannel[] midiChannels 	= null;
	/** The width of the gui, not including the frame objects. */
	public int w								= 0;
	/** The height of the gui, not including the frame objects. */
	public int h								= 0;
	public KeyPanel keyPanel 					= null;
	public TimePanel timePanel 					= null;
	public FilePanel filePanel 					= null;
	public TextPanel textPanel 					= null;
	public DisplayPanel displayPanel 			= null;
	public boolean init							= false;
	public int barWidth							= 0;
	public static Rectangle rhythmRectangle		= null;
	
    /**
     * @return singleton instance of this class
     */
    public static AudioController getInstance(int w, int h) throws Exception {
        if (controller == null) {
        	controller = new AudioController(w, h);
    	}
    	return controller;
    }
	
    /**
     * @return singleton instance of this class
     */
    public static AudioController getInstance() {
    	return controller;
    }
    
    /** Private constructor */
    /**
     * 
     */
    private AudioController(int w, int h) throws Exception {
    	this.setBackground(Color.black);
    	this.w = w;
    	this.h = h;
		this.setSize(w, h);
		
		log.debug("gui w x h = " + w + " x " + h);

		// get and open the synthesizer
		try {
			if ((synthesizer = MidiSystem.getSynthesizer()) == null) {
				log.debug("getSynthesizer() failed!");
				return;
			}
			synthesizer.open();
			/*
			Soundbank sb = synthesizer.getDefaultSoundbank();
			Instrument[] instruments = sb.getInstruments();
			int i = 0;
			for(Instrument instrument: instruments) {
				log.debug(++i + " " + instrument.getName());
			}
			*/
		} catch (Exception e) {
			log.error(e);
		}
		
		/* does not work on mac osx	
		Soundbank sb = null;
		File file = new File("/Library/Java/JavaVirtualMachines/jdk1.8.0_111.jdk/Contents/Home/jre/lib/audio/soundbank-deluxe.gm"); //_deluxe
		BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file)); 
		try {
			sb = MidiSystem.getSoundbank(inputStream);
			log.debug(sb.getDescription());
			log.debug(sb.getName());			
			log.debug(sb.getVendor());
			log.debug(sb.getVersion());
		} catch(Exception e) {
			log.error(e);
		}
		if (sb != null) {
			Instrument[] instruments = sb.getInstruments();
			//synthesizer.loadInstrument(instruments[0]); // from orig
		} else {
			log.debug("couldn't get soundbank");
		}
		*/

		// get the synthesizer channels
		midiChannels = synthesizer.getChannels();
		
		setLayout(null);
		int x = 0;
	    int y = 1;

	    keyPanel = KeyPanel.getInstance(this);
	    keyPanel.setBounds(x, y, w, W[1]);
	    add(keyPanel);
	    y += W[1] + 1;
	    
	    // metronome panel
	    timePanel = TimePanel.getInstance(this);
	    timePanel.setBounds(0, y, w, W[1]);
	    add(timePanel);
	    y += W[1] + 1;
	    
	    // chord file panel - set bounds height to h - the combined height of all the other panels
	    filePanel = FilePanel.getInstance(this);
	    filePanel.setBounds(0, y, w, W[1]);
	    add(filePanel);
	    y += W[1] + 1;
	    
	    int height = h - (3 * (W[1] + 1));
	    
	    barWidth = (w - (W[16] + 1)) / BARS_PER_LINE;
	    int displayPanelWidth = BARS_PER_LINE * (barWidth + 1) - 1;
	    int textPanelWidth = w - (displayPanelWidth + 1); 
	    
	    textPanel = TextPanel.getInstance(this, textPanelWidth, height);
	    textPanel.setBounds(0, y, textPanelWidth, height);
	    add(textPanel);

	    displayPanel = DisplayPanel.getInstance(this);
	    displayPanel.setBounds(textPanelWidth + 1, y, displayPanelWidth, height);
	    add(displayPanel);

	    filePanel.setTextArea();
		init = true;
    }

	public void close() {
		synthesizer = null;
	}
	
	// set msg on key panel
	public void setMsg(String msg) {
		keyPanel.labels.get("msg").setText(msg);
	}
	
	// clear msg on key panel
	public void clearMsg(String msg) {
		keyPanel.labels.get("msg").setText("");
	}
	
	// get pattern key by time and type
	public int getPatternKey(int time, int type) {
		return (time < 5) ? time : time * 10 + type;
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
        		screenY = r.y;
       		 	System.out.println("screenNum" + screenNum + ":" + screenW + ", " + screenH + ", " + screenX + ", " + screenY);
        	 }
        	 screenNum++;
        }
        rhythmRectangle = new Rectangle(screenX + screenW / 4, screenY + screenH / 4, screenW / 2, screenH / 2);
        
        JFrame frame = new JFrame("AudioController");

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
				controller.close();
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
		int w = screenW - insets.left - insets.right; 
		int h = screenH - TOP_BAR_HEIGHT - insets.top - insets.bottom;
		AudioController controller;
		try {
			controller = AudioController.getInstance(w, h);
			frame.getContentPane().add("Center", controller);

			// call setVisible to display gui
			frame.validate();
			frame.repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
} 
