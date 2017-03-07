package audio.chords;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Info implements ChordsConstants {
	/** Info fields derived from tune file. */
	public Map<String, String> infoFields = new HashMap<String, String>();
	
	/* Default definitions. */
	public String DEFAULT_TEMPO 			= "60";
	public String DEFAULT_METER 			= "4/4";

	/**
	 * @param field
	 * @return the value of the given field
	 */
	public String get(String field) {
		String value = (infoFields.containsKey(field)) 
				? infoFields.get(field)
				: "";
				
		if ("".equals(value)) {
			if (field.equals(INFO_TEMPO)) {
				value = DEFAULT_TEMPO; 
			} else if (field.equals(INFO_METER)) {
				value = DEFAULT_METER; 
			} 
		}
		
		return value;
	}
	
	/**
	 * @param fieldStr
	 */
	public void add(String fieldStr) {
		String[] arr =  fieldStr.split(COLON);
		infoFields.put(arr[0].trim(), arr[1].trim());		
	}
	
	/**
	 * @return abc representation of this info.
	 */
	public String toAbc() {
		StringBuffer sb = new StringBuffer();
		
		Set<Map.Entry<String, String>> entries = infoFields.entrySet();
		for(Map.Entry<String, String> entry : entries) {
		    sb.append(entry.getKey() + ": " + entry.getValue() + NL);
		}
		
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		Set<Map.Entry<String, String>> entries = infoFields.entrySet();
		for(Map.Entry<String, String> entry : entries) {
		    sb.append(entry.getKey() + ": " + entry.getValue() + NL);
		}
		
		return sb.toString();
	}
}
