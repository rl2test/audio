package audio.synth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/*
parse txt file generated from excel and write to midi file
- has extraneous graphics left over from MidiSynth.java (sun examples)
- has problem w/ channel instrument allocation
*/

public class Synth extends JFrame implements ActionListener, MetaEventListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Synthesizer synthesizer;
	MidiChannel midiChannels[] = null;
	int tempo;
	int[] instruments = new int[16];
    Sequencer sequencer;
    Sequence sequence;
    Track track;
	JFileChooser fc;
	MyFilter mf = new MyFilter();	
	File file;
    JLabel status;
    JButton startB, saveB, selectB;
	JTextField jtf;
	File currentDir;
	String[] channels = new String[16];
	JComboBox jcb;
	int currentChannel = 0;
	int currentRow = 0;
	int currentCol = 0;
    private String names[] = { 
	    "Piano", "Chromatic Perc.", "Organ", "Guitar", 
	    "Bass", "Strings", "Ensemble", "Brass", 
	    "Reed", "Pipe", "Synth Lead", "Synth Pad",
	    "Synth Effects", "Ethnic", "Percussive", "Sound Effects" };
    private int nRows = 8;
    private int nCols = names.length; // just show 128 instruments
	JTable table;
	
	//constructor
	public Synth() {
        super("My Synthesizer");
		System.out.println("Synth()");
        addWindowListener(new WindowAdapter() {
            public void windowIconified(WindowEvent e) {
            }
            public void windowDeiconified(WindowEvent e) {
            }
            public void windowClosing(WindowEvent e) {
				closeSynth();
                System.exit(0);
            }  
        });		
		currentDir = new File(System.getProperty("user.dir"));
		fc = new JFileChooser(currentDir);
		//assign default instruments
		for(int i = 0; i < 16; i++){
			instruments[i] = i * 8;
		}
	}

	//initialize synthesizer
    public void openSynth() {
		System.out.println("openSynth()");
        try {
            if (synthesizer == null) {
                if ((synthesizer = MidiSystem.getSynthesizer()) == null) {
                    System.out.println("getSynthesizer() failed!");
                    return;
                }
            } 
            synthesizer.open();
            sequencer = MidiSystem.getSequencer();
            sequencer.addMetaEventListener(this);
	        sequencer.open();
        }catch(Exception ex){
			ex.printStackTrace();
			return;
		}
		midiChannels = synthesizer.getChannels();
    }

	public void createGui(){
		System.out.println("createGui()");
		ImageIcon openIcon = new ImageIcon("images/open.gif");
		ImageIcon saveIcon = new ImageIcon("images/save.gif");
		ImageIcon soundIcon = new ImageIcon("images/sound.gif");
		
		//top panel
		JPanel jp = new JPanel();
        getContentPane().add(jp, BorderLayout.CENTER);
		jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));		
		
    	//status bar
	    status = createLabel(currentDir + "\\Synth.java");
        getContentPane().add(status, BorderLayout.SOUTH);		

		//3 panels
		JPanel jp1 = new JPanel();
		jp1.setLayout(new BoxLayout(jp1, BoxLayout.X_AXIS));
		jp1.setAlignmentX(LEFT_ALIGNMENT);
		JPanel jp2 = new JPanel();
		jp2.setLayout(new BoxLayout(jp2, BoxLayout.X_AXIS));
		jp2.setAlignmentX(LEFT_ALIGNMENT);
		JPanel jp3 = new JPanel();
		jp3.setLayout(new BoxLayout(jp3, BoxLayout.X_AXIS));
		jp3.setAlignmentX(LEFT_ALIGNMENT);
		jp.add(jp1);
		jp.add(Box.createRigidArea(new Dimension(0,10)));
		jp.add(jp2);
		jp.add(Box.createRigidArea(new Dimension(0,10)));
		jp.add(jp3);
		jp.add(Box.createVerticalGlue());
		
        selectB = createButton("Select","Select text file", openIcon, true);
        startB = createButton("Start","Start playing sequence", soundIcon, false);
        saveB = createButton("Save","Save sequence to midi file", saveIcon, false);
		jp1.add(selectB);				
		jp1.add(startB);
		jp1.add(saveB);		
		
		JLabel fileLabel = createLabel("File");
		jtf = new JTextField();
		//jtf.setColumns(20);
		jtf.setMaximumSize(new Dimension(300,20));
		jtf.setAlignmentY(TOP_ALIGNMENT);
		jtf.setAlignmentX(LEFT_ALIGNMENT);
		jp2.add(fileLabel);
		jp2.add(jtf);

		//channel combo
		for(int i = 0; i < 16; i++){channels[i] = "Channel " + i;}
		jcb = new JComboBox(channels);
		jcb.setSelectedIndex(0);
		jcb.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
        		JComboBox cb = (JComboBox)e.getSource();
		        currentChannel = cb.getSelectedIndex();
				System.out.println(currentChannel);
				int i = instruments[currentChannel];
				int c = i/nRows;
				int r = i % nRows;
				table.changeSelection(r,c,false,false);
		    }
		});
		jcb.setAlignmentY(TOP_ALIGNMENT);
		jcb.setAlignmentX(LEFT_ALIGNMENT);
		//instruments
		TableModel dataModel = new AbstractTableModel() {
		    public int getColumnCount() { return nCols; }
		    public int getRowCount() { return nRows;}
		    public Object getValueAt(int r, int c) { 
	            return Integer.toString(c*nRows+r);
		    }
		    public String getColumnName(int c) { 
		        return names[c];
		    }
		    public Class getColumnClass(int c) {
		        return getValueAt(0, c).getClass();
		    }
		    public boolean isCellEditable(int r, int c) {return false;}
		    public void setValueAt(Object obj, int r, int c) {}
		};
		table = new JTable(dataModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Listener for row changes
		ListSelectionModel lsm = table.getSelectionModel();
		lsm.addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent e) {
		        ListSelectionModel sm = (ListSelectionModel) e.getSource();
	        	currentRow = sm.getMinSelectionIndex();
				instruments[currentChannel] = currentCol*nRows + currentRow;
				System.out.println(instruments[currentChannel]);
				midiChannels[currentChannel].programChange(instruments[currentChannel]);
		    }
		});
		// Listener for column changes
		lsm = table.getColumnModel().getSelectionModel();
		lsm.addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent e) {
		        ListSelectionModel sm = (ListSelectionModel) e.getSource();
	        	currentCol = sm.getMinSelectionIndex();
				instruments[currentChannel] = currentCol*nRows + currentRow;
				System.out.println(instruments[currentChannel]);				
				midiChannels[currentChannel].programChange(instruments[currentChannel]);
		    }
		});
		table.setPreferredScrollableViewportSize(new Dimension(600, 200));
		table.setCellSelectionEnabled(true);
		table.setColumnSelectionAllowed(true);
		for (int i = 0; i < names.length; i++) {
		    TableColumn column = table.getColumn(names[i]);
		    column.setPreferredWidth(40);
		}
		table.setAutoResizeMode(table.AUTO_RESIZE_OFF);
		JScrollPane sp = new JScrollPane(table);
        sp.setVerticalScrollBarPolicy(sp.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(sp.HORIZONTAL_SCROLLBAR_ALWAYS);
		sp.setAlignmentY(TOP_ALIGNMENT);
		sp.setAlignmentX(LEFT_ALIGNMENT);
		jp3.add(jcb);
		jp3.add(sp);

		//init
		table.changeSelection(0,0,false,false);
	}

	//util method for buttons
	public JButton createButton(String name, String tip, ImageIcon icon, boolean state) {
		JButton jb = new JButton(name,icon);
		jb.setToolTipText(tip);
		jb.setFont(new Font("SansSerif", Font.PLAIN, 12));
		jb.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),BorderFactory.createEmptyBorder(5,5,5,5)));
		jb.setEnabled(state);
		jb.addActionListener(this);
		jb.setAlignmentY(TOP_ALIGNMENT);
		jb.setAlignmentX(LEFT_ALIGNMENT);
		return jb;
	}

	//util method for labels
	public JLabel createLabel(String name) {
        JLabel jl = new JLabel(name, JLabel.CENTER);
		jl.setBorder(BorderFactory.createEtchedBorder());
		jl.setForeground(Color.black);
		jl.setFont(new Font("SansSerif",Font.PLAIN,12));
		jl.setAlignmentY(TOP_ALIGNMENT);
		jl.setAlignmentX(LEFT_ALIGNMENT);
		return jl;
	}

	public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        if (button.equals(selectB)) {
			selectFile();
			try{
				getData();
			}catch(Exception ex){
				ex.printStackTrace();
			}
			createSequence();
        }else if(button.equals(startB)) {
            if(startB.getText().startsWith("Start")) {
				startSequence();
            }else{
				stopSequence();
            } 
        } else if (button.equals(saveB)) {
			saveSequence();
		}
    }

	public void selectFile() {
	    status.setText("selectFile()");
		mf.setExt("txt");
        fc.addChoosableFileFilter(mf);
		int returnVal = fc.showOpenDialog(Synth.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        file = fc.getSelectedFile();
			jtf.setText(file.getName());
		}
	}
	
	//get data
	public void getData() throws IOException, FileNotFoundException{
//		status.setText("getData()");
//		String s;
//		int i,j;
//		v = new Vector(10,10);
//		//Enumeration enum;
//		StringTokenizer st;
//		int[] a = new int[5]; //array to hold note data
//		FileReader fr = new FileReader(file.getName());
//		BufferedReader br = new BufferedReader(fr);
//		//int pulseNum, note, len, channel;
//
//		//first line is tempo
//		s = br.readLine();
//		tempo = Integer.parseInt(s);
//		//second line is instruments
//		//s = br.readLine();
//		//st = new StringTokenizer(s,"|");
//		//for(i=0;i<16;i++){
//		//	instruments[i] = Integer.parseInt(st.nextToken());
//		//}
//
//		//use stringTokenizer to get data
//		while((s = br.readLine()) != null){
//			st = new StringTokenizer(s,"|");
//			for(i=0;i<5;i++){
//				a[i] = Integer.parseInt(st.nextToken());
//			}
//			v.add(a.clone());
//		}
//
//		if (!(br == null)) { br.close(); }
//		if (!(fr == null)) { fr.close(); }
	}

	public void createSequence() {
//	    status.setText("createSequence()");
//		int pulsenum, note, lngth, state, channel, vol, i, j;
//		Enumeration enum;
//		int[] a = new int[5];
//
//		try{
//	        sequence = new Sequence(Sequence.PPQ, tempo);
//			track = sequence.createTrack();
//        } catch (Exception ex) { ex.printStackTrace(); return; }
//
//		//assign default instruments to channels
//		for(i=0;i<16;i++){
//			//midiChannels[i].programChange(instruments[i]);
//			midiChannels[i].programChange((int)(Math.random()*128));
//		}
//
//		enum = v.elements();
//		while(enum.hasMoreElements()){
//			a = (int[])enum.nextElement();
//	        for(i=0;i<a.length;i++){
//				pulsenum = a[0];
//				note = a[1];
//				lngth = a[2];
//				channel = a[3];
//				vol = a[4];
//				createShortEvent(144,note,pulsenum,channel,vol);
//				createShortEvent(128,note,pulsenum + lngth,channel,vol);
//			}
//		}
//		startB.setEnabled(true);
//		saveB.setEnabled(true);
//		try {sequencer.setSequence(sequence);}
//		catch (Exception ex) { ex.printStackTrace(); }
	}

	public void startSequence() {
	    status.setText("startSequence()");
        startB.setText("Stop");
        sequencer.start();
	}

	public void stopSequence() {
	    status.setText("stopSequence()");
        startB.setText("Start");
        sequencer.stop();
	}

	public void saveSequence() {
		status.setText("saveSequence()");
		mf.setExt("mid");
        fc.addChoosableFileFilter(mf);
		int returnVal = fc.showSaveDialog(Synth.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        file = fc.getSelectedFile();
			status.setText(file.getName());
		}
		try {
			int[] fileTypes = MidiSystem.getMidiFileTypes(sequence);
            if (fileTypes.length == 0) {
            	System.out.println("Can't save sequence");
            }else{
            	if (MidiSystem.write(sequence, fileTypes[0], file) == -1) {
                	throw new IOException("Problems writing to file");
                } 
            }
        } catch (Exception ex) { 
        	ex.printStackTrace(); 
        }
	}

    public void createShortEvent(int type, int num, int tick, int channel, int vol) {
		//used for recording - type designates noteOn/noteOff (144/128)
		ShortMessage message = new ShortMessage();
        try {
			message.setMessage(type+channel, num, vol); 
            MidiEvent event = new MidiEvent(message, (long)tick);
            track.add(event);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void closeSynth() {
	    System.out.println("closeSynth()");
		if (synthesizer != null) {
            synthesizer.close();
        }
		synthesizer = null;
		if (sequencer != null) {
        	sequencer.close();
		}
		sequencer = null;
    }

	public void meta(MetaMessage message) {
		if (message.getType() == 47) {  // 47 is end of track
			startB.setText("Start");
			status.setText("meta(MetaMessage message)");
		}
	}

	class MyFilter extends javax.swing.filechooser.FileFilter {
		private String myExt;
	    // Accept all directories and all txt files.
	    public boolean accept(File f) {
	        if (f.isDirectory()) {
	            return true;
	        }
	        String extension = getExtension(f);
			if (extension != null) {
	            if (extension.equals(myExt)) {
	                return true;
	            } else {
	                return false;
	            }
	    	}
	        return false;
	    }
	    public void setExt(String ext){
			myExt = ext;	
		}
		private String getExtension(File f) {
	        String ext = null;
	        String s = f.getName();
	        int i = s.lastIndexOf('.');
	        if (i > 0 &&  i < s.length() - 1) {
	            ext = s.substring(i+1).toLowerCase();
	        }
	        return ext;
	    }
	    // The description of this filter
	    public String getDescription() {
	        return "*." + myExt + " files";
	    }
	}

    public static void main(String args[]) {
	    System.out.println("main()");
        final Synth synth = new Synth();
		synth.openSynth();
		synth.createGui();
		synth.pack();
        synth.setLocation(0, 0);
        //synth.setSize(screenSize.width, screenSize.height);
		synth.setSize(800, 600);
        synth.setVisible(true);
    }
} 

