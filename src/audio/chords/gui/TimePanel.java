package audio.chords.gui;

import static audio.Constants.C;
import static audio.Constants.PATTERN_STRS;
import static audio.Constants.W;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import audio.jsound.Groove;

public class TimePanel extends AudioPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 	= 1L;
	/** The singleton instance of this class. */    
	private static TimePanel panel 				= null;
	/** The player. */
	public MetronomePlayer player 				= null;
	private final Listener listener 			= new Listener();
    // set property defaults
    public int time								= 4;
	public int type								= 1;
    public int beginTempo						= 90;
	public int endTempo							= 180;
    public int increment 						= 1;
	public int numBars 							= 8;
	public boolean set							= false; // if true this will override the tune settings
	public boolean metronome					= false;

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
		
		int[] times = {2, 3, 4, 5, 6, 7};
		int[] types = {1, 2};
		
    	// 50 - 250 in increment of 10
    	int[] tempos = new int[21];
    	for (int i = 0; i < 21; i++) {
    		tempos[i] = (i + 5) * 10;
    	}
    	
	    int[] numBarsArr = {8, 16, 32};
	    
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
	    
	    // time labels
	    w = 12;
	    dx = 0;
	    for (int i = 0; i < times.length; i++) {
	    	int time = times[i];
	    	String name = "time" + time;
	    	add(getLabel("" + time, name, C[12], C[0], x + dx, y, w, h, listener));
	    	dx += w + 1;
	    }
	    x += dx;
	    
	    // type label
	    w = W[2];
	    add(getLabel("Type", null, C[6], C[16], x, y, w, h, null));
	    x += w + 1;	    

	    // type labels
	    w = 12;
	    dx = 0;
	    for (int i = 0; i < types.length; i++) {
	    	String text = "" + types[i];
	    	String name = "type" + types[i];
	    	add(getLabel(text, name, C[12], C[0], x + dx, y, w, h, listener));
	    	dx += w + 1;
	    }
	    x += dx;
	    
	    // pattern label
	    w = W[3];
	    add(getLabel("Pattern", null, C[6], C[16], x, y, w, h, null));
	    x += w + 1;	    

	    // pattern value label
	    w = W[2];
	    add(getLabel("", "patternValue", C[16], C[0], x, y, w, h, null));
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
	    
	    // numBars label
	    w = W[3];
	    add(getLabel("NumBars", null, C[6], C[16], x, y, w, h, null));
	    x += w + 1;

	    // numBars labels
	    dx = 0;
	    for (int i = 0; i < numBarsArr.length; i++) {
	    	int val = numBarsArr[i];
	    	String name = "num" + val;
	    	w = (val < 10) ? 12 : W[1];
	    	add(getLabel("" + val, name, C[12], C[0], x + dx, y, w,h, listener));
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
	    
	    // metronome label
	    w = W[3];
	    add(getLabel("Metronome", "metronome", C[12], C[0], x, y, w, h, listener));
	    x += w + 1;	    

	    // groove label
	    w = W[3];
	    add(getLabel("Groove", "groove", C[12], C[0], x, y, w, h, listener));
	    x += w + 1;	    

	    // groovej label
	    w = W[1];
	    add(getLabel("J", "groovej", C[12], C[0], x, y, w, h, listener));
	    x += w + 1;	    

	    // groovej label
	    w = W[1];
	    add(getLabel("S", "grooves", C[12], C[0], x, y, w, h, listener));
	    x += w + 1;	 
	    
	    // set to defaults
		set("time" + time);
		set("type" + type);
		set("begin" + beginTempo);
		set("end" + endTempo);
		set("inc" + increment);
		set("num" + numBars);

		setPatternValue();
		labels.get("begin").setText("" + beginTempo);
		labels.get("end").setText("" + endTempo);

    }
    
    public void setTempoValue(int tempo) {
    	labels.get("tempoValue").setText("" + tempo);
    }
    
    public void setPatternValue() {
    	int patternKey = ac.getPatternKey(time, type);
    	labels.get("patternValue").setText(PATTERN_STRS.get(patternKey));
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
        	} else if (name.equals("metronome")) {
         		if (metronome) {
         			player.end();
         			player = null;
            	    unset(l);
         		} else {
             	    player = new MetronomePlayer(panel, ac);
           	    	player.start();
           	    	set(l);
         		}
         		metronome = !metronome;
         	} else if (name.equals("groove")) {
       	    	Groove groove = new Groove();
       	    	groove.init();
         	} else if (name.equals("groovej")) {
       	    	GrooveJ groove = new GrooveJ();
       	    	groove.init();
         	} else if (name.equals("grooves")) {
       	    	GrooveS groove = new GrooveS();
       	    	groove.init();
         	} else {
         		// time
         		if (name.startsWith("time")) {
             		if (time > 0) unset("time" + time);
            		time = Integer.parseInt(name.replace("time", ""));
            		setPatternValue();
            	// type	
    	     	} else if (name.startsWith("type")) {
    	     		if (type > 0) unset("type" + type);
    	     		type = Integer.parseInt(name.replace("type", ""));
    	     		setPatternValue();
             	// begin/end tempo	
             	} else if (name.startsWith("begin")) {
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
             	// num beats	
             	} else if (name.startsWith("num")) {
            		if (numBars > 0) unset("num" + numBars);
            		numBars = Integer.parseInt(name.replace("num", ""));
             	}	
        		set(l);
         	}
        }
    }    
}
