package audio.chords.gui2;

import static audio.Constants.BARS_FILE;
import static audio.Constants.C;
import static audio.Constants.END;
import static audio.Constants.NL;
import static audio.Constants.PIPE;
import static audio.Constants.ROW_HEIGHT;
import static audio.Constants.SPACE;
import static audio.Constants.US;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import audio.Config;
import audio.Util;
import audio.chords.Bar;


/**
 * Display bars and indicate the current bar, also output bars to file.
 *
 */
public class DisplayPanel extends JPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 	= 1L;
	/** The log. */
	private Logger log							= Logger.getLogger(getClass());	
	private List<JLabel> labels					= new ArrayList<JLabel>();
	private int barsPerLine						= Config.getInt("display.BarsPerLine.default");
	
    /** Public constructor */
    public DisplayPanel() {
    	setLayout(null);
        setBackground(C[10]);
    }    

    public void init(List<Bar> bars) {
    	log.debug("bars.size()=" + bars.size());
    	StringBuffer sb = new StringBuffer();
    	/*
    	Component[] components = this.getComponents();
    	if (components.length > 0) {
    		for (Component component: components) {
    			log.debug(component.getClass().getName());	
    		}
    	}
    	*/
		this.removeAll();
		labels.clear();

		int labelWidth = (int) (this.getWidth() / barsPerLine) - 1;
		
		int x = 1;
    	int y = 1;
    	int barCount = 0;
    	for (Bar bar: bars) {
    		if (bar.annotation != null) {
    			if (x != 1) {
    				// annotation is in a bar that is not the first of a standard 4-bar phrase
        	    	x = 1;
        	    	y += ROW_HEIGHT + 1;
        	    	barCount = 0;
    			}
    			String text = bar.annotation.replace(US, SPACE);
    			sb.append(text + NL);
        	    JLabel label = new JLabel(text);
        	    label.setBounds(x, y, labelWidth, ROW_HEIGHT);
        	    add(label);
    	    	y += ROW_HEIGHT + 1;
    		}
    		
    		// text label
    	    JLabel label = new JLabel(); //bar.sequence
    	    label.setBackground(C[12]);
    	    label.setOpaque(true);
    	    label.setFont(new Font("Arial", Font.PLAIN, 12));
    	    
    	    label.setBounds(x, y, labelWidth, ROW_HEIGHT);
    	    String text = bar.cleanBarStr.replace(",", ", ");
    	    sb.append(Util.pad(text, " ", 20, END) + PIPE);
    	    label.setText(text);
    	    add(label);
    	    
    	    labels.add(label);
    	    
    	    x += label.getWidth() + 1;
    	    barCount++;
    	    
    	    if (barCount % barsPerLine == 0) {
    	    	sb.append(NL);
    	    	x = 1;
    	    	y += ROW_HEIGHT + 1;
    	    	barCount = 0;
    	    }
    	}
    	Util.writeToFile(BARS_FILE, sb.toString());
    	log.debug("this.getComponents().length=" + this.getComponents().length);
    }
    
    public void updateBars(int barCount) {
    	if(barCount > 0) {
    		labels.get(barCount - 1).setBackground(C[12]);
    	} else {
    		labels.get(labels.size() - 1).setBackground(C[12]);
    	}
    	labels.get(barCount).setBackground(C[6]);
    }
}



