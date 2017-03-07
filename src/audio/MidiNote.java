package audio;

/**
 * Class representing a midi note
 */
public class MidiNote {
	public int channel;
	public int pitch;
	public int len;
	public int vol;
	
	/**
	 * @param channel
	 * @param pitch
	 * @param len
	 * @param vol
	 */
	public MidiNote (int channel, int pitch, int len, int vol) {
		this.channel	= channel;
		this.pitch		= pitch;
		this.len		= len;
		this.vol		= vol;
		//log.debug(this);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return channel + ", " + pitch + ", " + len + ", " + vol;
	}
}
