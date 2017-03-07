package audio;

public class Main {

	public static void main(String[] args) {
		try {
			if (args.length == 1) {
	    		String arg = args[0];
	    		//new Main().run(arg);
			} else {
				System.out.println("no arg");
			}
		} catch (Exception e) {
			System.out.println(e);
		}	
	}
}
