package audio.chords.gui;

import static audio.Constants.C;
import static audio.Constants.PATTERN_STRS;
import static audio.Constants.W;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

public class TimePanel extends AudioPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 	= 1L;
	/** The singleton instance of this class. */    
	private static TimePanel panel 				= null;
	/** The player. */
	public MetronomePlayer player 				= null;
	private final TimeListener listener 		= new TimeListener();
    // set property defaults
    public int time								= 4;
	public int timeType							= 1;
    public int beginTempo						= 90;
	public int endTempo							= 180;
    public int increment 						= 1;
	public int numBeats 						= 8;
	public boolean set							= false; // if true this will overide the tune settings
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
		int[] timeTypes = {1, 2};
		
    	// 50 - 250 in increment of 10
    	int[] tempos = new int[21];
    	for (int i = 0; i < 21; i++) {
    		tempos[i] = (i + 5) * 10;
    	}
    	
	    int[] numBeatsArr = {8, 16, 32};
	    
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
	    
	    // timeType label
	    w = W[2];
	    add(getLabel("Type", null, C[6], C[16], x, y, w, h, null));
	    x += w + 1;	    

	    // timeType labels
	    w = 12;
	    dx = 0;
	    for (int i = 0; i < timeTypes.length; i++) {
	    	int type = timeTypes[i];
	    	String name = "type" + type;
	    	add(getLabel("" + type, name, C[12], C[0], x + dx, y, w, h, listener));
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
	    
	    // numBeats label
	    w = W[3];
	    add(getLabel("NumBeats", null, C[6], C[16], x, y, w, h, null));
	    x += w + 1;

	    // numBeats labels
	    dx = 0;
	    for (int i = 0; i < numBeatsArr.length; i++) {
	    	int val = numBeatsArr[i];
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
	    
	    // set to defaults
		set("time" + time);
		set("type" + timeType);
		set("begin" + beginTempo);
		set("end" + endTempo);
		set("inc" + increment);
		set("num" + numBeats);

		setPatternValue();
		labels.get("begin").setText("" + beginTempo);
		labels.get("end").setText("" + endTempo);

    }
    
    public void setTempoValue(int tempo) {
    	labels.get("tempoValue").setText("" + tempo);
    }
    
    public void setPatternValue() {
    	int patternKey = (time < 5) ? time : time * 10 + timeType;
    	labels.get("patternValue").setText(PATTERN_STRS.get(patternKey));
    }
    
    private class TimeListener extends MouseAdapter {
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
         	} else {
         		// time
         		if (name.startsWith("time")) {
             		if (time > 0) unset("time" + time);
            		time = Integer.parseInt(name.replace("time", ""));
            		setPatternValue();
            	// timeType	
    	     	} else if (name.startsWith("type")) {
    	     		if (timeType > 0) unset("type" + timeType);
    	     		timeType = Integer.parseInt(name.replace("type", ""));
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
            		if (numBeats > 0) unset("num" + numBeats);
            		numBeats = Integer.parseInt(name.replace("num", ""));
             	}	
        		set(l);
         	}
        }
    }    
}
