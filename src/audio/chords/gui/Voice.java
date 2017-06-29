package audio.chords.gui;

public class Voice {
    String name; 
    int id;
    boolean mute = false;
    boolean[] pulses = null;
    public Voice(String name, int id) {
        this.name = name;
        this.id = id;
    }
}
