package audio.chords.gui;

import static audio.Constants.BG_COLOR;
import static audio.Constants.BG_COLOR_BLUE;
import static audio.Constants.BG_COLOR_MED;
import static audio.Constants.CHORDS_FOLDER;
import static audio.Constants.CHORD_INSERT_PANEL_WIDTH;
import static audio.Constants.COMMA;
import static audio.Constants.DOUBLE_ROW_PANEL_HEIGHT;
import static audio.Constants.EXT_CHORDS;
import static audio.Constants.FONT;
import static audio.Constants.FS;
import static audio.Constants.MUSIC_DIR;
import static audio.Constants.NL;
import static audio.Constants.ROW_HEIGHT;
import static audio.Constants.SEPARATOR_HEIGHT;
import static audio.Constants.SINGLE_ROW_PANEL_HEIGHT;
import static audio.Constants.SP;
import static audio.Constants.WIDTH_2;
import static audio.Constants.WIDTH_3;
import static audio.Constants.WIDTH_4;
import static audio.Constants.WIDTH_9;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import audio.Config;
import audio.ExtensionFilter;
import audio.Util;
import audio.chords.abc.ChordsToAbcScales;

public class ChordFilePanel extends JPanel implements ChordPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 		= 1L;
	/** The log. */
	private Logger log								= Logger.getLogger(getClass());
	/** The singleton instance of this class. */    
	private static ChordFilePanel chordFilePanel 	= null;
	/** The update tempo label, which gets updated in real time. */
	public JLabel updateTempoLabel 					= new JLabel("");
    /** The message label */
	public JLabel messageLabel 						= new JLabel("");
	/** The player. */
	public ChordPlayer chordPlayer 					= null;
	private static final String[] UNPOPULATED_LIST  = {"----"};
    /** Play button. */
	private final JButton playButton 				= new JButton("Play");
    /** Stop button. */
	private final JButton stopButton 				= new JButton("Stop");
	/** Genre box. */
	private final JComboBox<String> genreBox 		= new JComboBox<String>();
	/** Folder box. */
	private final JComboBox<String> folderBox 		= new JComboBox<String>();
	/** Tune box. */
	private final JComboBox<String> tuneBox 		= new JComboBox<String>();
	/** The text area. */
	public JTextArea textArea 						= null;
	/** StatusPanel reference. */
	public StatusPanel statusPanel 					= null;
	/** DisplayPanel. */
	public DisplayPanel displayPanel 				= null;
	/** Transpose box. */
	private final JComboBox<String> transposeBox 	= new JComboBox<String>();
	/** Transpose checkBox. */
	public JCheckBox transposeCheckBox 				= new JCheckBox("Transpose");
	/** abcChords checkBox. */
	public JCheckBox abcChordsCheckBox 				= new JCheckBox("abcChords");
	/** abcScales checkBox. */
	public JCheckBox abcScalesCheckBox 				= new JCheckBox("abcScales");
	/** Array of genre names. */
	public static String[] genreNames				= null;
	
	static {
		String genreNamesProperty = Config.get("genreNames");
		genreNames = genreNamesProperty.split(COMMA);
	}
    
	/**
     * @return singleton instance of this class
     */
    public static ChordFilePanel getInstance() {
        if (chordFilePanel == null) {
        	chordFilePanel = new ChordFilePanel();
    	}
    	return chordFilePanel;
    }
	
    /** Private constructor */
    private ChordFilePanel() {
        setBackground(BG_COLOR);
		setLayout(null);
		
    	int tempoStart	= 40;
    	int tempoEnd 	= 250;
    	int tempoInc 	= 10;
    	
    	int n = (tempoEnd - tempoStart) / tempoInc + 1;
    	Integer[] tempos = new Integer[n + 1]; // +1 allows for initial vale of 0
    	tempos[0] = 0; // if set to this value then ignore it
    	
    	int index = 1;
    	for (int i = tempoStart; i <= tempoEnd; i += tempoInc) {
    		tempos[index++] = i;
    	}
    	
	    Integer[] increments = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	    String[] transposeKeys = {"C", "Db", "D", "Eb", "E", "F", "F#", "Gb", "G", "Ab", "A", "Bb", "B"};
	    
	    int x = 0;
	    int y = 0;
	    
	    final int FOLDER_WIDTH 	= 400;
	    final int TUNE_WIDTH 	= 395;
	    
	    // separator ///////////////////////////////////////////////////////////
	    JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
	    separator.setBounds(x, y, GuiController.w, SEPARATOR_HEIGHT);
	    add(separator);
	    y += separator.getHeight();
	    
	    // row 1 ///////////////////////////////////////////////////////////////	
	    
	    // chord label
	    JLabel chordFileLabel = new JLabel("ChordFile");
	    chordFileLabel.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    chordFileLabel.setFont(FONT);
	    add(chordFileLabel);
	    x += chordFileLabel.getWidth() + SP;
	    
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
	    
	    // save button
	    final JButton saveButton = new JButton("Save");
	    saveButton.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    saveButton.setFont(FONT);
	    add(saveButton);
	    x += saveButton.getWidth() + SP;

	    // saveAs button
	    final JButton saveAsButton = new JButton("Save As");
	    saveAsButton.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    saveAsButton.setFont(FONT);
	    add(saveAsButton);
	    x += saveAsButton.getWidth() + SP;
	    
	    // abc button
	    final JButton abcButton = new JButton("abc");
	    abcButton.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    abcButton.setFont(FONT);
	    add(abcButton);
	    x += abcButton.getWidth() + SP;
	    
	    // abcChords checkBox
	    abcChordsCheckBox.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    abcChordsCheckBox.setOpaque(false);
	    abcChordsCheckBox.setSelected(false);
	    abcChordsCheckBox.setFont(FONT);
	    add(abcChordsCheckBox);
	    x += abcChordsCheckBox.getWidth() + SP;
	    
	    // abcScales checkBox
	    abcScalesCheckBox.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    abcScalesCheckBox.setOpaque(false);
	    abcScalesCheckBox.setSelected(false);
	    abcScalesCheckBox.setFont(FONT);
	    add(abcScalesCheckBox);
	    x += abcScalesCheckBox.getWidth() + SP;
	    
	    // message label
	    messageLabel.setBounds(x, y, WIDTH_9 + 10, ROW_HEIGHT);
	    messageLabel.setBackground(BG_COLOR_MED);
	    messageLabel.setForeground(Color.WHITE);
	    messageLabel.setOpaque(true);
	    messageLabel.setFont(FONT);
	    add(messageLabel);
	    x += messageLabel.getWidth() + SP;
	    
	    log.debug("row 1 final x=" + x);
	    
	    // row 2 ///////////////////////////////////////////////////////////////
	    x = 0;
		y += ROW_HEIGHT + SP;  
	    
	    // genre label
	    JLabel genreLabel = new JLabel("Genre:");
	    genreLabel.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    genreLabel.setFont(FONT);
	    add(genreLabel);
	    x += genreLabel.getWidth() + SP;
		
	    // genre combo box
	    genreBox.setModel(new DefaultComboBoxModel<String>(genreNames));
		// create and register genreBox listener
		genreBox.addItemListener(new GenreBoxListener());
		genreBox.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
		genreBox.setFont(FONT);
		add(genreBox);
	    x += genreBox.getWidth() + SP;

		// folder label
	    JLabel folderLabel = new JLabel("Folder:");
	    folderLabel.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    folderLabel.setFont(FONT);
	    add(folderLabel);
	    x += folderLabel.getWidth() + SP;
	    
	    // init folder combo box with unpopulated list
	    folderBox.setModel(new DefaultComboBoxModel<String>(UNPOPULATED_LIST)); // the default
		// create and register folderBox listener
	    folderBox.addItemListener(new FolderBoxListener());
		folderBox.setBounds(x, y, FOLDER_WIDTH, ROW_HEIGHT);
		folderBox.setFont(FONT);
		add(folderBox);
	    x += folderBox.getWidth() + SP;

		// tune label
	    JLabel tuneLabel = new JLabel("Tune:");
	    tuneLabel.setBounds(x, y, WIDTH_2, ROW_HEIGHT);
	    tuneLabel.setFont(FONT);
	    add(tuneLabel);
	    x += tuneLabel.getWidth() + SP; 
	    
		// init tune combo box with unpopulated list
		tuneBox.setModel(new DefaultComboBoxModel<String>(UNPOPULATED_LIST));
		// create and register tuneBox listener
	    tuneBox.addItemListener(new TuneBoxListener());
		tuneBox.setBounds(x, y, TUNE_WIDTH, ROW_HEIGHT);
		tuneBox.setFont(FONT);
		add(tuneBox);
		x += tuneBox.getWidth() + SP;

	    // refresh button
	    final JButton refreshTuneButton = new JButton("Refresh");
	    refreshTuneButton.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    refreshTuneButton.setFont(FONT);
	    add(refreshTuneButton);
	    x += refreshTuneButton.getWidth() + SP;

	    // transpose combo box
	    transposeBox.setModel(new DefaultComboBoxModel<String>(transposeKeys));
		// create and register folderBox listener
	    transposeBox.setBounds(x, y, WIDTH_3, ROW_HEIGHT);
	    transposeBox.setFont(FONT);
		add(transposeBox);
	    x += transposeBox.getWidth() + SP;
	    
	    // transpose checkBox
	    transposeCheckBox.setBounds(x, y, WIDTH_4, ROW_HEIGHT);
	    transposeCheckBox.setOpaque(false);
	    transposeCheckBox.setSelected(false);
	    transposeCheckBox.setFont(FONT);
	    add(transposeCheckBox);
	    x += transposeCheckBox.getWidth() + SP;
	    
	    log.debug("row 2 final x=" + x);
		
	    // row 3 ///////////////////////////////////////////////////////////////

		x = 0;
		y += ROW_HEIGHT + SP;  
		
		//log.debug("GuiController.h=" + GuiController.h);
		int textAreaHeight 		= (int) ((GuiController.h - (SINGLE_ROW_PANEL_HEIGHT * 3) - DOUBLE_ROW_PANEL_HEIGHT - y) * 0.33);
		int textAreaWidth		= GuiController.w - CHORD_INSERT_PANEL_WIDTH;
		int displayPanelHeight 	= textAreaHeight * 2;
		int displayPanelWidth 	= textAreaWidth;
		
		textArea = new JTextArea();
		textArea.setBounds(
				x, 
				y, 
				textAreaWidth, 
				textAreaHeight);
		textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
		add(textArea);
		
	    displayPanel = new DisplayPanel();
	    displayPanel.setBounds(
				x, 
				y + textAreaHeight, 
				displayPanelWidth, 
				displayPanelHeight);
	    add(displayPanel);
		
	    x += textArea.getWidth() + SP;
	    
	    ChordInsertPanel chordInsertPanel = new ChordInsertPanel(textArea);
	    chordInsertPanel.setBounds(
				x, 
				y, 
				CHORD_INSERT_PANEL_WIDTH, 
				GuiController.h - (SINGLE_ROW_PANEL_HEIGHT * 2) - y - ROW_HEIGHT);
	    add(chordInsertPanel);
	    
	    /* action listeners */
	    
	    // play button listener
	    playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusPanel.clear();

				// get current values
			    int beginTempo		= (Integer) beginTempoBox.getSelectedItem();
			    int endTempo 		= (Integer) endTempoBox.getSelectedItem();
			    int increment 		= (Integer) incrementBox.getSelectedItem();
			    
				String genre		= (String) genreBox.getSelectedItem();
  
				String text 		= textArea.getText();
				
				playButton.setEnabled(false);
				stopButton.setEnabled(true);
				
				chordPlayer = new ChordPlayer(
							beginTempo,
							endTempo,
							increment,
							genre,
							text,
							chordFilePanel,
							displayPanel);
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
	    
	    // refresh button listener
	    refreshTuneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String genre		= (String) genreBox.getSelectedItem();
				String folderName	= (String) folderBox.getSelectedItem();
				
				updateTuneBox(genre, folderName);
			}
		});
	    
	    // save button listener
	    saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String genre		= (String) genreBox.getSelectedItem();
				String folderName	= (String) folderBox.getSelectedItem();
				String tuneName		= (String) tuneBox.getSelectedItem();
				String text 		= textArea.getText();
				
				//default icon, custom title
				int n = JOptionPane.showConfirmDialog(
				    null,
				    "Overwrite existing file: " + tuneName + "?",
				    "Warning",
				    JOptionPane.YES_NO_OPTION);
				if (n == 0) {
					save(genre, folderName, tuneName, text);
					log.debug("save " + genre + NL + folderName + NL + tuneName + NL + text);
				}
			}
		});
	    
	    // save button listener
	    saveAsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String genre		= (String) genreBox.getSelectedItem();
				String folderName	= (String) folderBox.getSelectedItem();
				String tuneName		= (String) tuneBox.getSelectedItem();
				String text 		= textArea.getText();

			    String s = (String) JOptionPane.showInputDialog(
						null,
						"File Name",
						"Save As ...",
						JOptionPane.PLAIN_MESSAGE,
						null,
						null,
						tuneName);

				if ((s != null) && (s.length() > 0)) {
					if (s.endsWith(EXT_CHORDS)) {
						s.replace(EXT_CHORDS, "");
					}
				    save(	genre,
				    		folderName,
				    		s,
				    		text);
				} 
			}
		});
	    
	    // abc button listener
	    abcButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String genre		= (String) genreBox.getSelectedItem();
				String folderName	= (String) folderBox.getSelectedItem();
				String tuneName		= (String) tuneBox.getSelectedItem();
				// default value will generate an empty abc chart with just the chord symbols notated
				String mode 		= "none";  
				
				if (abcChordsCheckBox.isSelected() && abcScalesCheckBox.isSelected()) {
					mode = "all";
				} else if (abcChordsCheckBox.isSelected()) {
					mode = "chords";
				} else if (abcScalesCheckBox.isSelected()) {
					mode = "scales";
				} else {
					// default 'none'
				}

				new ChordsToAbcScales().run(
						new File(MUSIC_DIR,	genre + CHORDS_FOLDER + FS + folderName + FS + tuneName + EXT_CHORDS), 
						mode);
			}
		});
	    
	    textArea.addKeyListener( new KeyListener() {
	        public void keyPressed(KeyEvent keyEvent) {
	        }
	        public void keyReleased(KeyEvent keyEvent) {
	        }
	        public void keyTyped(KeyEvent keyEvent) {
	        	setTuneUpdated();
	        }
	    });

    }
    
    public void setTuneUpdated() {
    	messageLabel.setBackground(BG_COLOR_BLUE);
    	messageLabel.setText("The current tune has been updated");
    }
    
    public void clearTuneUpdated() {
    	messageLabel.setBackground(BG_COLOR_MED);
    	messageLabel.setText("");    	
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

		statusPanel.setText(msg);
    }
    
	/**
	 * Update the folderBox.
	 * 
	 * @param genreName
	 * @param folderName
	 */
	public void updateFolderBox(String genreName) {
		List<String> dirNames = Util.getDirNames(getGenreDir(genreName));
		
		String[] folderNames = new String[dirNames.size()];
		int i = 0;
		for(String dirName: dirNames) {
			folderNames[i++] = dirName;
		}

		
	    folderBox.setModel(new DefaultComboBoxModel<String>(folderNames));
	    
	    String folderName = folderNames[0];
	    log.debug("folderName " + folderName + " selected");
	    
	    updateTuneBox(genreName, folderName);
	}
    
    
	/**
	 * Update the tuneBox.
	 * 
	 * @param genreName
	 * @param folderName
	 */
	public void updateTuneBox(String genreName, String folderName) {
		File dir = new File(MUSIC_DIR, genreName + CHORDS_FOLDER + FS + folderName);
		File[] files = dir.listFiles(new ExtensionFilter(EXT_CHORDS));
		
		String[] tuneNames = new String[files.length];
		
		int i = 0;
		for (File file: files) {
			tuneNames[i++] = file.getName().replace(EXT_CHORDS, "");
		}

		tuneBox.setModel(new DefaultComboBoxModel<String>(tuneNames));
		setTextArea(genreName, folderName, tuneNames[0]);
	}

	/**
	 * @param genre
	 * @return the genreDir based on the genre name
	 */
	private static File getGenreDir(String genreName) { 
		return new File(MUSIC_DIR, genreName.toLowerCase() + CHORDS_FOLDER);
	}
	
	/**
	 * @param genreName
	 * @param folderName
	 * @param tuneName
	 * @return tuneFile
	 */
	private static File getTuneFile(String genreName, String folderName, String tuneName) { 
		return new File(getGenreDir(genreName), folderName + FS + tuneName + EXT_CHORDS);
	}
	
	/**
	 * @param genreName
	 * @param folderName
	 * @param tuneName
	 */
	private void setTextArea(String genreName, String folderName, String tuneName) {
		File tuneFile = getTuneFile(genreName, folderName, tuneName);
		textArea.setText(Util.getText(tuneFile));
		clearTuneUpdated();
	} 
	
	/**
	 * Save tune to tuneFile.
	 * 
	 * @param genreName
	 * @param folderName
	 * @param tuneName
	 * @param text
	 */
	private void save(String genreName, String folderName, String tuneName, String text) {
		File tuneFile = getTuneFile(genreName, folderName, tuneName);
		Util.writeToFile(tuneFile, text);
	} 

	/**
	 * Listener for the genre box. When the selection is changed the folder  
	 * box and the tune box are updated.
	 */
	class GenreBoxListener implements ItemListener {
	    // this method is called only if a new item has been selected.
	    public void itemStateChanged(ItemEvent event) {
	        if (event.getStateChange() == ItemEvent.SELECTED) {
	        	String genreName = event.getItem().toString();
	        	log.debug("GenreBoxListener: genre " + genreName + " selected");

	        	updateFolderBox(genreName);
	        }
	    }
	}

	/**
	 * Listener for the folder box. When the selection is changed the tune box 
	 * is updated.
	 */
	class FolderBoxListener implements ItemListener {
	    // this method is called only if a new item has been selected.
	    public void itemStateChanged(ItemEvent event) {
	        if (event.getStateChange() == ItemEvent.SELECTED) {
	        	String genre	= (String) genreBox.getSelectedItem();
	        	log.debug("FolderBoxListener: genre " + genre + " selected");

	        	String folderName = event.getItem().toString();
	        	log.debug("FolderBoxListener: folderName " + folderName + " selected");

	    	    updateTuneBox(genre, folderName);
	        }
	    }
	}
	
	/**
	 * Listener for the tune box. When the selection is changed the text area
	 * is updated.
	 */
	class TuneBoxListener implements ItemListener {
	    // this method is called only if a new item has been selected.
	    public void itemStateChanged(ItemEvent event) {
	        if (event.getStateChange() == ItemEvent.SELECTED) {
	        	String genreName = (String) genreBox.getSelectedItem();
	        	log.debug("TuneBoxListener: genre " + genreName + " selected");

	        	String folderName = (String) folderBox.getSelectedItem();
	        	log.debug("TuneBoxListener: folderName " + folderName + " selected");

	        	String tuneName = event.getItem().toString();
	        	log.debug("TuneBoxListener: tuneName " + tuneName + " selected");
        		setTextArea(genreName, folderName, tuneName);	
	        }
	    }
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
	}
	
	/* (non-Javadoc)
	 * @see audio.chords.gui.ChordPanel#updateTranspose(java.lang.String)
	 */
	public void updateMessage(String message) {
    	messageLabel.setBackground(BG_COLOR_BLUE);
    	messageLabel.setText(message);
	}

	/* (non-Javadoc)
	 * @see audio.chords.gui.ChordPanel#getTransposeTo()
	 */
	public String getTransposeTo() {
		if (transposeCheckBox.isSelected()) {
			return (String) transposeBox.getSelectedItem();
		}
		return "";
	}
	
	/**
	 * Init the drop-downs.
	 */
	public void init() {
		genreBox.setSelectedItem(Config.get("chordFile.GenreName.default"));
		folderBox.setSelectedItem(Config.get("chordFile.FolderName.default"));
		tuneBox.setSelectedItem(Config.get("chordFile.TuneName.default"));
	}
}

