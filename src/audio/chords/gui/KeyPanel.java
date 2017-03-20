package audio.chords.gui;

import static audio.Constants.C;
import static audio.Constants.TRANSPOSE_KEYS;
import static audio.Constants.TRANSPOSE_KEYS_MINOR;
import static audio.Constants.W;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

public class KeyPanel extends AudioPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 		= 1L;
	/** The singleton instance of this class. */    
	private static KeyPanel panel 					= null;
	/** The drone player. */
	public DronePlayer dronePlayer 			= null;
	private final KeyListener keyListener 	= new KeyListener();
	public int selectedKeyIndex				= -1;
	//public boolean transpose				= false; // if true this will overide the tune settings
	public boolean drone					= false;
	
    /**
     * @return singleton instance of this class
     */
    public static KeyPanel getInstance(AudioController ac)  throws Exception {
        if (panel == null) {
        	panel = new KeyPanel(ac);
    	}
    	return panel;
    }
	
    /** Public constructor */
    private KeyPanel(AudioController ac) throws Exception {
    	super(ac);
	    
	    // key label
    	w = W[2];
    	h = W[1];
    	
	    add(getLabel("Key", null, C[6], C[16], x, y, w, h, null));
	    x += w + 1;

	    // transpose label
	    //add(getLabel("Transpose", "transpose", C[12], C[0], x, y, W[3], W[1], keyListener));
	    //x += W[3] + 1;
	    
	    // transpose key labels
	    for (int i = 0, n = TRANSPOSE_KEYS.length; i < n; i++) {
	    	String key = TRANSPOSE_KEYS[i];
	    	String keyMinor = TRANSPOSE_KEYS_MINOR[i];
	    	String text = "<html>" + key + "<br>" + keyMinor + "</html>";
	    	int width = (i == 3 || i == 6) ? W[2] : W[1];
	    	add(getLabel(text, "key" + i, C[12], C[0], x, y, width, h, keyListener));
	    	x += width + 1;
	    }

	    // drone label
	    add(getLabel("Drone", "drone", C[12], C[0], x, y, w, h, keyListener));
    }  

    private class KeyListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            JLabel l = (JLabel) e.getSource();
            String name = l.getName();
            //log.debug("name=" + name + ", selectedKey=" + selectedKey);
            
            if (name.startsWith("key")) {
            	 int keyIndex = Integer.parseInt(name.replace("key", ""));
            	 if (keyIndex == selectedKeyIndex) {
             		selectedKeyIndex = -1;
             		unset(l);
             	 } else {
             		if (selectedKeyIndex != -1) {
             			unset("key" + selectedKeyIndex);
             		}	 
                 	set(l);
             		selectedKeyIndex = keyIndex;
             	 }
            	 //log.debug("selectedKey=" + selectedKey);
            //} else if (name.equals("transpose")) {
            //	 if (transpose) {
            //		 unset(l);
            //	 } else {
            //		 set(l);
            //	 }
        	//	 transpose = !transpose;
            } else if (name.equals("drone")) {
            	if (drone) {
            		unset(l);
            		drone = false;
					dronePlayer.end();
					dronePlayer = null;
            	} else {
            		if (selectedKeyIndex != -1) {
    					set(l);
    					drone = true;
    					dronePlayer = new DronePlayer(selectedKeyIndex, ac);
    					dronePlayer.start();        	
            		} else {
            			log.warn("no key selected for drone");
            		}
            	}            	 
            }
        }
    }
}
