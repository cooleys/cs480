/*
*	Sarah Cooley
*	CS480 - Winter2011
*	Assignment 2
*	Original Author: Tim Budd
*/

import java.io.*;

class Assn2 {
	public static void main(String [ ] args) {
		System.out.println("Reading file " + args[0]);
		try {
			FileReader instream = new FileReader(args[0]);
			Parser par = new Parser(new Lexer(instream), true);
			par.parse();
		}
			catch(ParseException e) 
				{ System.out.println("Parse Error " + e); }
			catch(FileNotFoundException e) 
				{ System.err.println("File not found " + e); }
			catch(IOException e) 
				{ System.err.println("File IO Exception " + e); }
		}
}
