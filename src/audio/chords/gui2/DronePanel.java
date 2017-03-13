package audio.chords.gui2;

import static audio.Constants.C;
import static audio.Constants.FONT;
import static audio.Constants.TRANSPOSE_KEYS;
import static audio.Constants.W;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

public class DronePanel extends JPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 		= 1L;
	/** The log. */
	private Logger log								= Logger.getLogger(getClass());
	/** The singleton instance of this class. */    
	private static DronePanel panel 				= null;
	/** The drone player. */
	public DronePlayer dronePlayer 					= null;
	private final MyMouseListener myMouseListener 	= new MyMouseListener();
	private final boolean LISTENER					= true;
	private final boolean NO_LISTENER				= false;
	public static String selectedKey				= "";
	private Map<String, JLabel> keyMap				= new HashMap<String, JLabel>();
	
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
        setBackground(C[6]);
		setLayout(null);
		
	    int x = 0;
	    int y = 0;
	    
	    // drone label
	    JLabel droneLabel = getLabel("Drone", C[6], C[16], x, y, W[2], NO_LISTENER);
	    add(droneLabel);
	    x += W[2] + 1;

	    for (String key: TRANSPOSE_KEYS) {
	    	JLabel label = getLabel(key, C[12], C[0], x, y, W[1], LISTENER);
	    	add(label);
	    	keyMap.put(key, label);
	    	x += W[1] + 1;
	    }
    }  
    
    public JLabel getLabel(String text, Color bg, Color fg, int x, int y, int w, boolean addListener) {
    	JLabel label = new JLabel(text);
        label.setBackground(bg);
        label.setForeground(fg);
        label.setOpaque(true);
        label.setBounds(x, y, w, W[1]);
        label.setFont(FONT);
        label.setHorizontalAlignment(JLabel.CENTER);
        if (addListener) {
        	label.addMouseListener(myMouseListener);
        }
        return label;
    }

    private class MyMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
             JLabel l = (JLabel) e.getSource();
        	 String key = l.getText();
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
        @Override
        public void mouseEntered(MouseEvent e) {
        	JLabel l = (JLabel) e.getSource();
        	if (!l.getText().equals(selectedKey)) {
        		l.setBackground(C[14]);
        	}
        }
        @Override
        public void mouseExited(MouseEvent e) {
        	JLabel l = (JLabel) e.getSource();
        	if (!l.getText().equals(selectedKey)) {
        		l.setBackground(C[12]);
        	}
        }
        
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
