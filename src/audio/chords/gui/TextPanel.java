package audio.chords.gui;

import static audio.Constants.C;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextArea;

public class TextPanel extends AudioPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 	= 1L;
	/** The singleton instance of this class. */    
	private static TextPanel textPanel 			= null;
	public JTextArea textArea 					= null;
	
	/**
     * @return singleton instance of this class
     */
    public static TextPanel getInstance(AudioController ac, int w, int h) throws Exception {
        if (textPanel == null) {
        	textPanel = new TextPanel(ac, w, h);
    	}
    	return textPanel;
    }
	
    /** Private constructor */
    private TextPanel(final AudioController ac, int w, int h) throws Exception {
        super(ac);
		
		textArea = new JTextArea();
		textArea.setBounds(x, y, w, h);
		textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
		add(textArea);
		
	    textArea.addKeyListener( new KeyListener() {
	        public void keyPressed(KeyEvent keyEvent) {}
	        public void keyReleased(KeyEvent keyEvent) {}
	        public void keyTyped(KeyEvent keyEvent) {
	        	ac.filePanel.setUpdated();
	        }
	    });
    }
}

