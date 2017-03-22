package audio.jsound;

import javax.swing.JApplet;

/**
 * A demo that shows JavaSound features. 
 *
 * Parameters that can be used in the JavaSound.html file inside
 * the applet tag to customize demo runs :
 *            <param name="dir" value="audioDirectory">
 *
 * @(#)JavaSoundApplet.java	1.1	99/11/01
 * @author Brian Lichtenwalter 
 */
public class JavaSoundApplet extends JApplet {

    static JavaSoundApplet applet;
    private JavaSound demo;

    public void init() {
        applet = this;
        String media = "../jfc/Java2D/media";
        String param = null;
        if ((param = getParameter("dir")) != null) {
            media = param;
        } 
        getContentPane().add("Center", demo = new JavaSound(media));
    }

    public void start() {
        demo.open();
    }

    public void stop() {
        demo.close();
    }
}
