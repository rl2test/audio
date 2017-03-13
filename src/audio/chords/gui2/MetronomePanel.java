package audio.chords.gui2;

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

public class MetronomePanel extends JPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 		= 1L;
	/** The log. */
	private Logger log								= Logger.getLogger(getClass());
	/** The singleton instance of this class. */    
	private static MetronomePanel panel 			= null;
	/** The metronome tempo label, which gets updated in real time. */
	public JLabel metronomeTempoLabel 				= new JLabel("");
	/** The player. */
	public MetronomePlayer player 					= null;
	private final MyMouseListener myMouseListener 	= new MyMouseListener();
	private final boolean LISTENER					= true;
	private final boolean NO_LISTENER				= false;
	public JLabel beginLabel						= null;
    public JLabel endLabel							= null;
    public int beginTempo							= 0;
	public int endTempo 							= 0;
    public int increment 							= 0;
	public int numBeats 							= 0;
    public Map<String, JLabel> tempoLabels			= new HashMap<String, JLabel>();
    public Map<String, JLabel> incrementLabels		= new HashMap<String, JLabel>();
    public Map<String, JLabel> numBeatsLabels		= new HashMap<String, JLabel>();
	
    /**
     * @return singleton instance of this class
     */
    public static MetronomePanel getInstance() {
        if (panel == null) {
        	panel = new MetronomePanel();
    	}
    	return panel;
    }
	
    /** Public constructor */
    public MetronomePanel() {
        setBackground(C[0]);
		setLayout(null);
		
    	int tempoStart 	= 50;
    	int tempoEnd 	= 250;
    	int tempoInc 	= 10;
    	int n = (tempoEnd - tempoStart) / tempoInc + 1;
    	Integer[] tempos = new Integer[n];
    	int index = 0;
    	for (int i = tempoStart; i <= tempoEnd; i += tempoInc) {
    		tempos[index++] = i;
    	}
    	
	    Integer[] numBeats		= {8, 16, 32};
	    
	    int x = 0;
	    int y = 0;
	    
	    //String text, String name, Color bg, Color fg, int x, int y, int w, int h, boolean addListener
	    // metronome label
	    add(getLabel("Metronome", null, C[6], C[16], x, y, W[3], W[1], NO_LISTENER));
	    x += W[3] + 1;	    
	    
	    // play/stop label
	    JLabel playStopLabel = getLabel(">", "playStop", C[12], C[0], x, y, W[1], W[1], LISTENER);
	    add(playStopLabel);
	    x += W[1] + 1;	    
	    
	    // begin label
	    beginLabel = getLabel("Begin", null, C[6], C[16], x, y, W[2], W[1], NO_LISTENER); 
	    add(beginLabel);
	    x += W[2] + 1;	    
	    log.debug("x=" + x);
	    
	    // begin labels
	    int dx = 0;
	    int dy = 0;
	    for (int tempo: tempos) {
	    	String name = "begin" + tempo;
	    	JLabel label = getLabel(null, name, C[8], null, x + dx, y + dy, 12, 12, LISTENER);  
	    	add(label);
	    	dx += 12 + 1;
	    	tempoLabels.put(name, label);
	    }
	    dx = 0;
	    dy += 12 + 1;
	    
	    // end labels
	    for (int tempo: tempos) {
	    	String name = "end" + tempo;
	    	JLabel label = getLabel(null, name, C[8], null, x + dx, y + dy, 12, 12, LISTENER);  
	    	add(label);
	    	dx += 12 + 1;
	    	tempoLabels.put(name, label);
	    }
	    x += dx;
	    
	    // end label
	    endLabel = getLabel("End", null, C[6], C[16], x, y, W[2], W[1], NO_LISTENER);
	    add(endLabel);
	    x += W[2] + 1;
	    
	    // increment label
	    add(getLabel("Increment", null, C[6], C[16], x, y, W[3], W[1], NO_LISTENER));
	    x += W[3] + 1;

	    // increment labels
	    dx = 0;
	    for (int i = 1; i <= 10; i++) {
	    	String name = "inc" + i;
	    	int w = (i == 10) ? W[1] : 12;
	    	JLabel label = getLabel("" + i, name, C[12], C[0], x + dx, y, w, W[1], LISTENER);  
	    	add(label);
	    	dx += w + 1;
	    	incrementLabels.put(name, label);
	    }
	    x += dx;
	    
	    // numBeats label
	    add(getLabel("NumBeats", null, C[6], C[16], x, y, W[3], W[1], NO_LISTENER));
	    x += W[3] + 1;

	    // numBeats labels
	    dx = 0;
	    for (int i = 0; i < numBeats.length; i++) {
	    	int val = numBeats[i];
	    	String name = "num" + val;
	    	int w = (val < 10) ? 12 : W[1];
	    	JLabel label = getLabel("" + val, name, C[12], C[0], x + dx, y, w, W[1], LISTENER);  
	    	add(label);
	    	dx += w + 1;
	    	numBeatsLabels.put(name, label);
	    }
	    x += dx;

	    
	    // tempo label
	    JLabel tempoLabel = getLabel("Tempo", "", C[6], C[16], x, y, W[2], W[1], NO_LISTENER);
	    add(tempoLabel);
	    x += W[2] + 1;

	    // tempo value label
	    JLabel tempoValueLabel = getLabel(null, "tempoValue", C[14], C[0], x, y, W[2], W[1], NO_LISTENER);
	    add(tempoValueLabel);
	    x += W[2] + 1;	    
	    
	    /*
	    // play button
	    final JButton playButton = new JButton("Play");
	    playButton.setBounds(x, y, W[4], W[1]);
	    playButton.setFont(FONT);
	    add(playButton);
	    x += playButton.getWidth() + SP;
	    
	    // stop button
	    final JButton stopButton = new JButton("Stop");
	    stopButton.setBounds(x, y, W[4], W[1]);
	    stopButton.setFont(FONT);
	    add(stopButton);
	    stopButton.setEnabled(false);
	    x += stopButton.getWidth() + SP;
	    
	    // begin label
	    JLabel beginLabel = new JLabel("Begin:");
	    beginLabel.setBounds(x, y, W[2], W[1]);
	    beginLabel.setFont(FONT);
	    add(beginLabel);
	    x += beginLabel.getWidth() + SP;
	    
	    // begin combo box
	    final JComboBox<Integer> beginTempoBox = new JComboBox<Integer>(tempos);
	    beginTempoBox.setBounds(x, y, W[3], W[1]);
	    beginTempoBox.setFont(FONT);
	    beginTempoBox.setSelectedItem(Config.getInt("metronome.beginTempoBox.default"));
	    add(beginTempoBox);
	    x += beginTempoBox.getWidth() + SP;
	    
	    // end label
	    JLabel endLabel = new JLabel("End:");
	    endLabel.setBounds(x, y, W[2], W[1]);
	    endLabel.setFont(FONT);
	    add(endLabel);
	    x += endLabel.getWidth() + SP;
	    
	    // end combo box
	    final JComboBox<Integer> endTempoBox = new JComboBox<Integer>(tempos);
	    endTempoBox.setBounds(x, y, W[3], W[1]);
	    endTempoBox.setFont(FONT);
	    endTempoBox.setSelectedItem(Config.getInt("metronome.endTempoBox.default"));
	    add(endTempoBox);
	    x += endTempoBox.getWidth() + SP;
	    
	    // increment label
	    JLabel incrementLabel = new JLabel("Increment:");
	    incrementLabel.setBounds(x, y, W[3], W[1]);
	    incrementLabel.setFont(FONT);
	    add(incrementLabel);
	    x += incrementLabel.getWidth() + SP;
	    
	    // increment combo box
	    final JComboBox<Integer> incrementBox = new JComboBox<Integer>(increments);
	    incrementBox.setBounds(x, y, W[3], W[1]);
	    incrementBox.setFont(FONT);
	    add(incrementBox);
	    x += incrementBox.getWidth() + SP;
	    
	    // tempo label
	    JLabel tempoLabel = new JLabel("Tempo:");
	    tempoLabel.setBounds(x, y, W[2], W[1]);
	    tempoLabel.setFont(FONT);
	    add(tempoLabel);
	    x += tempoLabel.getWidth() + SP;

	    // metronome tempo label
	    metronomeTempoLabel.setBounds(x, y, W[2], W[1]);
	    metronomeTempoLabel.setFont(FONT);
	    add(metronomeTempoLabel);
	    x += metronomeTempoLabel.getWidth() + SP;
	    
	    // numbeats label
	    JLabel numBeatsLabel = new JLabel("NumBeats:");
	    numBeatsLabel.setBounds(x, y, W[3], W[1]);
	    numBeatsLabel.setFont(FONT);
	    add(numBeatsLabel);
	    x += numBeatsLabel.getWidth() + SP;
	    
	    // numbeats combo box
	    final JComboBox<Integer> numBeatsBox = new JComboBox<Integer>(numBeats);
	    numBeatsBox.setBounds(x, y, W[3], W[1]);
	    numBeatsBox.setFont(FONT);
	    numBeatsBox.setSelectedItem(Config.getInt("metronome.numBeatsBox.default"));
	    add(numBeatsBox);
	    x += numBeatsBox.getWidth() + SP;

	    log.debug("final x=" + x);
	    */

	    
	    /* action listeners */
	    
	    /* play button listener
	    playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    // get current values
			    int beginTempo	= (Integer) beginTempoBox.getSelectedItem();
			    int endTempo 	= (Integer) endTempoBox.getSelectedItem();
			    int numBeats 	= (Integer) numBeatsBox.getSelectedItem();
			    int increment 	= (Integer) incrementBox.getSelectedItem();
  
				playButton.setEnabled(false);
				stopButton.setEnabled(true);
				
				//beatPanel.init(numBeats);
				
				player = new MetronomePlayer(
						beginTempo,
						endTempo,
						numBeats,
						increment,
						panel);
				player.start();
			}
		});

	    // stop button listener
	    stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				player.end();
				playButton.setEnabled(true);
				stopButton.setEnabled(false);
			}
		});
		*/
    }
    
    public JLabel getLabel(String text, String name, Color bg, Color fg, int x, int y, int w, int h, boolean addListener) {
    	JLabel label = (text == null) ? new JLabel() : new JLabel(text);
        if (text != null) {
        	label.setFont(FONT);
        	label.setHorizontalAlignment(JLabel.CENTER);
        }
    	label.setName(name);    	
        label.setBackground(bg);
    	if (fg != null) label.setForeground(fg);
        label.setBounds(x, y, w, h);
        if (addListener) {
        	label.addMouseListener(myMouseListener);
        }
        label.setOpaque(true);
        return label;
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
         	} else if (name.startsWith("num")) {
        		if (numBeats > 0) unselect(numBeatsLabels.get("num" + numBeats));
        		numBeats = Integer.parseInt(name.replace("num", ""));
         		select(l);
         	}
        }
    	/*
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
        */
        public void select(JLabel l) {
        	l.setBackground(C[4]);
        	l.setForeground(C[16]);
        }
        public void unselect(JLabel l) {
        	l.setBackground(C[12]);
        	l.setForeground(C[0]);        }

    }    
}
