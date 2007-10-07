/*
 * @(#) JExpectException.java 1.0 
 */

package expectj;

/**
 * This class extends the Exception class and encapsulates other exceptions.
 * 
 * @author	Sachin Shekar Shetty  
 */
public class ExpectJException extends Exception {
    ExpectJException(String message) {
        super(message);
    }
    
    ExpectJException(String message, Throwable cause) {
        super(message, cause);
    }
}
