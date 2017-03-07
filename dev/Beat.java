package audio.chords;

public class Beat {
	/** The chord duration. */
	public int c = 0;
	/** The fifth duration. */
	public int f = 0;
	/** The root duration. */
	public int r = 0;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[Beat] r=" + r + ", f=" + f + ", c=" + c;
	}
}
