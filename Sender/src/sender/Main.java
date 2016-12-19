package sender;

import java.io.File;
import java.io.IOException;

public class Main {
	
	/**
	 * <b>The program starts here.</b><br>
	 * Call the program with argument "-help" to see the use instructions,
	 * otherwise two arguments are needed:<br>
	 * <br>
	 * 1. <b>File Name</b> (including the path, if program does not start in the same folder as the file<br>
	 * 2. <b>Destination</b> IP-Address or "localhost"<br>
	 * 
	 * @param args contains the given arguments
	 */
	public static void main(String[] args) {
		// check if help is needed
		if ("-help".equals(args[0])) {
			// show help
			showHelp();
		} 
		// check number of given arguments
		else if (args.length != 2) {
			showError(
					"The number of arguments must be 2, but was " + args.length + "."
					+ "\r\n"
					+ "If you need help, start the program with the -help argument."
					);
		}
		// check if second argument is a IPv4 address or "localhost"
		else if (!validIpv4(args[1]) && !"localhost".equals(args[1])) {
			showError(
					"The given IPv4-Address is invalid. It should be something like \"192.168.2.1\"."
					+ "\r\n"
					+ "If you need help, start the program with the -help argument.");
		}
		// check if the given file is a directory
		else if (new File(args[0]).isDirectory()) {
			showError(
					"\"" + args[0] + "\" is a directory and not a file."
					+ "\r\n"
					+ "If you need help, start the program with the -help argument."
					);
		}
		// check if the given file exists
		else if (!new File(args[0]).exists()) {
			showError(
					"The named File \"" + args[0] + "\" can not be found."
					+ "\r\n"
					+ "If you need help, start the program with the -help argument."
					);
		}
		// everything is ok
		else {
			// get values from arguments
			final String fileName = args[0];
			final String destination = "localhost".equals(args[1]) ? "127.0.0.1" : args[1];
			try {
				// create new sender object, new File(fileName).toPath() might throw an exception
				final Sender sender = new Sender(new File(fileName).toPath(), destination);
				// start sending
				sender.send();
			} catch (IOException exception) {
				showError(
						"Sorry! An Error occured while reading the File \"" + fileName + "\"."
						+ "\r\n"
						+ "Please try again."
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
				+ "The program has to be called with two arguments:\r\n"
				+ "\r\n"
				+ "1. File Name\r\n"
				+ "\tThis argument is the complete File Name of the File to be send.\r\n"
				+ "\tIf the program does not start in the files direktory, you have\r\n"
				+ "\tto use the relative path from the program to the file.\r\n"
				+ "\tYou can reach the parent folder using \"../\" in the path.\r\n"
				+ "\tExample File Name: \"../../Folder/File.txt\"\r\n"
				+ "\r\n"
				+ "2. Destination\r\n"
				+ "\tThis argument is the IPv4-Address of the receiver.\r\n"
				+ "\tIf the receiver is localhost, you can just type \"localhost\"\r\n"
				+ "\tinstead of the IP-Address \"127.0.0.1\"\r\n"
				+ "\tExample IPv4-Address: \"192.169.2.1\"\r\n"
				+ "\r\n");
	}
	
	/**
	 * <b>Print the given Error Message.</b>
	 * 
	 * @param msg is the given message
	 */
	private static void showError(String msg) {
		System.err.println(msg);
	}
	
	/**
	 * <b>Check, if the argument is a valid IP-Address.</b>
	 * 
	 * @param ip is the given IP-Address.
	 * @return true, if the IP-Address is valid
	 */
	private static boolean validIpv4(String ip) {
		// store the result
		boolean result = false;
		// split the IP-Address at the dots
		final String[] splitted = ip.split(".");
		// a IPv4-Address should split in 4 elements
		if (splitted.length == 4) {
			// set result true and check all elements later
			result = true;
			// loop through all elements
			for (final String element : splitted) {
				try {
					// parse element to int (might throw an exception)
					final int elementAsInt = Integer.parseInt(element);
					// check if the element is NOT a number from 0 to 255
					if ((elementAsInt < 0) || (255 < elementAsInt)) {
						// IP-Address is invalid
						result = false;
					}
				} catch(NumberFormatException exception) {
					// if there was a parsing error, the IP-Address is invalid
					result = false;
				}
			}
		}
		return result;
	}

}
