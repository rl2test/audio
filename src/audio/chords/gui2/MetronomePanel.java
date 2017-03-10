package audio.chords.gui2;

import static audio.Constants.C;
import static audio.Constants.FONT;
import static audio.Constants.ROW_HEIGHT;
import static audio.Constants.SP;
import static audio.Constants.W;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import audio.Config;

public class MetronomePanel extends JPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 	= 1L;
	/** The log. */
	private Logger log							= Logger.getLogger(getClass());
	/** The singleton instance of this class. */    
	private static MetronomePanel panel 		= null;
	/** The metronome tempo label, which gets updated in real time. */
	public JLabel metronomeTempoLabel 			= new JLabel("");
	/** The player. */
	public MetronomePlayer player 				= null;
	
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
        setBackground(C[10]);
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
	    Integer[] increments 	= {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	    
	    int x = 0;
	    int y = 0;
	    
	    // row 1 ///////////////////////////////////////////////////////////////	
	    
	    // metronome label
	    JLabel metronomeLabel = new JLabel("Metronome");
	    metronomeLabel.setBounds(x, y, W[3], ROW_HEIGHT);
	    metronomeLabel.setFont(FONT);
	    add(metronomeLabel);
	    x += metronomeLabel.getWidth() + SP;

	    // play button
	    final JButton playButton = new JButton("Play");
	    playButton.setBounds(x, y, W[4], ROW_HEIGHT);
	    playButton.setFont(FONT);
	    add(playButton);
	    x += playButton.getWidth() + SP;
	    
	    // stop button
	    final JButton stopButton = new JButton("Stop");
	    stopButton.setBounds(x, y, W[4], ROW_HEIGHT);
	    stopButton.setFont(FONT);
	    add(stopButton);
	    stopButton.setEnabled(false);
	    x += stopButton.getWidth() + SP;
	    
	    // begin label
	    JLabel beginLabel = new JLabel("Begin:");
	    beginLabel.setBounds(x, y, W[2], ROW_HEIGHT);
	    beginLabel.setFont(FONT);
	    add(beginLabel);
	    x += beginLabel.getWidth() + SP;
	    
	    // begin combo box
	    final JComboBox<Integer> beginTempoBox = new JComboBox<Integer>(tempos);
	    beginTempoBox.setBounds(x, y, W[3], ROW_HEIGHT);
	    beginTempoBox.setFont(FONT);
	    beginTempoBox.setSelectedItem(Config.getInt("metronome.beginTempoBox.default"));
	    add(beginTempoBox);
	    x += beginTempoBox.getWidth() + SP;
	    
	    // end label
	    JLabel endLabel = new JLabel("End:");
	    endLabel.setBounds(x, y, W[2], ROW_HEIGHT);
	    endLabel.setFont(FONT);
	    add(endLabel);
	    x += endLabel.getWidth() + SP;
	    
	    // end combo box
	    final JComboBox<Integer> endTempoBox = new JComboBox<Integer>(tempos);
	    endTempoBox.setBounds(x, y, W[3], ROW_HEIGHT);
	    endTempoBox.setFont(FONT);
	    endTempoBox.setSelectedItem(Config.getInt("metronome.endTempoBox.default"));
	    add(endTempoBox);
	    x += endTempoBox.getWidth() + SP;
	    
	    // increment label
	    JLabel incrementLabel = new JLabel("Increment:");
	    incrementLabel.setBounds(x, y, W[3], ROW_HEIGHT);
	    incrementLabel.setFont(FONT);
	    add(incrementLabel);
	    x += incrementLabel.getWidth() + SP;
	    
	    // increment combo box
	    final JComboBox<Integer> incrementBox = new JComboBox<Integer>(increments);
	    incrementBox.setBounds(x, y, W[3], ROW_HEIGHT);
	    incrementBox.setFont(FONT);
	    add(incrementBox);
	    x += incrementBox.getWidth() + SP;
	    
	    // tempo label
	    JLabel tempoLabel = new JLabel("Tempo:");
	    tempoLabel.setBounds(x, y, W[2], ROW_HEIGHT);
	    tempoLabel.setFont(FONT);
	    add(tempoLabel);
	    x += tempoLabel.getWidth() + SP;

	    // metronome tempo label
	    metronomeTempoLabel.setBounds(x, y, W[2], ROW_HEIGHT);
	    metronomeTempoLabel.setFont(FONT);
	    add(metronomeTempoLabel);
	    x += metronomeTempoLabel.getWidth() + SP;
	    
	    // numbeats label
	    JLabel numBeatsLabel = new JLabel("NumBeats:");
	    numBeatsLabel.setBounds(x, y, W[3], ROW_HEIGHT);
	    numBeatsLabel.setFont(FONT);
	    add(numBeatsLabel);
	    x += numBeatsLabel.getWidth() + SP;
	    
	    // numbeats combo box
	    final JComboBox<Integer> numBeatsBox = new JComboBox<Integer>(numBeats);
	    numBeatsBox.setBounds(x, y, W[3], ROW_HEIGHT);
	    numBeatsBox.setFont(FONT);
	    numBeatsBox.setSelectedItem(Config.getInt("metronome.numBeatsBox.default"));
	    add(numBeatsBox);
	    x += numBeatsBox.getWidth() + SP;

	    log.debug("final x=" + x);
	    
	    /* action listeners */
	    
	    // play button listener
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
    }    
}
