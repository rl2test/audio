package audio.chords.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import org.apache.log4j.Logger;

import javax.swing.event.*;
import javax.sound.midi.*;
import java.util.Vector;

public class GrooveJ extends JPanel implements ActionListener, MetaEventListener {
	/** The log. */
	Logger log = Logger.getLogger(getClass());
    final int PROGRAM = 192;
    final int NOTEON = 144;
    final int NOTEOFF = 128;
    int velocity = 100;
    //TempoDial tempoDial = new TempoDial();
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    TableModel dataModel;
    JTable table;
    int row, col;
    JButton loopB, startB;
    JComboBox combo;
    String instruments[] = 
        { "Acoustic bass drum", "Bass drum 1", "Side stick", "Acoustic snare",
          "Hand clap", "Electric snare", "Low floor tom", "Closed hi-hat",
          "High floor tom", "Pedal hi-hat", "Low tom", "Open hi-hat", 
          "Low-mid tom", "Hi-mid tom", "Crash cymbal 1", "High tom", 
          "Ride cymbal 1", "Chinese cymbal", "Ride bell", "Tambourine", 
          "Splash cymbal", "Cowbell", "Crash cymbal 2", "Vibraslap", 
          "Ride cymbal 2", "Hi bongo", "Low bongo", "Mute hi conga", 
          "Open hi conga", "Low conga", "High timbale", "Low timbale", 
          "High agogo", "Low agogo", "Cabasa", "Maracas", 
          "Short whistle", "Long whistle", "Short guiro", "Long guiro", 
          "Claves", "Hi wood block", "Low wood block", "Mute cuica", 
          "Open cuica", "Mute triangle", "Open triangle" };
    Vector data = new Vector(instruments.length);
	AudioController ac = null;


    public GrooveJ() {
        setLayout(new BorderLayout(5,0));

        for (int i = 0, id = 35; i < instruments.length; i++, id++) {
            data.add(new Data(id + " " + instruments[i], id));
        }

        final String[] names = { "Instrument", 
                                 "1", "a", "2", "a", "3", "a",
                                 "4", "a", "5", "a", "6", "a"};

        dataModel = new AbstractTableModel() {
            public int getColumnCount() { return names.length; }
            public int getRowCount() { return data.size();}
            public Object getValueAt(int row, int col) { 
                if (col == 0) {
                    return ((Data) data.get(row)).name;
                } else {
                    return ((Data) data.get(row)).staff[col-1];
                }
            }
            public String getColumnName(int col) { return names[col]; }
            public Class getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }
            public boolean isCellEditable(int row, int col) {
                return col == 0 ? false : true;
            }
            public void setValueAt(Object aValue, int row, int col) {
                if (col == 0) {
                    ((Data) data.get(row)).name = (String) aValue;
                } else {
                    ((Data) data.get(row)).staff[col-1] = (Color) aValue;
                }
            }
        };

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            public void setValue(Object value) {
                setBackground((Color) value);
            }
        };

        table = new JTable(dataModel);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getColumn(names[0]).setMinWidth(120);
        TableColumnModel tcm = table.getColumnModel();
        for (int i = 1; i < names.length; i++) {
            TableColumn col = tcm.getColumn(i);
            col.setCellRenderer(renderer);
        }

        // Listener for row changes
        ListSelectionModel lsm = table.getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel sm = (ListSelectionModel) e.getSource();
                if (!sm.isSelectionEmpty()) {
                    row = sm.getMinSelectionIndex();
                }
            }
        });

        // Listener for column changes
        lsm = table.getColumnModel().getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel sm = (ListSelectionModel) e.getSource();
                if (!sm.isSelectionEmpty()) {
                    col = sm.getMinSelectionIndex();
                }
                if (col != 0) {
                    Color c = ((Data) data.get(row)).staff[col-1];
                    if (c.equals(Color.white)) {
                        ((Data) data.get(row)).staff[col-1] = Color.black;
                    } else {
                        ((Data) data.get(row)).staff[col-1] = Color.white;
                    }
                    table.tableChanged(new TableModelEvent(dataModel));
                }
            }
        });
        
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        SoftBevelBorder sbb = new SoftBevelBorder(BevelBorder.RAISED);
        //p1.add(tempoDial);
        p1.add(Box.createVerticalStrut(10));

        JPanel p2 = new JPanel(new GridLayout(0,1,2,10));
        p2.add(startB = makeButton("Start", getBackground()));
        p2.add(loopB = makeButton("Loop", getBackground()));
        p2.add(makeButton("Clear Table", getBackground()));

        combo = new JComboBox();
        combo.addActionListener(this);
        combo.addItem("Jig 1");
        combo.addItem("Jig 2");
        combo.addItem("Jig 3");
        p2.add(combo);

        p1.add(p2);
        p1.add(Box.createVerticalStrut(120));
        add("West", p1);

        add("Center", new JScrollPane(table));
    }


    public void open() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
        } catch (Exception e) { e.printStackTrace(); }
        //tempoDial.setSequencer(sequencer);
        sequencer.addMetaEventListener(this);
    }


    public void close() {
        if (startB.getText().startsWith("Stop")) {
            startB.doClick(0);
        }
        if (sequencer != null) {
            sequencer.close();
        }
        sequencer = null;
    }


    private JButton makeButton(String bName, Color c) {
        JButton b = new JButton(bName);
        b.setBackground(c);
        b.addActionListener(this);
        return b;
    }


    private void buildTrackThenStartSequencer() {
        //Sequence sequence = null;
        try {
           sequence = new Sequence(Sequence.PPQ, 4);
        } catch (Exception ex) { ex.printStackTrace(); }
        track = sequence.createTrack();
        createEvent(PROGRAM, 9, 1, 0);
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
        // so we always have a track from 0 to 15.
        createEvent(PROGRAM, 9, 1, 12);

        // set and start the sequencer.
        try {
            sequencer.setSequence(sequence);
        } catch (Exception ex) { ex.printStackTrace(); }
        sequencer.setLoopCount(1000);
        sequencer.start();
        //sequencer.setTempoInBPM(tempoDial.getTempo());
        int bpm = AudioController.getInstance().timePanel.endTempo;
        log.debug("bpm=" + bpm);
        sequencer.setTempoInBPM(bpm);
    }


    private void presetTracks(int num) {

        final int ACOUSTIC_BASS = 35;
        final int BASS_DRUM_1 = 36;
        final int ACOUSTIC_SNARE = 38;
        final int HAND_CLAP = 39;
        final int LOW_FLOOR_TOM = 41;
        final int CLOSED_HIHAT = 42;
        final int PEDAL_HIHAT = 44;
        final int LO_TOM = 45;
        final int HI_MID_TOM = 48;
        final int CRASH_CYMBAL1 = 49;
        final int HI_TOM = 50;
        final int RIDE_BELL = 53;

        clearTable();

        switch (num) {
            case 0 : 	//setCell(48, 2);
            			//setCell(48, 4);
            			setCell(45, 8);
            			//setCell(48, 10);

            			/*setCell(44, 1);
            			setCell(44, 3);
            			setCell(44, 5);
	                    setCell(44, 7);
	                    setCell(44, 9);
	                    setCell(44, 11);*/
	                    //setCell(45, 5);
	                    setCell(41, 4);
	                    setCell(41, 10);
	                    int bass1[] = { 0, 6 };
	                    for (int i = 0; i < bass1.length; i++) {
	                        setCell(35, bass1[i]); 
	                    }
	                    break;
            case 1 :	setCell(ACOUSTIC_SNARE, 4);
			            setCell(LO_TOM, 8);
			            setCell(LO_TOM, 9);
			            setCell(ACOUSTIC_SNARE, 10);
			            int bass2[] = { 0, 6 };
			            for (int i = 0; i < bass2.length; i++) {
			                setCell(ACOUSTIC_BASS, bass2[i]); 
			            }
			            break;
            case 2 : 	setCell(ACOUSTIC_SNARE, 4);
			            setCell(LO_TOM, 8);
			            setCell(LO_TOM, 9);
			            setCell(ACOUSTIC_SNARE, 10);
			            int bass3[] = { 0, 6 };
			            for (int i = 0; i < bass3.length; i++) {
			                setCell(ACOUSTIC_BASS, bass3[i]); 
			            }
			            break;
            default :
        }
        table.tableChanged(new TableModelEvent(dataModel));
    }


    private void setCell(int id, int tick) {
        for (int i = 0; i < data.size(); i++) {
            Data d = (Data) data.get(i);
            if (d.id == id) {
                d.staff[tick] = Color.black;
                break;
            }
        }
    }


    private void clearTable() {
        for (int i = 0; i < data.size(); i++) {
            Data d = (Data) data.get(i);
            for (int j = 0; j < d.staff.length; j++) {
                d.staff[j] = Color.white;
            }
        }
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
            if (loopB.getBackground().equals(Color.gray)) {
                if (sequencer != null && sequencer.isOpen()) {
                	log.debug(message.getType());
                	//sequencer.start();
                    //sequencer.setTempoInBPM(tempoDial.getTempo());
                }
            } else {
                startB.setText("Start");
            }
        }
    }


    public void actionPerformed(ActionEvent e) {
        Object object = e.getSource();
        if (object instanceof JComboBox) {
            presetTracks(((JComboBox) object).getSelectedIndex());
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


    /**
     * Storage class for instrument and musical staff represented by color.
     */
    class Data extends Object {
        String name; int id; Color staff[] = new Color[16];
        public Data(String name, int id) {
            this.name = name;
            this.id = id;
            for (int i = 0; i < staff.length; i++) {
                staff[i] = Color.white;
            }
        }
    }


    public void init() {
        final JFrame f = new JFrame("Rhythm Groove Box");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	//System.exit(0);
            	f.dispose();
            }
        });
        f.getContentPane().add("Center", this);
        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 640;
        int h = 440;
        f.setLocation(screenSize.width/2 - w/2, screenSize.height/2 - h/2);
        f.setSize(w, h);
        f.show();
        f.setVisible(true);
        this.open();
    }
    
    public static void main(String args[]) {
        final GrooveJ groove = new GrooveJ();
        groove.init();
        /*
        JFrame f = new JFrame("Rhythm Groove Box");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        f.getContentPane().add("Center", groove);
        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 640;
        int h = 440;
        f.setLocation(screenSize.width/2 - w/2, screenSize.height/2 - h/2);
        f.setSize(w, h);
        f.show();
        groove.open();
        */
    }
} 
