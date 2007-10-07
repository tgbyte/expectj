package expectj;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.TimeoutException;

/**
 * This class represents a spawned process. This will also interact with
 * the process to read and write to it.
 *
 * @author	Sachin Shekar Shetty  
 */
public class SpawnedProcess implements TimerEventListener {

    /** Default time out for expect commands */
    private long m_lDefaultTimeOutSeconds = -1;

    private BufferedWriter out = null;

    // Debugger object
    Debugger debug = new Debugger("SpawnedProcess", true);

    private SpawnableHelper spawnHelper = null;

    private volatile boolean continueReading = true;

    // Piper objects to pipe the process streams to standard streams
    private StreamPiper interactIn = null;
    private StreamPiper interactOut = null;
    private StreamPiper interactErr = null;
    
    /**
     * Constructor
     *
     * @param spawn This is what we'll control.
     * @param lDefaultTimeOutSeconds Default timeout for expect commands
     * @throws Exception on trouble launching the spawn
     */
    SpawnedProcess(Spawnable spawn, long lDefaultTimeOutSeconds)
    throws Exception
    {
        if (lDefaultTimeOutSeconds < -1) {
            throw new IllegalArgumentException("Timeout must be >= -1, was "
                                               + lDefaultTimeOutSeconds);
        }
        m_lDefaultTimeOutSeconds = lDefaultTimeOutSeconds;
        
        this.spawnHelper = new SpawnableHelper(spawn, lDefaultTimeOutSeconds);
        this.spawnHelper.start();
        debug.print("Spawned Process: " + spawn);               
        
        out = new BufferedWriter(new OutputStreamWriter(spawnHelper.getOutputStream()));
    }

    /**
     * Timer callback method
     * This method is invoked when the time-out occur
     */
    public synchronized void timerTimedOut() {

        continueReading = false;

    }

    /**
     * Timer callback method
     * This method is invoked by the Timer, when the timer thread
     * receives an interrupted exception
     * @param reason Why we were interrupted
     */
    public void timerInterrupted(InterruptedException reason) {

        continueReading = false;

    } 

    /**
     * @return true if the last expect() or expectErr() method 
     * returned because of a time out rather then a match against 
     * the output of the process. 
     */
    public boolean isLastExpectTimeOut() {

        return (!continueReading);

    }
    /**
     * This method functions exactly like the Unix expect command. 
     * It waits until a string is read from the standard output stream 
     * of the spawned process that matches the string pattern. 
     * SpawnedProcess does a cases insensitive substring match for pattern 
     * against the output of the spawned process. 
     * lDefaultTimeOut is the timeout in seconds that the expect command 
     * should wait for the pattern to match. This function returns 
     * when a match is found or after lTimOut seconds. 
     * You can use the SpawnedProcess.isLastExpectTimeOut() to identify 
     * the return path of the method. A timeout of -1 will make the expect 
     * method wait indefinitely until the supplied pattern matches 
     * with the Standard Out. 
     * 
     * @param pattern The case-insensitive substring to match against.
     * @param lTimeOutSeconds The timeout in seconds before the match fails.
     * @throws ExpectJException when some error occurs.
     * @throws IOException on IO trouble waiting for pattern
     * @throws TimeoutException on timeout waiting for pattern
     */
    public void expect(String pattern, long lTimeOutSeconds)
    throws ExpectJException, IOException, TimeoutException
    {
        expect(pattern, lTimeOutSeconds, spawnHelper.getInputStream());
    }
    
    /**
     * Wait for the spawned process to finish.
     * @param lTimeOutSeconds The number of seconds to wait before giving up, or
     * -1 to wait forever.
     * @throws ExpectJException
     * @throws TimeoutException 
     * @see #expectClose()
     */
    public void expectClose(long lTimeOutSeconds)
    throws ExpectJException, TimeoutException
    {
        if (lTimeOutSeconds < -1) {
            throw new IllegalArgumentException("Timeout must be >= -1, was "
                                               + lTimeOutSeconds);
        }

        debug.print("SpawnedProcess.expectClose()");
        Timer tm = null;
        if (lTimeOutSeconds != -1 ) {
            tm = new Timer(lTimeOutSeconds, this);
            tm.startTimer();
        }
        continueReading = true;
        boolean closed = false;
        while(continueReading) {
            // Sleep if process is still running
            if(spawnHelper.isClosed()) {
                closed = true;
                break;
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new ExpectJException("Interrupted waiting for spawn to finish",
                                               e);
                }
            }
        }
        debug.print("expect Over");
        debug.print("Found: " + closed);
        debug.print("Continue Reading:" + continueReading );
        if (tm != null) {
            debug.print("Timer Status:" + tm.getStatus());
        }
        if (!continueReading) {
            throw new TimeoutException("Timeout waiting for spawn to finish");
        }
    }
    
    /**
     * Wait the default timeout for the spawned process to finish.
     * @throws ExpectJException If something fails.
     * @throws TimeoutException 
     * @see #expectClose(long)
     */
    public void expectClose() 
    throws ExpectJException, TimeoutException
    {
        expectClose(m_lDefaultTimeOutSeconds);
    }
    
    /**
     * Workhorse of the expect() and expectErr() methods.
     * @see #expect(String, long)
     * @param pattern What to look for
     * @param lTimeOutSeconds How long to look before giving up
     * @param readMe Where to look
     * @throws IOException on IO trouble waiting for pattern
     * @throws TimeoutException on timeout waiting for pattern
     */
    private void expect(String pattern, long lTimeOutSeconds, InputStream readMe)
    throws ExpectJException, IOException, TimeoutException
    {
        if (lTimeOutSeconds < -1) {
            throw new IllegalArgumentException("Timeout must be >= -1, was "
                                               + lTimeOutSeconds);
        }
        
        debug.print("SpawnedProcess.expect(" + pattern + ")");               
        Timer tm = null;
        if (lTimeOutSeconds != -1 ) {
            tm = new Timer(lTimeOutSeconds, this);
            tm.startTimer();
        }
        continueReading = true;
        boolean found = false;
        int i = 0;
        StringBuffer line = new StringBuffer();
        here: while(continueReading) {
            // Sleeping if bytes are not available rather then
            // blocking
            while (readMe.available() == 0 ) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new ExpectJException("Interrupted waiting for pattern", e);
                }
                // Go back and check the condition
                continue here;
            }
            i = readMe.read();
            if (i== -1)
                break;
            char ch = (char)i;
            line.append(ch);
            if (line.toString().trim().toUpperCase().indexOf(pattern.toUpperCase()) != -1) {
                debug.print("Matched for " + pattern + ":" 
                            + line);
                found = true;
                break;
            }
            if (i == '\n') {
                debug.print("Line read: " + line);               
                line.setLength(0);
            }
        }
        debug.print("expect Over");
        debug.print("Found: " + found);
        debug.print("Continue Reading:" + continueReading );
        if (tm != null) {
            debug.print("Timer Status:" + tm.getStatus());
        }
        if (!continueReading) {
            throw new TimeoutException("Timeout trying to match \"" + pattern + "\"");
        }
    }

    /**
     * This method functions exactly like the corresponding expect 
     * function except for it tries to match the pattern with the 
     * output  of standard error stream of the spawned process.
     * @see #expect(String, long)
     * @param pattern The case-insensitive substring to match against.
     * @param lTimeOutSeconds The timeout in seconds before the match fails.
     * @throws ExpectJException when some error occurs.
     * @throws TimeoutException on timeout waiting for pattern
     * @throws IOException on IO trouble waiting for pattern
     */
    public void expectErr(String pattern, long lTimeOutSeconds)  
    throws ExpectJException, IOException, TimeoutException
    {
        expect(pattern, lTimeOutSeconds, spawnHelper.getErrorStream());
    }

    /**
     * This method functions exactly like expect described above, 
     * but uses the default timeout specified in the ExpectJ constructor. 
     * @param pattern The case-insensitive substring to match against.
     * @throws ExpectJException when some error occurs.
     * @throws TimeoutException on timeout waiting for pattern
     * @throws IOException on IO trouble waiting for pattern
     */
    public void expect(String pattern)
    throws IOException, TimeoutException, ExpectJException
    {
        expect(pattern, m_lDefaultTimeOutSeconds);
    }

    /**  
     * This method functions exactly like the corresponding expect 
     * function except for it tries to match the pattern with the output  
     * of standard error stream of the spawned process.
     * @param pattern The case-insensitive substring to match against.
     * @throws ExpectJException when some error occurs.
     * @throws TimeoutException on timeout waiting for pattern
     * @throws IOException on IO trouble waiting for pattern
     */
    public void expectErr(String pattern)
    throws ExpectJException, IOException, TimeoutException
    {
        expectErr(pattern, m_lDefaultTimeOutSeconds);
    }

    /**
     * This method should be use use to check the process status 
     * before invoking send()
     * @return true if the process has already exited. 
     */
    public boolean isClosed() {
        return spawnHelper.isClosed();
    }

    /**
     * @return the exit code of the process if the process has 
     * already exited.
     * @throws ExpectJException if the spawn is still running.
     */
    public int getExitValue()
    throws ExpectJException
    {
        return spawnHelper.getExitValue();
    }

    /**
     * This method writes the string line to the standard input of the spawned process.
     * @param string The string to send.  Don't forget to terminate it with \n if you
     * want it linefed.
     * @throws IOException on IO trouble talking to spawn
     */
    public void send(String string)
    throws IOException {
        debug.print("Sending " + string);
        out.write(string);
        out.flush();
    }

    /** 
     * This method functions like exactly the Unix interact command. 
     * It allows the user to interact with the spawned process.
     * Known Issues: User input is echoed twice on the screen, need to 
     * fix this ;)
     *
     */
    public void interact() {
        interactIn = new StreamPiper(null, 
                                     System.in, spawnHelper.getOutputStream());
        interactIn.start();
        interactOut = new StreamPiper(null, 
                                      spawnHelper.getInputStream(), System.out);
        interactOut.start();
        interactErr = new StreamPiper(null, 
                                      spawnHelper.getErrorStream(), System.err);
        interactErr.start();
        spawnHelper.stopPipingToStandardOut();
    }

    /**
     * This method kills the process represented by SpawnedProcess object.
     */ 
    public void stop() {

        if (interactIn != null)
            interactIn.stopProcessing();
        if (interactOut != null)
            interactOut.stopProcessing();
        if (interactErr != null)
            interactErr.stopProcessing();
        spawnHelper.stop();
    } 

    /**
     * @return the available contents of Standard Out
     */
    public String getCurrentStandardOutContents() {
        return spawnHelper.getCurrentStandardOutContents();
    }
    
    /**
     * @return the available contents of Standard Err
     */
    public String getCurrentStandardErrContents() {
        return spawnHelper.getCurrentStandardErrContents();
    }
}
