package audio.chords.gui;

import static audio.Constants.C;
import static audio.Constants.W;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;


@SuppressWarnings("serial")
public class TimePanel extends AudioPanel { 
	/** The singleton instance of this class. */    
	private static TimePanel panel 				= null;
	private final Listener listener 			= new Listener();
    // set property defaults
    public int beginTempo						= 90;
	public int endTempo							= 180;
    public int increment 						= 1;
	public boolean set							= false; // if true this will override the tune settings

    /**
     * @return singleton instance of this class
     */
    public static TimePanel getInstance(AudioController ac) throws Exception {
        if (panel == null) {
        	panel = new TimePanel(ac);
    	}
    	return panel;
    }
	
    /** public constructor builds ui */
    private TimePanel(AudioController ac) throws Exception {
    	super(ac);
		
    	// 50 - 250 in increment of 10
    	int[] tempos = new int[21];
    	for (int i = 0; i < 21; i++) {
    		tempos[i] = (i + 5) * 10;
    	}
    	
	    h = W[1];
	    int dx = 0, dy = 0;
	    
	    // time label
	    w = W[2];
	    add(getLabel("Time", null, C[6], C[16], x, y, w, h, null));
	    x += w + 1;	    

	    // set label
	    w = W[2];
	    add(getLabel("Set", "set", C[12], C[0], x, y, w, h, listener));
	    x += w + 1;		    
	    
	    // begin label
	    add(getLabel("", "begin", C[6], C[16], x, y, w, h, null));
	    x += w + 1;	    
	    
	    // begin labels
	    w = 12;
	    dx = 0;
	    for (int tempo: tempos) {
	    	String name = "begin" + tempo;
	    	add(getLabel(null, name, C[12], null, x + dx, y + dy, w, 12, listener));
	    	dx += w + 1;
	    }
	    dx = 0;
	    dy += 12 + 1;
	    
	    // end labels
	    for (int tempo: tempos) {
	    	String name = "end" + tempo;
	    	add(getLabel(null, name, C[12], null, x + dx, y + dy, w, 12, listener));
	    	dx += w + 1;
	    }
	    x += dx;
	    
	    // end label
	    w = W[2];
	    add(getLabel("", "end", C[6], C[16], x, y, w, h, null));
	    x += w + 1;
	    
	    // increment label
	    w = W[3];
	    add(getLabel("Increment", null, C[6], C[16], x, y, w, h, null));
	    x += w + 1;

	    // increment labels
	    dx = 0;
	    for (int i = 1; i <= 10; i++) {
	    	String name = "inc" + i;
	    	w = (i == 10) ? W[1] : 12;
	    	add(getLabel("" + i, name, C[12], C[0], x + dx, y, w, h, listener));
	    	dx += w + 1;
	    }
	    x += dx;
	    
	    // tempo label
	    w = W[2];
	    add(getLabel("Tempo", "", C[6], C[16], x, y, w, h, null));
	    x += w + 1;

	    // tempo value label
	    add(getLabel("", "tempoValue", C[16], C[0], x, y, w, h, null));
	    x += w + 1;	    
	    
		set("begin" + beginTempo);
		set("end" + endTempo);
		set("inc" + increment);

		labels.get("begin").setText("" + beginTempo);
		labels.get("end").setText("" + endTempo);

    }
    
    public void setTempoValue(int tempo) {
    	labels.get("tempoValue").setText("" + tempo);
    }
    
    private class Listener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            JLabel l = (JLabel) e.getSource();
        	String name = l.getName();
        	log.debug("name=" + name);
        	
        	// setTime
        	if (name.equals("set")) {
        		if (set) {
        			unset(l);
           	 	} else {
           	 		set(l);
           	 	}
    			set = !set;
         	} else {
         		// time
         		if (name.startsWith("begin")) {
             		if (beginTempo > 0) unset("begin" + beginTempo);
            		beginTempo = Integer.parseInt(name.replace("begin", ""));
            		labels.get("begin").setText("" + beginTempo);
            	} else if (name.startsWith("end")) {
            		if (endTempo > 0) unset("end" + endTempo);
              		endTempo = Integer.parseInt(name.replace("end", "")); 
              		labels.get("end").setText("" + endTempo);
            	// increment
            	} else if (name.startsWith("inc")) {
            		if (increment > 0) unset("inc" + increment);
            		increment = Integer.parseInt(name.replace("inc", ""));
             	}
         		
        		set(l);
         	}
        }
    }    
}
