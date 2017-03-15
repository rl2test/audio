package audio.chords.gui;

import static audio.Constants.FONT;
import static audio.Constants.W;

import java.awt.Color;
import java.awt.event.MouseAdapter;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class AudioPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	/*
    public JLabel getLabel(String text, Color bg, Color fg, int x, int y, int w, MouseAdapter listener) {
    	JLabel label = new JLabel(text);
        label.setBackground(bg);
        label.setForeground(fg);
        label.setOpaque(true);
        label.setBounds(x, y, w, W[1]);
        label.setFont(FONT);
        label.setHorizontalAlignment(JLabel.CENTER);
        if (listener != null) {
        	label.addMouseListener(listener);
        }
        return label;
    }
    */
    public JLabel getLabel(String text, String name, Color bg, Color fg, int x, int y, int w, int h, MouseAdapter listener) {
    	JLabel label = (text == null) ? new JLabel() : new JLabel(text);
        if (text != null) {
        	label.setFont(FONT);
        	label.setHorizontalAlignment(JLabel.CENTER);
        }
        if (name != null) label.setName(name);    	
        label.setBackground(bg);
    	if (fg != null) label.setForeground(fg);
        label.setBounds(x, y, w, h);
        if (listener != null) {
        	label.addMouseListener(listener);
        }
        label.setOpaque(true);
        return label;
    }
}
