package audio;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import audio.player.BluesPlayer;
import audio.player.CelticPlayer;
import audio.player.JazzPlayer;
import audio.player.KlezmerPlayer;
import audio.player.MetronomePlayer;
import audio.player.Player;

/**
 * Play in real time.
 */
public class Controller extends JPanel implements Constants {
	private static final long serialVersionUID	= 1L;
	/** The log. */
	private Logger log 							= Logger.getLogger(this.getClass());
	private JButton playButton					= null;
	private JButton stopButton					= null;
	private JComboBox key 						= null;
	private JComboBox tempo						= null;
	private JComboBox playerType				= null;
	private JComboBox type						= null;
	private JComboBox tune						= null;

	private Player player						= null;
	private Synthesizer synthesizer 			= null;
	private Map<String, Integer> keyMap			= new HashMap<String, Integer>();
	
	public Controller() {
		this.setBackground(new Color(193, 193, 216));
		//get and open synthesizer and sequencer
		try {
			if ((synthesizer = MidiSystem.getSynthesizer()) == null) {
				log.debug("getSynthesizer() failed!");
				return;
			}
			synthesizer.open();
		} catch (Exception e) {
			log.error(e);
		}
		
		//get channels
		final MidiChannel[] midiChannels = synthesizer.getChannels();
		
		setLayout(new FlowLayout());
	
		keyMap.put("A", 21 + OCT);
		keyMap.put("B", 23 + OCT);
		keyMap.put("C", 24 + OCT);
		keyMap.put("D", 26 + OCT);
		keyMap.put("E", 28 + OCT);
		keyMap.put("F", 29 + OCT);
		keyMap.put("G", 31 + OCT);
		
		// buttons
		playButton = new JButton("Play");
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    // get current values
			    String keySelected			= (String) key.getSelectedItem();
			    int tempoSelected 			= (Integer) tempo.getSelectedItem();
			    String playerTypeSelected	= (String) playerType.getSelectedItem();
			    String typeSelected			= (String) type.getSelectedItem();
			    String tuneSelected			= (String) tune.getSelectedItem();
			    
			    int root = keyMap.get(keySelected);
  
			    // TODO use getInstance instead
				if (playerTypeSelected.equals("Blues")) {
					player = new BluesPlayer(midiChannels, root, tempoSelected);	
				} else if (playerTypeSelected.equals("Celtic")) {
					player = new CelticPlayer(midiChannels, root, tempoSelected, typeSelected);
				} else if (playerTypeSelected.equals("Jazz")) {
					player = new JazzPlayer(midiChannels, tempoSelected, tuneSelected);
				} else if (playerTypeSelected.equals("Klezmer")) {
					player = new KlezmerPlayer(midiChannels, tempoSelected, tuneSelected);
				} else if (playerTypeSelected.equals("Metronome")) {
					//player = new MetronomePlayer(midiChannels, root, tempoSelected);
				} else {
				}
			    
				playButton.setEnabled(false);
				stopButton.setEnabled(true);
				
				if (playerTypeSelected.equals("Klezmer2")) {
					
				} else {
					player.start();
				}
			}
		});


		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(player != null){
					player.end();
					player = null;
				}
				playButton.setEnabled(true);
				stopButton.setEnabled(false);
			}
		});
		
		// combo boxes
		
		// keys
		String[] keys = {"A", "B", "C", "D", "E", "F", "G"};
	    key = new JComboBox(keys);
	    
		// tempos
	    Integer[] tempos = {30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180};
	    tempo = new JComboBox(tempos);

	    // TODO
	    //tempo.setSelectedItem(anObject)

		// playerTypes
		String[] playerTypes = {
				"Jazz",
				"Klezmer",
				"Celtic",
				"Blues",
				"Metronome"
		};
		playerType = new JComboBox(playerTypes);

		// types
		String[] types = {
				"Jig", 
				"Reel"
		};
		type = new JComboBox(types);

		// tunes
		String[] tunes = getTunes();
		
		tune = new JComboBox(tunes);

		
		add(playButton);
		add(stopButton);
	    add(key);
	    add(tempo);
	    add(playerType);
	    add(type);
	    add(tune);

	}
	
	public void close() {
		synthesizer = null;
	}
	
	public static void main(String args[]) {
		final Controller controller = new Controller();

		JFrame frame = new JFrame("Controller");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				controller.close();
				System.exit(0);
			}
		});

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(screenSize.width/4, screenSize.height/4);
		frame.setSize(screenSize.width/2, screenSize.height/2);

		frame.getContentPane().add("Center", controller);
		frame.setVisible(true);
	}
	
	private String[] getTunes() {
		File chordsDir = new File(CHORDS_DIR);
		String[] tunes = chordsDir.list(new ExtFilter(CHORDS_EXT));

		return tunes;
	}
	
	public class ExtFilter implements FilenameFilter { 
		String ext;
		
		public ExtFilter(String ext) { 
			this.ext = "." + ext; 
		}
		
		public boolean accept(File dir, String name) { 
			return name.endsWith(ext); 
		} 
	}
} 



/*
String[] tunes = {
		"Ale Brider",
		"Galitzyaner Tanz",
		"Khasid U Rabina",
};
*/