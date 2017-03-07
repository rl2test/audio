package audio.chords.gui2;

public interface ChordPanel {
	/**
	 * Update the tempo indicator panel.
	 * 
	 * @param tempo
	 */
	public void updateTempo(String tempo);

	/**
	 * Update the chord indicator.
	 * 
	 * @param chord
	 */
	public void updateChord(String chord);

	/**
	 * @param message
	 */
	public void updateMessage(String message);

	/**
	 * @return the key to transpose to, or empty if 'Transpose' is not checked.
	 */
	public String getTransposeTo();

	/**
	 * Stop playing.
	 * 
	 * @param msg
	 */
	public void stop(String msg);
}
