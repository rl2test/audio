package audio;

import static audio.Constants.PROPERTIES_FILE;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
//import org.apache.log4j.Logger;

public class Config {
	//private static Logger log = Logger.getLogger(Config.class);
	public static Map<String, String> properties = new HashMap<String, String>();
	
	static {
		LineIterator li = null;
		try {
			li = FileUtils.lineIterator(PROPERTIES_FILE);
			while (li.hasNext()) {
			     String line = li.nextLine().trim();
			     if (!line.startsWith("#") && line.contains("=")) {
				     String[] arr = line.split("=");
				     properties.put(arr[0].trim(), arr[1].trim());
			     }
			}
		} catch (Exception e) {
		} finally {
			li.close();
		}
	}
	
	public static String get(String name) {
		return properties.get(name);
	}
	
	public static int getInt(String name) {
		return Integer.parseInt(properties.get(name));
	}
	
	public static boolean getBoolean(String name) {
		return (properties.get(name) == null) ? false : properties.get(name).equalsIgnoreCase("true");
	}
}
