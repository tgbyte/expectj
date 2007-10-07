package expectj;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Helper class that wraps Spawnables to make them crunchier for ExpectJ to run.
 * @author Johan Walles
 */
class SpawnableHelper
implements TimerEventListener 
{
    private Spawnable spawnable;
    
    /**
     * @param timeOutSeconds time interval in seconds to be allowed for spawn execution
     * @param runMe the spawnable to execute
     */
    SpawnableHelper(Spawnable runMe, long timeOutSeconds) {
        if (timeOutSeconds < -1) {
            throw new IllegalArgumentException("Time-out is invalid");
        }
        if (timeOutSeconds != -1) {
            tm = new Timer(timeOutSeconds, this);
        }
        this.spawnable = runMe;
    }
    
    /**
     * @param runMe the spawnable to execute
     */
    SpawnableHelper(Spawnable runMe) {
        this(runMe, -1);
    }
    
    // Timer object to monitor our Spawnable
    private Timer tm = null;

    // Piped Streams to copy the output to standard streams
    private PipedInputStream readSystemOut = null;
    private PipedInputStream readSystemErr = null;

    private PipedOutputStream writeSystemOut = null;
    private PipedOutputStream writeSystemErr = null;

    // StreamPiper objects to pipe the output of one stream to other
    private StreamPiper spawnOutToSystemOut = null;
    private StreamPiper spawnErrToSystemErr = null;
    
    /**
     * Time callback method
     * This method is invoked when the time-out occurr
     */
    public void timerTimedOut() {
        stop();
    }

    /**
     * This method stops the spawn.
     */
    void stop() {
        spawnable.stop();
    }

    /**
     * Timer callback method
     * This method is invoked by the Timer, when the timer thread
     * receives an interrupted exception.
     * @param reason The reason we were interrupted.
     */
    public void timerInterrupted(InterruptedException reason) {
        // Print the stack trace and ignore the problem, this will make us never time
        // out.  Too bad.  /JW-2006apr10
        reason.printStackTrace();
    }

    /**
     * This method is used to Stop all the piper object from copying the
     * content to standard out. This is used after interact command.
     */
    synchronized void stopPipingToStandardOut() {
        spawnOutToSystemOut.stopPipingToStandardOut();
        if (spawnErrToSystemErr != null) {
            spawnErrToSystemErr.stopPipingToStandardOut();
        }
    }

    synchronized void startPipingToStandardOut() {
        spawnOutToSystemOut.startPipingToStandardOut();          
        if (spawnErrToSystemErr != null) {
            spawnErrToSystemErr.startPipingToStandardOut();
        }
    }

    /**
     * This method launches our Spawnable within the specified time
     * limit.  It tells the spawnable to start, and starts the timer when
     * enabled. It starts the piped streams to enable copying of spawn
     * stream contents to standard streams.
     * @throws Exception if launching the spawnable fails
     */ 
    void start() throws Exception {
        // Start the spawnable and timer if needed
        spawnable.start();
        if (tm != null)
            tm.startTimer();
        
        // Starting the piped streams and StreamPiper objects
        readSystemOut = new PipedInputStream();
        writeSystemOut = new PipedOutputStream(readSystemOut);
        spawnOutToSystemOut = new StreamPiper(System.out, 
                                              spawnable.getInputStream(), writeSystemOut);
        spawnOutToSystemOut.start();
        
        if (spawnable.getErrorStream() != null) {
            readSystemErr = new PipedInputStream();
            writeSystemErr = new PipedOutputStream(readSystemErr);
            spawnErrToSystemErr = new StreamPiper(System.err, 
                                                  spawnable.getErrorStream(),
                                                  writeSystemErr);
            spawnErrToSystemErr.start();
        }
    }

    /**
     * @return the input stream of the spawn.
     */
    InputStream getInputStream() {
        return readSystemOut;
    }

    /**
     * @return the output stream of the spawn.
     */
    OutputStream getOutputStream() {
        return spawnable.getOutputStream();
    }

    /**
     * @return the error stream of the spawn.
     */
    InputStream getErrorStream() {
        return readSystemErr;
    }

    /**
     * @return true if the spawn has exited.
     */
    boolean isClosed() {
        return spawnable.isClosed();
    }

    /**
     * If the spawn representes by this object has already exited, it
     * returns the exit code. isClosed() should be used in conjunction
     * with this method.
     * @return The exit code from the exited spawn.
     * @throws ExpectJException If the spawn is still running.
     */
    int getExitValue() 
    throws ExpectJException
    {
        if (!isClosed()) {
            throw new ExpectJException("Spawn is still running");
        }
        return spawnable.getExitValue();
    }


    /**
     * @return the available contents of Standard Out
     */
    String getCurrentStandardOutContents() {
        return spawnOutToSystemOut.getCurrentContents();
    }

    /**
     * @return the available contents of Standard Err, or null if stderr is not available
     */
    String getCurrentStandardErrContents() {
        if (spawnErrToSystemErr == null) {
            return null;
        }
        return spawnErrToSystemErr.getCurrentContents();
    }

}
