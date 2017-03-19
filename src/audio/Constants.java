package audio;

import java.awt.Color;
import java.awt.Font;
import java.io.File;

import org.apache.log4j.Logger;

public class Constants {
	/** The log. */
	protected static Logger log 						= Logger.getLogger(Constants.class);
	public static final String WK						= "wk";
	public static final String HM						= "hm";
	//java.runtime.version=1.7.0_79-b15
	public static final String RUNTIME_VERSION			= System.getProperty("java.runtime.version");
	public static final String ENV						= (RUNTIME_VERSION.equals("1.7.0_79-b15")) ? WK : HM;

	public static final File APP_DIR					= new File("/Users/rlowe/rob/apps/audio"); 
	
	public static final File PROPERTIES_FILE			= new File(APP_DIR, "audio.properties");
	public static final File BARS_FILE					= new File(APP_DIR, "bars.txt");

	/* dir definitions */
	/** The application data dir. */
	public static final File DATA_DIR					= new File(APP_DIR, "data");
	
	/** The top-level music dir. */
	public static final File MUSIC_DIR					= (ENV.equals(WK))
			? new File("/Users/rlowe/rob/music")
			: new File("/Volumes/IOMEGA-1000/Rob/music");

	/** The abc output dir. */
	public static final File ABC_DIR					= new File(MUSIC_DIR, "rlp-abc/files");
	
	/* char definitions */
	public static final String BS						= "\\";		// back slash
	public static final String COLON					= ":"; 
	public static final String COMMA					= ","; 
	public static final String SQ						= "'"; 
	public static final String DASH						= "-"; 
	public static final String FS						= "/"; 		// forward slash
	public static final String NL						= "\n";		// new line	
	public static final String PERIOD					= "."; 
	public static final String PIPE						= "|"; 
	public static final String SPACE					= " "; 
	public static final String TAB						= "\t"; 
	public static final String US						= "_"; 
	public static final String PARENS_OPEN 				= "(";	
	public static final String PARENS_CLOSE				= ")"; 
	public static final String DQ						= "\"";
	public static final String EQUALS					= "=";
	public static final String DIGITS 					= "0123456789";
	public static final String REST 					= "z";
	public static final String GT 						= ">";
	public static final String LT 						= "<";
	public static final String HASH						= "#";

	public static final String DIVIDER_40				= "========================================";
	
	/** un-transposable elements: :><|-/() z0-9 */
	public static final String UNTRANSPOSABLE_ABC_ELEMENTS = COLON + GT + LT + PIPE + DASH + FS + PARENS_OPEN + PARENS_CLOSE + SPACE + REST + DIGITS;
	
	public static final String N						= "n";		// n
	public static final String Y						= "y";		// y
	
	/** Html line break. */
	public static final String BR						= "<br />";
	/** Html line break with new-line char. */
	public static final String BRNL						= BR + NL;
	
	/** Undefined int. */
	public static final int UNDEF						= -1;
	
	/* file extensions */
	/** The '.abc' extension. */
	public static final String EXT_ABC					= ".abc";
	/** The '.chords' extension. */
	public static final String EXT_CHORDS				= ".chords";
	/** The '.html' extension. */
	public static final String EXT_HTML					= ".html";
	/** The '.txt' extension. */
	public static final String EXT_TXT					= ".txt";


	/* data definition files */
	/** The chord-types definition file. */
	public static final File CHORD_TYPES_FILE			= new File(DATA_DIR, "chord-types" + EXT_TXT);
	/** The integer-notes definition file. */
	public static final File INTEGER_NOTES_FILE			= new File(DATA_DIR, "integer-notes" + EXT_TXT);
	/** The integer-notes-abc definition file. */
	public static final File INTEGER_NOTES_ABC_FILE		= new File(DATA_DIR, "integer-notes-abc" + EXT_TXT);
	/** The abc-notes definition file. */
	public static final File ABC_NOTES_FILE				= new File(DATA_DIR, "abc-notes" + EXT_TXT);		
	/** C-scales as defined in Coursera - Introduction To Improvisation - L02_the_10_scales.pdf. */
	public static final File C_SCALES_FILE				= new File(DATA_DIR, "c-scales" + EXT_TXT);
	/** Keys file. */
	public static final File KEYS_FILE					= new File(DATA_DIR, "keys" + EXT_TXT);
	
	/* file filters */
	/** Dir file filter. */
	public static final DirFileFilter DIR_FILE_FILTER	= new DirFileFilter();    
	/** '.chords' extension file filter. */
	public static final ExtensionFilter CHORDS_FILE_FILTER	
														= new ExtensionFilter(EXT_CHORDS);    
	/* misc string definitions */
	/** The pipe delimiter as a regexp. */
	public static final String PIPE_DELIM 				= "[" + PIPE + "]";
	public static final String CSV						= ", ";
	public static final String BARLINE					= SPACE + PIPE + SPACE;
	public static final String BARLINE_DOUBLE			= SPACE + PIPE + PIPE + SPACE;
	public static final String BARLINE_REPEAT			= SPACE + COLON + PIPE + SPACE;
	public static final String REST_4					= "z4";
	public static final String ALPHABET					= "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	/*
		C major:	C	Dm	Em	F	G	Am
		G major:	G	Am	Bm	C	D	Em
		D major:	D	Em	F♯m	G	A	Bm
		A major:	A	Bm	C♯m	D	E	F♯m
		E major:	E	F♯m	G♯m	A	B	C♯m
		B major:	B	C♯m	D♯m	E	F♯	G♯m
		F♯ major:	F♯	G♯m	A♯m	B	C♯	D♯m
		G♭ major:	G♭	A♭m	B♭m	C♭	D♭	E♭m
		D♭ major:	D♭	E♭m	Fm	G♭	A♭	B♭m
		A♭ major:	A♭	B♭m	Cm	D♭	E♭	Fm
		E♭ major:	E♭	Fm	Gm	A♭	B♭	Cm
		B♭ major:	B♭	Cm	Dm	E♭	F	Gm
		F major:	F	Gm	Am	B♭	C	Dm

		C  F  Bb Eb    Ab Db Gb/F# B  E  A  D G
		c  f  bb eb/d# g# c# f#    b  e  a  d g  
	 */
	
	public static final String[] TRANSPOSE_KEYS 		= {"C", "F", "Bb", "Eb",    "Ab", "Db", "Gb/F#", "B", "E", "A", "D", "G"};
	public static final String[] TRANSPOSE_KEYS_MINOR	= {"c", "f", "bb", "eb/d#", "g#", "c#", "f#",    "b", "e", "a", "d", "g"};
	public static final int[] TRANSPOSE_KEY_INTERVALS	= { 0,   5,   10,   3,       8,    1,    6,       11,  4,   9,   2,   7 };
	
	/** The list of alphabetical tokens. */	
	public static final String[] ALPHABET_TOKENS 		= {"C", "D", "E", "F", "G", "A", "B"};
	public static final String[] SHARPS					= {"F", "C", "G", "D", "A", "E", "B"};
	public static final String[] FLATS					= {"B", "E", "A", "D", "G", "C", "F"};

	/* interval integers. */
	/** Perfect fifth. */
	public static final int PERFECT_FIFTH				= 7;
	/** Octave. */
	public static final int OCTAVE 						= 12;
	
	/* MIDI instrument definitions */
	public static final int PIANO_1 					= 0;
	public static final int PIANO_2 					= 1;
	public static final int PIANO_3 					= 2;
	public static final int HONKY_TONK 					= 3;
	public static final int ELECTRIC_PIANO_1 			= 4;
	public static final int ELECTRIC_PIANO_2 			= 5;
	public static final int HARPSICHORD 				= 6;
	public static final int CLAVICHORD					= 7;
	public static final int CELESTA 					= 8;
	public static final int GLOCKENSPIEL 				= 9;
	public static final int MUSIC_BOX 					= 10;
	public static final int VIBRAPHONE 					= 11;
	public static final int MARIMBA 					= 12;
	public static final int XYLOPHONE 					= 13;
	public static final int TUBULAR_BELL 				= 14;
	public static final int SANTUR 						= 15;
	public static final int ORGAN_1 					= 16;
	public static final int ORGAN_2				 		= 17;
	public static final int ORGAN_3 					= 18;
	public static final int CHURCH_ORG_1 				= 19;
	public static final int REED_ORGAN 					= 20;
	public static final int ACCORDION_FR 				= 21;
	public static final int HARMONICA 					= 22;
	public static final int BANDONEON 					= 23;
	public static final int NYLON_STRING_GUITAR			= 24;
	public static final int STEEL_STRING_GUITAR			= 25;
	public static final int JAZZ_GUITAR					= 26;
	public static final int CLEAN_GUITAR				= 27;
	public static final int MUTED_GUITAR				= 28;
	public static final int OVERDRIVE_GUITAR			= 29;
	public static final int DISTORTION_GUITAR			= 30;
	public static final int GUITAR_HARMONICS 			= 31;
	public static final int ACOUSTIC_BASS 				= 32;
	public static final int FINGERED_BASS				= 33;
	public static final int PICKED_BASS 				= 34;
	public static final int FRETLESS_BASS				= 35;
	public static final int SLAP_BASS_1 				= 36;
	public static final int SLAP_BASS_2 				= 37;
	public static final int SYNTH_BASS_1 				= 38;
	public static final int SYNTH_BASS_2 				= 39;
	public static final int VIOLIN 						= 40;
	public static final int VIOLA 						= 41;
	public static final int CELLO 						= 42;
	public static final int CONTRABASS 					= 43;
	public static final int TREMOLO_STRINGS				= 44;
	public static final int PIZZICATO_STRINGS			= 45;
	public static final int HARP 						= 46;
	public static final int TIMPANI 					= 47;
	public static final int STRINGS 					= 48;
	public static final int SLOW_STRINGS 				= 49;
	public static final int SYN_STRINGS1 				= 50;
	public static final int SYN_STRINGS2 				= 51;
	public static final int CHOIR_AAHS 					= 52;
	public static final int VOICE_OOHS 					= 53;
	public static final int SYNVOX 						= 54;
	public static final int ORCHESTRAHIT 				= 55;
	public static final int TRUMPET 					= 56;
	public static final int TROMBONE 					= 57;
	public static final int TUBA 						= 58;
	public static final int MUTEDTRUMPET 				= 59;
	public static final int FRENCH_HORNS 				= 60;
	public static final int BRASS_1 					= 61;
	public static final int SYNTH_BRASS_1				= 62;
	public static final int SYNTH_BRASS_2				= 63;
	public static final int SOPRANO_SAX 				= 64;
	public static final int ALTO_SAX 					= 65;
	public static final int TENOR_SAX 					= 66;
	public static final int BARITONE_SAX 				= 67;
	public static final int OBOE 						= 68;
	public static final int ENGLISH_HORN 				= 69;
	public static final int BASSOON 					= 70;
	public static final int CLARINET 					= 71;
	public static final int PICCOLO 					= 72;
	public static final int FLUTE 						= 73;
	public static final int RECORDER 					= 74;
	public static final int PAN_FLUTE 					= 75;
	public static final int BOTTLE_BLOW 				= 76;
	public static final int SHAKUHACHI 					= 77;
	public static final int WHISTLE 					= 78;
	public static final int OCARINA 					= 79;
	public static final int SQUARE_WAVE 				= 80;
	public static final int SAW_WAVE 					= 81;
	public static final int SYN_CALLIOPE 				= 82;
	public static final int CHIFFER_LEAD 				= 83;
	public static final int CHARANG 					= 84;
	public static final int SOLO_VOX 					= 85;
	public static final int FIFTH_SAW_WAVE 				= 86;
	public static final int BASS_AND_LEAD 				= 87;
	public static final int FANTASIA 					= 88;
	public static final int WARM_PAD 					= 89;
	public static final int POLYSYNTH 					= 90;
	public static final int SPACE_VOICE 				= 91;
	public static final int BOWED_GLASS 				= 92;
	public static final int METAL_PAD 					= 93;
	public static final int HALO_PAD 					= 94;
	public static final int SWEEP_PAD 					= 95;
	public static final int ICE_RAIN 					= 96;
	public static final int SOUNDTRACK 					= 97;
	public static final int CRYSTAL 					= 98;
	public static final int ATMOSPHERE 					= 99;
	public static final int BRIGHTNESS 					= 100;
	public static final int GOBLIN 						= 101;
	public static final int ECHO_DROPS 					= 102;
	public static final int STAR_THEME 					= 103;
	public static final int SITAR 						= 104;
	public static final int BANJO 						= 105;
	public static final int SHAMISEN 					= 106;
	public static final int KOTO 						= 107;
	public static final int KALIMBA 					= 108;
	public static final int BAGPIPE 					= 109;
	public static final int FIDDLE 						= 110;
	public static final int SHANAI 						= 111;
	public static final int TINKLE_BELL 				= 112;
	public static final int AGOGO 						= 113;
	public static final int STEEL_DRUMS 				= 114;
	public static final int WOODBLOCK 					= 115;
	public static final int TAIKO 						= 116;
	public static final int MELO_TOM_1 					= 117;
	public static final int SYNTH_DRUM 					= 118;
	public static final int REVERSE_CYM 				= 119;
	public static final int GUITAR_FRETNOISE			= 120;
	public static final int BREATH_NOISE 				= 121;
	public static final int SEASHORE 					= 122;
	public static final int BIRD 						= 123;
	public static final int TELEPHONE_1 				= 124;
	public static final int HELICOPTER 					= 125;
	public static final int APPLAUSE 					= 126;
	public static final int GUN_SHOT 					= 127;
	
	/* *********************** Chords constants ***************************** */
	
	public static final ExtensionFilter CHORDS_EXTENSION_FILTER 	
														= new ExtensionFilter(EXT_CHORDS);
	public static final String CHORDS_FOLDER			= "/chords";
	public static final String CHORDS_TO_ABC_DIR		= "/abc/audio/chordsToAbc";

	/* output to single or multiple files */
	/** 
	 * Output from all input files will be written to a single '.abc' 
	 * chordscales file.
	 */
	public static final int OUTPUT_TO_SINGLE_ABC_FILE	= 1;
	/** 
	 * Output from each input file will be written to a corresponding '.abc' 
	 * chordscales file.
	 */
	public static final int OUTPUT_TO_INDIVIDUAL_ABC_FILES	
														= 2;
	
	public final String SYSTEM_MYMIT					= "mymit";
	public final String SYSTEM_AEBERSOLD				= "aebersold";
	public final String SCALE_SYSTEM					= SYSTEM_AEBERSOLD; //default setting 
	
	/* '.chords' file info field definitions, corresponding to '.abc' file definitions. */
	public static final String INFO_TITLE 				= "T";
	public static final String INFO_COMPOSER 			= "C";
	public static final String INFO_TEMPO 				= "Q";
	public static final String INFO_METER 				= "M";
	
	/* ************************* Gui constants ****************************** */
	
	/** The height of the Windows taskbar. */
	public static int TOP_BAR_HEIGHT 					= 22;
	public static int DOCK_WIDTH 						= 38;
	
	/** The default font. */
	public static Font FONT 							= new Font("Arial", Font.PLAIN, 12);
	/** The default font small. */
	public static Font FONT_SM 							= new Font("Arial", Font.PLAIN, 10);
	
	/* start, end defs */
	public static int START								= 0;
	public static int END								= 1;

	public static String[] GENRE_NAMES = {"country", "jazz", "jewish", "klezmer", "other"};
	public static boolean GENERATE_CHORD_TYPES_HTML_FILE = false;
	public static String GENRE_NAME = "jazz";
	public static String FOLDER_NAME = "Aebersold - Jazz Handbook";
	public static String TUNE_NAME = "01";
	public static int BARS_PER_LINE = 8;
	/*
		#
		genreNames
		generateChordTypesHtmlFile=false
		#chordFilePanel
		chordFile.GenreName.default=jazz
		chordFile.FolderName.default=Aebersold - Jazz Handbook
		chordFile.TuneName.default=01
		#displayPanel
		display.BarsPerLine.default=8
	 */
	
	/*
	0 0
	1 15
	2 31
	3 47
	4 63
	5 79
	6 95
	7 111
	8 127
	9 143
	10 159
	11 175
	12 191
	13 207
	14 223
	15 239
	16 255
	*/
	/* Preset values between 0 and 255 used for vol and pan. */ 
	public static final int[] V 						= new int[17];
	/* Preset widths in multiples of 25. */
	public static int[] W 								= new int[17];
	/* Preset colors. */ 
	public static Color[] C								= new Color[17];
	/* Preset octaves. */ 
	public static int[] O								= new int[10];

	static {
		int i = 0;
		for (i = 0; i <= 16; i++) {
			int v = (i == 0) ? 0 : i * 16 - 1;
			log.debug(i + " " + v);
			V[i] = v;
			W[i] = i * 25;
			//C[i] = new Color(v, v, v);
		}
		Color[] colors = new Color[17];
		i = 16;
		C[i--] = new Color(255, 255, 255);
		C[i--] = new Color(235, 235, 243);
		C[i--] = new Color(216, 216, 231);
		C[i--] = new Color(197, 197, 218);
		C[i--] = new Color(178, 178, 206);
		C[i--] = new Color(160, 160, 193);
		C[i--] = new Color(142, 142, 179);
		C[i--] = new Color(124, 124, 166);
		C[i--] = new Color(107, 107, 152);
		C[i--] = new Color(91, 91, 137);
		C[i--] = new Color(75, 75, 122);
		C[i--] = new Color(60, 60, 107);
		C[i--] = new Color(45, 45, 90);
		C[i--] = new Color(31, 31, 73);
		C[i--] = new Color(19, 19, 54);
		C[i--] = new Color(8, 8, 32);
		C[i--] = new Color(0, 0, 0);
		
		for (i = 0; i < 10; i++) {
			O[i] = i * 12 + 12;
		}
	}
}
