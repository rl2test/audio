package audio.chords.gui;

import static audio.Constants.BG_COLOR;
import static audio.Constants.FONT;
import static audio.Constants.INSTRUMENT_GUITAR;
import static audio.Constants.INSTRUMENT_VIOLIN;
import static audio.Constants.ROW_HEIGHT;
import static audio.Constants.SEPARATOR_HEIGHT;
import static audio.Constants.SP;
import static audio.Constants.WIDTH_3;
import static audio.Constants.WIDTH_4;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

public class TunerPanel extends JPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 	= 1L;
	/** The log. */
	private Logger log							= Logger.getLogger(getClass());
	/** The singleton instance of this class. */    
	private static TunerPanel panel 			= null;
	/** The note player. */
	public NotePlayer notePlayer 				= null;
	/** StatusPanel reference. */
	public StatusPanel statusPanel 				= null;
	
    /**
     * @return singleton instance of this class
     */
    public static TunerPanel getInstance() {
        if (panel == null) {
        	panel = new TunerPanel();
    	}
    	return panel;
    }
	
    /** Public constructor */
    public TunerPanel() {
        setBackground(BG_COLOR);
		setLayout(null);
		
	    int x = 0;
	    int y = 0;
	    
	    // separator ///////////////////////////////////////////////////////////
	    JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
	    separator.setBounds(x, y, GuiController.w, SEPARATOR_HEIGHT);
	    add(separator);
	    y += separator.getHeight();
	    
	    // row 1 ///////////////////////////////////////////////////////////////	    
	    
	    // tuner label
	    JLabel tunerLabel = new JLabel("Tuner");
	    tunerLabel.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    tunerLabel.setFont(FONT);
	    add(tunerLabel);
	    x += tunerLabel.getWidth() + SP;

	    // tuners 
	    
	    // violin button
	    final JButton violinButton = new JButton(INSTRUMENT_VIOLIN);
	    violinButton.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    violinButton.setFont(FONT);
	    add(violinButton);
	    x += violinButton.getWidth() + SP;

	    // guitar button
	    final JButton guitarButton = new JButton(INSTRUMENT_GUITAR);
	    guitarButton.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    guitarButton.setFont(FONT);
	    add(guitarButton);
	    x += guitarButton.getWidth() + SP;
	    
	    log.debug("final x=" + x);
	    
	    /* action listeners */
	    // violin button listener
	    violinButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (notePlayer == null) {
					notePlayer = new NotePlayer(INSTRUMENT_VIOLIN);
					notePlayer.start();
				} else {
					notePlayer.end();
					notePlayer = null;
				}

			}
		});
	    
	    // violin button listener
	    guitarButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (notePlayer == null) {
					notePlayer = new NotePlayer(INSTRUMENT_GUITAR);
					notePlayer.start();
				} else {
					notePlayer.end();
					notePlayer = null;
				}

			}
		});	    
    }    
}
