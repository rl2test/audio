package audio.chords.gui;

import static audio.Constants.C;
import static audio.Constants.CHORDS_FOLDER;
import static audio.Constants.DIR_FILTER;
import static audio.Constants.CHORDS_FILTER;
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

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

//import audio.Config;
import audio.Util;

public class FilePanel extends AudioPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 	= 1L;
	/** The singleton instance of this class. */    
	private static FilePanel panel 			= null;
	private final FileListener listener 		= new FileListener();
	/** The player. */
	public TunePlayer player 					= null;
	/** Genre box. */
	private final JComboBox<String> genreBox 	= new JComboBox<String>();
	/** Folder box. */
	private final JComboBox<String> folderBox 	= new JComboBox<String>();
	/** Tune box. */
	private final JComboBox<String> tuneBox 	= new JComboBox<String>();
	public String genreName						= GENRE_NAME;
	public String folderName					= FOLDER_NAME;
	public String tuneName						= TUNE_NAME;	
	public boolean playing						= false;

	
	/**
     * @return singleton instance of this class
     */
    public static FilePanel getInstance(AudioController ac) throws Exception {
        if (panel == null) {
        	panel = new FilePanel(ac);
    	}
    	return panel;
    }
	
    /** Private constructor */
    private FilePanel(AudioController ac) throws Exception {
        super(ac);
	    
        h = W[1];
        
		// genre
        w = W[2];
        add(getLabel("Genre", null, C[6], C[16], x, y, w, h, null));
	    x += w + 1;

	    // genre combo box
	    w = W[4];
	    genreBox.setModel(new DefaultComboBoxModel<String>(GENRE_NAMES));
		genreBox.addItemListener(new GenreBoxListener());
		genreBox.setBounds(x, y, w, h);
		genreBox.setFont(FONT);
		add(genreBox);
		genreBox.setSelectedItem(genreName);
		// genre combo box bg label - note: paints BEFORE preceding element
		add(getLabel(null, null, C[6], null, x, y, w, h, null));
	    x += w + 1;

	    // folder
	    w = W[2];
		add(getLabel("Folder", null, C[6], C[16], x, y, w, h, null));
	    x += w + 1;
	    
	    // folder combo box
	    w = W[16];
	    folderBox.setModel(new DefaultComboBoxModel<String>(getFolderNames()));
	    folderBox.addItemListener(new FolderBoxListener());
		folderBox.setBounds(x, y, w, h);
		folderBox.setFont(FONT);
		add(folderBox);
		folderBox.setSelectedItem(folderName);
		// folder combo box bg label - note: paints BEFORE preceding element
		add(getLabel(null, null, C[6], null, x, y, w, h, null));
	    x += w + 1;

		// tune label
	    w = W[2];
		add(getLabel("Tune", null, C[6], C[16], x, y, w, h, null));
	    x += w + 1;
	    
		// tune combo box
	    w = W[16];
	    tuneBox.setModel(new DefaultComboBoxModel<String>(getTuneNames()));
	    tuneBox.addItemListener(new TuneBoxListener());
		tuneBox.setBounds(x, y, w, h);
		tuneBox.setFont(FONT);
		add(tuneBox);
		tuneBox.setSelectedItem(tuneName);
		// tune combo box bg label - note: paints BEFORE preceding element
		add(getLabel(null, null, C[6], null, x, y, w, h, null));
	    x += w + 1;

	    // play/stop
	    w = W[2];
	    add(getLabel("Play", "playStop", C[6], C[16], x, y, w, h, listener));
	    x += w + 1;
	    
	    // save
	    add(getLabel("Save", "save", C[6], C[16], x, y, w, h, listener));
	    x += w + 1;

	    // save as
	    w = W[3];
	    add(getLabel("Save as", "saveAs", C[6], C[16], x, y, w, h, listener));
	    x += w + 1;

	    // refresh
		add(getLabel("Refresh", "refresh", C[6], C[16], x, y, w, h, listener));
	    x += w + 1;

	    // refresh
		add(getLabel("Updated", "updated", C[6], C[16], x, y, w, h, listener));
	    x += w + 1;

    }
    
    public void setUpdated() {
    	labels.get("updated").setBackground(Color.RED);
    }
    
    public void clearUpdated() {
    	labels.get("updated").setBackground(C[6]);
    }
    
    /**
     * This method is called when the chord player throws an exception.
     * 
     * @param msg
     */
    public void stop(String msg) {
		player.destroyPlayer();
		player = null;

		log.debug("msg=" + msg);
    }
    
	/**
	 * Save tune to tuneFile.
	 * 
	 * @param text
	 */
	private void save(String text) {
		Util.writeToFile(getTuneFile(), text);
	} 

	/**
	 * Save tune to tuneFile.
	 * 
	 * @param text
	 */
	private void saveAs(String s, String text) {
		Util.writeToFile(getTuneFile(s), text);
	} 

	/**
	 * Listener for the genre box. When the selection is changed the folder  
	 * box and the tune box are updated.
	 */
	class GenreBoxListener implements ItemListener {
	    public void itemStateChanged(ItemEvent event) {
	    	log.debug("GenreBoxListener");	
	        if (event.getStateChange() == ItemEvent.SELECTED) {
	        	genreName = event.getItem().toString();
	        	log.debug("GenreBoxListener: genre " + genreName + " selected");

	        	if (ac.init) updateFolderBox();
	        }
	    }
	}

	/**
	 * Listener for the folder box. When the selection is changed the tune box 
	 * is updated.
	 */
	class FolderBoxListener implements ItemListener {
	    public void itemStateChanged(ItemEvent event) {
	    	log.debug("FolderBoxListener");
	        if (event.getStateChange() == ItemEvent.SELECTED) {
	        	folderName = event.getItem().toString();
	        	log.debug("FolderBoxListener: folderName " + folderName + " selected");

	        	if (ac.init) updateTuneBox();
	        }
	    }
	}
	
	/**
	 * Listener for the tune box. When the selection is changed the text area
	 * is updated.
	 */
	class TuneBoxListener implements ItemListener {
	    public void itemStateChanged(ItemEvent event) {
	    	log.debug("TuneBoxListener");
	        if (event.getStateChange() == ItemEvent.SELECTED) {
	        	tuneName = event.getItem().toString();
	        	log.debug("TuneBoxListener: tuneName " + tuneName + " selected");

	        	if (ac.init) setTextArea();	
	        }
	    }
	}

	/**
	 * Update the folderBox.
	 */
	public void updateFolderBox() {
		String[] folderNames = getFolderNames();
	    folderBox.setModel(new DefaultComboBoxModel<String>(getFolderNames()));
	    folderName = folderNames[0];
	    log.debug("folderName " + folderName + " selected");
	    updateTuneBox();
	}
    
	/**
	 * Update the tuneBox.
	 */
	public void updateTuneBox() {
		String[] tuneNames = getTuneNames();
		tuneBox.setModel(new DefaultComboBoxModel<String>(tuneNames));
		tuneName = tuneNames[0];
		log.debug("tuneName " + tuneName + " selected");
		if (ac.init) setTextArea();
	}

	/**
	 * @return the genreDir
	 */
	private File getGenreDir() { 
		return new File(MUSIC_DIR, genreName + CHORDS_FOLDER);
	}
	
	/**
	 * @return the folderDir
	 */
	private File getFolderDir() { 
		return new File(getGenreDir(), folderName);
	}

	/**
	 * @return the tuneFile
	 */
	private File getTuneFile() { 
		return new File(getFolderDir(), tuneName + EXT_CHORDS);
	}
	
	public String[] getFolderNames() {
		String[] names = getGenreDir().list(DIR_FILTER);
		Arrays.sort(names);
		return names;
	}
	
	public String[] getTuneNames() {
		String[] names = getFolderDir().list(CHORDS_FILTER); 
		Arrays.sort(names);
		for (int i = 0; i < names.length; i++) {
			names[i] = names[i].replaceAll(EXT_CHORDS, "");
		}
		return names;
	}
	
	/**
	 * @param s
	 * @return File
	 */
	private File getTuneFile(String s) { 
		return new File(getGenreDir(), folderName + FS + s + EXT_CHORDS);
	}
	
	/**
	 * @param genreName
	 * @param folderName
	 * @param tuneName
	 */
	public void setTextArea() {
		ac.textPanel.textArea.setText(Util.getText(getTuneFile()));
		clearUpdated();
	}
	
    private class FileListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            JLabel l = (JLabel) e.getSource();
            String name = l.getName();
            log.debug("name=" + name);
            
            if (name.equals("playStop")) {
            	if (playing) {
    				player.destroyPlayer();
    				player = null;
    				playing = false;
    				l.setText("Play");
            	} else {
					String text = ac.textPanel.textArea.getText();
					player = new TunePlayer(text, ac);
					player.start();
					playing = true;
					l.setText("Stop");
            	}	
            } else if (name.equals("refresh")) {
            	updateTuneBox();
            } else if (name.equals("save")) {
				String text 		= ac.textPanel.textArea.getText();
				
				int n = JOptionPane.showConfirmDialog(
				    null,
				    "Overwrite existing file: " + tuneName + "?",
				    "Warning",
				    JOptionPane.YES_NO_OPTION);
				if (n == 0) {
					save(text);
					log.debug("save " + NL + genreName + NL + folderName + NL + tuneName + NL + text);
				}
            } else if (name.equals("saveAs")) {
				String text 		= ac.textPanel.textArea.getText();

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
					    saveAs(s, text);
					    updateTuneBox();
					    tuneBox.setSelectedItem(s);
					    log.debug("save " + NL + genreName + NL + folderName + NL + s + NL + text);
					}
				} 
            }
        }
    }
}

