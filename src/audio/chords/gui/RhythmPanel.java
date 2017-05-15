package audio.chords.gui;

import static audio.Constants.C;
import static audio.Constants.FONT;
import static audio.Constants.NL;
import static audio.Constants.PIPE;
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
	private int maxInstrumentNameLen = instrumentNames[0].length();
	private List<Instrument> instruments = new ArrayList<Instrument>();
	private Map<String, Instrument> instrumentMap = new HashMap<String, Instrument>();
	private Map<Integer, Instrument> instrumentIdMap = new HashMap<Integer, Instrument>();
	private boolean playing = false;
	private final Listener listener = new Listener();
	private List<Rhythm> rhythms;
	private Rhythm rhythm;
	
	public RhythmPanel(Rectangle r) throws Exception {
    	super(null);
        setBackground(C[6]);
        
		final JFrame frame = new JFrame("Rhythm");
		frame.setSize(r.width, r.height);
		frame.setLocation(r.x, r.y);
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
	    add(getLabel("Play", "playStop", C[8], C[16], x, y, w, h, listener));
	    x += w + 1;  
	    
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

	    add(getLabel("Save", "save", C[8], C[16], x, y, w, h, listener));
	    x += w + 1;  
	    
	    add(getLabel("Save as", "saveAs", C[8], C[16], x, y, w, h, listener));
	    x += w + 1;
	    
	    add(getLabel("Clear", "clear", C[8], C[16], x, y, w, h, listener));
	    x += w + 1;  

        x = 0;
        y += h + 1;

	    
	    // tempo label
	    w = W[2] + 22;
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
     		instrumentIdMap.put(id, instrument);
     		id++;
     	}
		
		x = 0;
		w = W[8];

		y += h + 1;
		add(getLabel("Instrument", "instrument", C[12], C[0], x, y, w, h, null));
		y += h + 1;
     	for (Instrument instrument: instruments) {
			add(getLabel(instrument.id + " " + instrument.name, "i-" + instrument.id, C[12], C[0], x, y, w, h, null));
			y += h + 1;
		}
	}
	
	// open rhythm
	private void openRhythm(int num) throws Exception {
        rhythm = rhythms.get(num);
        log.debug("rhythm=" + NL + rhythm);

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
		y = (h + 1) * 2; 
		w = W[1];
        for (int i = 0; i < rhythm.beats; i++) {
        	add(getLabel("" + (i + 1), "h-" + i, C[12], C[0], x, y, w, h, null));
        	x += W[1] + 1;
        	for (int j = 1; j < rhythm.subBeats; j++) {
        		add(getLabel("-", "h-" + i + "-" + j, C[12], C[0], x, y, w, h, null));
        		x += W[1] + 1;
            }
        }

		y += h + 1;
		for (Instrument instrument: instruments) {
			instrument.pulses = new boolean[rhythm.getNumPulses()];
			x = W[8] + 1;
			for (int i = 0, n = rhythm.getNumPulses(); i < n; i++) {
				add(getLabel("",  "p-" + instrument.id + "-" + i, C[12], C[0], x, y, w, h, listener));
				x += w + 1;
			}
			y += h + 1;
		}
		
     	for (String instr: rhythm.instruments) {
     		String[] arr = instr.split(PIPE_DELIM);
			String instrName = arr[0].trim();
     		Instrument instrument = instrumentMap.get(instrName);
			String pulseStr = arr[1]; 
			for (int i = 0, n = pulseStr.length(); i < n; i++) {
				String s = pulseStr.substring(i, i + 1);
				if (s.equals("-")) {
					instrument.pulses[i] = true;
     				log.debug("p-" + instrument.id + "-" + i);
     				set("p-" + instrument.id + "-" + i);
				}
			}
     	}

     	repaint();
	}
	
	private void startSequencer() {
		try {
            sequence = new Sequence(Sequence.PPQ, rhythm.ppq);
            track = sequence.createTrack();
            
            createEvent(PROGRAM, CHANNEL, 1, 0);
	        for (Instrument instrument: instruments) {
	     		for (int i = 0, n = instrument.pulses.length; i < n; i++) {
	     			if (instrument.pulses[i]) {
	     				createEvent(NOTEON, CHANNEL, instrument.id, i);
	     				createEvent(NOTEOFF, CHANNEL, instrument.id, i + 1);
	     			}
	        	}     
	      	}
	        // so we always have a track of numPulses length.
	        createEvent(PROGRAM, CHANNEL, 1, rhythm.getNumPulses());

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
			@Jig 1            |6|2|6
			#                 |1-2-3-4-5-6-
			Acoustic bass drum|-     -
			Low floor tom     |    -     -
			Low tom           |        -
			*/
			if (!line.startsWith("#")) {
				String[] arr = line.split(PIPE_DELIM);
				if (line.startsWith("@")) {
					r = new Rhythm();
					r.name = arr[0].substring(1).trim();
					r.beats = Integer.parseInt(arr[1]);
					r.subBeats = Integer.parseInt(arr[2]);
					r.ppq = Integer.parseInt(arr[3]);
					rhythms.add(r);
				} else {
					r.instruments.add(line);
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
    
    public String getSpaces(String name) {
		String s = "";
		for (int i = 0, n = maxInstrumentNameLen - name.length(); i < n; i++ ) {
			s += " ";
		}
		return s;
    }
    
    public void saveRhythms() {
    	String s = "";
    	for (Rhythm rhythm: rhythms) {
    		s += rhythm.toString() + NL;
    	}
    	Util.writeToFile(RHYTHMS_FILE, s);
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
            	if (!playing) {
            		String[] arr = name.split("-");
            		int id = Integer.parseInt(arr[1]);
            		int pulse = Integer.parseInt(arr[2]);
            		Instrument instrument = instrumentIdMap.get(id);
            		if (instrument.pulses[pulse]) {
            			unset(l);	
            			instrument.pulses[pulse] = false;
            		} else {
            			set(l);	
            			instrument.pulses[pulse] = true;            			
            		}
            	}
            } else if (name.equals("clear")) {
            	//updateTuneBox();
            } else if (name.equals("save")) {
            	rhythm.instruments.clear();
            	for (Instrument instrument: instruments) {
            		boolean save = false;
            		for (boolean pulse: instrument.pulses) {
            			if (pulse) {
            				save = true;
            				break;
            			}
            		}
            		if (save) {
            			String instr = instrument.name + getSpaces(instrument.name) + PIPE;
            			for (boolean pulse: instrument.pulses) {
            				instr += (pulse) ? "-" : " ";
                		}
            			instr.trim();
            			rhythm.instruments.add(instr);
            		}
            	}
            	saveRhythms();
            } else if (name.equals("saveAs")) {
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
		/*
		@Jig 1            |6|2|6
		#                 |1-2-3-4-5-6-
		*/
    	String name = "";
    	int beats = 0;
    	int subBeats = 0;
    	int ppq = 0; // pulses per quarter-note
    	List<String> instruments = new ArrayList<String>();
    	
    	public int getNumPulses() {
    		return beats * subBeats;
    	}
    	
    	public String toString() {
    		String s = "@" + name + getSpaces("@" + name) + PIPE + beats + PIPE + subBeats + PIPE + ppq + NL;
    		s += "#" + getSpaces("#") + PIPE;
    		for (int i = 0; i < beats; i++) {
    			s += (i + 1);
    			for (int j = 1; j < subBeats; j++) {
    				s += "-";
    			}
    		}
    		s += NL;
    		for (String instrument: instruments) {
    			s += instrument + NL;	
    		}
    		return s;
    	}
    }
 
    /**
     * Instrument data
     */
    class Instrument {
        String name; 
        int id;
        boolean[] pulses = null;
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
	    int rx = 0, ry = 0, rw = 0, rh = 0;
	
	    System.out.println(len + " screens detected: isDual=" + isDual + ", useScreen=" + useScreen);
	    int screenNum = 1;
	    for(GraphicsDevice gd: gds) {
	    	 GraphicsConfiguration	graphicsConfiguration = gd.getDefaultConfiguration();
	    	 Rectangle r = graphicsConfiguration.getBounds();
	    	 if (useScreen == screenNum) {
	    		rw = r.width / 2;
	    		rh = r.height / 2;
	    		rx = r.x + rw / 2;
	    		ry = r.y + rh / 2;
	    		System.out.println("screenNum=" + screenNum + ": w=" + rw + ", h=" + rh + ", x=" + rx + ", y=" + ry);
	    	 }
	    	 screenNum++;
	     }
	    
		try {
			new RhythmPanel(new Rectangle(rx, ry, rw, rh + 12));
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
} 
