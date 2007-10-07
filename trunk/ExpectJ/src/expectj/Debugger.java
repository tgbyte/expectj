package expectj;

import java.io.*;

/**
 * This class is used for debugging, rather just to have a centalized 
 * gateway to standard output and few more things.
 *
 * @author	Sachin Shekar Shetty  
 */

public class Debugger {

	private final boolean DEBUG;
	private static boolean  STATICDEBUG = false;
	private final String CLASSNAME;
	private static PrintStream outStream; 	
	private static PrintWriter out = null;


    /**
     * Constructor 
     * 
     * @param fileName Name of the log file.
     * @param className Name of the class printing the debug
     */ 
    Debugger(String fileName, String className)  {

        System.out.println("Disributed Debugger is initailizing ;)  ....");
        STATICDEBUG = true;
        DEBUG = true;
		CLASSNAME = className;
		try {
			outStream = new PrintStream(new FileOutputStream(
                        fileName, true));
			out = new PrintWriter(outStream, true);
            out.println("");
            out.println("*****Logger initialized at " + new java.util.Date() 
                    + " *****");
            out.println("");
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
     * @param className Name of the class printing the debug
     * @param DEBUG boolean to switch on/off debugging.
     */
	Debugger(String className ,boolean DEBUG)  {

		this.DEBUG = DEBUG;
		CLASSNAME = className;

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
            out.println("<message class='" + CLASSNAME + "' time-stamp='"
                    + new java.util.Date() + "'>");
            out.println("<exception>" + exp + "</exception>");
            out.println("<stack-trace>");
            exp.printStackTrace(outStream);
            out.println("</stack-trace>");
            out.println("</message>");
            outStream.flush();

        }

	}

    /**
     * This method writes the string <code>msg</code> in to the log file
     * is logging is enabled.
     * @param msg The message to write to the log file.
     */
	public void print(String msg) {
        
        if (DEBUG && STATICDEBUG) {
            out.println("<message class='" + CLASSNAME + "' time-stamp='"
                    + new java.util.Date() + "'>");
            out.println(msg); 
            out.println("</message>");
            out.flush();
        }

	}



    /**
     * This method can be used for making stoping the execution witing
     * for a line feed.
     */
	void waitHere() {

		try {
			System.in.read();
		}
		catch (IOException  ie) {
			System.err.println("DEBUGGER:Failed while waiting for input " + ie);
		}

	}



}



