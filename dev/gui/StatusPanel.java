package audio.chords.gui;

import static audio.Constants.BG_COLOR;
import static audio.Constants.ROW_HEIGHT;
import static audio.Constants.SEPARATOR_HEIGHT;
import static audio.Constants.SP;
import static audio.Constants.WIDTH_3;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

public class StatusPanel extends JPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 	= 1L;
	/** The log. */
	private Logger log							= Logger.getLogger(getClass());
	/** The singleton instance of this class. */    
	private static StatusPanel panel 			= null;
	/** The status update label, which gets updated in real time. */
	public JLabel statusUpdateLabel 			= new JLabel("");
	
    /**
     * @return singleton instance of this class
     */
    public static StatusPanel getInstance() {
        if (panel == null) {
        	panel = new StatusPanel();
    	}
    	return panel;
    }
	
    /** Public constructor */
    public StatusPanel() {
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
	    
	    // status label
	    JLabel statusLabel = new JLabel("Status:");
	    statusLabel.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    add(statusLabel);
	    x += statusLabel.getWidth() + SP;

	    // status update label
	    statusUpdateLabel.setBounds(x, y, GuiController.w - x, ROW_HEIGHT);
	    add(statusUpdateLabel);
	    x += statusUpdateLabel.getWidth() + SP;
	    
	    log.debug("final x=" + x);
    }
    
    /**
     * 
     */
    public void clear() {
    	statusUpdateLabel.setText("");
    }
    
    /**
     * @param s
     */
    public void setText(String s) {
    	statusUpdateLabel.setText(s);
    }
}
