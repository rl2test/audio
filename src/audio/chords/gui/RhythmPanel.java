package audio.chords.gui;

import static audio.Constants.C;
import static audio.Constants.EXT_CHORDS;
import static audio.Constants.FONT;
import static audio.Constants.FS;
import static audio.Constants.GENRE_NAMES;
import static audio.Constants.NL;
import static audio.Constants.PIPE_DELIM;
import static audio.Constants.RHYTHMS_FILE;
import static audio.Constants.TOP_BAR_HEIGHT;
import static audio.Constants.W;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.table.*;

import org.apache.log4j.Logger;

import audio.Util;
import audio.chords.gui.FilePanel.GenreBoxListener;

import javax.swing.event.*;
import javax.sound.midi.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "unchecked", "serial" })
public class RhythmPanel extends AudioPanel implements MetaEventListener {
	/** The log. */
	private Logger log = Logger.getLogger(getClass());
    final int PROGRAM = 192;
    final int NOTEON = 144;
    final int NOTEOFF = 128;
    int velocity = 100;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    int row, col;
    JComboBox<String> combo = new JComboBox<String>();
    String instruments[] = { 
    		"Acoustic bass drum", "Bass drum 1", "Side stick", "Acoustic snare",
	        "Hand clap", "Electric snare", "Low floor tom", "Closed hi-hat",
	        "High floor tom", "Pedal hi-hat", "Low tom", "Open hi-hat", 
	        "Low-mid tom", "Hi-mid tom" };
    List<InstrumentData> instrumentData = new ArrayList<InstrumentData>();
	AudioController ac = null;
	Map<String, Integer> map = new HashMap<String, Integer>();
	int numColumns = 0;
	int w = 0;
	int h = 0;
	String[] types = {"Reel", "Jig", "Slide"};
	boolean playing = false;
	private final Listener listener = new Listener();
	

	public RhythmPanel(AudioController ac, int type) throws Exception {
    	super(ac);
        setBackground(C[6]);
        
        List<Rhythm> rhythms = getRhythms();
        String[] rhythmNames = new String[rhythms.size()];
        int i = 0;
        for (Rhythm rhythm: rhythms) {
        	log.debug(rhythm);
        	rhythmNames[i++] = rhythm.name;
        }
        
     	int numBeats = 0;
     	int numSubBeats = 0;
     	
     	switch (type) {
     		case 1: numBeats = 4; numSubBeats = 4; break; 
     		case 2: numBeats = 6; numSubBeats = 2; break; 
     		case 3: numBeats = 9; numSubBeats = 2; break; 
     		default: break;
     	}
     	
     	int numCells = numBeats * numSubBeats;
        
        int id = 35;
     	for (String instrument: instruments) {
     		instrumentData.add(new InstrumentData(instrument, id, numCells));
     		map.put(instrument, id);
     		id++;
     	}
    	
        List<String> headings = new ArrayList<String>();
        headings.add("Instrument");
        for (i = 0; i < numBeats; i++) {
        	headings.add("" + i);
        	for (int j = 1; j < numSubBeats; j++) {
        		headings.add("-");	
            }	
        }
        
	    h = W[1];
	    
	    // play/stop label
	    w = W[2];
	    add(getLabel("Play", "playStop", C[6], C[16], x, y, w, h, listener));
	    x += w + 1;  
	    
	    add(getLabel("Clear", "clear", C[6], C[16], x, y, w, h, listener));
	    x += w + 1;  
        
	    // genre combo box
	    w = W[4];
	    combo.setModel(new DefaultComboBoxModel<String>(rhythmNames));
	    combo.addItemListener(new ComboListener());
	    combo.setBounds(x, y, w, h);
	    combo.setFont(FONT);
		add(combo);
		combo.setSelectedItem(rhythmNames[0]);

		Rhythm rhythm = rhythms.get(0);
		
		x = 0;
		y += W[1] + 1; 
		w = W[1];
		i = 0; 
		for (String heading: headings) {
			log.debug(heading + " " + x);
			if (i == 0) {
				add(getLabel(heading, null, C[12], C[0], x, y, W[8], h, null));
				x += W[8] + 1;
			} else {
				add(getLabel(heading, null, C[12], C[0], x, y, w, h, null));
				x += w + 1;
			}
			
			i++;
		}
		
		for (InstrumentData data: instrumentData) {
			x = 0;
			y += W[1] + 1;
			w = W[1];
			add(getLabel(data.name, null, C[12], C[0], x, y, W[8], h, null));
			x += W[8] + 1;
			for (i = 0; i < numBeats * numSubBeats; i++) {
				add(getLabel("", data.name.replace(" ", "-") + "-" + i, C[12], C[0], x, y, w, h, listener));
				x += w + 1;
			}
			
		}
	}


    public void open() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
        } catch (Exception e) { e.printStackTrace(); }
        sequencer.addMetaEventListener(this);
    }


    public void close() {
        //if (startB.getText().startsWith("Stop")) {
        //   startB.doClick(0);
        //}
        if (sequencer != null) {
            sequencer.close();
        }
        sequencer = null;
    }

    private void buildTrackThenStartSequencer() {
        try {
           sequence = new Sequence(Sequence.PPQ, 4);
        } catch (Exception ex) { ex.printStackTrace(); }
        track = sequence.createTrack();
        createEvent(PROGRAM, 9, 1, 0);
        /*
        for (int i = 0; i < data.size(); i++) {
            Data d = (Data) data.get(i);
            for (int j = 0; j < d.staff.length; j++) {
                if (d.staff[j].equals(Color.black)) {
                	log.debug(d.id);
                     createEvent(NOTEON, 9, d.id, j); 
                     createEvent(NOTEOFF, 9, d.id, j+1); 
                }
            }
        }
        */
        // so we always have a track from 0 to 15.
        createEvent(PROGRAM, 9, 1, 16);

        // set and start the sequencer.
        try {
            sequencer.setSequence(sequence);
        } catch (Exception ex) { ex.printStackTrace(); }
        sequencer.setLoopCount(1000);
        int bpm = 180; //AudioController.getInstance().timePanel.endTempo;
        log.debug("bpm=" + bpm);
        sequencer.setTempoInBPM(bpm);

        sequencer.start();
    }


    private void presetTracks(int num) {

        final int ACOUSTIC_BASS = 35;
        final int ACOUSTIC_SNARE = 38;
        final int HAND_CLAP = 39;
        final int PEDAL_HIHAT = 44;
        final int LO_TOM = 45;
        final int CLOSED_HIHAT = 42;
        final int CRASH_CYMBAL1 = 49;
        final int HI_TOM = 50;
        final int RIDE_BELL = 53;

        clearTable();

        switch (num) {
            case 0 : for (int i = 0; i < 16; i+=2) {
                         setCell(CLOSED_HIHAT, i); 
                     }
                     setCell(ACOUSTIC_SNARE, 4);
                     setCell(ACOUSTIC_SNARE, 12);
                     int bass1[] = { 0, 3, 6, 8 };
                     for (int i = 0; i < bass1.length; i++) {
                         setCell(ACOUSTIC_BASS, bass1[i]); 
                     }
                     break;
            case 1 : for (int i = 0; i < 16; i+=4) {
                         setCell(CRASH_CYMBAL1, i); 
                     }
                     for (int i = 0; i < 16; i+=2) {
                         setCell(PEDAL_HIHAT, i); 
                     }
                     setCell(ACOUSTIC_SNARE, 4);
                     setCell(ACOUSTIC_SNARE, 12);
                     int bass2[] = { 0, 2, 3, 7, 9, 10, 15 };
                     for (int i = 0; i < bass2.length; i++) {
                         setCell(ACOUSTIC_BASS, bass2[i]); 
                     }
                     break;
            case 2 : for (int i = 0; i < 16; i+=4) {
                         setCell(RIDE_BELL, i); 
                     }
                     for (int i = 2; i < 16; i+=4) {
                         setCell(PEDAL_HIHAT, i); 
                     }
                     setCell(HAND_CLAP, 4);
                     setCell(HAND_CLAP, 12);
                     setCell(HI_TOM, 13);
                     setCell(LO_TOM, 14);
                     int bass3[] = { 0, 3, 6, 9, 15 };
                     for (int i = 0; i < bass3.length; i++) {
                         setCell(ACOUSTIC_BASS+1, bass3[i]); 
                     }
                     break;
            default :
        }
    }

    private void setCell(int id, int tick) {
        /*for (int i = 0; i < data.size(); i++) {
            Data d = (Data) data.get(i);
            if (d.id == id) {
                d.staff[tick] = Color.black;
                break;
            }
        }*/
    }


    private void clearTable() {
        /*for (int i = 0; i < data.size(); i++) {
            Data d = (Data) data.get(i);
            for (int j = 0; j < d.staff.length; j++) {
                d.staff[j] = Color.white;
            }
        }*/
    }

    private void createEvent(int type, int chan, int num, long tick) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(type, chan, num, velocity); 
            MidiEvent event = new MidiEvent( message, tick );
            track.add(event);
        } catch (Exception ex) { ex.printStackTrace(); }
    }


    public void meta(MetaMessage message) {
    	log.debug(message.getType());
        if (message.getType() == 47) {  // 47 is end of track
        	//log.debug(message.getType() + " " + loopB.getBackground() + " " + Color.gray);
            /*
        	if (loopB.getBackground().equals(Color.gray)) {
                if (sequencer != null && sequencer.isOpen()) {
                	log.debug(message.getType());
                	//sequencer.start();
                    //sequencer.setTempoInBPM(tempoDial.getTempo());
                }
            } else {
                startB.setText("Start");
            }
            */
        }
    }

    /*
    public void actionPerformed(ActionEvent e) {
        Object object = e.getSource();
        if (object instanceof JComboBox) {
            presetTracks(((JComboBox<String>) object).getSelectedIndex());
            if (startB.getText().startsWith("Stop")) {
                sequencer.stop();
                buildTrackThenStartSequencer();
            }
        } else if (object instanceof JButton) {
            JButton b = (JButton) object;
            if (b.equals(startB)) {
                if (b.getText().startsWith("Start")) {
                    buildTrackThenStartSequencer();
                    b.setText("Stop");
                } else {
                    sequencer.stop();
                    b.setText("Start");
                }
            } else if (b.equals(loopB)) {
                b.setSelected(!b.isSelected());
                if (loopB.getBackground().equals(Color.gray)) {
                    loopB.setBackground(getBackground());
                } else {
                    loopB.setBackground(Color.gray);
                }
            } else if (b.getText().startsWith("Clear")) {
                clearTable();
                table.tableChanged(new TableModelEvent(dataModel));
            }
        }
    }
	*/
    
    private class Listener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            JLabel l = (JLabel) e.getSource();
            String name = l.getName();
            log.debug("name=" + name);
            
            if (name.equals("playStop")) {
            	if (playing) {
    				//player.destroyPlayer();
    				//player = null;
    				playing = false;
    				l.setText("Play");
            	} else {
					//String text = ac.textPanel.textArea.getText();
					//player = new TunePlayer(text, ac);
					//player.start();
					playing = true;
					l.setText("Stop");
            	}	
            } else if (name.equals("clear")) {
            	//updateTuneBox();
            } else if (name.equals("save")) {
            	/*
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
				*/
            } else if (name.equals("saveAs")) {
            	/*
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
				*/ 
            }
        }
    }
    

	/**
	 * Listener for combo box.
	 */
	class ComboListener implements ItemListener {
	    public void itemStateChanged(ItemEvent event) {
	    	log.debug("ComboListener");	
	        if (event.getStateChange() == ItemEvent.SELECTED) {
	        	String name = event.getItem().toString();
	        	log.debug("ComboListener: name " + name + " selected");

	        	//updateFolderBox();
	        }
	    }
	}    
    
    /**
     * Storage class for instrument
     */
    class InstrumentData {
        String name; 
        int id; 
        boolean[] cells;
        public InstrumentData(String name, int id, int numCells) {
            this.name = name;
            this.id = id;
            cells = new boolean[numCells];
            for (int i = 0; i < cells.length; i++) {
                cells[i] = false;
            }
        }
    }
    
    public void init() {    
		GraphicsEnvironment ge 	= GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gds[] 	= ge.getScreenDevices();
	    int len 				= gds.length;
	    boolean isDual 			= (len > 1);
	    int useScreen			= (isDual) ? 2 : 1;
	
	    log.debug(len + " screens detected: isDual=" + isDual + ", useScreen=" + useScreen);
	    int screenNum = 1;
	    for(GraphicsDevice gd: gds) {
	    	 GraphicsConfiguration	graphicsConfiguration = gd.getDefaultConfiguration();
	    	 Rectangle r = graphicsConfiguration.getBounds();
	    	 if (useScreen == screenNum) {
	    		w = r.width / 2;
	    		h = r.height / 2;
	    		x = w / 2;
	    		y = h / 2;
	    		log.debug("screenNum=" + screenNum + ": w=" + w + ", h=" + h + ", x=" + x + ", y=" + y);
	    	 }
	    	 screenNum++;
	     }
	    
		final JFrame frame = new JFrame("Rhythm");
	
		frame.setSize(w, h);
		frame.setLocation(x, y);
	
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.dispose();
			}
		});
	
		// call setVisible to get insets
		frame.setVisible(true);
	
		Insets insets = frame.getInsets();
		
		int left 	= insets.left;
		int right 	= insets.right;
		int top 	= insets.top;
		int bottom	= insets.bottom;
		
		log.debug("insets: " + left + " " + right + " " + top + " " + bottom);
	
		w = w - insets.left - insets.right; 
		h = h - TOP_BAR_HEIGHT - insets.top - insets.bottom;

        frame.getContentPane().add("Center", this);
		// call setVisible to display gui
		frame.validate();
		frame.repaint();
        //frame.pack();
    }
    
    
    public static void main(String args[]) {
        RhythmPanel rhythm;
		try {
			rhythm = new RhythmPanel(null, 1);
	        rhythm.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    // rhythm section
    
    private List<Rhythm> getRhythms() {
    	List<Rhythm> rhythms = new ArrayList<Rhythm>();
    	Rhythm rhythm = null;
		List<String> lines 	= Util.getLines(RHYTHMS_FILE);
		for (String line: lines) {
			/*
			@Jig 1            |123456789012
			Acoustic bass drum|-     -
			Low floor tom     |    -     -
			Low tom           |        -
			*/
			if (line.startsWith("#")) {
				// comment
			} else if (line.startsWith("@")) {
				rhythm = new Rhythm();
				String[] arr = line.substring(1).split(PIPE_DELIM);
				rhythm.name = arr[0].trim();
				rhythm.numBeats = arr[1].length();
				rhythms.add(rhythm);
			} else {
				String[] arr = line.split(PIPE_DELIM);
				RhythmInstrument instrument = new RhythmInstrument();
				rhythm.instruments.add(instrument);
				instrument.name = arr[0].trim();
				String beats = arr[1]; 
				int len = beats.length();
				for (int i = 0; i < len; i++) {
					String s = beats.substring(i, i + 1);
					if (s.equals("-")) {
						instrument.beats.add(i);
					}
				}
			}
		}
    	
    	return rhythms;
    }
    
    class Rhythm {
    	String name = "";
    	int numBeats = 0;
    	List<RhythmInstrument> instruments = new ArrayList<RhythmInstrument>();
    	public String toString() {
    		String s = NL + name + ": " + numBeats + NL;
    		for (RhythmInstrument instrument: instruments) {
    			s += "  " + instrument.toString();	
    		}
    		return s;
    	}
    }
    
    class RhythmInstrument {
    	String name;
    	List<Integer> beats = new ArrayList<Integer>();
       	public String toString() {
    		String s = name + ": ";
    		for (Integer beat: beats) {
    			s += beat + " ";	
    		}
    		s += NL;
    		return s;
    	}
    }
} 


