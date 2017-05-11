package audio.chords.gui;

import static audio.Constants.C;
import static audio.Constants.FONT;
import static audio.Constants.NL;
import static audio.Constants.PIPE_DELIM;
import static audio.Constants.RHYTHMS_FILE;
import static audio.Constants.W;
import static audio.Constants.V;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.apache.log4j.Logger;

import audio.Util;

import javax.sound.midi.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "serial" })
public class RhythmPanel extends AudioPanel implements MetaEventListener {
	/** The log. */
	private Logger log = Logger.getLogger(getClass());
	private final int PROGRAM = 192;
	private final int NOTEON = 144;
	private final int NOTEOFF = 128;
	private final int CHANNEL = 9;
	private final int LOOP_COUNT = 1000;
	private int bpm = 150;
	private Sequencer sequencer;
	private Sequence sequence;
	private Track track;
	private JComboBox<String> comboBox = new JComboBox<String>();
	private String instrumentNames[] = { 
    		"Acoustic bass drum", 
    		"Bass drum 1", 
    		"Side stick", 
    		"Acoustic snare",
	        "Hand clap", 
	        "Electric snare", 
	        "Low floor tom", 
	        "Closed hi-hat",
	        "High floor tom", 
	        "Pedal hi-hat", 
	        "Low tom", 
	        "Open hi-hat", 
	        "Low-mid tom", 
	        "Hi-mid tom"
	};
	private List<Instrument> instruments = new ArrayList<Instrument>();
	private Map<String, Instrument> instrumentMap = new HashMap<String, Instrument>();	
	private boolean playing = false;
	private final Listener listener = new Listener();
	private List<Rhythm> rhythms;
	private Rhythm rhythm;
	
	public RhythmPanel(Rectangle r) throws Exception {
    	super(null);
        setBackground(C[6]);
        
        this.x = r.x;
        this.y = r.y;
        this.w = r.width;
        this.h = r.height;
        
		final JFrame frame = new JFrame("Rhythm");
		frame.setSize(w, h);
		frame.setLocation(x, y);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
                sequencer.stop();
				playing = false;
				closeSequencer();
				frame.dispose();
			}
		});
		frame.setVisible(true);
		
		initUi();
		
		frame.getContentPane().add("Center", this);
		frame.validate();
		frame.repaint();
		
     	openSequencer();
     	openRhythm(0);	// load the first one
	}	

	// init ui
	private void initUi() throws Exception {
        rhythms = getRhythms();
        String[] rhythmNames = new String[rhythms.size()];
        int i = 0;
        for (Rhythm r: rhythms) {
        	rhythmNames[i++] = r.name;
        }
        
        x = 0;
        y = 0;
 	    h = W[1];
	    
	    // play/stop label
	    w = W[2];
	    add(getLabel("Play", "playStop", C[6], C[16], x, y, w, h, listener));
	    x += w + 1;  
	    
	    //add(getLabel("Clear", "clear", C[6], C[16], x, y, w, h, listener));
	    //x += w + 1;  
        
	    // genre combo box
	    w = W[4];
	    comboBox.setModel(new DefaultComboBoxModel<String>(rhythmNames));
	    comboBox.setName("comboBox");
	    comboBox.addItemListener(new ComboListener());
	    comboBox.setBounds(x, y, w, h);
	    comboBox.setFont(FONT);
		add(comboBox);
		comboBox.setSelectedItem(rhythmNames[0]);
		x += w + 1;
		
	    // tempo label
	    w = W[2];
	    add(getLabel("Tempo", "tempoLabel", C[6], C[16], x, y, w, h, null));
	    x += w - 1;

	    w = W[1];
     	for (i = 60; i < 260; i += 10) {
			add(getLabel("" + i, "b-" + i, C[12], C[0], x, y, w, h, listener));
			x += w + 1;
		}
     	set("b-" + bpm);
	    
		
        int id = 35;
     	for (String instrumentName: instrumentNames) {
     		Instrument instrument = new Instrument(instrumentName, id);
     		instruments.add(instrument);
     		instrumentMap.put(instrumentName, instrument);
     		id++;
     	}
		
		x = 0;
		w = W[8];

		y += W[1] + 1;
		add(getLabel("Instrument", "instrument", C[12], C[0], x, y, w, h, null));
		y += W[1] + 1;
     	for (Instrument instrument: instruments) {
			add(getLabel(instrument.id + " " + instrument.name, "i-" + instrument.id, C[12], C[0], x, y, w, h, null));
			y += W[1] + 1;
		}
	}
	
	// open rhythm
	private void openRhythm(int num) throws Exception {
        rhythm = rhythms.get(num);
        log.debug("rhythm=" + rhythm);

 		Component[] componentList = this.getComponents();
 		log.debug(componentList.length);
 		for(Component c: componentList){
 			String name = c.getName();
 		    if (name.startsWith("h-") || name.startsWith("p-")) { // heading or pulse
 		        this.remove(c);
 		        labels.remove(name);
 		    }
 		}
        
		x = W[8] + 1;
		y = W[1] + 1; 
		w = W[1];
        for (int i = 0; i < rhythm.beats; i++) {
        	add(getLabel("" + (i + 1), "h-" + i, C[12], C[0], x, y, w, h, null));
        	x += W[1] + 1;
        	for (int j = 1; j < rhythm.subBeats; j++) {
        		add(getLabel("-", "h-" + i + "-" + j, C[12], C[0], x, y, w, h, null));
        		x += W[1] + 1;
            }
        }

		y += W[1] + 1;
		for (Instrument instrument: instruments) {
			x = W[8] + 1;
			for (int i = 0; i < rhythm.pulses; i++) {
				add(getLabel("",  "p-" + instrument.id + "-" + i, C[12], C[0], x, y, w, h, listener));
				x += w + 1;
			}
			y += W[1] + 1;
		}
		
     	for (RhythmInstrument rhythmInstrument: rhythm.instruments) {
     		Instrument instrument = instrumentMap.get(rhythmInstrument.name);
     		for (int pulse: rhythmInstrument.pulses) {
     			set("p-" + instrument.id + "-" + pulse);
     		}
     	}

     	repaint();
	}
	
	private void startSequencer() {
		try {
            sequence = new Sequence(Sequence.PPQ, rhythm.ppq);
            track = sequence.createTrack();
            
            createEvent(PROGRAM, CHANNEL, 1, 0);
	        for (RhythmInstrument rhythmInstrument: rhythm.instruments) {
	      		Instrument instrument = instrumentMap.get(rhythmInstrument.name);
	      		for (int pulse: rhythmInstrument.pulses) {
	                 createEvent(NOTEON, CHANNEL, instrument.id, pulse); 
	                 createEvent(NOTEOFF, CHANNEL, instrument.id, pulse + 1);      		}
	      	}
	        // so we always have a track of numPulses length.
	        createEvent(PROGRAM, CHANNEL, 1, rhythm.pulses);

	        // set and start the sequencer.
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(LOOP_COUNT);
            log.debug("bpm=" + bpm);
            sequencer.setTempoInBPM(bpm);
            sequencer.start();
		} catch (Exception e) { 
			log.error(e);
	    }
    }
	
    private void createEvent(int type, int chan, int num, long tick) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(type, chan, num, V[8]); // , velocity
            MidiEvent event = new MidiEvent( message, tick );
            track.add(event);
		} catch (Exception e) { 
			log.error(e);
	    }
    }


    public void meta(MetaMessage message) {
    	log.debug(message.getType());
        if (message.getType() == 47) {  // 47 is end of track
            sequencer.stop();
            playing = false;
        }
    }

    private List<Rhythm> getRhythms() {
    	List<Rhythm> rhythms = new ArrayList<Rhythm>();
    	Rhythm r = null;
		List<String> lines 	= Util.getLines(RHYTHMS_FILE);
		for (String line: lines) {
			/*
			@Jig 1            |1-2-3-4-5-6-  
			Acoustic bass drum|-     -
			Low floor tom     |    -     -
			Low tom           |        -
			*/
			if (!line.startsWith("#")) {
				if (line.startsWith("@")) {
					String[] arr = line.substring(1).split(PIPE_DELIM);
					r = new Rhythm(arr[0].trim());
					rhythms.add(r);
				} else {
					String[] arr = line.split(PIPE_DELIM);
					RhythmInstrument instrument = new RhythmInstrument();
					r.instruments.add(instrument);
					instrument.name = arr[0].trim();
					String pulses = arr[1]; 
					int len = pulses.length();
					for (int i = 0; i < len; i++) {
						String s = pulses.substring(i, i + 1);
						if (s.equals("-")) {
							instrument.pulses.add(i);
						}
					}
				}				
			}
		}
    	
    	return rhythms;
    }
    
    public void openSequencer() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
        } catch (Exception e) { 
        	log.debug(e);
        }
        sequencer.addMetaEventListener(this);
    }

    public void closeSequencer() {
        if (sequencer != null) {
            sequencer.close();
        }
        sequencer = null;
    }
    
    // classes =========================================================================================
    
    class Listener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            JLabel l = (JLabel) e.getSource();
            String name = l.getName();
            log.debug("name=" + name);
            
            if (name.equals("playStop")) {
            	if (playing) {
                    sequencer.stop();
    				playing = false;
    				l.setText("Play");
            	} else {
            		startSequencer();
					playing = true;
					l.setText("Stop");
            	}	
            } else if (name.startsWith("b-")) {
            	unset("b-" + bpm);
            	bpm = Integer.parseInt(l.getText());
            	set(l);
            } else if (name.startsWith("p-")) {
            	//set(l);
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
	        	//String name = event.getItem().toString();
	        	String name = (String) comboBox.getSelectedItem();
	        	int index = comboBox.getSelectedIndex();
	        	log.debug("ComboListener: name=" + name + ", index=" + index);
	        	try {
					openRhythm(index);
				} catch (Exception e) {
					log.error(e);
				}
	        }
	    }
	}   
	
    /**
     * Rhythm data
     */
	class Rhythm {
    	String name = "";
    	int beats = 0;
    	int subBeats = 0;
    	int pulses = 0;
    	int ppq = 0; // pulses per quarter-note
    	List<RhythmInstrument> instruments = new ArrayList<RhythmInstrument>();
    	public Rhythm(String name) {
    		this.name = name;
    		if (name.startsWith("Jig")){
				beats = 6;
				subBeats = 2;
				ppq = 6;
			} else if (name.startsWith("Slip Jig")){
				beats = 9;
				subBeats = 2;
				ppq = 6;
			} else if (name.startsWith("Reel")){
				beats = 4;
				subBeats = 4;
				ppq = 4;
			}
			pulses = beats * subBeats;
    	}
    	public String toString() {
    		String s = NL + name + ": " + beats + " " + subBeats + " " + pulses + " " + ppq + NL;
    		for (RhythmInstrument instrument: instruments) {
    			s += "  " + instrument.toString();	
    		}
    		return s;
    	}
    }
    
    /**
     * RhythmInstrument data
     */
    class RhythmInstrument {
    	String name;
    	int id;
    	List<Integer> pulses = new ArrayList<Integer>();
       	public String toString() {
    		String s = name + ": ";
    		for (Integer pulse: pulses) {
    			s += pulse + " ";	
    		}
    		s += NL;
    		return s;
    	}
    }
    
    /**
     * Instrument data
     */
    class Instrument {
        String name; 
        int id; 
        public Instrument(String name, int id) {
            this.name = name;
            this.id = id;
        }
    }

    /**
     * @param args
     */
    public static void main(String args[]) {
		GraphicsEnvironment ge 	= GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gds[] 	= ge.getScreenDevices();
	    int len 				= gds.length;
	    boolean isDual 			= (len > 1);
	    int useScreen			= (isDual) ? 2 : 1;
	    int x = 0, y = 0, w = 0, h = 0;
	
	    System.out.println(len + " screens detected: isDual=" + isDual + ", useScreen=" + useScreen);
	    int screenNum = 1;
	    for(GraphicsDevice gd: gds) {
	    	 GraphicsConfiguration	graphicsConfiguration = gd.getDefaultConfiguration();
	    	 Rectangle r = graphicsConfiguration.getBounds();
	    	 if (useScreen == screenNum) {
	    		w = r.width / 2;
	    		h = r.height / 2;
	    		x = r.x + w / 2;
	    		y = r.y + h / 2;
	    		System.out.println("screenNum=" + screenNum + ": w=" + w + ", h=" + h + ", x=" + x + ", y=" + y);
	    	 }
	    	 screenNum++;
	     }
	    
		try {
			new RhythmPanel(new Rectangle(x, y, w, h - 13));
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
} 


