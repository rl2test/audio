package audio.chords.gui;

import static audio.Constants.BG_COLOR;
import static audio.Constants.BG_COLOR_MED;
import static audio.Constants.FONT;
import static audio.Constants.ROW_HEIGHT;
import static audio.Constants.SEPARATOR_HEIGHT;
import static audio.Constants.SP;
import static audio.Constants.WIDTH_2;
import static audio.Constants.WIDTH_3;
import static audio.Constants.WIDTH_4;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import audio.Config;

public class ChordTextPanel extends JPanel implements ChordPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 		= 1L;
	/** The log. */
	private Logger log								= Logger.getLogger(getClass());
	/** The singleton instance of this class. */    
	private static ChordTextPanel chordTextPanel 	= null;
	/** The update tempo label, which gets updated in real time. */
	public JLabel updateTempoLabel 					= new JLabel("");
	/** The chord label, which gets updated in real time. */
	public JLabel updateChordLabel 					= new JLabel("");
	/** The player. */
	public ChordPlayer chordPlayer 					= null;
    /** Play button. */
	private final JButton playButton 				= new JButton("Play");
    /** Stop button. */
	private final JButton stopButton 				= new JButton("Stop");
	public JTextField textField 					= null;
	/** StatusPanel reference. */
	public StatusPanel statusPanel 					= null;
 
    /**
     * @return singleton instance of this class
     */
    public static ChordTextPanel getInstance() {
        if (chordTextPanel == null) {
        	chordTextPanel = new ChordTextPanel();
    	}
    	return chordTextPanel;
    }
	
    /** Private constructor */
    private ChordTextPanel() {
        setBackground(BG_COLOR);
		setLayout(null);
		
    	int tempoStart	= 60;
    	int tempoEnd 	= 240;
    	int tempoInc 	= 10;
    	
    	int n = (tempoEnd - tempoStart) / tempoInc + 1;
    	Integer[] tempos = new Integer[n + 1]; // +1 allows for initial vale of 0
    	tempos[0] = 0; // if set to this value then ignore it
    	
    	int index = 1;
    	for (int i = tempoStart; i <= tempoEnd; i += tempoInc) {
    		tempos[index++] = i;
    	}
    	
	    Integer[] increments = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	    
	    int x = 0;
	    int y = 0;
	    
	    // separator ///////////////////////////////////////////////////////////
	    JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
	    separator.setBounds(x, y, GuiController.w, SEPARATOR_HEIGHT);
	    add(separator);
	    y += separator.getHeight();
	    
	    // row 1 ///////////////////////////////////////////////////////////////	
	    
	    // chord label
	    JLabel chordTextLabel = new JLabel("ChordText");
	    chordTextLabel.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    chordTextLabel.setFont(FONT);
	    add(chordTextLabel);
	    x += chordTextLabel.getWidth() + SP;

//	    JLabel chordLabel = new JLabel("Chord");
//	    chordLabel.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
//	    add(chordLabel);
//	    x += chordLabel.getWidth() + SP;

	    // play button, in class declaration
	    playButton.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    playButton.setFont(FONT);
	    add(playButton);
	    x += playButton.getWidth() + SP;
	    
	    // stop button, in class declaration
	    stopButton.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    stopButton.setFont(FONT);
	    add(stopButton);
	    stopButton.setEnabled(false);
	    x += stopButton.getWidth() + SP;
	    
	    // begin tempo label
	    JLabel beginTempoLabel = new JLabel("Begin:");
	    beginTempoLabel.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    beginTempoLabel.setFont(FONT);
	    add(beginTempoLabel);
	    x += beginTempoLabel.getWidth() + SP;
	    
	    // begin tempo combo box
	    final JComboBox<Integer> beginTempoBox = new JComboBox<Integer>(tempos);
	    beginTempoBox.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    beginTempoBox.setFont(FONT);
	    beginTempoBox.setSelectedItem(Config.getInt("chordText.beginTempoBox.default"));
	    add(beginTempoBox);
	    x += beginTempoBox.getWidth() + SP;
	    
	    // end tempo label
	    JLabel endTempoLabel = new JLabel("End:");
	    endTempoLabel.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    endTempoLabel.setFont(FONT);
	    add(endTempoLabel);
	    x += endTempoLabel.getWidth() + SP;
	    
	    // end tempo combo box
	    final JComboBox<Integer> endTempoBox = new JComboBox<Integer>(tempos);
	    endTempoBox.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    endTempoBox.setFont(FONT);
	    endTempoBox.setSelectedItem(Config.getInt("chordText.endTempoBox.default"));
	    add(endTempoBox);
	    x += endTempoBox.getWidth() + SP;
	    
	    // increment label
	    JLabel incrementLabel = new JLabel("Increment:");
	    incrementLabel.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    incrementLabel.setFont(FONT);
	    add(incrementLabel);
	    x += incrementLabel.getWidth() + SP;
	    
	    // increment tempo combo box
	    final JComboBox<Integer> incrementBox = new JComboBox<Integer>(increments);
	    incrementBox.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    incrementBox.setFont(FONT);
	    add(incrementBox);
	    x += incrementBox.getWidth() + SP;
	    
	    // tempo label
	    JLabel tempoLabel = new JLabel("Tempo:");
	    tempoLabel.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    tempoLabel.setFont(FONT);
	    add(tempoLabel);
	    x += tempoLabel.getWidth() + SP;
	    
	    // chord tempo label
	    updateTempoLabel.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    updateTempoLabel.setBackground(BG_COLOR_MED);
	    updateTempoLabel.setOpaque(true);
	    updateTempoLabel.setFont(FONT);
	    add(updateTempoLabel);
	    x += updateTempoLabel.getWidth() + SP;
	    
	    // tempo label
	    JLabel chordLabel = new JLabel("Chord:");
	    chordLabel.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    chordLabel.setFont(FONT);
	    add(chordLabel);
	    x += chordLabel.getWidth() + SP;
	    
	    // update chord label
	    updateChordLabel.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    updateChordLabel.setBackground(BG_COLOR_MED);
	    updateChordLabel.setOpaque(true);
	    updateChordLabel.setFont(FONT);
	    add(updateChordLabel);
	    x += updateChordLabel.getWidth() + SP;

	    log.debug("row 1 final x=" + x);
	    
	    // row 2 ///////////////////////////////////////////////////////////////

	    x = 0;
		y += ROW_HEIGHT + SP;  
		
		// text label
	    JLabel textLabel = new JLabel("Text:");
	    textLabel.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    textLabel.setFont(FONT);
	    add(textLabel);
	    x += textLabel.getWidth() + SP; 

		textField 				= new JTextField(80);
		textField.setBounds(
				x, 
				y, 
				460, 
				ROW_HEIGHT);
		textField.setFont(new Font("Courier New", Font.PLAIN, 12));
	    add(textField);
	    x += textField.getWidth() + SP;
		
	    /* action listeners ***************************************************/
	    
	    // play button listener
	    playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusPanel.clear();
				
				// get current values
			    int beginTempo		= (Integer) beginTempoBox.getSelectedItem();
			    int endTempo 		= (Integer) endTempoBox.getSelectedItem();
			    int increment 		= (Integer) incrementBox.getSelectedItem();
			    
				String genre		= ""; // TODO (String) genreBox.getSelectedItem();
  
				String text = textField.getText();
				
				playButton.setEnabled(false);
				stopButton.setEnabled(true);
				
				chordPlayer = new ChordPlayer(
							beginTempo,
							endTempo,
							increment,
							genre,
							text,
							chordTextPanel,
							null);
				chordPlayer.start();
			}
		});

	    // stop button listener
	    stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chordPlayer.destroyPlayer();
				chordPlayer = null;

				playButton.setEnabled(true);
				stopButton.setEnabled(false);
			}
		});
    }
    
    /**
     * This method is called when the chord player throws an exception.
     * 
     * @param msg
     */
    public void stop(String msg) {
		chordPlayer.destroyPlayer();
		chordPlayer = null;

		playButton.setEnabled(true);
		stopButton.setEnabled(false);

		log.debug("msg=" + msg);

		// TODO statusLabel.setText(msg);
    }
    
	/* (non-Javadoc)
	 * @see audio.chords.gui.ChordPanel#updateTempo(java.lang.String)
	 */
	public void updateTempo(String tempo) {
		updateTempoLabel.setText(tempo);
	}

	/* (non-Javadoc)
	 * @see audio.chords.gui.ChordPanel#updateChord(java.lang.String)
	 */
	public void updateChord(String chord) {
		updateChordLabel.setText(chord);
	}
	
	/* (non-Javadoc)
	 * @see audio.chords.gui.ChordPanel#updateTranspose(java.lang.String)
	 */
	public void updateMessage(String message) {
	}
	
	/* (non-Javadoc)
	 * @see audio.chords.gui.ChordPanel#getTransposeTo()
	 */
	public String getTransposeTo() {
		return "";
	}
}
