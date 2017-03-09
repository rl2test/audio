package audio.chords.gui2;

import static audio.Constants.BG_COLOR;
import static audio.Constants.FONT;
import static audio.Constants.ROW_HEIGHT;
import static audio.Constants.SEPARATOR_HEIGHT;
import static audio.Constants.SP;
import static audio.Constants.TRANSPOSE_KEYS;
import static audio.Constants.WIDTH_2;
import static audio.Constants.WIDTH_3;
import static audio.Constants.WIDTH_4;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

public class DronePanel extends JPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 	= 1L;
	/** The log. */
	private Logger log							= Logger.getLogger(getClass());
	/** The singleton instance of this class. */    
	private static DronePanel panel 			= null;
	/** The drone player. */
	public DronePlayer dronePlayer 				= null;
	/** Key box. */
	private final JComboBox<String> keyBox 		= new JComboBox<String>();
    /** Play button. */
	private final JButton playButton 			= new JButton("Play");
    /** Stop button. */
	private final JButton stopButton 			= new JButton("Stop");
	
    /**
     * @return singleton instance of this class
     */
    public static DronePanel getInstance() {
        if (panel == null) {
        	panel = new DronePanel();
    	}
    	return panel;
    }
	
    /** Public constructor */
    public DronePanel() {
        setBackground(BG_COLOR);
		setLayout(null);
		
	    int x = 0;
	    int y = 0;
	    
	    // drone label
	    JLabel droneLabel = new JLabel("Drone");
	    droneLabel.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    droneLabel.setFont(FONT);
	    add(droneLabel);
	    x += droneLabel.getWidth() + SP;

	    // drone combo box
	    keyBox.setModel(new DefaultComboBoxModel<String>(TRANSPOSE_KEYS));
		// create and register folderBox listener
	    keyBox.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    keyBox.setFont(FONT);
		add(keyBox);
	    x += keyBox.getWidth() + SP;

	    // play button, in class declaration
	    playButton.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    playButton.setFont(FONT);
	    add(playButton);
	    x += playButton.getWidth() + SP;
	    
	    // stop button, in class declaration
	    stopButton.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    stopButton.setFont(FONT);
	    add(stopButton);
	    stopButton.setEnabled(false);
	    x += stopButton.getWidth() + SP;
	    
	    log.debug("final x=" + x);
	    
	    /* action listeners */

	    // play button listener
	    playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String key = (String) keyBox.getSelectedItem();	
				
				playButton.setEnabled(false);
				stopButton.setEnabled(true);
				
				dronePlayer = new DronePlayer(key);
				dronePlayer.start();
			}
		});

	    // stop button listener
	    stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dronePlayer.end();
				dronePlayer = null;

				playButton.setEnabled(true);
				stopButton.setEnabled(false);
			}
		});
    }    
}
