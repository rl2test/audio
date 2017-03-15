package audio.chords.gui;

import static audio.Constants.C;
import static audio.Constants.TRANSPOSE_KEYS;
import static audio.Constants.W;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;

//import org.apache.log4j.Logger;

public class KeyPanel extends AudioPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 		= 1L;
	/** The log. */
	//private Logger log								= Logger.getLogger(getClass());
	/** The singleton instance of this class. */    
	private static KeyPanel panel 					= null;
	/** The drone player. */
	public DronePlayer dronePlayer 					= null;
	private final MyMouseListener listener 			= new MyMouseListener();
	public static String selectedKey				= "";
	private Map<String, JLabel> keyMap				= new HashMap<String, JLabel>();
	
    /**
     * @return singleton instance of this class
     */
    public static KeyPanel getInstance() {
        if (panel == null) {
        	panel = new KeyPanel();
    	}
    	return panel;
    }
	
    /** Public constructor */
    public KeyPanel() {
        setBackground(C[0]);
		setLayout(null);
		
	    int x = 0;
	    int y = 0;
	    
	    // drone label
	    add(getLabel("Drone", null, C[6], C[16], x, y, W[2], W[1], null));
	    x += W[2] + 1;

	    for (String key: TRANSPOSE_KEYS) {
	    	JLabel label = getLabel(key, key, C[12], C[0], x, y, W[1], W[1], listener);
	    	add(label);
	    	keyMap.put(key, label);
	    	x += W[1] + 1;
	    }
    }  

    private class MyMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
             JLabel l = (JLabel) e.getSource();
        	 String key = l.getName();
        	 if (key.equals(selectedKey)) {
        		selectedKey = "";
        		unselect(l);
        	 } else {
        		if (selectedKey != "") {
        			JLabel selected = keyMap.get(selectedKey);
        			unselect(selected);
        		}	 
            	select(l);
        		selectedKey = key;
        	 }
        }
        /*
        @Override
        public void mouseEntered(MouseEvent e) {
        	JLabel l = (JLabel) e.getSource();
        	if (!l.getName().equals(selectedKey)) {
        		l.setBackground(C[14]);
        	}
        }
        @Override
        public void mouseExited(MouseEvent e) {
        	JLabel l = (JLabel) e.getSource();
        	if (!l.getName().equals(selectedKey)) {
        		l.setBackground(C[12]);
        	}
        }
        */
        public void select(JLabel l) {
			dronePlayer = new DronePlayer(l.getText());
			dronePlayer.start();        	
       	 	l.setBackground(C[4]);
       	 	l.setForeground(C[16]);
        }
        public void unselect(JLabel l) {
			dronePlayer.end();
			dronePlayer = null;        	
        	l.setBackground(C[12]);
        	l.setForeground(C[0]);
        }
    }
}
