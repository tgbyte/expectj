package expectj;

import java.io.IOException;

/**
 * Implementors can create processes.
 * <p>
 * Implementors are encouraged to implement {@link #toString()} for logging purposes.
 * 
 * @author Johan Walles, johan.walles@gmail.com
 */
public interface Executor {
    /**
     * Create a new process.  Will only be called once.
     * @return The new process.
     * @throws IOException if there's a problem starting the new process.
     * @see #toString()
     */
    Process execute()
    throws IOException;
    
    /**
     * @return A short description of what {@link #execute()} returns.
     */
    public String toString();
}
