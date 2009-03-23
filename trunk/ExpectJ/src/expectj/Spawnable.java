package expectj;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementors of this interface can be spawned by {@link expectj.ExpectJ}.
 *
 * @author Johan Walles
 */
public interface Spawnable
{
    /**
     * This method launches the spawn.  It starts the piped streams to enable copying of
     * process stream contents to standard streams.
     * @throws Exception on trouble.
     */
    void start() throws Exception;

    /**
     * Get a stream from which the {@link Spawn}'s stdout can be read.
     * @return A stream that represents stdout of a spawned process.
     * @see Process#getInputStream()
     */
    InputStream getStdout();

    /**
     * Get a stream through which the {@link Spawn}'s stdin can be written to.
     * @return A stream that represents stdin of a spawned process.
     * @see Process#getOutputStream()
     */
    OutputStream getStdin();

    /**
     * Get a stream from which the {@link Spawn}'s stderr can be read.
     * @return A stream that represents stderr of a spawned process, or null if there is
     * no stderr.
     * @see Process#getErrorStream()
     */
    InputStream getStderr();

    /**
     * Find out whether the {@link Spawn} has finished.
     * @return true if a spawned process has finished.
     */
    boolean isClosed();

    /**
     * If the {@link Spawn} has exited, its exit code is returned.
     * @return The exit code of the finished spawn.
     * @throws ExpectJException if the spawn is still running.
     * @see #isClosed()
     * @see System#exit(int)
     */
    int getExitValue() throws ExpectJException;

    /**
     * Stops a running {@link Spawn}.  After this method returns,
     * {@link #isClosed()} must return true.
     */
    public void stop();
}
