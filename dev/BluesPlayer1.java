package player;

import javax.sound.midi.MidiChannel;

import org.apache.log4j.Logger;

public class BluesPlayer1 extends Thread implements Constants {
	/** The log. */
	private Logger log 			= Logger.getLogger(this.getClass());
	private boolean runFlag 	= true;
	private MidiChannel bass1	= null;	
	private MidiChannel bass2	= null;
	private MidiChannel perc1	= null;
	private double bpm;
	private int R;
	private int r, r3d, r3, r5, r6;
	
	public BluesPlayer1(
			MidiChannel[] midiChannels, 
			int root, 
			double bpm) {
		this.R 	= root;
		this.bpm	= bpm;
		
		bass1 = midiChannels[0];
		bass1.controlChange(10, 0);
		bass1.programChange(34);  // acoustic bass

		bass2 = midiChannels[1];
		bass2.controlChange(10, 64);
		bass2.programChange(34);  // acoustic bass		
		
		perc1 = midiChannels[2];
		perc1.controlChange(10, 127);
		perc1.programChange(47);
	}
	
	public void run() {
		int ppb 		= 3; // pulse per beat
		int pulseLen 	= (int) (1000d * 60d / (bpm * ppb));
		
		log.debug("pulse=" + pulseLen);
		
		int pulse 		= 1; // pulse count
		int beat 		= 1; // beat count
		int bar			= 1; // bar count
		int verse		= 1; // verse count
		
		// set init values
		r  				= R;
		r3d				= r + i3d;
		r3				= r + i3;
		r5 				= r + i5;
		r6 				= r + i6;		
		
		int v 			= v8; // volume
		
		log.debug("verse=" + verse);

		while(runFlag){
			
			// bars
			// 1  2  3  4
			// I  I  I  I
			//	
			// 5  6  7  8
			// IV IV I  I
			//	
			// 9  10 11 12
			// V  IV I  V
			
			// turn notes off
			if (pulse == 1 || pulse == 3) {
				bass1.noteOff(r);	
				bass2.noteOff(r5);
				bass2.noteOff(r6);
			}
			
			if (bar <= 4 || bar == 7 || bar == 8 || bar == 11) {
				r  = R;
			} else if (bar == 5 || bar == 6 || bar == 10) {
				r = R  + i4;
			} else {
				r = R  + i5;
			}

			r3d	= r + i3d;
			r3	= r + i3;
			r5 	= r + i5;
			r6 	= r + i6;		
		
			// ////////////		// pc - pulse count
			// |  |  |  |		// bc - beat count
			// 1  2  3  4		// bc - beat count
			// bass2
			// 5 56 65  335		// interval
			//          d		// modifier
			// bass1
			// r rr rr rr r		// r - chord root

			// bass1
			if (pulse == 1 || pulse == 3) {
				v = (pulse == 1) ? v8 : v6; 
				bass1.noteOn(r, v);	
			}
			
			// bass2
			if (beat == 1) {
				if (pulse == 1 || pulse == 3) {
					v = (pulse == 1) ? v3 : v2;
					bass2.noteOn(r5, v);
				}
			} else if (beat == 2) {
				if (pulse == 1 || pulse == 3) {
					v = (pulse == 1) ? v3 : v2;
					bass2.noteOn(r6, v);
				}
			} else if (beat == 3) {
				if (pulse == 1) {
					v = v3;
					bass2.noteOn(r5, v);
				}
			} else if (beat == 4) {
				if (pulse == 1) {
					bass2.noteOn(r3d, v4);
				} else if (pulse == 2) {
					bass2.noteOff(r3d);
					bass2.noteOn(r3, v3);
				} else if (pulse == 3) {
					bass2.noteOff(r3);
					bass2.noteOn(r5, v2);
				} else {
				}
			} else {
			}
			
			try {
				sleep(pulseLen);
			} catch(InterruptedException e) {
				log.error(e);
			}

			pulse++;
			if (pulse > 3) {
				pulse = 1;
				beat++;
				if (beat > 4) {
					beat = 1;
					bar++;
					if (bar > 12) {
						bar = 1;
						verse++;
						log.debug("verse=" + verse);
					}
				}
			}
			//perc1.noteOff(root, 127);
		}
	}
	
	public void end() {
		runFlag = false;
		bass1.noteOff(r);
		bass2.noteOff(r3d);
		bass2.noteOff(r3);
		bass2.noteOff(r5);
		bass2.noteOff(r6);
	}
}
