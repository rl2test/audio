package audio.chords.gui;

import static audio.Constants.C;
import static audio.Constants.FONT;
import static audio.Constants.W;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import audio.Config;

public class TimePanel extends AudioPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 	= 1L;
	/** The log. */
	private Logger log							= Logger.getLogger(getClass());
	/** The singleton instance of this class. */    
	private static TimePanel panel 				= null;
	/** The player. */
	public MetronomePlayer player 				= null;
	private final MyMouseListener listener 		= new MyMouseListener();
	public JLabel patternLabel					= null;
	public JLabel beginLabel					= null;
    public JLabel endLabel						= null;
    public int beginTempo						= 0;
	public int endTempo							= 0;
    public int increment 						= 0;
	public int numBeats 						= 0;
	public Map<String, JLabel> timeLabels		= new HashMap<String, JLabel>();
	public Map<String, JLabel> timeTypeLabels	= new HashMap<String, JLabel>();
    public Map<String, JLabel> tempoLabels		= new HashMap<String, JLabel>();
    public Map<String, JLabel> incrementLabels	= new HashMap<String, JLabel>();
    public Map<String, JLabel> numBeatsLabels	= new HashMap<String, JLabel>();
    public JLabel patternValueLabel				= null;
    public JLabel tempoValueLabel				= null;

    /**
     * @return singleton instance of this class
     */
    public static TimePanel getInstance() {
        if (panel == null) {
        	panel = new TimePanel();
    	}
    	return panel;
    }
	
    /** Public constructor */
    public TimePanel() {
        setBackground(C[0]);
		setLayout(null);
		
		int[] times = {2, 3, 4, 5, 6, 7};
		String[] timeTypes = {"a", "b"};
		
    	// 50 - 250 in increment of 10
    	int[] tempos = new int[21];
    	for (int i = 0; i < 21; i++) {
    		tempos[i] = (i + 5) * 10;
    	}
    	
	    int[] numBeats = {8, 16, 32};
	    
	    int x = 0, y = 0, dx = 0, dy = 0;
	    
	    //String text, String name, Color bg, Color fg, int x, int y, int w, int h, boolean addListener
	    // metronome label
	    add(getLabel("Metronome", "metronome", C[6], C[16], x, y, W[3], W[1], null));
	    x += W[3] + 1;	    
	    
	    // play/stop label
	    JLabel playStopLabel = getLabel(">", "playStop", C[12], C[0], x, y, W[1], W[1], listener);
	    add(playStopLabel);
	    x += W[1] + 1;	    
	    
	    // time label
	    add(getLabel("Time", null, C[6], C[16], x, y, W[2], W[1], null));
	    x += W[2] + 1;	    

	    // time labels
	    dx = 0;
	    for (int i = 0; i < times.length; i++) {
	    	int time = times[i];
	    	String name = "time" + time;
	    	JLabel label = getLabel("" + time, name, C[12], C[0], x + dx, y, 12, W[1], listener);  
	    	add(label);
	    	dx += 12 + 1;
	    	timeLabels.put(name, label);
	    }
	    x += dx;
	    
	    // timeType label
	    add(getLabel("Type", null, C[6], C[16], x, y, W[2], W[1], null));
	    x += W[2] + 1;	    

	    // timeType labels
	    dx = 0;
	    for (int i = 0; i < timeTypes.length; i++) {
	    	String type = timeTypes[i];
	    	String name = "type" + type;
	    	JLabel label = getLabel(type, name, C[12], C[0], x + dx, y, 12, W[1], listener);  
	    	add(label);
	    	dx += 12 + 1;
	    	timeTypeLabels.put(name, label);
	    }
	    x += dx;
	    
	    // pattern label
	    add(getLabel("Pattern", null, C[6], C[16], x, y, W[3], W[1], null));
	    x += W[3] + 1;	    

	    // pattern value label
	    patternValueLabel = getLabel("", "patternValue", C[14], C[0], x, y, W[2], W[1], null);
	    add(patternValueLabel);
	    x += W[2] + 1;	 	    
	    
	    // begin label
	    beginLabel = getLabel("Begin", null, C[6], C[16], x, y, W[2], W[1], null); 
	    add(beginLabel);
	    x += W[2] + 1;	    
	    
	    // begin labels
	    dx = 0;
	    for (int tempo: tempos) {
	    	String name = "begin" + tempo;
	    	JLabel label = getLabel(null, name, C[10], null, x + dx, y + dy, 12, 12, listener);  
	    	add(label);
	    	dx += 12 + 1;
	    	tempoLabels.put(name, label);
	    }
	    dx = 0;
	    dy += 12 + 1;
	    
	    // end labels
	    for (int tempo: tempos) {
	    	String name = "end" + tempo;
	    	JLabel label = getLabel(null, name, C[10], null, x + dx, y + dy, 12, 12, listener);  
	    	add(label);
	    	dx += 12 + 1;
	    	tempoLabels.put(name, label);
	    }
	    x += dx;
	    
	    // end label
	    endLabel = getLabel("End", null, C[6], C[16], x, y, W[2], W[1], null);
	    add(endLabel);
	    x += W[2] + 1;
	    
	    // increment label
	    add(getLabel("Increment", null, C[6], C[16], x, y, W[3], W[1], null));
	    x += W[3] + 1;

	    // increment labels
	    dx = 0;
	    for (int i = 1; i <= 10; i++) {
	    	String name = "inc" + i;
	    	int w = (i == 10) ? W[1] : 12;
	    	JLabel label = getLabel("" + i, name, C[12], C[0], x + dx, y, w, W[1], listener);  
	    	add(label);
	    	dx += w + 1;
	    	incrementLabels.put(name, label);
	    }
	    x += dx;
	    
	    // numBeats label
	    add(getLabel("NumBeats", null, C[6], C[16], x, y, W[3], W[1], null));
	    x += W[3] + 1;

	    // numBeats labels
	    dx = 0;
	    for (int i = 0; i < numBeats.length; i++) {
	    	int val = numBeats[i];
	    	String name = "num" + val;
	    	int w = (val < 10) ? 12 : W[1];
	    	JLabel label = getLabel("" + val, name, C[12], C[0], x + dx, y, w, W[1], listener);  
	    	add(label);
	    	dx += w + 1;
	    	numBeatsLabels.put(name, label);
	    }
	    x += dx;
	    
	    // tempo label
	    JLabel tempoLabel = getLabel("Tempo", "", C[6], C[16], x, y, W[2], W[1], null);
	    add(tempoLabel);
	    x += W[2] + 1;

	    // tempo value label
	    tempoValueLabel = getLabel("", "tempoValue", C[14], C[0], x, y, W[2], W[1], null);
	    add(tempoValueLabel);
	    x += W[2] + 1;	    
	    
    }
    
    public void setBeginTempo() {
    	JLabel l = tempoLabels.get("begin" + beginTempo);
    	l.setBackground(C[4]);
    }
    public void setEndTempo() {
    	JLabel l = tempoLabels.get("end" + endTempo);
    	l.setBackground(C[4]);
    }
    public void setIncrement() {
    	JLabel l = incrementLabels.get("inc" + increment);
    	l.setBackground(C[4]);
    	l.setForeground(C[16]);
    }
    public void setNumBeats() {
    	JLabel l = numBeatsLabels.get("num" + numBeats);
    	l.setBackground(C[4]);
    	l.setForeground(C[16]);
    }
    
    private class MyMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            JLabel l = (JLabel) e.getSource();
        	String name = l.getName();
        	log.debug("name=" + name);
        	// begin/end tempo
        	if (name.startsWith("begin")) {
         		if (beginTempo > 0) tempoLabels.get("begin" + beginTempo).setBackground(C[8]);
        		beginTempo = Integer.parseInt(name.replace("begin", ""));
        		beginLabel.setText("" + beginTempo);
        		l.setBackground(C[4]);
        	} else if (name.startsWith("end")) {
        		if (endTempo > 0) tempoLabels.get("end" + endTempo).setBackground(C[8]);
          		endTempo = Integer.parseInt(name.replace("end", "")); 
        		endLabel.setText("" + endTempo);
        		l.setBackground(C[4]);
        	// increment
        	} else if (name.startsWith("inc")) {
        		if (increment > 0) unselect(incrementLabels.get("inc" + increment));
        		increment = Integer.parseInt(name.replace("inc", ""));
         		select(l);
         	// num beats	
         	} else if (name.startsWith("num")) {
        		if (numBeats > 0) unselect(numBeatsLabels.get("num" + numBeats));
        		numBeats = Integer.parseInt(name.replace("num", ""));
         		select(l);
         	// play/stop	
         	} else if (name.equals("playStop")) {
         		String text = l.getText();
         		if (text.equals(">")) {
             		if (beginTempo == 0) beginTempo = 90; setBeginTempo();
             		if (endTempo == 0) endTempo	= 180; setEndTempo();
             		if (increment == 0) increment = 1; setIncrement();
             		if (numBeats == 0) numBeats	= 8; setNumBeats();
             		beginLabel.setText("" + beginTempo);
             		endLabel.setText("" + endTempo);
             	    player	= new MetronomePlayer(beginTempo, endTempo, increment, numBeats, panel);
            	    l.setText("||");
           	    	player.start();
         		} else {
         			player.end();
         			player = null;
            	    l.setText(">");
         		}
         	}
        }
        public void select(JLabel l) {
        	l.setBackground(C[4]);
        	l.setForeground(C[16]);
        }
        public void unselect(JLabel l) {
        	l.setBackground(C[12]);
        	l.setForeground(C[0]);
        }
    }    
}
