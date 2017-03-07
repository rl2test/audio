package audio.chords.parser.jazzstandards;

import static audio.Constants.DATA_DIR;
import static audio.Constants.NL;
import static audio.Constants.Y;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import audio.Util;

/**
 * This generic class parses a delim data files and generates output. 
 */
public class JazzStandards {
	/** The log. */
	private Logger log 					= Logger.getLogger(getClass());
	private final File jazzStandardsDir	= new File(DATA_DIR, "jazzStandards");
	
	
	/**
	 * Parse 'RealbookJazzLTDindex.txt'.
	 */
	/*
	public void parseRealbookJazzLTDindex() {
		String fileName = "RealbookJazzLTDindex";
		File inputFile	= new File(jazzStandardsDir, fileName + EXT_TXT);
		File outputFile	= new File(jazzStandardsDir, fileName + "-out" + EXT_TXT);
		StringBuffer sb = new StringBuffer();
		
		List<String> lines = Util.getLines(inputFile);

		for (String line: lines) {
			line = line.trim();
			log.debug(line);

			//A Bid For 1 Jazz LTD
			//A CALL FOR ALL DEMONS 1 The Realbook 1
			//A CHILD IS BORN 2 The Realbook 1
			//A FAMILY JOY 4 The Realbook 1
			//A FINE ROMANCE 3 The Realbook 1
			
			if (line.contains("The Realbook")) {
				String[] arr = line.split(SPACE);
				int arrLen = arr.length;  
				if (arrLen > 1) {
					String rbNum = arr[arrLen - 1];
					String pageNum = arr[arrLen - 4];
					String title = "";
					for (int i = 0, n = arrLen - 4; i < n; i++) {
						title += Util.ucFirst(arr[i]) + SPACE; 
					}
					sb.append(title.trim() + PIPE + pageNum + PIPE + rbNum + NL);
				}				
			}
		}			

		Util.writeToFile(outputFile, sb.toString(), false, false);
	}
	*/
	
	/**
	 * This method merges lists of jazz standards from various sources into a 
	 * single file suitable for loading into a google spreadsheet.
	 */
	public void mergeJazzStandards() {
		// array used when reading from files
		List<String> lines = null;
		
		// exported from google JazzStandards file (originally generated from jazzstandards.com)
		File jazzStandardsFile								= new File(jazzStandardsDir, "JazzStandards.tsv");
		// generated from  data/RealbookJazzLTDindex.pdf
		File realbookIndexFile								= new File(jazzStandardsDir, "RealbookJazzLTDindex-out.txt");
		// data/irealb-jazz1200.txt
		File irealbJazzFile									= new File(jazzStandardsDir, "irealb-jazz1200.txt");
		// data/irealb-gypsyJazz.txt
		File irealbGypsyJazzFile							= new File(jazzStandardsDir, "irealb-gypsyJazz.txt");

		List<JazzStandardsItem> jazzStandardsItems 			= new ArrayList<JazzStandardsItem>();
		Map<String, RealbookItem> realbookItemsMap 			= new HashMap<String, RealbookItem>();
		Map<String, IrealbItem> irealbItemsMap 				= new HashMap<String, IrealbItem>();
		Map<String, IrealbGypsyItem> irealbGypsyItemsMap	= new HashMap<String, IrealbGypsyItem>();

		// parse jazzStandardsFile to generate jazzStandardsItems list
		lines = Util.getLines(jazzStandardsFile);
		for (String line: lines) {
			line = line.trim();

			JazzStandardsItem jazzStandardsItem = new JazzStandardsItem(line); 
			jazzStandardsItems.add(jazzStandardsItem);
		}
		
		// generate realbookItemsMap - titleKey->realbookItem
		lines = Util.getLines(realbookIndexFile);
		for (String line: lines) {
			// 52nd Street Theme|93|2
			line = line.trim();
			RealbookItem realbookItem = new RealbookItem(line);
			
			// note: there are some cases of multiple entries for the same title with different rb references
			if (realbookItemsMap.containsKey(realbookItem.titleKey)) {
				RealbookItem existingRealbookItem = realbookItemsMap.get(realbookItem.titleKey);
				existingRealbookItem.rb += "," + realbookItem.rb;  
			} else {
				realbookItemsMap.put(realbookItem.titleKey, realbookItem);				
			}
		}			
		
		// generate irealbItemsMap - titleKey->irealbItem
		lines = Util.getLines(irealbJazzFile);
		for (String line: lines) {
			IrealbItem irealbItem = new IrealbItem(line);
			irealbItemsMap.put(irealbItem.titleKey, irealbItem);
		}			
		
		// generate irealbGypsyItemsMap - titleKey->irealbGypsyItem
		lines = Util.getLines(irealbGypsyJazzFile);
		for (String line: lines) {
			IrealbGypsyItem irealbGypsyItem = new IrealbGypsyItem(line);
			irealbGypsyItemsMap.put(irealbGypsyItem.titleKey, irealbGypsyItem);
		}			
		
		// merge realbookItems with jazzStandardsItems
		for (JazzStandardsItem jazzStandardsItem: jazzStandardsItems) {
			RealbookItem realbookItem = realbookItemsMap.get(jazzStandardsItem.titleKey);
			if (realbookItem != null) {
				jazzStandardsItem.rb = realbookItem.rb;
				//jazzStandardsItem.srcs += "," + JazzStandardsItem.RBI;
				realbookItem.matchFound = true;
			}
		}	

		// add unmerged realbookItems to jazzStandardsItems
		Set<Map.Entry<String, RealbookItem>> realbookEntries = realbookItemsMap.entrySet();
		for(Map.Entry<String, RealbookItem> entry : realbookEntries) {
			RealbookItem realbookItem = entry.getValue();
			if (!realbookItem.matchFound) {
				jazzStandardsItems.add(new JazzStandardsItem(realbookItem));
			}
		}

		// merge irealbItems with jazzStandardsItems
		for (JazzStandardsItem jazzStandardsItem: jazzStandardsItems) {
			IrealbItem irealbItem = irealbItemsMap.get(jazzStandardsItem.titleKey);
			if (irealbItem != null) {
				jazzStandardsItem.composer = irealbItem.composer;
				//jazzStandardsItem.srcs += "," + JazzStandardsItem.IRB;
				jazzStandardsItem.irb = Y;
				irealbItem.matchFound = true;
			}
		}	

		// add unmerged irealbItems to jazzStandardsItems
		Set<Map.Entry<String, IrealbItem>> irealbEntries = irealbItemsMap.entrySet();
		for(Map.Entry<String, IrealbItem> entry : irealbEntries) {
			IrealbItem irealbItem = entry.getValue();
			if (!irealbItem.matchFound) {
				jazzStandardsItems.add(new JazzStandardsItem(irealbItem));
			}
		}
		
		// merge irealbGypsyItems with jazzStandardsItems
		for (JazzStandardsItem jazzStandardsItem: jazzStandardsItems) {
			IrealbGypsyItem irealbGypsyItem = irealbGypsyItemsMap.get(jazzStandardsItem.titleKey);
			if (irealbGypsyItem != null) {
				//jazzStandardsItem.srcs += "," + JazzStandardsItem.IRBG;
				jazzStandardsItem.irbg = Y;
				irealbGypsyItem.matchFound = true;
			}
		}	

		// add unmerged irealbGypsyItems to jazzStandardsItems
		Set<Map.Entry<String, IrealbGypsyItem>> irealbGypsyEntries = irealbGypsyItemsMap.entrySet();
		for(Map.Entry<String, IrealbGypsyItem> entry : irealbGypsyEntries) {
			IrealbGypsyItem irealbGypsyItem = entry.getValue();
			if (!irealbGypsyItem.matchFound) {
				jazzStandardsItems.add(new JazzStandardsItem(irealbGypsyItem));
			}
		}

		
		// sort
		Collections.sort(jazzStandardsItems, jazzStandardsItemComparator);
		
		// generate output	
		StringBuffer sb = new StringBuffer();
		sb.append(JazzStandardsItem.getHeader() + NL);
		for (JazzStandardsItem jazzStandardsItem: jazzStandardsItems) {
			sb.append(jazzStandardsItem.toString() + NL);			
		}

		// the jazz standards merge file
		File mergeFile	= new File(jazzStandardsDir, "JazzStandardsMerge.txt");

		// write output to file
		Util.writeToFile(mergeFile, sb.toString(), false, false);
		
		log.debug("ok");
	}	
	
	/** Comparator used to order a list of Interval objects. */
	public static final Comparator<JazzStandardsItem> jazzStandardsItemComparator 
									= new Comparator<JazzStandardsItem>() {
		public int compare(JazzStandardsItem i1,  JazzStandardsItem i2) {
			return i1.title.compareTo(i2.title);
		}
		
	};
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//new JazzStandards().parseRealbookJazzLTDindex();
		new JazzStandards().mergeJazzStandards();
	}
}


