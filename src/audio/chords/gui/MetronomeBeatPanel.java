package audio.chords.gui;

import static audio.Constants.BG_COLOR_MED;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;


public class MetronomeBeatPanel extends JPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID = 1L;
	/** The log. */
	//private Logger log		= Logger.getLogger(getClass());	
	/** The control reference. */
	public int numBeats		= 0;
	public int beatCount	= 0;

    /** Public constructor */
    public MetronomeBeatPanel() {
        setBackground(BG_COLOR_MED);
    }    
    
    /**
     * @param numBeats
     */
    public void init(int numBeats) {
    	this.numBeats	= numBeats;
    	this.beatCount 	= 0;
    	
    	repaint();
    }
    
    /**
     * Called by repaint(). Paints the img panel.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (numBeats > 0) {
            g.setColor(Color.black);        	

            for (int i = 0; i <= numBeats; i++) {
            	int x1 = 0 + (i * 10);
            	int y2 = (i % 4 == 0) ? 24 : 12;
            	g.drawLine(x1, 0, x1, y2);
            } 

            if (beatCount > 0) {
            	g.setColor(Color.blue);
            	int x1 = (beatCount - 1) * 10; 
            	g.fillRect(x1, 0, 10, 10);
            }
        }

    }
}



