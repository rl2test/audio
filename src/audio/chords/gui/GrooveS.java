package audio.chords.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class GrooveS extends JPanel implements ActionListener, MetaEventListener {
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
    JButton startB;
    JComboBox<String> combo;
    String instruments[] = { 
    		"Acoustic bass drum", "Bass drum 1", "Side stick", "Acoustic snare",
	        "Hand clap", "Electric snare", "Low floor tom", "Closed hi-hat",
	        "High floor tom", "Pedal hi-hat", "Low tom", "Open hi-hat", 
	        "Low-mid tom", "Hi-mid tom" };
    List<Data> data = new ArrayList<Data>();
	AudioController ac = null;
	Map<String, Integer> map = new HashMap<String, Integer>();
	int numColumns = 0;

	public GrooveS() {
    	int j = 35;
     	for (String instrument: instruments) {
     		map.put(instrument, j);
     		j++;
     	}
    	
        setLayout(new BorderLayout(5,0));

        final String[] columns = { "Instrument", 
                "1", "-", "2", "-", "3", "-",
                "4", "-", "5", "-", "6", "-",
                "7", "-", "8", "-", "9", "-"
                };
        numColumns = columns.length;
        log.debug("numColumns=" + numColumns);
        
        for (int i = 0, id = 35; i < instruments.length; i++, id++) {
            data.add(new Data(id + " " + instruments[i], id));
        }

        dataModel = new AbstractTableModel() {
            public int getColumnCount() { 
            	return columns.length;
            }
            public int getRowCount() { 
            	return data.size();
            }
            public Object getValueAt(int row, int col) { 
            	log.debug("getValueAt: " + row + " " + col);
                if (col == 0) {
                	log.debug(data.get(row).name);
                    return (data.get(row)).name;
                } else {
                	log.debug(data.get(row).staff[col-1]);
                    return (data.get(row)).staff[col-1];
                }
            }
            public String getColumnName(int col) { 
            	return columns[col]; 
            }
            public Class getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }
            public boolean isCellEditable(int row, int col) {
                return col == 0 ? false : true;
            }
            public void setValueAt(Object aValue, int row, int col) {
            	log.debug("setValueAt: " + row + " " + col);
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

        log.debug("numColumns=" + numColumns);
        
        table = new JTable(dataModel);
        table.setGridColor(Color.LIGHT_GRAY);
        table.getColumn(columns[0]).setMinWidth(150);
        TableColumnModel tcm = table.getColumnModel();
        for (int i = 1; i < columns.length; i++) {
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
        p1.add(Box.createVerticalStrut(10));

        JPanel p2 = new JPanel(new GridLayout(0,1,2,10));
        p2.add(startB = makeButton("Start", getBackground()));
        p2.add(makeButton("Clear Table", getBackground()));

        combo = new JComboBox<String>();
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
                     createEvent(NOTEOFF, 9, d.id, j + 1); 
                }
            }
        }
        // 
        createEvent(PROGRAM, 9, 1, numColumns - 1);

        // set and start the sequencer.
        try {
            sequencer.setSequence(sequence);
        } catch (Exception ex) { ex.printStackTrace(); }
        sequencer.setLoopCount(1000);
        //sequencer.setTempoInBPM(tempoDial.getTempo());
        int bpm = 180; //AudioController.getInstance().timePanel.endTempo;
        log.debug("bpm=" + bpm);
        sequencer.setTempoInBPM(bpm);
        
        sequencer.start();

    }


    private void presetTracks(int num) {

    	/*
    	 	35 "Acoustic bass drum", 
    	 	36 "Bass drum 1", 
    	 	37 "Side stick", 
    	 	38 "Acoustic snare",
	        39 "Hand clap", 
	        40 "Electric snare", 
	        41 "Low floor tom", 
	        42 "Closed hi-hat",
	        43 "High floor tom", 
	        44 "Pedal hi-hat", 
	        45 "Low tom", 
	        46 "Open hi-hat", 
	        47 "Low-mid tom", 
	        48 "Hi-mid tom"
    	 */
    	
        clearTable();

        switch (num) {
            case 0 : 	setCell(map.get("Low floor tom"), 0);
            			setCell(map.get("Low floor tom"), 6);
            			setCell(map.get("Low floor tom"), 12);
            			setCell(map.get("Hi-mid tom"), 4);
            			setCell(map.get("Hi-mid tom"), 10);
            			setCell(map.get("Hi-mid tom"), 14);
            			setCell(map.get("Hi-mid tom"), 16);
	                    setCell(map.get("Low tom"), 15);
	                    break;
            case 1 :	break;
            case 2 : 	break;
            default :
        }
        table.tableChanged(new TableModelEvent(dataModel));
    }


    private void setCell(int id, int tick) {
        for (int i = 0; i < data.size(); i++) {
            Data d = (Data) data.get(i);
            log.debug("setCell: " + d.id + " " + id);
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
            startB.setText("Start");
        }
    }

    @SuppressWarnings("unchecked")
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
        String name; int id; Color staff[] = new Color[numColumns - 1];
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
        //f.show();
        f.setVisible(true);
        this.open();
    }
    
    public static void main(String args[]) {
        final GrooveS groove = new GrooveS();
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
