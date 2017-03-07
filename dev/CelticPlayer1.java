package player;

import javax.sound.midi.MidiChannel;

import org.apache.log4j.Logger;

public class CelticPlayer1 extends Thread {
	/** The log. */
	private Logger log 			= Logger.getLogger(this.getClass());
	private boolean runFlag 	= true;
	private MidiChannel drone	= null;
	private MidiChannel drum	= null;
	private int root;
	private double bpm;
	private int o1;
	private int o2;
	private int ffth2;
	private int o3;
	private int ffth3;
	private final int OCTAVE	= 12;
	private final int FIFTH		= 7;

	public CelticPlayer1(
			MidiChannel drone, 
			MidiChannel drum, 
			int root, 
			double bpm) {
		this.drone 	= drone;
		this.drum 	= drum;
		this.root 	= root;
		this.bpm	= bpm;
	}
	
	public void run() {
		log.debug("");
		o1		= root + OCTAVE;
		o2		= o1 + OCTAVE;
		ffth2	= o2 + FIFTH;
		o3		= o2 + OCTAVE;
		ffth3	= o2 + FIFTH;

		drone.noteOn(o1, 48);
		drone.noteOn(o2, 40);
		drone.noteOn(ffth2, 32);
		drone.noteOn(o3, 24);
		drone.noteOn(ffth3, 16);
		
		int beat = (int) (1000d * 60d / bpm);
		
		while(runFlag){
			drum.noteOn(o1, 127);
			try {
				sleep(beat);
			} catch(InterruptedException e) {
				log.error(e);
			}
			drum.noteOff(root, 127);
		}
	}

	public void end() {
		runFlag = false;
		drone.noteOff(o1);
		drone.noteOff(o2);
		drone.noteOff(ffth2);
		drone.noteOff(o3);
		drone.noteOff(ffth3);
	}
}
