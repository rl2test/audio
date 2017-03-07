package audio;

import static audio.Constants.PERIOD;

import java.io.File;
import java.io.FileFilter;

/**
 * Filter files by extension.
 */
public class ExtensionFilter implements FileFilter { 
	String ext;
	
	/**
	 * Create a FileFilter for the given extension.
	 * @param ext
	 */
	public ExtensionFilter(String ext) { 
		this.ext = (ext.startsWith(PERIOD)) ? ext : PERIOD + ext; 
	}
	
	/* (non-Javadoc)
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File file) {
		if (file == null || file.isDirectory()) {
			return false;
		}
		return file.getName().endsWith(ext); 
	} 
}
