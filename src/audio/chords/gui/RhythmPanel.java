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
import java.util.Collections;
import java.util.Comparator;
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
	private final int BPM = 120;
	private int bpm = BPM;
	private int bpmInc = bpm;
	private Sequencer sequencer;
	private Sequence sequence;
	private Track track;
	private JComboBox<String> comboBox = new JComboBox<String>();
	private String instrumentNames[] = { 
    		"Acoustic bass drum", 
    		"_Bass drum 1", // same as Acoustic bass drum
    		"Side stick", 
    		"Acoustic snare",
	        "_Hand clap", // same as Side stick
	        "_Electric snare", // same as Acoustic snare 
	        "Low floor tom", 
	        "Closed hi-hat",
	        "High floor tom", 
	        "_Pedal hi-hat", // same as Closed hi-hat
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
	private List<Rhythm> rhythms = new ArrayList<Rhythm>();
	private String[] rhythmNames;
	private Rhythm rhythm;
	private boolean accelerate = false;
	private int inc = 1;
	private int defaultLoopCount = 5;
	private int loopCount = defaultLoopCount;
	
	public RhythmPanel(Rectangle r) throws Exception {
    	super(null);
        setBackground(C[4]);
        
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
		
		loadRhythms();
		initUi();
		
		frame.getContentPane().add("Center", this);
		frame.validate();
		frame.repaint();
		
     	openSequencer();
     	openRhythm(0);	// load the first one
	}	

	// init ui
	private void initUi() throws Exception {
        x = 0;
        y = 0;
 	    h = W[1];
	    
	    // play/stop label
	    w = W[2];
	    add(getLabel("Play", "playStop", C[6], C[16], x, y, w, h, listener));
	    x += w + 1;  
	    
	    // genre combo box
	    w = W[12];
	    comboBox.setModel(new DefaultComboBoxModel<String>(rhythmNames));
	    comboBox.setName("comboBox");
	    comboBox.addItemListener(new ComboListener());
	    comboBox.setBounds(x, y, w, h);
	    comboBox.setFont(FONT);
		add(comboBox);
		comboBox.setSelectedItem(rhythmNames[0]);
		x += w + 1;

		// buttons
		w = W[2];		
	    add(getLabel("Save", "save", C[6], C[16], x, y, w, h, listener));
	    x += w + 1;  
	    
	    w = W[3];
	    add(getLabel("Save as", "saveAs", C[6], C[16], x, y, w, h, listener));
	    x += w + 1;
	    
	    //add(getLabel("Clear", "clear", C[6], C[16], x, y, w, h, listener));
	    //x += w + 1;  

	    x = 0;
	    y += h + 1;
	    
	    // accelerate
	    w = W[2];
	    add(getLabel("Acc", "accelerate", C[12], C[0], x, y, w, h, listener));
	    x += w + 1;  

     	// inc
     	w = W[2];
	    add(getLabel("Inc", "inc", C[0], C[16], x, y, w, h, null));
	    x += w + 1;
	    
	    // acc inc
	    w = W[1];
     	for (int i = 1; i < 11; i++) {
			add(getLabel("" + i, "inc-" + i, C[12], C[0], x, y, w, h, listener));
			x += w + 1;
		}
     	set("inc-1");	    
	    
     	// loop
     	w = W[2];
	    add(getLabel("Loop", "loop", C[0], C[16], x, y, w, h, null));
	    x += w + 1;
	    
	    // loop num
	    w = W[1];
	    add(getLabel("4", "l-4", C[12], C[0], x, y, w, h, listener)); x += w + 1;
		add(getLabel("8", "l-8", C[12], C[0], x, y, w, h, listener)); x += w + 1;
		add(getLabel("16", "l-16", C[12], C[0], x, y, w, h, listener)); x += w + 1;
		add(getLabel("32", "l-32", C[12], C[0], x, y, w, h, listener)); x += w + 1;
		w = W[2];
		add(getLabel("" + defaultLoopCount, "l-" + defaultLoopCount, C[12], C[0], x, y, w, h, listener)); x += w + 1;
     	set("l-" + defaultLoopCount);	    

        x = 0;
        y += h + 1;
	    
	    // bpm label
	    w = W[2] + 22;
	    add(getLabel("Bpm", "bpmLabel", C[0], C[16], x, y, w, h, null));
	    x += w - 1;

	    // bpms
	    w = W[1];
     	for (int i = 60; i < 260; i += 10) {
			add(getLabel("" + i, "b-" + i, C[12], C[0], x, y, w, h, listener));
			x += w + 1;
		}
     	set("b-" + bpm);
	   
	    // bpm value
	    w = W[2] + 22;
	    add(getLabel("" + bpm, "bpmValue", C[16], C[0], x, y, w, h, null));
	    x += w - 1;

     	
     	// instrument section ==================================================================
     	
		// create instruments
        int id = 35;
     	for (String instrumentName: instrumentNames) {
     		if (!instrumentName.startsWith("_")) {
	     		Instrument instrument = new Instrument(instrumentName, id);
	     		instruments.add(instrument);
	     		instrumentMap.put(instrumentName, instrument);
	     		instrumentIdMap.put(id, instrument);
     		}	
	     	id++;
     	}
		
		x = 0;
		y += h + 1;

		// instrument names
		w = W[8];
		add(getLabel("Instrument", "instrument", C[12], C[0], x, y, w, h, null));
		y += h + 1;
     	for (Instrument instrument: instruments) {
			add(getLabel(instrument.id + " " + instrument.name, "i-" + instrument.id, C[12], C[0], x, y, w, h, null));
			y += h + 1;
		}

		// on/off
		x = W[8] + 1;
		y = (h + 1) * 3;
     	w = W[1];
		add(getLabel("x", "", C[12], C[0], x, y, w, h, null));
		y += h + 1;
     	for (Instrument instrument: instruments) {
			add(getLabel("", "x-" + instrument.id, C[12], C[0], x, y, w, h, listener));
			y += h + 1;
		}
     	
	}
	
	// open rhythm
	private void openRhythm(int num) throws Exception {
        rhythm = rhythms.get(num);
        log.debug("rhythm=" + NL + rhythm);

        // remove existing rhythm components
 		Component[] componentList = this.getComponents();
 		log.debug(componentList.length);
 		for(Component c: componentList){
 			String name = c.getName();
 		    if (name.startsWith("h-") || name.startsWith("p-")) { // heading or pulse
 		        this.remove(c);
 		        labels.remove(name);
 		    }
 		}
        
 		// build beats header
		x = W[8] + W[1] + 2;
		y = (h + 1) * 3; 
		w = W[1];
        for (int i = 0; i < rhythm.beats; i++) {
        	add(getLabel("" + (i + 1), "h-" + i, C[12], C[0], x, y, w, h, null));
        	x += W[1] + 1;
        	for (int j = 1; j < rhythm.subBeats; j++) {
        		add(getLabel("-", "h-" + i + "-" + j, C[12], C[0], x, y, w, h, null));
        		x += W[1] + 1;
            }
        }

        // build pulses and reset x
		y += h + 1;
		for (Instrument instrument: instruments) {
			instrument.pulses = new boolean[rhythm.getNumPulses()];
			x = W[8] + W[1] + 2;
			for (int i = 0, n = rhythm.getNumPulses(); i < n; i++) {
				add(getLabel("",  "p-" + instrument.id + "-" + i, C[12], C[0], x, y, w, h, listener));
				x += w + 1;
			}
			y += h + 1;
			
			unset("x-" + instrument.id);
		}
		
		// set pulses
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
			bpmInc = bpm;
            sequence = new Sequence(Sequence.PPQ, rhythm.subBeats);
            track = sequence.createTrack();
            
            log.debug("creating track for " + rhythm.name);
            createEvent(PROGRAM, CHANNEL, 1, 0, 0);
	        for (Instrument instrument: instruments) {
	        	if (!instrument.mute && instrument.pulses.length > 0) {
     				log.debug(instrument.id + " " + instrument.name);
	        		for (int i = 0, n = instrument.pulses.length; i < n; i++) {
		     			if (instrument.pulses[i]) {
		     				createEvent(NOTEON, CHANNEL, instrument.id, V[8], i);
		     				createEvent(NOTEOFF, CHANNEL, instrument.id, 0, i + 1);
		     			}
		        	}     
	        	}	
	        }
	        // so we always have a track of numPulses length.
	        createEvent(PROGRAM, CHANNEL, 1, 0, rhythm.getNumPulses());

	        // set and start the sequencer.
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(loopCount - 1);
            sequencer.setTempoFactor((float) bpm / (float) BPM);
            log.debug("sequencer.getTempoFactor()=" + sequencer.getTempoFactor());
            sequencer.start();
		} catch (Exception e) { 
			log.error(e);
	    }
    }
	
    private void createEvent(int type, int chan, int id, int vol, long tick) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(type, chan, id, vol);
            MidiEvent event = new MidiEvent(message, tick);
            track.add(event);
		} catch (Exception e) { 
			log.error(e);
	    }
    }


    public void meta(MetaMessage message) {
    	log.debug(message.getType());
        if (message.getType() == 47) {  // 47 is end of track
        	if (accelerate) {
        		bpmInc += inc;
        		sequencer.setTempoFactor((float) bpmInc / (float) BPM);
            	log.debug("sequencer.getTempoFactor()=" + sequencer.getTempoFactor());
            	labels.get("bpmValue").setText("" + bpmInc);
                sequencer.start();
        	} else {
                sequencer.stop();
                playing = false;
                JLabel l = labels.get("playStop");
                l.setText("Play");
                set(l);
        	}
        }
    }

    private void loadRhythms() {
    	rhythms.clear();
    	
    	Rhythm r = null;
		List<String> lines 	= Util.getLines(RHYTHMS_FILE);
		for (String line: lines) {
			/*
				@ basic-jazz-pattern
				$ http://www.freedrumlessons.com/drum-lessons/basic-jazz-pattern.php
				% 4|3
				#                 |1--2--3--4--
				Acoustic bass drum|-       -
				Acoustic snare    |   - -   - -
				Pedal hi-hat      |   -     -
				Open hi-hat       |-  - --  - -
			*/
			if (!line.startsWith("#")) {
				String[] arr = line.split(PIPE_DELIM);
				if (line.startsWith("@")) {
					r = new Rhythm();
					r.name = arr[0].substring(1).trim();
					rhythms.add(r);
				} else if (line.startsWith("$")) {
					r.src = arr[0].substring(1).trim();
				} else if (line.startsWith("%")) {
					r.beats = Integer.parseInt(arr[0].substring(1).trim());
					r.subBeats = Integer.parseInt(arr[1].trim());
				} else {
					r.instruments.add(line);
				}				
			}
		}
		log.debug(rhythms.size());
        rhythmNames = new String[rhythms.size()];
        for (int i = 0, n = rhythms.size(); i < n; i++) {
        	log.debug(rhythms.get(i).name);
        	rhythmNames[i] = rhythms.get(i).name;
        }
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
    
    public void setRhythmInstruments() {
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
    }
    
    public void saveRhythms(boolean refresh) {
    	if (refresh) {
    		Collections.sort(rhythms, new Comparator<Rhythm>() {
    	        @Override
    	        public int compare(Rhythm r2, Rhythm r1) {
    	            return  r2.name.compareTo(r1.name);
    	        }
    	    });
    	}
    	for (Rhythm r: rhythms) {
    		log.debug(r.name);
    	}
    	String s = "";
    	for (Rhythm rhythm: rhythms) {
    		s += rhythm.toString() + NL;
    	}
    	Util.writeToFile(RHYTHMS_FILE, s);
    	if (refresh) {
    		loadRhythms();
    		comboBox.setModel(new DefaultComboBoxModel<String>(rhythmNames));
    	}
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
            //} else if (name.equals("clear")) {
            	//
            } else if (name.startsWith("inc-")) {
            	unset("inc-" + inc);
            	inc = Integer.parseInt(l.getText());
            	set(l);
            } else if (name.startsWith("l-")) {
            	unset("l-" + loopCount);
            	loopCount = Integer.parseInt(l.getText());
            	set(l);
            } else if (name.startsWith("b-")) {
            	unset("b-" + bpm);
            	bpm = Integer.parseInt(l.getText());
            	set(l);
            	labels.get("bpmValue").setText("" + bpm);
            } else if (name.startsWith("x-")) {
            	if (!playing) {
            		String[] arr = name.split("-");
            		int id = Integer.parseInt(arr[1]);
            		Instrument instrument = instrumentIdMap.get(id);
            		if (instrument.mute) {
            			l.setText("");
            			instrument.mute = false;
            		} else {
            			l.setText("x");	
            			instrument.mute = true;            			
            		}
            	}
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
            		labels.get("save").setForeground(Color.red); // flag 'save' btn
            	}
            } else if (name.equals("save")) {
            	setRhythmInstruments();
            	saveRhythms(false); // no need to refresh ui
            	labels.get(name).setForeground(Color.white);
            } else if (name.equals("saveAs")) {
			    String s = (String) JOptionPane.showInputDialog(
						null,
						"File Name",
						"Save As ...",
						JOptionPane.PLAIN_MESSAGE,
						null,
						null,
						rhythm.name);
			    if ((s != null) && (s.length() > 0) && !s.equals(rhythm.name)) {
			    	log.debug(name + ": " + s);
			    	setRhythmInstruments();
			    	Rhythm r = rhythm.clone();
			    	r.name = s;
			    	rhythms.add(r);
			    	log.debug(r.toString());
			    	saveRhythms(true); // refresh ui
			    }
            } else if (name.equals("accelerate")) {
            	if (accelerate) {
            		accelerate = false;
            		unset(l);
            		unset("l-" + loopCount);
            		loopCount = defaultLoopCount;
            		set(labels.get("l-" + defaultLoopCount));
            	} else {
            		accelerate = true;
            		set(l);
            		unset("l-" + loopCount);
            		loopCount = 8;
            		set(labels.get("l-8"));
            	}
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
    	String src = "";
    	List<String> instruments = new ArrayList<String>();
    	
    	public Rhythm clone() {
    		Rhythm r = new Rhythm();
    		r.beats = beats;
    		r.subBeats = subBeats;
	    	for (String instrument: rhythm.instruments) {
	    		r.instruments.add(instrument);
	    	}
    		return r;
    	}
    	public int getNumPulses() {
    		return beats * subBeats;
    	}
    	
		/*
			@ basic-jazz-pattern
			$ http://www.freedrumlessons.com/drum-lessons/basic-jazz-pattern.php
			% 4|3
			#                 |1--2--3--4--
			Acoustic bass drum|-       -
			Acoustic snare    |   - -   - -
			Pedal hi-hat      |   -     -
			Open hi-hat       |-  - --  - -
		*/
    	public String toString() {
    		String s = "@" + name + NL;
    		s += "$" + src + NL;
    		s += "% " + beats + PIPE + subBeats + NL;
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
        boolean mute = false;
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
			new RhythmPanel(new Rectangle(rx, ry, rw, rh + 12 + 25));
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
} 
