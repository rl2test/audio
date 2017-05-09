package audio.chords.gui;

import static audio.Constants.C;
import static audio.Constants.FONT;
import static audio.Constants.FONT_SM;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

public class AudioPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	// log
	public Logger log = Logger.getLogger(getClass());	
	// labels with listeners
    public Map<String, JLabel> labels = new HashMap<String, JLabel>();	
    public int x = 0;
    public int y = 0;
    public int w = 0;
    public int h = 0;
    public AudioController ac;
    
    public AudioPanel(AudioController ac) {
    	this.ac = ac;
        setBackground(C[0]);
		setLayout(null);
    }
    
    // set/unset labels
    public void set(String name) {
   		set(labels.get(name));	
    }    
    public void unset(String name) {
   		unset(labels.get(name));	
    } 
    public void set(JLabel l) {
    	l.setBackground(C[4]);
    	l.setForeground(C[16]);
    }
    public void unset(JLabel l) {
    	l.setBackground(C[12]);
    	l.setForeground(C[0]);
    }
	
    // get label
    public JLabel getLabel(
    		String text, 
    		String name, 
    		Color bg, 
    		Color fg, 
    		int x, 
    		int y, 
    		int w, 
    		int h, 
    		MouseAdapter listener) throws Exception {
    	JLabel label = (text == null) ? new JLabel() : new JLabel(text);
        if (text != null) {
        	if (name != null && name.startsWith("key")) {
        		label.setFont(FONT_SM);
        	} else {
        		label.setFont(FONT);
        	}
        	if (name != null && name.startsWith("i")) {
        		label.setHorizontalAlignment(JLabel.LEFT);
        	} else {
        		label.setHorizontalAlignment(JLabel.CENTER);	
        	}
       			
        }
        if (name != null) label.setName(name);    	
        label.setBackground(bg);
    	if (fg == null) fg = C[0];
    	label.setForeground(fg);
        label.setBounds(x, y, w, h);
        if (listener != null) {
        	label.addMouseListener(listener);
        }
        label.setOpaque(true);
    	if (name != null) {
    		if (labels.get(name) != null) {
    			throw new Exception("label name not unique: " + name);
    		} else {
    			labels.put(name, label);		
    		}
    	}
        return label;
    }
}
