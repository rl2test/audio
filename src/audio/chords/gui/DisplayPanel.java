package audio.chords.gui;

import static audio.Constants.BARS_FILE;
import static audio.Constants.BARS_PER_LINE;
import static audio.Constants.C;
import static audio.Constants.END;
import static audio.Constants.NL;
import static audio.Constants.PIPE;
import static audio.Constants.W;

import java.util.List;

import audio.Util;
import audio.chords.Bar;

/**
 * Display bars and indicate the current bar, also output bars to file.
 *
 */
public class DisplayPanel extends AudioPanel { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 	= 1L;
	/** The singleton instance of this class. */    
	private static DisplayPanel displayPanel 	= null;	
	//List<JLabel> myLabels = new ArrayList<JLabel>();
	
	/**
     * @return singleton instance of this class
     */
    public static DisplayPanel getInstance(AudioController ac) throws Exception {
        if (displayPanel == null) {
        	displayPanel = new DisplayPanel(ac);
    	}
    	return displayPanel;
    }
	
    /** Public constructor */
    private DisplayPanel(AudioController ac) {
    	super(ac);
        setBackground(C[6]);
    }    

    public void init(List<Bar> bars) throws Exception {
    	log.debug("bars.size()=" + bars.size());
    	StringBuffer sb = new StringBuffer();

		this.removeAll();
		//myLabels.clear();
		labels.clear();
		x = 0;
		y = 0;    	
		int barCount = 0;
    	for (Bar bar: bars) {
    	    String text = bar.cleanBarStr.replace(",", ", ");
    	    sb.append(Util.pad(text, " ", 20, END) + PIPE);
    	    
    		add(getLabel(text, "bar" + barCount, C[12], C[0], x, y, ac.barWidth, W[1], null));
    	    x += ac.barWidth + 1;
    	    barCount++;
    	    
    	    if (barCount % BARS_PER_LINE == 0) {
    	    	sb.append(NL);
    	    	x = 0;
    	    	y += W[1] + 1;
    	    }
    	}
    	Util.writeToFile(BARS_FILE, sb.toString());
    	log.debug("this.getComponents().length=" + this.getComponents().length);
    }
    
    public void updateBars(int barCount) {
    	int prevBarCount = (barCount > 0) ? barCount - 1 : labels.size() - 1;
    	unset(labels.get("bar" + prevBarCount));
    	set(labels.get("bar" + barCount));
    }
}


/*
if (bar.annotation != null) {
	if (x != 1) {
		// annotation is in a bar that is not the first of a standard 4-bar phrase
    	x = 1;
    	y += W[1] + 1;
    	barCount = 0;
	}
	String text = bar.annotation.replace(US, SPACE);
	sb.append(text + NL);
    JLabel label = new JLabel(text);
    label.setBounds(x, y, labelWidth, W[1]);
    add(label);
	y += W[1] + 1;
}
*/

/*
Component[] components = this.getComponents();
if (components.length > 0) {
	for (Component component: components) {
		log.debug(component.getClass().getName());	
	}
}
*/

