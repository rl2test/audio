package audio.chords.gui;

import static audio.Constants.C;
import static audio.Constants.FONT;
import static audio.Constants.GROOVES_FILE;
import static audio.Constants.NL;
import static audio.Constants.PIPE;
import static audio.Constants.PIPE_DELIM;
import static audio.Constants.TRANSPOSE_KEYS;
import static audio.Constants.TRANSPOSE_KEY_INTERVALS;
import static audio.Constants.V;
import static audio.Constants.W;

import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import audio.Util;

@SuppressWarnings({ "serial" })
public class GroovePanel extends AudioPanel implements MetaEventListener {
	final Logger log 								= Logger.getLogger(getClass());
	final int PROGRAM 								= 192;
	final int NOTE_ON 								= 144;
	final int NOTE_OFF 								= 128;
	final int BPM 									= 120;
	int bpm 										= BPM;
	int bpmInc 										= bpm; // init
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	final JComboBox<String> comboBox				= new JComboBox<String>();
	final String instrumentNames[]					= { 
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
	        "Hi-mid tom",
	        "Chord",
	        "Fifth",
	        "Root"
	};
	final int maxInstrumentNameLen 					= instrumentNames[0].length();
	final List<Instrument> instruments 				= new ArrayList<Instrument>();
	final Map<String, Instrument> instrumentMap 	= new HashMap<String, Instrument>();
	final Map<Integer, Instrument> instrumentIdMap 	= new HashMap<Integer, Instrument>();
	boolean playing 								= false;
	final Listener listener 						= new Listener();
	List<Rhythm> rhythms 							= new ArrayList<Rhythm>();
	String[] rhythmNames;
	Rhythm rhythm;
	boolean accelerate 								= false; // default
	int inc 										= 1; // default
	final int defaultLoopCount 						= 1000;
	int loopCount 									= defaultLoopCount; // default
	final Map<String, Integer[]> chordMap 			= new HashMap<String, Integer[]>();
	final Map<String, Integer> noteToValueMap 		= new HashMap<String, Integer>(); 
	final String[] chordTypes 						= {"maj7", "m7", "7"};
	final Integer[] CHORD_7 						= {4, 7, 10};
	final Integer[] CHORD_MINOR_7 					= {3, 7, 10};
	final Integer[] CHORD_MAJOR_7 					= {4, 7, 11};
	final int CHANNEL_BASS 							= 0; // TODO set pan
	final int CHANNEL_CHRD	 						= 1; // TODO set pan
	final int CHANNEL_PERC 							= 9;
	final int VOL_BASS 								= V[8];
	final int VOL_CHRD 								= V[6];
	final int VOL_PERC 								= V[5];
	String chord 									= "C"; // default
	String chordType 								= "7"; // default
	final int baseNoteValue 						= 36;
	
	public GroovePanel(Rectangle r) throws Exception {
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
		
		// init keys
		int i = 0;
		for (String note: TRANSPOSE_KEYS) {
			noteToValueMap.put(note, TRANSPOSE_KEY_INTERVALS[i]);
			i++;
		}
		
		// init chordMap
		chordMap.put("7", CHORD_7);
		chordMap.put("m7", CHORD_MINOR_7);
		chordMap.put("maj7", CHORD_MAJOR_7);
		
		
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
	    
 	    // row 1 - grooves / save / save as ================================================================
 	    
	    // play/stop label
	    w = W[2];
	    add(getLabel("Play", "playStop", C[6], C[16], x, y, w, h, listener));
	    x += w + 1;  
	    
	    // grooves combo box
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
	    
	    add(getLabel("Clear", "clear", C[6], C[16], x, y, w, h, listener));
	    x += w + 1;  

     	// row 2 - bpm ================================================================
        x = 0;
        y += h + 1;
	    
	    // bpm label
	    w = W[2];
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
	    
	    // row 3 - accelerate ================================================================
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

	    // row 4 - chord ================================================================
	    x = 0;
	    y += h + 1;
	    
	    // chord
	    w = W[2];
	    add(getLabel("Chord", "chord", C[0], C[16], x, y, w, h, null));
	    x += w + 1;  
    	
	    //"C", "F", "Bb", "Eb",    "Ab", "Db", "Gb/F#", "B", "E", "A", "D", "G"
	    for (String note: TRANSPOSE_KEYS) {
	    	w = (note.equals("Gb/F#")) ? W[2] : W[1];
	    	add(getLabel(note, "c-" + note, C[12], C[0], x, y, w, h, listener));
	    	x += w + 1;
	    }
	    set("c-C");

	    // chord type
	    w = W[2];
	    add(getLabel("Type", "type", C[0], C[16], x, y, w, h, null));
	    x += w + 1;  

	    add(getLabel("7", "t-7", C[12], C[0], x, y, w, h, listener)); x += w + 1;
	    add(getLabel("m7", "t-m7", C[12], C[0], x, y, w, h, listener)); x += w + 1;
	    add(getLabel("maj7", "t-maj7", C[12], C[0], x, y, w, h, listener)); x += w + 1;
	    set("t-7");

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
		y = (h + 1) * 4;
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
		y = (h + 1) * 4; 
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
			bpmInc = bpm; // reset
            sequence = new Sequence(Sequence.PPQ, rhythm.subBeats);
            track = sequence.createTrack();
            
            int root = baseNoteValue + noteToValueMap.get(chord);
            log.debug("root=" + root);
            int fifth = root + 7;
            int octave = root + 12;
            
            log.debug("creating track for " + rhythm.name);
            createEvent(PROGRAM, CHANNEL_PERC, 1, 0, 0);
            
	        for (Instrument instrument: instruments) {
	        	if (!instrument.mute) {
     				log.debug(instrument.id + " " + instrument.name);
     				for (int i = 0, n = instrument.pulses.length; i < n; i++) {
		     			if (instrument.pulses[i]) {
	     					int len = rhythm.subBeats;
	     					if (i + len > n) {
	     						len = n - i;
	     					}
		     				if (instrument.name == "Root") {
		     					createNote(CHANNEL_BASS, root, VOL_BASS, i, len);
		     				} else if (instrument.name == "Fifth") {
		     					createNote(CHANNEL_BASS, fifth, VOL_BASS, i, len);
		     				} else if (instrument.name == "Chord") {
		     					Integer[] arr = chordMap.get(chordType);
		     					for(int a: arr) {
		     						createNote(CHANNEL_CHRD, octave + a, VOL_CHRD, i, len);
		     					}
		     				} else { // perc - len is 1
		     					createNote(CHANNEL_PERC, instrument.id, VOL_PERC, i, 1);
		     				} 
		     			}
		        	}     
	        	}	
	        }
	        // so we always have a track of numPulses length.
	        createEvent(PROGRAM, CHANNEL_PERC, 1, 0, rhythm.getNumPulses());

	        // set and start the sequencer.
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(loopCount - 1); // note: first time through is not counted as being a loop
            sequencer.setTempoFactor((float) bpm / (float) BPM);
            log.debug("sequencer.getTempoFactor()=" + sequencer.getTempoFactor());
            sequencer.start();
		} catch (Exception e) { 
			log.error(e);
	    }
    }
	
	// create note
    private void createNote(int chan, int note, int vol, long tick, int len) {
    	createEvent(NOTE_ON, chan, note, vol, tick);
		createEvent(NOTE_OFF, chan, note, 0, tick + len);
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
        		sequencer.setLoopCount(loopCount);
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
		List<String> lines 	= Util.getLines(GROOVES_FILE);
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
    	Util.writeToFile(GROOVES_FILE, s);
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
					labels.get("bpmValue").setText("" + bpm); // reset
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
            } else if (name.equals("clear")) {
            	for (Instrument instrument: instruments) {
            		int id = instrument.id;
            		for (int i = 0; i < instrument.pulses.length; i++) {
            			if (instrument.pulses[i]) {
                			instrument.pulses[i] = false;
                			unset(labels.get("p-" + id + "-" + i));
            			}
            		}
            	}
            } else if (name.startsWith("b-")) { // bpm
            	unset("b-" + bpm);
            	bpm = Integer.parseInt(l.getText());
            	set(l);
            	labels.get("bpmValue").setText("" + bpm);
            } else if (name.startsWith("inc-")) { // accelerate increment
            	unset("inc-" + inc);
            	inc = Integer.parseInt(l.getText());
            	set(l);
            } else if (name.startsWith("l-")) { // accelerate loop count
            	unset("l-" + loopCount);
            	loopCount = Integer.parseInt(l.getText());
            	set(l);
            } else if (name.startsWith("c-")) { // chord
            	unset("c-" + chord);
            	chord = l.getText();
            	set(l);
            } else if (name.startsWith("t-")) { // chord type
            	unset("t-" + chordType);
            	chordType = l.getText();
            	set(l);
            } else if (name.startsWith("x-")) { // x - mute
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
            } else if (name.startsWith("p-")) { // pulse
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
			new GroovePanel(new Rectangle(rx, ry, rw, rh + 12 + 25 + 1));
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
} 
