package audio.chords.gui;

import static audio.Constants.C;
import static audio.Constants.CHORDS_FOLDER;
import static audio.Constants.EXT_CHORDS;
import static audio.Constants.FONT;
import static audio.Constants.FS;
import static audio.Constants.GENRE_NAMES;
import static audio.Constants.MUSIC_DIR;
import static audio.Constants.NL;
import static audio.Constants.W;
import static audio.Constants.GENRE_NAME;
import static audio.Constants.FOLDER_NAME;
import static audio.Constants.TUNE_NAME;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

//import audio.Config;
import audio.ExtensionFilter;
import audio.Util;

public class FilePanel extends AudioPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 		= 1L;
	/** The singleton instance of this class. */    
	private static FilePanel filePanel 				= null;
	private final FileListener listener 			= new FileListener();
	/** The player. */
	public ChordPlayer chordPlayer 					= null;
	private static final String[] UNPOPULATED_LIST  = {"----"};
	/** Genre box. */
	private final JComboBox<String> genreBox 		= new JComboBox<String>();
	/** Folder box. */
	private final JComboBox<String> folderBox 		= new JComboBox<String>();
	/** Tune box. */
	private final JComboBox<String> tuneBox 		= new JComboBox<String>();
	/** The text area. */
	public JTextArea textArea 						= null;
	public boolean playing							= false;
	
	/**
     * @return singleton instance of this class
     */
    public static FilePanel getInstance(AudioController ac) throws Exception {
        if (filePanel == null) {
        	filePanel = new FilePanel(ac);
    	}
    	return filePanel;
    }
	
    /** Private constructor */
    private FilePanel(AudioController ac) throws Exception {
        super(ac);
	    
		// genre
		add(getLabel("Genre", null, C[6], C[16], x, y, W[2], W[1], null));
	    x += W[2] + 1;

	    // genre combo box
	    genreBox.setModel(new DefaultComboBoxModel<String>(GENRE_NAMES));
		genreBox.addItemListener(new GenreBoxListener());
		genreBox.setBounds(x, y, W[4], W[1]);
		genreBox.setFont(FONT);
		add(genreBox);
		// genre combo box bg label - note: paints BEFORE preceding element
		add(getLabel(null, null, C[6], null, x, y, W[4], W[1], null));
	    x += W[4] + 1;

	    // folder
		add(getLabel("Folder", null, C[6], C[16], x, y, W[2], W[1], null));
	    x += W[2] + 1;
	    
	    // init folder combo box with unpopulated list
	    folderBox.setModel(new DefaultComboBoxModel<String>(UNPOPULATED_LIST));
	    folderBox.addItemListener(new FolderBoxListener());
		folderBox.setBounds(x, y, W[16], W[1]);
		folderBox.setFont(FONT);
		add(folderBox);
		// folder combo box bg label - note: paints BEFORE preceding element
		add(getLabel(null, null, C[6], null, x, y, W[16], W[1], null));
	    x += W[16] + 1;

		// tune label
		add(getLabel("Tune", null, C[6], C[16], x, y, W[2], W[1], null));
	    x += W[2] + 1;
	    
		// init tune combo box with unpopulated list
		tuneBox.setModel(new DefaultComboBoxModel<String>(UNPOPULATED_LIST));
		// create and register tuneBox listener
	    tuneBox.addItemListener(new TuneBoxListener());
		tuneBox.setBounds(x, y, W[16], W[1]);
		tuneBox.setFont(FONT);
		add(tuneBox);
		// tune combo box bg label - note: paints BEFORE preceding element
		add(getLabel(null, null, C[6], null, x, y, W[16], W[1], null));
	    x += W[16] + 1;

	    // play/stop
	    add(getLabel("Play", "playStop", C[6], C[16], x, y, W[2], W[1], listener));
	    x += W[2] + 1;
	    
	    // save
	    add(getLabel("Save", "save", C[6], C[16], x, y, W[2], W[1], listener));
	    x += W[2] + 1;

	    // save as
	    add(getLabel("Save as", "saveAs", C[6], C[16], x, y, W[3], W[1], listener));
	    x += W[3] + 1;

	    // refresh
		add(getLabel("Refresh", "refresh", C[6], C[16], x, y, W[2], W[1], listener));
	    x += W[2] + 1;

	    // row 2 ///////////////////////////////////////////////////////////////
		x = 0;
		y += W[1];  
		
		int textAreaHeight 		= (int) ((AudioController.h - (2 * (W[1] + 1) + y)));
		int textAreaWidth		= (int) (AudioController.w  * 0.3);
		int displayPanelHeight 	= textAreaHeight;
		int displayPanelWidth 	= (int) (AudioController.w  * 0.7);
		
		textArea = new JTextArea();
		textArea.setBounds(
				x, 
				y, 
				textAreaWidth, 
				textAreaHeight);
		textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
		add(textArea);
		
		DisplayPanel displayPanel = new DisplayPanel();
	    displayPanel.setBounds(
				x + textAreaWidth, 
				y, 
				displayPanelWidth, 
				displayPanelHeight);
	    add(displayPanel);
	    ac.displayPanel = displayPanel;
		
	    x += textArea.getWidth();
	    
	    textArea.addKeyListener( new KeyListener() {
	        public void keyPressed(KeyEvent keyEvent) {
	        }
	        public void keyReleased(KeyEvent keyEvent) {
	        }
	        public void keyTyped(KeyEvent keyEvent) {
	        	//setTuneUpdated();
	        }
	    });

		genreBox.setSelectedItem(GENRE_NAME);
		folderBox.setSelectedItem(FOLDER_NAME);
		tuneBox.setSelectedItem(TUNE_NAME);
    }
    
    /*
    public void setTuneUpdated() {
    	messageLabel.setBackground(Color.BLUE);
    	messageLabel.setText("The current tune has been updated");
    }
    
    public void clearTuneUpdated() {
    	messageLabel.setBackground(C[12]);
    	messageLabel.setText("");    	
    }
    */
    
    /**
     * This method is called when the chord player throws an exception.
     * 
     * @param msg
     */
    public void stop(String msg) {
		chordPlayer.destroyPlayer();
		chordPlayer = null;

		log.debug("msg=" + msg);
    }
    
	/**
	 * Update the folderBox.
	 * 
	 * @param genreName
	 * @param folderName
	 */
	public void updateFolderBox(String genreName) {
		log.debug("genreName=" + genreName);
		File f = getGenreDir(genreName);
		log.debug("f.getpath()=" + f.getPath());
		
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

		Arrays.sort(tuneNames);
		
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
		//clearTuneUpdated();
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
	 * @see audio.chords.gui.ChordPanel#getTransposeTo()
	 */
	/*public String getTransposeTo() {
		if (transposeCheckBox.isSelected()) {
			return (String) transposeBox.getSelectedItem();
		}
		return "";
	}*/
	
	/**
	 * Init the drop-downs.
	 */
	public void init() {
		//genreBox.setSelectedItem(GENRE_NAME);
		//folderBox.setSelectedItem(FOLDER_NAME);
		//tuneBox.setSelectedItem(TUNE_NAME);
	}
	
    private class FileListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            JLabel l = (JLabel) e.getSource();
            String name = l.getName();
            log.debug("name=" + name);
            
            if (name.equals("playStop")) {
            	if (playing) {
    				chordPlayer.destroyPlayer();
    				chordPlayer = null;
    				playing = false;
    				l.setText("Play");
            	} else {
					String text = textArea.getText();
					chordPlayer = new ChordPlayer(text, ac);
					chordPlayer.start();
					playing = true;
					l.setText("Stop");
            	}	
            } else if (name.equals("refresh")) {
            	String genre		= (String) genreBox.getSelectedItem();
            	String folderName	= (String) folderBox.getSelectedItem();
            	updateTuneBox(genre, folderName);
            } else if (name.equals("save")) {
            	String genre		= (String) genreBox.getSelectedItem();
				String folderName	= (String) folderBox.getSelectedItem();
				String tuneName		= (String) tuneBox.getSelectedItem();
				String text 		= textArea.getText();
				
				int n = JOptionPane.showConfirmDialog(
				    null,
				    "Overwrite existing file: " + tuneName + "?",
				    "Warning",
				    JOptionPane.YES_NO_OPTION);
				if (n == 0) {
					save(genre, folderName, tuneName, text);
					log.debug("save " + NL + genre + NL + folderName + NL + tuneName + NL + text);
				}
            } else if (name.equals("saveAs")) {
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
					boolean save = false;	
					if (s.endsWith(EXT_CHORDS)) {
						s.replace(EXT_CHORDS, "");
					}
					if (s.equals(tuneName)) {
						int n = JOptionPane.showConfirmDialog(
							    null,
							    "Overwrite existing file: " + tuneName + "?",
							    "Warning",
							    JOptionPane.YES_NO_OPTION);
						if (n == 0) {
							save = true;
						}
					} else {
						save = true;
					}
					if (save) {
					    save(genre, folderName, s, text);
					    updateTuneBox(genre, folderName);
					    log.debug("save " + NL + genre + NL + folderName + NL + s + NL + text);
					}
				} 
            }
        }
    }
}

