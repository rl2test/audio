package audio;

import java.io.File;
import java.io.FileFilter;

public class DirFileFilter implements FileFilter {
    public boolean accept(File f) {
        return f.isDirectory();
    }
}

