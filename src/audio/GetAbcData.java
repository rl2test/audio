// create DATABASE mysite;
//
// DROP TABLE irish_tunes;
// CREATE TABLE irish_tunes (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(200), aka VARCHAR(400), tune_type VARCHAR(50), tune_group VARCHAR(50), tune_key VARCHAR(10), source VARCHAR(200), source_num INT, source_url VARCHAR(400), id_string VARCHAR(200), rating INT);
//
// DROP TABLE irish_tune_files;
// CREATE TABLE irish_tune_files (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, tune_id INT, filename VARCHAR(200), rating INT);

package audio;

import static audio.Constants.ABC_FILTER;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Class used to write abc tune metadata from local abc files containing multiple 
 * tunes to local db.
 */
public class GetAbcData {
	/** The log. */
	private Logger log 				= Logger.getLogger(getClass());
	private final static String NL	= "\n";			// newline
	private final static String FS	= "/";			// file separator
	/** Abc path - this will eventually be under the localhost DocumentRoot. */
	private String abcPath 			= "/Users/rlowe/rob/music/celtic/abc/";
	/** 
	 * The key corresponds to the abc file 'R:' definitions; the value corresponds to theSession.org
	 * tune-group definitions (with spaces replaced by hyphens where applicable), If there is no matching
	 * theSession.org definition then a custom tune-group definition is used. In the irish_tunes table
	 * the key-value pairs correspond to tune-type and tune-group.
	 * 
	 * thesession.org tune-group definitions:
	 *     jigs, reels, slip jigs, hornpipes, polkas, slides, waltzs, barndances, strathspeys, three-twos, mazurkas
	 */
	private static Connection conn 	= null;
	private static Statement stmt 	= null;
	private static ResultSet rs 	= null;
	
	// for local abc files containing multiple tunes - run1()
	private static Map<String, String> tuneTypeToGroup 	= new HashMap<String, String>();
	// for thesession - run2()
	private static Map<String, Integer> tuneGroupPages 	= new HashMap<String, Integer>();
	private static Map<Integer, String> charToAscii 	= new HashMap<Integer, String>();
	
	/* init ascii special chars map */
	static {
		charToAscii.put(193, "A"); 
		charToAscii.put(201, "E");
		charToAscii.put(205, "I");
		charToAscii.put(211, "O");
		charToAscii.put(218, "U");
		charToAscii.put(225, "a");
		charToAscii.put(233, "e");
		charToAscii.put(237, "i");
		charToAscii.put(243, "o");
		charToAscii.put(250, "u");	

		// thesession.org tune-group definitions:
		tuneTypeToGroup.put("jig", 			"jigs");
		tuneTypeToGroup.put("reel", 		"reels"); 
		tuneTypeToGroup.put("slip jig", 	"slip-jigs");
		tuneTypeToGroup.put("hornpipe", 	"hornpipes");
		tuneTypeToGroup.put("polka", 		"polkas");
		tuneTypeToGroup.put("slide", 		"slides");
		tuneTypeToGroup.put("waltz", 		"waltzs");
		tuneTypeToGroup.put("barndance", 	"barndances");	
		tuneTypeToGroup.put("strathspey", 	"strathspeys");	
		tuneTypeToGroup.put("three-two", 	"three-twos");	
		tuneTypeToGroup.put("mazurka", 		"mazurkas");	

		// custom tune-group definitions for session-tunes-1,2 and o'briens 'R: ' definitions 
		tuneTypeToGroup.put("set dance", 	"set-dances");
		tuneTypeToGroup.put("march", 		"marches");
		tuneTypeToGroup.put("planxty", 		"planxtys");
		tuneTypeToGroup.put("single jig", 	"jigs");
		tuneTypeToGroup.put("hop jig", 		"jigs");
		tuneTypeToGroup.put("song", 		"songs");
		
		tuneGroupPages.put("jigs", 			20);
		tuneGroupPages.put("reels", 		20); 
		tuneGroupPages.put("slip-jigs", 	20);
		tuneGroupPages.put("hornpipes", 	20);
		tuneGroupPages.put("polkas", 		20);
		tuneGroupPages.put("slides", 		10);
		tuneGroupPages.put("waltzs", 		10);
		tuneGroupPages.put("barndances", 	10);	
		tuneGroupPages.put("strathspeys", 	 5);	
		tuneGroupPages.put("three-twos", 	 5);	
		tuneGroupPages.put("mazurkas", 		 5);	
	}
	
	/**
	 * Prepare my-tunes folder for db insert - parse files and copy to tune-type dirs
	 */	
	protected void runMisc() {
		List<File> files = Util.getFiles(new File("/Users/rlowe/rob/aws/htdocs/abc/file/misc"), ABC_FILTER);
		for(File file: files) {
			String filename = file.getName().replace(".abc", "");
			String source = "misc";
			log.debug(filename);
			if (!existsInDb(filename, source)){
				List<String>  lines = Util.getLines(file);
				AbcTune tune = new AbcTune();
				tune.source = source;
				tune.source_url = "";
				tune.source_num = 0;
				tune.filename = filename; 

				for (String line: lines) {
					line = line.trim();
					/*
						X:1
						T:Limerick Lassies, The
						S:Trad, arr. Paddy O'Brien 
						Z:Set: Limerick Lassies/Old Copper Plate/Buckley's Fancy
						R:reel
						E:9
						I:speed 350
						M:C|
						K:D
						c2|dBAF DEFD|GFGA BE~E2|dBAF DFAF|GBAG FD~D2|\
					 */
					if (line.startsWith("X:")) {
					} else if (line.startsWith("T:")) {
						String cleanName = cleanName(line.substring(2).trim());
						if (!tune.names.contains(cleanName)) {
							tune.names.add(cleanName);	
						}
					} else if (line.startsWith("R:")) {
						tune.tune_type = line.substring(2).trim().toLowerCase();
					} else if (line.startsWith("K:")) {
						tune.tune_key = line.substring(2).trim();
					} else {
					} 
					if (!line.startsWith("***")) {
						tune.abc += line.trim() + NL;
					}
				}
				writeToDb(tune);
			} else {
				log.debug(filename + " EXISTS");
			}
		}
	}
	
	/**
	 * Prepare my-tunes folder for db insert - parse files and copy to tune-type dirs
	 */	
	protected void runMyTunes() {
		List<File> files = Util.getFiles(new File("/Users/rlowe/rob/abc/my-tunes"), ABC_FILTER);
		for(File file: files) {
			String fileName = file.getName().replace(".abc", "");
			log.debug(fileName);
			List<String>  lines = Util.getLines(file);
			AbcTune tune = new AbcTune();
			tune.source = "my-tunes";
			tune.source_url = "";
			tune.source_num = 0;

			for (String line: lines) {
				line = line.trim();
				/*
					X:1
					T:Limerick Lassies, The
					S:Trad, arr. Paddy O'Brien 
					Z:Set: Limerick Lassies/Old Copper Plate/Buckley's Fancy
					R:reel
					E:9
					I:speed 350
					M:C|
					K:D
					c2|dBAF DEFD|GFGA BE~E2|dBAF DFAF|GBAG FD~D2|\
				 */
				if (line.startsWith("X:")) {
				} else if (line.startsWith("T:")) {
					tune.names.add(cleanName(line.substring(2).trim()));
				} else if (line.startsWith("R:")) {
					tune.tune_type = line.substring(2).trim().toLowerCase();
				} else if (line.startsWith("K:")) {
					tune.tune_key = line.substring(2).trim();
				} else {
				} 
				if (!line.startsWith("***")) {
					tune.abc += line.trim() + NL;
				}
			}
			saveTune(fileName, tune);
		}
	}
	
	/**
	 * Parse local abc files containing multiple tunes and write to local fs and db
	 */
	protected void runMultipleLocal() {
		try {
			// original names - renamed to tuneFilenames
		    //sessionTunes.abc.txt
			//sessionTunesII.abc.txt
			//OBrien.abc.txt
			
			String[] tuneFilenames = {"session-tunes-1", "session-tunes-2", "obrien"};
			//String[] tuneFilenames = {"session-tunes-1"};
			
			for (String tuneFilename: tuneFilenames) {
				File file 	= new File("/Users/rlowe/Downloads/" + tuneFilename + ".abc");
				String tuneSource = tuneFilename;
	
				List<String>  lines = Util.getLines(file);
				AbcTune tune = null;
				for (String line: lines) {
					line = line.trim();
					/*
						X:1
						T:Limerick Lassies, The
						S:Trad, arr. Paddy O'Brien 
						Z:Set: Limerick Lassies/Old Copper Plate/Buckley's Fancy
						R:reel
						E:9
						I:speed 350
						M:C|
						K:D
						c2|dBAF DEFD|GFGA BE~E2|dBAF DFAF|GBAG FD~D2|\
					 */
					if (line.startsWith("X:")) {
						if (tune != null) {
							saveTune(tune);
						}
						tune = new AbcTune();
						tune.source = tuneSource;
						tune.source_url = "";
						tune.source_num = Integer.parseInt(line.substring(2).trim());
					} else if (line.startsWith("T:")) {
						tune.names.add(cleanName(line.substring(2).trim()));
					} else if (line.startsWith("R:")) {
						tune.tune_type = line.substring(2).trim();
					} else if (line.startsWith("K:")) {
						tune.tune_key = line.substring(2).trim();
					} else {
					} 
					if (tune != null) {
						if (!line.startsWith("***")) {
							tune.abc += line.trim() + NL;
						}
					}	
				}
				if (tune != null) {
					saveTune(tune);
				}
			}
		} catch(Exception e) {
			log.error(e);
		}
		log.debug("ok");
	}

	/** 
	 * Get data from thesession 
	 */
	public void runThesession() {
		InputStream inputStream	= null;
		BufferedReader br 		= null;
		
		try {
			log.debug("runThesession()");
			
			//for thesession - run2() 
			String[] tuneGroups	= {"jigs", "slip-jigs", "reels", "hornpipes", "polkas", "slides", "waltzs", "barndances", "strathspeys", "three-twos", "mazurkas"};
			//String[] tuneGroups	= {"jigs", "reels"};
			//String[] tuneGroups	= {"mazurkas"}; //
			String baseUrl 	= "https://thesession.org/tunes/";
			
			List<ThesessionTune> tunes = new ArrayList<ThesessionTune>();
	
			for (String tuneGroup: tuneGroups) {
				int count = 1;
				String path = abcPath + "thesession" + FS + tuneGroup;
				log.debug(tuneGroup + ": path=" + path);
				File dir = new File(path);
				if (dir.exists()) {
					log.debug("dir exists: " + path);
				} else {
					dir.mkdir();	
				}
				
				int numPages = GetAbcData.tuneGroupPages.get(tuneGroup);
				log.debug("numPages=" + numPages);
				
				for (int i = 1; i <= numPages; i++) {
					String urlTuneGroup = (tuneGroup.equals("slip-jigs")) ? "slip%20jigs" : tuneGroup;
					String urlStr = baseUrl + "popular/" + urlTuneGroup + "?page=" + i;
					
					URL url = new URL(urlStr);
					inputStream = url.openStream();
					br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

					String line;
					while ((line = br.readLine()) != null) {
						if 	(line.indexOf("<a href=\"/tunes/") == 0 && line.indexOf("<a href=\"/tunes/popular") == -1) {
							//<a href="/tunes/388">The Drops Of Brandy</a>
							//log.debug(line);
							
							//<a href="/tunes/display/1098">The Haunted House</a><br />
							int beginIndex = line.indexOf("/tunes/") + 7;
							int endIndex = line.indexOf(">") - 1;
							int thesessionId = Integer.parseInt(line.substring(beginIndex, endIndex));
							
							beginIndex = line.indexOf(">") + 1;
							endIndex = line.indexOf("</a>");
							String name = line.substring(beginIndex, endIndex);
							name = cleanName(name);
							log.debug(count + " " + name);
							tunes.add(new ThesessionTune(thesessionId, name, tuneGroup, count));
							count++;
						}
					}
					br.close();
					inputStream.close();
				}
			}
			// download tunes	
			for (ThesessionTune thesessionTune: tunes) {
				String urlStr = baseUrl + thesessionTune.thesessionId + "/abc";
				String fileStr = abcPath + "thesession/" + thesessionTune.tuneGroup + FS + toIdString(thesessionTune.name) + ".abc";
				if (!new File(fileStr).exists()) {
					log.debug(thesessionTune + " " + Util.getFileByLine(urlStr, fileStr));
				} else {
					log.debug(thesessionTune + " exists");
				}		
				// writeToDb;
				List<String>  lines = Util.getLines(new File(fileStr));
				AbcTune abcTune = null;
				for (String line: lines) {
					line = line.trim();
					/*
						X: 1
						T: Sonny's
						Z: jimmydearing
						S: https://thesession.org/tunes/8758#setting8758
						R: mazurka
						M: 3/4
						L: 1/8
						K: Gmaj
					 */
					if (line.startsWith("X:")) {
						if (abcTune != null) {
							break;
						}
						abcTune = new AbcTune();
						abcTune.source = "thesession";
						abcTune.source_num = thesessionTune.orderNum;
						abcTune.source_url = "" + thesessionTune.thesessionId;
					} else if (line.startsWith("T:")) {
						abcTune.names.add(cleanName(line.substring(2).trim()));
					} else if (line.startsWith("R:")) {
						abcTune.tune_type = line.substring(2).trim();
					} else if (line.startsWith("K:")) {
						abcTune.tune_key = line.substring(2).trim();
					} else {
					} 
				}
				if (abcTune != null) {
					writeToDb(abcTune);
				}	
			}
		} catch (Exception e) {
			log.error(e);
		} finally {
			try {
				if (br != null) br.close();
			} catch (Exception e) {}
			try {
				if (inputStream != null) inputStream.close();
			} catch (Exception e) {}
		}
		log.debug("ok");
	}	
	
	/**
	 * Save tune to fs and local db irish_tunes table.
	 * 
	 * @param tune
	 */
	public void saveTune(AbcTune tune) {
		try {
			File file = new File(abcPath + tune.source + FS + toIdString(tune.names.get(0)) + ".abc");
			if (!file.exists()) {
				Util.writeToFile(file, tune.abc);	
			} else {
				log.debug(tune.names.get(0) + " exists");
			}
			writeToDb(tune);
		} catch(Exception e) {
			log.debug(e);
		}    
	} 
	
	/**
	 * Save tune to fs and local db irish_tunes table.
	 * 
	 * @param tune
	 */
	public void saveTune(String fileName, AbcTune tune) {
		try {
			File file = new File(abcPath + tune.source + FS + toIdString(fileName) + ".abc");
			if (!file.exists()) {
				Util.writeToFile(file, tune.abc);	
			} else {
				log.debug(tune.names.get(0) + " exists");
			}
			writeToDb(tune);
		} catch(Exception e) {
			log.debug(e);
		}    
	} 
	
	public void writeToDb(AbcTune tune) {
		try {
		    log.debug(tune.toQuery());
		    int result = stmt.executeUpdate(tune.toQuery());
		    log.debug(result + " " + tune.names.get(0));
		} catch(Exception e) {
			log.debug(e);
		}		
	}
	
	public boolean existsInDb(String filename, String source) {
		int count = 0;
		try {
			String query = "select * from irish_tunes where id_string = '" + filename + "' and source = '" + source + "'";
			rs = stmt.executeQuery(query);
		    // process resultSet
		    while (rs.next()) {
		    	count++;
		    }	
		} catch(Exception e) {
			log.debug(e);
		}
		return (count > 0);
	}
	
	public String toIdString(String s) {
		return s.toLowerCase().replace(" ", "-").replace(",", "").replace("'", "").replace("#", "");
	}
	
	/**
	 * @param s
	 * @return s with special chars replaced with ascii
	 */
	public String cleanName(String s) {
		s = s.replace("&#8216;", "'").replace("&#8217;", "'");
		if (s.startsWith("The ")) {
			s = s.substring(4) + ", The";
		}
		String ret = "";
		for(int i = 0, n = s.length(); i < n; i++) {
			char c = s.charAt(i);
			int ascii = (int) c;
			if (ascii < 32 || ascii > 126) {
				//log.debug(ascii);
				if (GetAbcData.charToAscii.containsKey(ascii)) {
					ret += GetAbcData.charToAscii.get(ascii);	
				}
			} else {
				ret += c;
			}
		}
		return ret;
	}
	
	/**
	 * @param s
	 * @return s with escaped single-quotes
	 */
	public String addSlashes(String s) {
		return (s == null) ? "" : s.replace("'", "\\'");
	}
	

	/**
	 * Class representing abc tune metadata as per db schema.
	 */
	class AbcTune {
 		public List<String> names 	= new ArrayList<String>();	// list of names for the tune
 		public String tune_type 	= "";						// tune type as per thesession, session-tunes-1&2, obrien, my-tunes	
 		public String tune_key 		= "";
 		public String source 		= "";						// "session-tunes-1", "session-tunes-2", "obrien", "thesession", "my-tunes", "misc"
		public int source_num		= 0;						// depends on source - 0 for mytunes, misc	
		public String source_url 	= "";						// for thesession
		public String abc 			= "";						// first 2 bars - not used for my-tunes
		public String filename 		= "";						// filename - only used for misc
		
		public String toQuery() {
			String name = names.get(0);
			String aka = "";
			if (names.size() > 1) {
				for (int i = 1; i < names.size(); i++) {
					aka += names.get(i); 
					if (i < names.size() - 1) {
						aka += ", ";
					}
				}
			}
			String tune_group= "";
			if (tuneTypeToGroup.containsKey(tune_type)) {
				tune_group = tuneTypeToGroup.get(tune_type);
			} else {
				log.warn("tune_group not found for " + tune_type);
			}	
			/*
			1	idPrimary	int(11)			No	None		AUTO_INCREMENT
			2	name	varchar(200)
			3	aka	varchar(400)
			4	tune_type	varchar(50)
			5	tune_group	varchar(50)
			6	tune_key	varchar(10)
			7	source	varchar(200)
			8	source_num	int(11)
			9	source_url	varchar(400)
			10	id_string	varchar(200)
			11	rating	int(11)
			12	comment	varchar(500)
			13	notation	int(11)
			 */
			String idString = (source.equals("misc")) ? filename : toIdString(names.get(0));
			int rating = 0;
			String comment = "";
			int notation = 1;
			
			String query = "insert into irish_tunes values (null,'" + 
					addSlashes(name) + "','" + 
					addSlashes(aka) + "','" + 
					tune_type + "','" + 
					tune_group + "','" + 
					tune_key + "','" + 
					source + "'," + 
					source_num + ",'" + 
					source_url + "','" + 
					idString + "', " +
					rating + ", '" + 
					comment + "', " + 
					notation + ")";
							
			return query;
		}
	}
	
	/**
	 * Class representing thesession abc tune metadata
	 */
	class ThesessionTune {
		int thesessionId;
		String name;
		String tuneGroup;
		int orderNum;
		public ThesessionTune(int thesessionId, String name, String tuneGroup, int orderNum) {
			this.thesessionId = thesessionId;
			this.name = cleanName(name);
			this.tuneGroup = tuneGroup;
			this.orderNum = orderNum;
		}
		public String toString() {
			return tuneGroup + " " + thesessionId + " " + name;
		}
	}
	
	public static void main(String[] args) {
		try {
			DriverManager.registerDriver (new com.mysql.jdbc.Driver());
			conn = DriverManager.getConnection("jdbc:mysql://localhost/mysite?user=root&password=root");
		    stmt = conn.createStatement();	
		    
		    //new AbcData().runMultipleLocal();
		    //new AbcData().runThesession();
		    //new GetAbcData().runMyTunes();
		    new GetAbcData().runMisc();
		} catch (Exception e) {
			System.out.println(e);
		} finally {
		    if (rs != null) {
		        try {
		            rs.close();
		        } catch (Exception e) {}
		        rs = null;
		    }
			if (stmt != null) {
		        try {
		            stmt.close();
		        } catch (Exception e) {}
		        stmt = null;
		    }
		    if (conn != null) {
		        try {
		            conn.close();
		        } catch (Exception e) {}
		        conn = null;
		    }
		}
	}
}

/*
// use $last_id = $conn->insert_id;
public String toSubquery(int tune_id) {
	String query = "insert into irish_tune_files values (null," + tune_id + ",'" + toIdString() + "', 0)";
	return query;
}
*/

/*
int result1 = stmt.executeUpdate(tune.toQuery(), Statement.RETURN_GENERATED_KEYS);
rs = stmt.getGeneratedKeys();
if (rs.next()){
    int id = rs.getInt(1);
    int result2 = stmt.executeUpdate(tune.toSubquery(id));
    log.debug(result1 + " " + id + " " + result2 + " " + tune.names.get(0));
}
rs.close();
*/

/*
public String toQueryString() {
	String name = names.get(0);
	String aka = "";
	if (names.size() > 1) {
		for (int i = 1; i < names.size(); i++) {
			aka += names.get(i); 
			if (i < names.size() - 1) {
				aka += ", ";
			}
		}
	}
	String id_string = name.toLowerCase().replace(" ", "-").replace(",", "").replace("'", "");
	String query = "insert into irish_tunes values (null,'" + name.replace("'", "\'") + "','" + aka + "','" + tune_type + "','" + tune_key + "','" + source + "'," + source_num + ",'','" + id_string + "')";
	try {
		//query = URLEncoder.encode(query, "UTF-8");
	} catch (Exception e) {
		log.error(e);
	}
	return "op=runQuery&query=" + query;
}
*/

/*
public void getTune(String queryString) {
	
	try {	
		log.debug(queryString);
		URL obj = new URL(urlString + "?" + queryString);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	
		// optional default is GET
		con.setRequestMethod("GET");
	
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Cookie", cookie);
	
		int responseCode = con.getResponseCode();
		log.debug("Response Code : " + responseCode);
		
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		log.debug(response.toString());
	} catch(Exception e) {
		log.debug(e);
	} 
}
*/	