package audio;


import java.io.File;
import java.io.FilenameFilter;

/**
 * Filter files by extension.
 */
public class AbcFilter implements FilenameFilter { 
	public boolean accept(File dir, String s) {
		return s.endsWith(".abc") && !s.startsWith("."); 
	} 
}
