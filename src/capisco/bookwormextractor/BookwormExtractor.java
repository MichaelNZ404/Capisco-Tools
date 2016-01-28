package capisco.bookwormextractor;

import java.io.IOException;

import capisco.bookwormextractor.handlers.LettersAnalyze;

/**
 * 
 * @author mjc62
 * Note: Since the cases for each text file are different, the extraction for each file format will need to be written into the handler package
 *
 */
public class BookwormExtractor {
	
	/**
	 * 
	 * @param args[0] input text file
	 * @param args[1] output directory
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String input = args[0];
		String output = args[1];
		
		if(!output.endsWith("/")){
			output = output + "/";
		}
		
		LettersAnalyze.DickensLettersAnalyze(input, output);
	}		
}
