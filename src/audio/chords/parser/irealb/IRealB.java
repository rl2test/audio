package audio.chords.parser.irealb;

import static audio.Constants.DATA_DIR;
import static audio.Constants.MUSIC_DIR;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.log4j.Logger;

import audio.Util;


/**
 * This class uses the song definitions in 'irealb[n].txt'.
 */
public class IRealB {
	/** The log. */
	private Logger log = Logger.getLogger(getClass());
	private File jazzChordsDir = new File(MUSIC_DIR, "jazz/chords");
	
	public void run() {
		File irealbDir 	= new File(DATA_DIR, "irealb");
		// input data file
		File inputFile 	= null;	
		// output chords dir
		File outputDir 	= null;
		// source
		String source  	= null;	
		
		// Each '<datFileName>.txt' file contains multiple songs in a url-encoded
		// file, stored as a single String. The songs can be split on the '='
		// char prior to decoding them. Note - remove irealb protocol at the 
		// start of each input string.
		
		// jazz 1-4 ////////////////////////////////////////////////////////////
		/**/
		// parse each 'jazz[n].txt' file and generate corresponding '.chords' files.
		source = "IRealB - Jazz";
		for (int i = 1; i < 5; i++) {
			// input data file
			inputFile = new File(irealbDir, "jazz" + i + ".txt");	
			// output chords dir
			outputDir = new File(jazzChordsDir, "IRealB Jazz");

			irealbToChords(inputFile, outputDir, source);
		}
		
		
		// gypsyJazz ///////////////////////////////////////////////////////////
		/*
		// parse gypsyJazz.txt' file and generate corresponding '.chords' files.
		source = "iRealB - Gypsy Jazz";
		// input data file
		inputFile = new File(irealbDir, "gypsyJazz.txt");	
		// output chords dir
		outputDir = new File(JAZZ_CHORDS_DIR, "IRealB GypsyJazz");
		
		irealbToChords(inputFile, outputDir, source);
		*/
		
		
		// exercises ///////////////////////////////////////////////////////////
		/*
		source = "iRealB - Exercises"; 
		// input data file
		inputFile = new File(irealbDir, "exercises.txt");	
		// output chords dir
		outputDir = new File(JAZZ_CHORDS_DIR, "IRealB Exercises");
		*/
		

		// test ///////////////////////////////////////////////////////////
		/*
		source = "IRealB - Test"; 
		// input data file
		inputFile = new File(irealbDir, "test.txt");	
		// output chords dir
		outputDir = new File(JAZZ_CHORDS_DIR, "IRealB Test");
		
		irealbToChords(inputFile, outputDir, source);
		*/
	}

	public void irealbToChords(File inputFile, File outputDir, String source) {
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		// get the first line and trim it	
		List<String> lines = Util.getLines(inputFile);
	
		for (String line: lines) {
			line = line.trim();
			
			// split into individual songs on the '=' char
			String[] songStrs = line.split("=");
			
			//int songCount = 1;
			for (String songStr: songStrs) {
				try {
					// decode the songStr
					songStr = URLDecoder.decode(songStr, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					log.error(songStr);
				}
				
				// strip unused syntax (text annotations, alternate chords)
				//log.debug(songStr);
				songStr = strip(songStr);

				if (songStr.contains("=")) {
					//log.debug(songStr);
					
					// create new song
					Song song = new Song(songStr, source);
					//log.debug(songCount + NL  + song + NL);
					
					// save sing as '.chords' file	
					song.saveAsChordFile(outputDir);
					
					//songCount++;
				}
			}			
		}
	}
	
	/**
	 * Strip unused syntax (text annotations, alternate chords).
	 * 
	 * @param s
	 * @return
	 */
	private String strip(String s) {
		s = s.replaceAll("\\<.*?\\>", "");
		s = s.replaceAll("\\(.*?\\)", "");

		return s;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new IRealB().run();
	}

}

