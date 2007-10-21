package expectj;

import java.io.*;

/**
 * This class is used for debugging, rather just to have a centralized 
 * gateway to standard output and few more things.
 *
 * @author	Sachin Shekar Shetty  
 */

public class Debugger {

    /**
     * True if debugging is enabled for this class.  False otherwise.
     */
	private final boolean DEBUG;
	
	/**
	 * True if debugging is enabled globally.  False otherwise.
	 */
	private static boolean  STATICDEBUG = false;
	
	/**
	 * The name of the class to print debug messages for.
	 */
	private final String CLASSNAME;
	
	/**
	 * Exception stack traces go here.
	 */
	private static PrintStream outStream; 	
	
	/**
	 * Log messages go here.
	 */
	private static PrintWriter outWriter = null;


	/**
	 * Take a class and return its name without package information.
	 * @param clazz The class to name.
	 * @return The name of the class, without package information.
	 */
	static String classToName(Class clazz) {
	    return clazz.getSimpleName();
	}
	
	/**
     * Constructor 
     * 
     * @param fileName Name of the log file.
     * @param sourceClass Name of the class printing the debug
     */ 
    Debugger(String fileName, Class sourceClass)  {

        System.out.println("Disributed Debugger is initailizing ;)  ....");
        STATICDEBUG = true;
        DEBUG = true;
		CLASSNAME = classToName(sourceClass);
		try {
			outStream = new PrintStream(new FileOutputStream(
                        fileName, true));
			outWriter = new PrintWriter(outStream, true);
            outWriter.println("");
            outWriter.println("*****Logger initialized at " + new java.util.Date() 
                    + " *****");
            outWriter.println("");
            System.out.println("Enterprise Log file located at: " + fileName);
		}
		catch (Exception exp) {
			System.err.println("Could not open log file:" + fileName);
			System.err.println("Exception: " + exp);
            System.err.println("No Messages will be logged");
            STATICDEBUG = false;
		}

    }

    /**
     * Constructor
     *
     * @param sourceClass The class printing the debug
     * @param DEBUG boolean to switch on/off debugging.
     */
	Debugger(Class sourceClass ,boolean DEBUG)  {

		this.DEBUG = DEBUG;
		CLASSNAME = classToName(sourceClass);

	}

   
    /**
     * This method writes the stacktrace for the exception
     * <code>exp</code> to the log file is logging is
     * enabled.
     * 
     * @param exp The exception to write to the log file.
     */
	public void writeException(Exception exp) {

        if (DEBUG && STATICDEBUG) {
            outWriter.println("<message class='" + CLASSNAME + "' time-stamp='"
                    + new java.util.Date() + "'>");
            outWriter.println("<exception>" + exp + "</exception>");
            outWriter.println("<stack-trace>");
            exp.printStackTrace(outStream);
            outWriter.println("</stack-trace>");
            outWriter.println("</message>");
            outStream.flush();

        }

	}

    /**
     * This method writes the string <code>msg</code> in to the log file
     * if logging is enabled.
     * @param msg The message to write to the log file.
     */
	public void print(String msg) {
        
        if (DEBUG && STATICDEBUG) {
            outWriter.println("<message class='" + CLASSNAME + "' time-stamp='"
                    + new java.util.Date() + "'>");
            outWriter.println(msg); 
            outWriter.println("</message>");
            outWriter.flush();
        }

	}



    /**
     * This method can be used for making stopping the execution writing
     * for a line feed.
     */
	void waitHere() {

		try {
			System.in.read();
		}
		catch (IOException ie) {
			System.err.println("DEBUGGER:Failed while waiting for input " + ie);
		}

	}



}



