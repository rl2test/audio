package audio.chords.gui;
import static audio.Constants.BG_COLOR;
import static audio.Constants.BG_COLOR_MED;
import static audio.Constants.COLOR_DARK;
import static audio.Constants.NL;
import static audio.Constants.SP;
import static audio.Constants.SPACE;
import static audio.Constants.WIDTH_1;
import static audio.Constants.WIDTH_2;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

public class ChordInsertPanel extends JPanel implements MouseListener { 
	/** Default serialVersionUID. */
	private static final long serialVersionUID 			= 1L;
	/** The log. */
	private Logger log									= Logger.getLogger(getClass());	
	/** The notes. */
	private final String[] NOTES						= {
			"C", "C#", "Db", "D", "D#", "Eb", "E", "F", "F#", "Gb", "G", "G#", "Ab", "A", "A#", "Bb", "B", "Cb"
	};
	/** The common chord types. */
	private final String[] CHORDS						= {
			"",
			"6",
			"maj7",
			"7",
			"7+5",
			"7+9",
			"7-9",
			"m",
			"m6",
			"m7",
			"m7-5",
			"dim7",
	};
	/** The symbols. */
	private final String[] SYMBOLS						= {
			"@", "%", "{", "}", ":}", "{ :}", "[", "]", ":]", "[ :]", ":", "|",  
	};
	/** The single digits. */
	private final String[] NUMBERS_1					= {
			"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "NL", "SPACE",    
	};
	/** The tens. */
	private final String[] NUMBERS_2					= {
			"50", "60", "70", "80", "90", "100", "110", "120", "-", ",", "", "",      
	};
	/** Assigned strings. */
	private static final String[] ANNOTATIONS			= {
			"^1", "^2", "^3", "^4", "^5", "^6", "^7", "^8", "^9", "^10", "^11", "^12",
	};
	/** Assigned string values. */
	public static String[] ANNOTATION_STRINGS	= {
			"@4|80-120|10" + NL,		// tempo info
			"! C: ",					// composer
			"! A: ",					// author of lyrics	
			"! S: ",					// source
			"! R: ",					// rhythm
			"! K: ",					// key
			"% " + NL,					// new part
			"% A" + NL,					// A part 
			"% B" + NL,					// B part 
			"% C" + NL,					// C part
			"% D" + NL,					// D part
			"% 2nd time repeat" + NL,	// 2nd time repeat part
	};
	
	/** 
	 * The length of all of the arrays in defined as members of this class, with
	 * the exception of the NOTES array.
	 */
	private static final int arrLen  	= 12;
	
	/* Annotation maps. */
	public static Map<String, String> annotationToString = new HashMap<String, String>(); 
	public static Map<String, String> stringToAnnotation = new HashMap<String, String>();
	
	static {
		for (int i = 0; i < arrLen; i++) {
			annotationToString.put(ANNOTATIONS[i], ANNOTATION_STRINGS[i]);
			stringToAnnotation.put(ANNOTATION_STRINGS[i], ANNOTATIONS[i]);
		}
	}

	private final int NUM_NOTES				= NOTES.length;
	private final int NUM_CHORDS			= CHORDS.length;

	private final int NUM_VERT 				= NUM_NOTES + 4;
	private final int NUM_HORIZ 			= NUM_CHORDS;

	private final int CELL_H				= WIDTH_2 + SP * 2;
	private final int CELL_V				= WIDTH_1;
	
	private String[][] arr 					= null;
	private JTextArea textArea				= null;
	
    /** Public constructor */
    public ChordInsertPanel(JTextArea textArea) {
    	this.textArea = textArea;
    	setLayout(null);
        setBackground(BG_COLOR);
        
        addMouseListener(this);

        arr = new String[NUM_VERT][NUM_HORIZ];
        
        for (int v = 0; v < NUM_NOTES; v++) {
        	for (int h = 0; h < NUM_HORIZ; h++) {
        		arr[v][h] = NOTES[v] + CHORDS[h]; // + " | "
        	}
        }
        int v, h;
        
        v = NUM_NOTES;
    	for (h = 0; h < NUM_HORIZ; h++) {
    		arr[v][h] = SYMBOLS[h];
    	}
    	v++;
    	for (h = 0; h < NUM_HORIZ; h++) {
    		arr[v][h] = NUMBERS_1[h];
    	}
    	v++;
    	for (h = 0; h < NUM_HORIZ; h++) {
    		arr[v][h] = NUMBERS_2[h];
    	}
    	v++;
    	for (h = 0; h < NUM_HORIZ; h++) {
    		arr[v][h] = ANNOTATIONS[h];
    	}
    }    
    
    /**
     * Called by repaint(). Paints the img panel.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    	int x = 0, y = 0, x1 = 0, x2 = 0, y1 = 0, y2 = 0;
    	
    	g.setColor(BG_COLOR_MED);
    	g.fillRect(0, 0, NUM_HORIZ * CELL_H, NUM_VERT * CELL_V);
        	
        g.setColor(COLOR_DARK);
        // draw horiz lines
		x1 = 0;
		x2 = NUM_HORIZ * CELL_H;
        for (int v = 0; v <= NUM_VERT; v++) {
    		y1 = v * CELL_V;
    		y2 = y1;
    		g.drawLine(x1, y1, x2, y2);
    	}
        // draw vert lines
		y1 = 0;
		y2 = NUM_VERT * CELL_V;
        for (int h = 0; h <= NUM_HORIZ; h++) {
    		x1 = h * CELL_H;
    		x2 = x1;
    		g.drawLine(x1, y1, x2, y2);
    		
    		if (h > 0) {
    			// draw pipe line
    			g.drawLine(x1 - SP * 2, y1, x2 - SP * 2, y2 - CELL_V * 4);
    		}
    	}
    	
        // draw text
        
        int xOffset = 2;
        int yOffset = 16;
        int commaOffset = CELL_H - SP - 3;
        
        g.setColor(Color.black);
        // vert
    	for (int v = 0; v < NUM_VERT; v++) {
            // horiz
        	for (int h = 0; h < NUM_HORIZ; h++) {
        		x = h * CELL_H + xOffset;
        		y = v * CELL_V + yOffset;
        		g.drawString(arr[v][h], x, y);
        		if (v < NUM_NOTES) {
        			g.drawString(",", x + commaOffset, y);	
        		}
        	}
    	}
    	
    	// draw strings
    	y += CELL_V;
    	int stringOffset = WIDTH_1;  
    	for (int i = 0; i < ANNOTATION_STRINGS.length; i++) {
    		g.drawString((i + 1) + "", xOffset, y);
    		g.drawString(ANNOTATION_STRINGS[i], stringOffset, y);
    		y += 14;
    	}
    }

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		
		log.debug(x + ", " + y);
		
		int hIndex = x / CELL_H;
		int vIndex = y / CELL_V;
		
		log.debug(vIndex + ", " + hIndex);
		
		if (vIndex >= NUM_VERT || hIndex >= NUM_HORIZ) {
			log.debug("nothing to do");
		} else {
			String s = arr[vIndex][hIndex];
			
			if (vIndex < NUM_NOTES) {
				// append either comma or pipe to chord
				boolean comma = (x % CELL_H) > (CELL_H - SP * 2);  
				s += (comma) ? ", " : " | ";
			} else {
				if (s.startsWith("^")) {
					// assigned string
					int index = Integer.parseInt(s.substring(1)) - 1;
					s = ANNOTATION_STRINGS[index];
				} else if (s.equals("NL")) {
					s = NL;
				} else if (s.equals("SPACE")) {
					s = SPACE;
				} else {
				}
			}

			log.debug("s=" + s);
			
			String text = textArea.getText();
			
			int pos = textArea.getCaretPosition();
			
			String newText = text.substring(0, pos) + s + text.substring(pos); 
			
			textArea.setText(newText);			
			
			textArea.setCaretPosition(pos + s.length());
			textArea.grabFocus();
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}



// debug
/*
for (v = 0; v < NUM_VERT; v++) {
	String s = "";
	for (h = 0; h < NUM_HORIZ; h++) {
		s += arr[v][h] + SPACE;
	}
	log.debug(s);
}
*/
