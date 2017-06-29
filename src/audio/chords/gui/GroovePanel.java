package audio.chords.gui;

import static audio.Constants.C;
import static audio.Constants.FONT;
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



@SuppressWarnings({ "serial" })
public class GroovePanel extends AudioPanel implements MetaEventListener {
	final Logger log 							= Logger.getLogger(getClass());
	final int CONTROL 							= 176;
	final int PROGRAM 							= 192;
	final int BPM 								= 120;
	int bpm 									= BPM;
	int bpmInc 									= bpm; // init
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	final JComboBox<String> comboBox			= new JComboBox<String>();
	final List<Voice> voices 					= new ArrayList<Voice>();
	final Map<String, Voice> voiceMap 			= new HashMap<String, Voice>();
	final Map<Integer, Voice> voiceIdMap 		= new HashMap<Integer, Voice>();
	boolean playing 							= false;
	final Listener listener 					= new Listener();
	GrooveUtil gu 								= GrooveUtil.getInstance();
	Groove groove;
	boolean accelerate 							= false; // default
	int inc 									= 1; // default
	final int defaultLoopCount 					= 1000;
	int loopCount 								= defaultLoopCount; // default
	final Map<String, Integer[]> chordMap 		= new HashMap<String, Integer[]>();
	final Map<String, Integer> noteToValueMap	= new HashMap<String, Integer>(); 
	final String[] chordTypes 					= {"maj7", "m7", "7"};
	final Integer[] CHORD_7 					= {4, 7, 10};
	final Integer[] CHORD_MINOR_7 				= {3, 7, 10};
	final Integer[] CHORD_MAJOR_7 				= {4, 7, 11};
	final int CHANNEL_BASS 						= 0; // TODO set pan
	final int CHANNEL_CHRD	 					= 1; // TODO set pan
	final int CHANNEL_PERC 						= 9;
	final int VOL_BASS 							= V[8];
	final int VOL_CHRD 							= V[6];
	final int VOL_PERC 							= V[5];
	String chord 								= "C"; // default
	String chordType 							= "7"; // default
	final int baseNoteValue						= 36;
	static final int heightExtension			= 38;
	
	public GroovePanel(Rectangle r) throws Exception {
    	super(null);
        setBackground(C[4]);
        
		final JFrame frame = new JFrame("Groove");
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
		
		// init voices with cloned voices from ru
		for (String voiceKey: gu.voiceKeys) {
			Voice voice = new Voice(voiceKey, gu.voiceMap.get(voiceKey));
			voices.add(voice);
			voiceMap.put(voice.name, voice);
			voiceIdMap.put(voice.id, voice);
		}

		initUi();
		
		frame.getContentPane().add("Center", this);
		frame.validate();
		frame.repaint();
		
     	openSequencer();
     	openGroove(0);	// load the first
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
	    comboBox.setModel(new DefaultComboBoxModel<String>(gu.grooveNames));
	    comboBox.setName("comboBox");
	    comboBox.addItemListener(new ComboListener());
	    comboBox.setBounds(x, y, w, h);
	    comboBox.setFont(FONT);
		add(comboBox);
		comboBox.setSelectedItem(gu.grooveNames[0]);
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

     	// voice section ==================================================================
		
		x = 0;
		y += h + 1;

		// voice names
		w = W[8];
		add(getLabel("Voice", "voice", C[12], C[0], x, y, w, h, null));
		y += h + 1;
     	for (Voice voice: voices) {
			add(getLabel(voice.id + " " + voice.name, "v-" + voice.id, C[12], C[0], x, y, w, h, null));
			y += h + 1;
		}

		// on/off
		x = W[8] + 1;
		y = (h + 1) * 4;
     	w = W[1];
		add(getLabel("x", "", C[12], C[0], x, y, w, h, null));
		y += h + 1;
     	for (Voice voice: voices) {
			add(getLabel("", "x-" + voice.id, C[12], C[0], x, y, w, h, listener));
			y += h + 1;
		}
     	
	}
	
	// open groove
	private void openGroove(int num) throws Exception {
        groove = gu.grooves.get(num);
        log.debug("groove=" + NL + groove);

        // remove existing groove components
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
        for (int i = 0; i < groove.beats; i++) {
        	add(getLabel("" + (i + 1), "h-" + i, C[12], C[0], x, y, w, h, null));
        	x += W[1] + 1;
        	for (int j = 1; j < groove.subBeats; j++) {
        		add(getLabel("-", "h-" + i + "-" + j, C[12], C[0], x, y, w, h, null));
        		x += W[1] + 1;
            }
        }

        // build pulses and reset x
		y += h + 1;
		for (Voice voice: voices) {
			voice.pulses = new boolean[groove.numPulses];
			x = W[8] + W[1] + 2;
			for (int i = 0, n = groove.numPulses; i < n; i++) {
				add(getLabel("",  "p-" + voice.id + "-" + i, C[12], C[0], x, y, w, h, listener));
				x += w + 1;
			}
			y += h + 1;
			
			unset("x-" + voice.id);
		}
		
		// set pulses
     	for (String voiceStr: groove.voiceStrs) {
     		String[] arr = voiceStr.split(PIPE_DELIM);
			String voiceName = arr[0].trim();
     		Voice voice = voiceMap.get(voiceName);
			String pulseStr = arr[1]; 
			for (int i = 0, n = pulseStr.length(); i < n; i++) {
				String s = pulseStr.substring(i, i + 1);
				if (s.equals("-")) {
					voice.pulses[i] = true;
     				log.debug("p-" + voice.id + "-" + i);
     				set("p-" + voice.id + "-" + i);
				}
			}
     	}

     	repaint();
	}
	
	private void startSequencer() {
		try {
			bpmInc = bpm; // reset
            sequence = new Sequence(Sequence.PPQ, groove.subBeats);
            track = sequence.createTrack();
            
            int root = baseNoteValue + noteToValueMap.get(chord);
            log.debug("root=" + root);
            int fifth = root + 7;
            int octave = root + 12;
            
            log.debug("creating track for " + groove.name);
            createEvent(PROGRAM, CHANNEL_PERC, 1, 0, 0);
	        // so we always have a track of numPulses length.
	        createEvent(PROGRAM, CHANNEL_PERC, 1, 0, groove.numPulses);
	        
	        createEvent(CONTROL, CHANNEL_BASS, 10, V[0], 0); // set pan
	        createEvent(CONTROL, CHANNEL_CHRD, 10, V[8], 0); // set pan
	        createEvent(CONTROL, CHANNEL_PERC, 10, V[4], 0); // set pan
            
	        for (Voice voice: voices) {
	        	if (!voice.mute) {
     				log.debug(voice.id + " " + voice.name);
     				for (int i = 0, n = voice.pulses.length; i < n; i++) {
		     			if (voice.pulses[i]) {
	     					int len = groove.subBeats;
	     					if (i + len > n) {
	     						len = n - i;
	     					}
		     				if (voice.id == gu.ROOT) {
		     					createNote(CHANNEL_BASS, root, VOL_BASS, i, len);
		     				} else if (voice.id == gu.FIFTH) {
		     					createNote(CHANNEL_BASS, fifth, VOL_BASS, i, len);
		     				} else if (voice.id == gu.CHORD) {
		     					Integer[] arr = chordMap.get(chordType);
		     					for(int a: arr) {
		     						createNote(CHANNEL_CHRD, octave + a, VOL_CHRD, i, len);
		     					}
		     				} else { // perc - len is 1
		     					createNote(CHANNEL_PERC, voice.id, VOL_PERC, i, 1);
		     				} 
		     			}
		        	}     
	        	}	
	        }

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
    	createEvent(ShortMessage.NOTE_ON, chan, note, vol, tick);
		createEvent(ShortMessage.NOTE_OFF, chan, note, 0, tick + len);
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
    
    public void setGrooveVoiceStrs() {
    	groove.voiceStrs.clear();
    	for (Voice voice: voices) {
    		boolean save = false;
    		for (boolean pulse: voice.pulses) {
    			if (pulse) {
    				save = true;
    				break;
    			}
    		}
    		if (save) {
    			String voiceStr = voice.name + GrooveUtil.getSpaces(voice.name) + PIPE;
    			for (boolean pulse: voice.pulses) {
    				voiceStr += (pulse) ? "-" : " ";
        		}
    			voiceStr.trim();
    			groove.voiceStrs.add(voiceStr);
    		}
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
            	setGrooveVoiceStrs();
            	gu.saveGrooves(false); // no need to refresh ui
            	labels.get(name).setForeground(Color.white);
            } else if (name.equals("saveAs")) {
			    String s = (String) JOptionPane.showInputDialog(
						null,
						"File Name",
						"Save As ...",
						JOptionPane.PLAIN_MESSAGE,
						null,
						null,
						groove.name);
			    if ((s != null) && (s.length() > 0) && !s.equals(groove.name)) {
			    	log.debug(name + ": " + s);
			    	setGrooveVoiceStrs();
			    	Groove r = groove.clone();
			    	r.name = s;
			    	gu.grooves.add(r);
			    	log.debug(r.toString());
			    	gu.saveGrooves(true); // refresh ui
			    	comboBox.setModel(new DefaultComboBoxModel<String>(gu.grooveNames));
			    }
            } else if (name.equals("clear")) {
            	for (Voice voice: voices) {
            		int id = voice.id;
            		for (int i = 0; i < voice.pulses.length; i++) {
            			if (voice.pulses[i]) {
            				voice.pulses[i] = false;
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
            		Voice voice = voiceIdMap.get(id);
            		if (voice.mute) {
            			l.setText("");
            			voice.mute = false;
            		} else {
            			l.setText("x");	
            			voice.mute = true;            			
            		}
            	}
            } else if (name.startsWith("p-")) { // pulse
            	if (!playing) {
            		String[] arr = name.split("-");
            		int id = Integer.parseInt(arr[1]);
            		int pulse = Integer.parseInt(arr[2]);
            		Voice voice = voiceIdMap.get(id);
            		if (voice.pulses[pulse]) {
            			unset(l);	
            			voice.pulses[pulse] = false;
            		} else {
            			set(l);	
            			voice.pulses[pulse] = true;            			
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
					openGroove(index);
				} catch (Exception e) {
					log.error(e);
				}
	        }
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
			new GroovePanel(new Rectangle(rx, ry, rw, rh + heightExtension));  // 12 + 25 + 1
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
} 
