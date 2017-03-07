package audio.chords.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import audio.chords.Tune;


public class TunePanel extends JPanel implements GuiConstants { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 	= 1L;
	/** The log. */
	private Logger log							= Logger.getLogger(getClass());
	public Tune tune 							= null;
	public BufferedImage bi						= null;
	
    /** Public constructor */
    public TunePanel() {
    	setBackground(Color.WHITE);
    	//log.debug(getWidth() + " x " + getHeight());
    }    
    
    /**
     */
    public void init() {
    	//log.debug(getWidth() + " x " + getHeight());
    	bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    	repaint();
    }
    
    /**
     * Called by repaint(). Paints the img panel.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (bi != null) {
        	g.drawImage(bi, 0, 0, null);
        }
        
        log.debug("paintComponent completed");
    }
}



