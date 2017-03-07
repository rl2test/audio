package audio.chords.parser.guitarcodex;

import static audio.Constants.BS;
import static audio.Constants.DATA_DIR;
import static audio.Constants.EXT_ABC;
import static audio.Constants.MUSIC_DIR;
import static audio.Constants.NL;
import static audio.Constants.PIPE_DELIM;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import audio.Util;

/**
 * This class uses the chord definitions (in 'guitarCodex-orig.txt') extracted 
 * (using a java decompiler) from GuitarCodex_Plus.jar (http://www.microtools.de/gcplus/) 
 * to generate an .abc file with custom abc/midi chord definitions.
 */
public class GuitarCodex {
	/** The log. */
	private Logger log = Logger.getLogger(getClass());
	
	public void run() {
		File inputFile 						= new File(DATA_DIR, "guitarCodex-orig.txt");
		//File byLineFile 					= new File(DATA_DIR + FS + "guitaCodex-format.txt");
		File outputFile 					= new File(MUSIC_DIR, "jazz/abc/GuitarCodex/" + "chordlist" + EXT_ABC);
		StringBuffer sb						= new StringBuffer();
		//StringBuffer sbByLine				= new StringBuffer();
		List<AbcDefinition> abcDefinitions 	= new ArrayList<AbcDefinition>(); 
		Set<String> intervalSet				= new HashSet<String>(); 	
		
		sb.append("X:1" + NL);
		sb.append("T:GuitarCodex Chord Definitions" + NL);
		sb.append("S:GuitarCodex - http://www.microtools.de/gcplus/" + NL);
		sb.append("M:4/4" + NL);
		sb.append("Q:1/4=80" + NL);
		sb.append("L:1/4" + NL);
		sb.append("K:C" + NL);
		sb.append("%%MIDI program   26      % electric jazz guitar" + NL);
		sb.append("%%MIDI chordprog 4       % electric piano" + NL);
		sb.append("%%MIDI bassprog  32      % acoustic bass" + NL);
		sb.append("%%MIDI gchord    fzczbzcz" + NL);
		
		String line = Util.getFirstLine(inputFile);
		line = line.trim();
		String[] chords = line.split(PIPE_DELIM); 

		for (String chord: chords) {
			log.debug(chord);
			//sbByLine.append(chord + NL);
			
			AbcDefinition abcDefinition = new AbcDefinition();
			
			String[] arr = chord.split(":");
			int i = 1;
			
			for (String s: arr) {
				s = s.trim();
				if (i == 1) {
					abcDefinition.symbol = s;
					String abcSymbol = toAbcSymbol(s);
					abcDefinition.abcSymbol = (abcSymbol.equals(s)) ? "" : abcSymbol;
				} else if (i == 2) {
					abcDefinition.desc = s.trim();
				} else {
					intervalSet.add(s);
					abcDefinition.intervals += s + " ";
					abcDefinition.semitones += AbcDefinition.intervalSemitoneMap.get(s) + " ";
					abcDefinition.cNotes.add(AbcDefinition.intervalCNoteMap.get(s));
				}
				
				i++;
			}
				
			abcDefinitions.add(abcDefinition);
		}
		
		//Util.writeToFile(byLineFile, sbByLine.toString(), false, false);
		
		for (AbcDefinition abcDefinition: abcDefinitions) {
			sb.append(abcDefinition.toMidiString() + NL);
		}
		
		int counter = 0;
		for (AbcDefinition md: abcDefinitions) {
			sb.append(md.toAbc());

			if (counter % 2 == 0) {
				sb.append(BS + NL);	
			} else {
				sb.append(NL);
			}
			
			counter++;
		}
		
		Util.writeToFile(outputFile, sb.toString(), false, false);

		String intervalsArr = "";
		for (String s: intervalSet) {
			intervalsArr += "\"" + s + "\", ";
		}
		log.debug(intervalsArr);
	}
	
	/**
	 * Some codex definitions contain characters not recognized by abcexplorer.
	 * eg '/' needs to be replaced with 'add'
	 * 
	 * @param s
	 * @return
	 */
	public String toAbcSymbol(String s) {
		if (s.contains("/")) {
			s = s.replace("/", "add");
		}
		return s;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new GuitarCodex().run();
	}

}
