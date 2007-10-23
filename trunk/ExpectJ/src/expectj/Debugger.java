package expectj;

import java.io.*;
import java.util.Date;

/**
 * This class is used for debugging, rather just to have a centralized 
 * gateway to standard output and few more things.
 *
 * @author	Sachin Shekar Shetty  
 */
class Debugger {
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
	 * Log messages go here.
	 */
	private static PrintWriter output = null;


	/**
	 * Take a class and return its name without package information.
	 * @param clazz The class to name.
	 * @return The name of the class, without package information.
	 */
	static String classToName(Class clazz) {
	    return clazz.getSimpleName();
	}
	
	/**
     * Globally enable logging.
     * 
     * @param logDestination Where log messages will be written to
     */ 
    static void initialize(PrintWriter logDestination)  {

        System.out.println("Disributed Debugger is initailizing ;)  ....");
        STATICDEBUG = true;

		Debugger.output = logDestination;
		logDestination.println("");
		logDestination.println("*****Logger initialized at " + new Date() + " *****");
		logDestination.println("");
    }

    /**
     * Constructor
     *
     * @param sourceClass The class printing the debug
     * @param DEBUG boolean to switch on/off debugging.
     */
	Debugger(Class sourceClass, boolean DEBUG)  {

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
            output.println("<message class='" + CLASSNAME + "' time-stamp='"
                    + new java.util.Date() + "'>");
            output.println("<exception>" + exp + "</exception>");
            output.println("<stack-trace>");
            exp.printStackTrace(output);
            output.println("</stack-trace>");
            output.println("</message>");
            output.flush();
        }

	}

    /**
     * This method writes the string <code>msg</code> in to the log file
     * if logging is enabled.
     * @param msg The message to write to the log file.
     */
	public void print(String msg) {
        
        if (DEBUG && STATICDEBUG) {
            output.println("<message class='" + CLASSNAME + "' time-stamp='"
                    + new java.util.Date() + "'>");
            output.println(msg); 
            output.println("</message>");
            output.flush();
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



