package audio.metronome;

import static audio.Constants.BG_COLOR;
import static audio.Constants.ROW_HEIGHT;
import static audio.Constants.SEPARATOR_HEIGHT;
import static audio.Constants.SP;
import static audio.Constants.WIDTH_2;
import static audio.Constants.WIDTH_3;
import static audio.Constants.WIDTH_4;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

public class MetronomePanel extends JPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 	= 1L;
	/** The log. */
	private Logger log							= Logger.getLogger(getClass());
	/** The singleton instance of this class. */    
	private static MetronomePanel panel 		= null;
	/** The beat panel, which gets updated in real time. */
	public MetronomeBeatPanel beatPanel 		= new MetronomeBeatPanel();
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
        setBackground(BG_COLOR);
		setLayout(null);
		
    	int tempoStart 	= 50;
    	int tempoEnd 	= 200;
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
	   
	    // separator ///////////////////////////////////////////////////////////
	    JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
	    separator.setBounds(x, y, Metronome.w, SEPARATOR_HEIGHT);
	    add(separator);
	    y += separator.getHeight();
	    
	    // row 1 ///////////////////////////////////////////////////////////////	
	    
	    // metronome label
	    JLabel metronomeLabel = new JLabel("Metronome");
	    metronomeLabel.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    add(metronomeLabel);
	    x += metronomeLabel.getWidth() + SP;

	    // play button
	    final JButton playButton = new JButton("Play");
	    playButton.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    add(playButton);
	    x += playButton.getWidth() + SP;
	    
	    // stop button
	    final JButton stopButton = new JButton("Stop");
	    stopButton.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    add(stopButton);
	    stopButton.setEnabled(false);
	    x += stopButton.getWidth() + SP;
	    
	    // begin label
	    JLabel beginLabel = new JLabel("Begin:");
	    beginLabel.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    add(beginLabel);
	    x += beginLabel.getWidth() + SP;
	    
	    // begin combo box
	    final JComboBox beginTempoBox = new JComboBox(tempos);
	    beginTempoBox.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    add(beginTempoBox);
	    x += beginTempoBox.getWidth() + SP;
	    
	    // end label
	    JLabel endLabel = new JLabel("End:");
	    endLabel.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    add(endLabel);
	    x += endLabel.getWidth() + SP;
	    
	    // end combo box
	    final JComboBox endTempoBox = new JComboBox(tempos);
	    endTempoBox.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    add(endTempoBox);
	    x += endTempoBox.getWidth() + SP;
	    
	    // increment label
	    JLabel incrementLabel = new JLabel("Increment:");
	    incrementLabel.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    add(incrementLabel);
	    x += incrementLabel.getWidth() + SP;
	    
	    // increment combo box
	    final JComboBox incrementBox = new JComboBox(increments);
	    incrementBox.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    add(incrementBox);
	    x += incrementBox.getWidth() + SP;
	    
	    log.debug("row 1 final x=" + x);
	    
	    // row 2 ///////////////////////////////////////////////////////////////
	    x = 0;
		y += ROW_HEIGHT + SP;  
	    
	    // tempo label
	    JLabel tempoLabel = new JLabel("Tempo:");
	    tempoLabel.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    add(tempoLabel);
	    x += tempoLabel.getWidth() + SP;

	    // metronome tempo label
	    metronomeTempoLabel.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    add(metronomeTempoLabel);
	    x += metronomeTempoLabel.getWidth() + SP;
	    
	    log.debug("row 2 final x=" + x);
	    
	    // row 3 ///////////////////////////////////////////////////////////////
	    x = 0;
		y += ROW_HEIGHT + SP;  

	    // numbeats label
	    JLabel numBeatsLabel = new JLabel("NumBeats:");
	    numBeatsLabel.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    add(numBeatsLabel);
	    x += numBeatsLabel.getWidth() + SP;
	    
	    // numbeats combo box
	    final JComboBox numBeatsBox = new JComboBox(numBeats);
	    numBeatsBox.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    add(numBeatsBox);
	    x += numBeatsBox.getWidth() + SP;

	    // the beat panel
	    beatPanel.setBounds(x, y, (10 * 32 + 1), ROW_HEIGHT);
	    add(beatPanel);
	    x += beatPanel.getWidth() + SP;
	    
	    log.debug("row 3 final x=" + x);
	    
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
				
				beatPanel.init(numBeats);
				
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
