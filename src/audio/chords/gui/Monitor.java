package audio.chords.gui;

public class Monitor extends Thread {
	int barLen					= 0;
	boolean runFlag				= true;
	DisplayPanel displayPanel 	= null;
	int barCount				= 0;
	
	public Monitor(int barLen, AudioController ac) {
		this.barLen = barLen;
		displayPanel =  ac.displayPanel;
	}
	
	public void run() {
		try {

			while(runFlag){
				// break out of loop
				if (!runFlag) {
					break;
				}

				displayPanel.updateBars(barCount);
					
				barCount++;

				// sleep till the next pulse
				sleep(barLen);

			}
		} catch (Exception e) {
		}		
	}
	
	public void destroy() {
		runFlag = false;
	}
}
