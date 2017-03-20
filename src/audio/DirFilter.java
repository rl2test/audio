package audio;

import java.io.File;
import java.io.FilenameFilter;

public class DirFilter implements FilenameFilter {
    public boolean accept(File dir, String s) {
        return new File(dir, s).isDirectory();
    }
}

