package player;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

/**
 * Play in real time.
 */
public class Celtic extends JPanel implements Constants {
	private static final long serialVersionUID	= 1L;
	/** The log. */
	private Logger log 							= Logger.getLogger(this.getClass());
	private JButton playButton					= null;
	private JButton stopButton					= null;
	private JComboBox key 						= null;
	private JComboBox tempo						= null;
	private MidiChannel drone					= null;
	private MidiChannel drum					= null;
	private CelticPlayer1 player					= null;
	private Synthesizer synthesizer 			= null;
	private Map<String, Integer> keyMap			= new HashMap<String, Integer>();
	
	public Celtic() {
		setLayout(new FlowLayout());
	
		keyMap.put("A", 21);
		keyMap.put("B", 23);
		keyMap.put("D", 26);
		keyMap.put("E", 28);
		keyMap.put("G", 31);
		
		// buttons
		playButton = new JButton("Play");
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    // get current values
			    String keySelected	= (String) key.getSelectedItem();
			    int tempoSelected 	= (Integer) tempo.getSelectedItem();

			    int root = keyMap.get(keySelected);
  
				player = new CelticPlayer1(drone, drum, root, tempoSelected);
				playButton.setEnabled(false);
				stopButton.setEnabled(true);
				player.start();
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
		String[] keys = {"A", "B", "D", "E", "G"};
	    key = new JComboBox(keys);
	    
		// tempos
	    Integer[] tempos = {80, 85, 90, 95, 100, 105, 110, 115, 120};
	    tempo = new JComboBox(tempos);

	    add(playButton);
		add(stopButton);
	    add(key);
	    add(tempo);
	    
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
		MidiChannel midiChannels[] = synthesizer.getChannels();

		drone = midiChannels[0];
		drone.controlChange(10, 0);
		drone.programChange(20);  // reed organ

		drum = midiChannels[1];
		drum.controlChange(10, 127);
		drum.programChange(47);
	}
	
	public void close() {
		synthesizer = null;
	}
	
	public static void main(String args[]) {
		final Celtic celtic = new Celtic();

		JFrame frame = new JFrame("Celtic");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				celtic.close();
				System.exit(0);
			}
		});

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(screenSize.width/4, screenSize.height/4);
		frame.setSize(screenSize.width/2, screenSize.height/2);

		frame.getContentPane().add("Center", celtic);
		frame.setVisible(true);
	}
} 



