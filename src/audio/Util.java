package audio;

import static audio.Constants.COMMA;
import static audio.Constants.DATA_DIR;
import static audio.Constants.DIR_FILE_FILTER;
import static audio.Constants.END;
import static audio.Constants.EXT_TXT;
import static audio.Constants.NL;
import static audio.Constants.PIPE;
import static audio.Constants.PIPE_DELIM;
import static audio.Constants.SPACE;
import static audio.Constants.UNDEF;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

public class Util {
	/** The log. */
	private static Logger log = Logger.getLogger(Util.class);	

	/**
	 * @param file
	 * @return list of lines in a text file, ignoring empty lines
	 */
	public static List<String> getLines(File file) {
		List<String> lines = new ArrayList<String>();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {				
					lines.add(line);
				}
			}
		} catch (Exception e) {
			log.error(e);
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
				}
			}
		}
		
		return lines;
	}
	
	/**
	 * @param file
	 * @return contents of file as text, ignoring empty lines
	 */
	public static String getText(File file) {
		StringBuilder sb = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {				
					sb.append(line + NL);
				}
			}
		} catch (Exception e) {
			log.error(e);
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
				}
			}
		}
		
		return sb.toString();
	}

	/**
	 * Get the first line from file specified path.
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getFirstLine(File file) {
		BufferedReader bufferedReader = null;
		FileReader fileReader = null;
		String s = null;
		try {
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			s = bufferedReader.readLine();
		} catch (Exception e) {
			log.error(e);
		} finally {
			try {
				if (fileReader != null)
					fileReader.close();
			} catch (Exception e) {
			}
			try {
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (Exception e) {
			}
		}
		return s;
	} 	

	/**
	 * Write String s to file with 'isAppend' and 'addNewLine' set to false.
	 * 
	 * @param file
	 * @param s
	 */
	public static void writeToFile(
    		File file, 
			String s) {
		writeToFile(file, s, false, false);
	}
		
	
	/**
	 * Write String s to file.
	 * 
	 * @param file
	 * @param s
	 * @param isAppend
	 * @param addNewLine
	 */
	public static void writeToFile(
    		File file, 
			String s, 
			boolean isAppend,
			boolean addNewLine) {
        FileOutputStream fos = null;
        PrintStream ps = null;

        try {
            fos = new FileOutputStream(file, isAppend);
            ps = new PrintStream(fos);
            ps.print(s);
            if (addNewLine) {
            	ps.print(NL);
            }
           	ps.close();
           	fos.close();
        } catch (Exception e) {
        	log.error(e);
        } finally {
            try { 
            	ps.close();
            } catch (Exception e) {
            }
            try {
            	fos.close();
            } catch (Exception e) {
            }
        }
    }
	
	/**
     * @param dir
     * @return a list of dir names in dir
     */
    public static List<String> getDirNames(File dir) {
        return getFileNames(dir, DIR_FILE_FILTER);
    }
    
    /**
     * @param dir
     * @return a list of files in File dir
     */
    public static List<File> getFiles(File dir) {
    	return getFiles(dir, null);
    }      
    
    /**
     * @param dir
     * @param filter
     * @return a list of files in File dir using filter filter
     */
    public static List<File> getFiles(File dir, FileFilter filter) {
    	List<File> list = new ArrayList<File>();

    	if(dir.isDirectory()) {
        	File[] files = (filter == null) ? dir.listFiles() : dir.listFiles(filter);
            if (files.length > 0) {
                Arrays.sort(files);
                list = Arrays.asList(files); 
            }    		
    	}
        return list;
    }   

    /**
     * @param dir
     * @param filter
     * @return a list of file names in File dir using filter filter
     */
    public static List<String> getFileNames(File dir, FileFilter filter) {
    	List<String> list = new ArrayList<String>();

    	if(dir.isDirectory()) {
        	File[] files = dir.listFiles(filter);
            if (files.length > 0) {
                Arrays.sort(files);
            	for (File file: files) {
            		list.add(file.getName());
            	}
            }    		
    	}

        return list;
    }
    
    /**
     * @param s
     * @return s with all multiple spaces reduced to a single space
     */
    public static String collapseSpaces(String s) {
    	while (s.indexOf("  ") != -1) {
    		s = s.replace("  ", " ");
    	} 
    	return s;
    }
    
    /**
     * @param s
     * @return s with all multiple spaces reduced to a single space
     */
    public static String stripSpaces(String s) {
    	return s.replace(" ", "");
    }
    
    /**
     * @param n
     * @return
     */
    public static String getSpaces(int n) {
    	String spaces = "";
    	for (int i = 0; i < n; i++) {
    		spaces += " ";
    	}
    	return spaces;
    }
    
	/**
	 * @param s
	 * @param len
	 * @return s padded to length len with appended spaces
	 */
    public static String padWithSpaces(String s, int len) {
    	if (s == null) {
    		return getSpaces(len);
    	}
    	int sLen = s.length();
   		return (sLen >= len) 
   				? s 
   				: s + getSpaces(len - sLen); 
    }
    
	/**
	 * Return a front-zero-padded String of length len based on the int value n,
	 * or the String representation of n if the number of digits in n is greater 
	 * than or equal to len, eg.
	 * 
	 * 		(1, 3) 		-> "001"
	 * 		(123, 3)	-> "123"
	 * 		(1234, 3)	-> "1234"
	 *  
	 * @param n
	 * @param len
	 * @return
	 */
	public static String padInt(int n, int len) {
		String intStr = "" + n;
		int intLen = intStr.length();

		if (intStr.length() >= len) {
			return "" + n;
		} else {
			int num = len - intLen; 
			for (int i = 0; i < num; i++) {
				intStr = "0" + intStr;
			}
		}

		return intStr;
	}
    
	/**
	 * @param s
	 * @param delim
	 * @return a list of the tokens in s, using delim as the delimiter
	 */
	public static List<String> getList(String s, String delim) {
		List<String> list = new ArrayList<String>();

		String[] arr = s.split(delim);
		for (String a: arr) {
			a = a.trim();
			if (!"".equals(a)) {
				list.add(a);
			}
		}
		
		return list;
	}	
    
	/**
	 * @param s
	 * @return a list of the tokens in s using the default SPACE as the delim
	 */
	public static List<String> getList(String s) {
		return getList(s, SPACE);
	}
	
    /**
	 * @param sequence
	 * @param start
	 * @param end
	 * @return
	 */
	public static String expandSequence(
			String sequence, 
			String start, 
			String end) {
		if (sequence.indexOf(start) != -1) {
			// eg. found '{'
			StringBuffer sb = new StringBuffer();
			int pos = 0;

			while (sequence.indexOf(end) != -1) {
				// eg. found '}'
				pos = sequence.indexOf(start);
				// eg. append data before '{' 
				sb.append(sequence.substring(0, pos));
				// eg. get data after '{'
				sequence = sequence.substring(pos + 1); 
						
				pos = sequence.indexOf(end);
				// extract the repeat data, eg. 'C7|:2'
				String repeatData = sequence.substring(0, pos);
				//log.debug("repeatData=" + repeatData);
				String[] data = repeatData.split(":");
				int dataLen = data.length;
				// extract the repeat phrase, eg. 'C7|'
				String repeatPhrase = data[0];
				
				// get the number of repeats, defaulting to 2 if no value is present
				int num = (dataLen == 1) ? 2 : Integer.parseInt(data[1]);
				for (int i = 0; i < num; i++) {
					sb.append(repeatPhrase);
				}
				sequence = sequence.substring(pos + 1);				
			}
			sb.append(sequence);

			sequence = sb.toString();
		}
		
		return sequence;
	}
	
	/**
	 * Return an abc notation of a note in conventional notation, eg Bb -> _B.
	 * The original case of the  note will be preserved, eg. bb -> _b.
	 *  
	 * @param note
	 * @return 
	 */
	public static String noteToAbc(String note) {
		return note.substring(1).replace("b", "_").replace("#", "^") + note.substring(0, 1);
	}
	
	/**
	 * Upper-case the first character of a String, lower-casing the rest.
	 *
	 * @param     s  the string to be converted
	 * @return    the converted string
	 */
	public static String ucFirst(String s) {
		if ((s == null) || (s.length() == 0)) {
			return "";
		} else if (s.length() == 1) {
			return s.toUpperCase();
		} else if (s.startsWith("(")) {
			// uppercase the 2nd char
			return "(" + s.substring(1, 2).toUpperCase() + s.substring(2).toLowerCase();
		} else {
			return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();	
		}
	}
	
	/**
	 * @param title
	 * @return title formatted to a standard format:
	 * 		1. All words lowercase with the first letter in uppercase, with 
	 *         handling for words in parentheses, eg.
	 *             'Ghost of a Chance' -> 'Ghost Of A Chance' 
	 *      2. 'The ' at the beginning is moved to the end of the title, eg.
	 *             'The Theme' -> 'Theme, The'
	 */
	public static String formatTitle(String title) {
		title = title.trim();
		
		String[] arr = title.split(SPACE);
		title = "";
		for (String s: arr) {
			s = ucFirst(s);
			title += s + SPACE;
		}
		
		title = title.trim();
		
		if (title.startsWith("The ")) {
			title = title.substring(4) + ", The";
		}
		
		return title.trim();
	}
	
	/**
	 * @param title
	 * @return a key derived from the title to be used as a map entry.
	 */
	public static String getTitleKey(String title) {
		return (title.endsWith(", The")) ? title.replace(", The", "") : title;
	}
	
	/**
	 * This method is a wrapper around the String.split() method. In the 
	 * following example, s.split() would return an array of length 5:
	 * 
	 * a,,b,,c,,,,,
	 * 
	 * whereas we may in some situations need an array of length 10
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param s the string to be split
	 * @param delim the delimiter
	 * @param n the number of elements to be returned
	 * 
	 * @return
	 */
	public static String[] split(String s, String delim, int n) {
		if (delim.equals(PIPE)) {
			delim = PIPE_DELIM;
		}
		
		String[] arr = new String[n];
		String[] sArr = s.split(delim); 
				
		for (int i = 0, len = arr.length; i < len; i++) {
			arr[i] = (i < sArr.length) ? sArr[i] : "";
		}
		
		return arr;
	}

	/**
	 * @param arr
	 * @return
	 */
	public static String arrToString(String[] arr) {
		return arrToString(arr, COMMA);
	}

	/**
	 * @param arr
	 * @return
	 */
	public static String arrToString(int[] arr) {
		return arrToString(arr, COMMA);
	}
	

	/**
	 * @param arr
	 * @return
	 */
	public static String arrToString(String[] arr, String delimiter) {
		String ret = "";
		if (arr != null) {
			int len = arr.length;
			if (len > 0) {
				for (String s: arr) {
					ret += s + delimiter;
				}
				ret = ret.substring(0, ret.length() - 1);
			}
		}
		return ret;
	}
	
	/**
	 * @param arr
	 * @return
	 */
	public static String arrToString(int[] arr, String delimiter) {
		String ret = "";
		if (arr != null) {
			int len = arr.length;
			if (len > 0) {
				for (int n: arr) {
					ret += n + delimiter;
				}
				ret = ret.substring(0, ret.length() - 1);
			}
		}
		return ret;
	}
	
	/**
	 * @param arr
	 * @return
	 */
	public static String arrToString(Integer[] arr) {
		String ret = "";
		if (arr != null) {
			int len = arr.length;
			if (len > 0) {
				for (int n: arr) {
					ret += n + ",";
				}
				ret = ret.substring(0, ret.length() - 1);
			}
		}
		return ret;
	}

	/**
	 * This method returns the remainder in the form more usually expected. 
	 * Simple java modulo arithmetic returns negative numbers if i is negative.
	 *  
	 * 		eg. for modulus = 3, i = -6 -> 6
	 * 
	 *		i	java rem
	 *          mod
	 *		-6	 0	 0
	 *		-5	-2	 1
	 *		-4	-1	 2
	 *		-3	 0	 0
	 *		-2	-2	 1
	 *		-1	-1	 2
	 *		 0	 0	 0
	 *		 1	 1	 1
	 *		 2	 2	 2
	 *		 3	 0	 0
	 *		 4	 1	 1
	 *		 5	 2	 2
	 *		 6	 0	 0
	 * 
	 * @param i
	 * @param modulus
	 * @return remainder
	 */
	public static int getRemainder(int i, int modulus) {
		return (i % modulus + modulus) % modulus;
	}
	
	/**
	 * @param list
	 * @return a String representation of the list using a default ',' separator
	 */
	public static String listToString(List<String> list) {
	    return listToString(list, ",");
	}
	
	/**
	 * @param list
	 * @param separator
	 * @return a String representation of the array using a given separator
	 */
	public static String listToString(List<String> list, String separator) {
		if (list == null || list.size() == 0) {
			return "";
		}
		
	    StringBuffer sb = new StringBuffer("");
	    int size = list.size();
	    int i = 0;
	    for (String s: list) {
	        sb.append(s);
	        if (i < size - 1) {
	            sb.append(separator);
	        }
	        i++;
	    }
	    return sb.toString();
	}

	/**
	 * @param title
	 * @param styleSheet
	 * @return an html header
	 */
	public static String getHtmlHeader(String title, String styleSheet) {
		String text = getText(new File(DATA_DIR, "htmlHeader" + EXT_TXT));

		text = text.replace("$title", title);
		text = text.replace("$styleSheet", styleSheet);
		
		return text;
	}	
	
	/**
	 * @param title
	 * @return an html header
	 */
	public static String getHtmlHeader(String title) {
		return getHtmlHeader(title, "style.css");
	}	
	
	/**
	 * @return an html footer
	 */
	public static String getHtmlFooter() {
		return getText(new File(DATA_DIR, "htmlFooter" + EXT_TXT));
	}

	
	/**
	 * @param arr
	 * @param s
	 * @return the index of s in array arr
	 */
	public static int getIndex(String[] arr, String s) {
		for (int i = 0, n = arr.length; i < n; i++) {
			if (arr[i].equals(s)) {
				return i;
			}
		}
		return UNDEF;
	}
	
	/**
	 * This method pads a string using the character ch. The resulting string 
	 * will be of length len and the padding will occur either at the beginning 
	 * or end of the string depending on the value of the 'where' parameter.
	 *  
	 * Note: this method does not truncate the original string if its length is 
	 * greater than the value of the length parameter.
	 * 
	 * TODO Add boolean parameter 'truncate' to handle that scenario.
	 * 
	 * @param s
	 * @param ch
	 * @param length
	 * @param where
	 * @return the padded string
	 */
	public static String pad(String s, String with, int length, int where) {
		while (s.length() < length ) {
			if (where == END)
				s += with;
			else
				s = with + s;
		}
		return s;
	}
	
	/**
	 * Read the contents the file specified by urlStr into an array of Strings.
	 * 
	 * @param urlStr
	 * @return
	 */	
	public static List<String> getLinesFromUrl(String urlStr) {
		return getLinesFromUrl(urlStr, true, true);
	}
	
	/**
	 * Read the contents the file specified by urlStr into an array of Strings.
	 * 
	 * @param urlStr
	 * @param removeEmptyLines
	 * @return
	 */
	public static List<String> getLinesFromUrl(
			String urlStr, 
			boolean removeEmptyLines,
			boolean trim) {
		List<String> lines = new ArrayList<String>();

		log.debug("urlStr=" + urlStr);

		InputStream inputStream	= null;
		BufferedReader br		= null;
		
		try {
			URL url = new URL(urlStr);

			inputStream = url.openStream();
			br = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while ((line = br.readLine()) != null) {
				if (trim) {
					line = line.trim();					
				}
				if (line.length() == 0 && removeEmptyLines) {
				} else {
					lines.add(line);
				}
			}
			br.close();
			inputStream.close();
		} catch (Exception e) {
			log.error(e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
			}
		}
		
		return lines;
	}
	
	/**
	 * Get the file specified by fileUrl and write to filePath using  
	 * BufferedReader.readLine(). 
	 * 
	 * @param fileUrl 	the absolute URL of the file to be downloaded
	 * @param filePath 	the absolute path to save the download to
	 * @return			the length of the downloaded file in bytes, or a 
	 *                  negative integer representing a status
	 */
	public static int getFileByLine(
			String fileUrl, 
			String filePath) {
		log.debug("fileUrl  = " + fileUrl);
		log.debug("filePath = " + filePath);

		InputStream inputStream				= null;
		BufferedReader br					= null;
		FileWriter fileWriter				= null;
		int len 							= 0;

		File file = new File(filePath);

		try {
			URL url = new URL(fileUrl);

			inputStream = url.openStream();
			br = new BufferedReader(new InputStreamReader(inputStream));
			fileWriter = new FileWriter(file, false);

			String line;
			while ((line = br.readLine()) != null) {
				fileWriter.write(line + NL);
				len++;
			}
			
			inputStream.close();
			fileWriter.close();
		} catch (Exception e) {
			log.error(e);
			len = -1;
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (fileWriter != null) {
					fileWriter.close();
				}
			} catch (Exception e) {
			}
		}
		
		return len;
	}	
}
