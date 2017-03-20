package audio;


import java.io.File;
import java.io.FilenameFilter;

/**
 * Filter files by extension.
 */
public class ChordsFilter implements FilenameFilter { 
	public boolean accept(File dir, String s) {
		return s.endsWith(".chords") && !s.startsWith("."); 
	} 
}
