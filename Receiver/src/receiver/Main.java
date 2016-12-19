package receiver;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
	
	/**
	 * <b>The program starts here.</b><br>
	 * Call the program with argument "-help" to see the use instructions<br>
	 * <br>
	 * @param args contains the given arguments
	 */
	public static void main(String[] args) {
		// check if help is needed
		if (args.length != 0 && "-help".equals(args[0])) {
			// show help
			showHelp();
		} 
		// normal start
		else {
			try {
				// create a new Receiver object
				final Receiver receiver = new Receiver("files/file.txt");
				// start receiving
				receiver.receive();
			} catch (FileNotFoundException exception) {
				showError(
						"Sorry! The file to write to could not be found."
						);
			} catch (IOException exception) {
				showError(
						"Sorry! An Error occured while writing the File."
						);
			}
		}
	}
	
	/**
	 * <b>Print the use instructions.</b>
	 */
	private static void showHelp() {
		System.out.println(
				""
				+ "####################\r\n"
				+ "# USE INSTRUCTIONS #\r\n"
				+ "####################\r\n"
				+ "\r\n"
				+ "The program has to be called with no arguments.\r\n"
				+ "\r\n"
				+ "Have fun!");
	}
	
	/**
	 * <b>Print the given Error Message.</b>
	 * 
	 * @param msg is the given message
	 */
	private static void showError(String msg) {
		System.err.println(msg);
	}
	
}
