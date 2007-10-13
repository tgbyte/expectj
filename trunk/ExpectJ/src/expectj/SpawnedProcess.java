package expectj;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Date;
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

    /**
     * Buffered wrapper stream for slave's stdin.
     */
    private BufferedWriter out = null;

    // Debugger object
    Debugger debug = new Debugger("SpawnedProcess", true);

    /**
     * This is what we're actually talking to.
     */
    private SpawnableHelper slave = null;

    private volatile boolean continueReading = true;

    // Piper objects to pipe the process streams to standard streams
    private StreamPiper interactIn = null;
    private StreamPiper interactOut = null;
    private StreamPiper interactErr = null;
    
    /**
     * Wait for data from spawn's stdout.
     */
    private Selector stdoutSelector;
    
    /**
     * Wait for data from spawn's stderr.
     */
    private Selector stderrSelector;
    
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
        
        slave = new SpawnableHelper(spawn, lDefaultTimeOutSeconds);
        slave.start();
        debug.print("Spawned Process: " + spawn);               
        
        if (slave.getOutputStream() != null) {
            out =
                new BufferedWriter(new OutputStreamWriter(slave.getOutputStream()));
        }
        
        stdoutSelector = Selector.open();
        slave.getSourceChannel().register(stdoutSelector, SelectionKey.OP_READ);
        if (slave.getErrorSourceChannel() != null) {
            stderrSelector = Selector.open();
            slave.getErrorSourceChannel().register(stderrSelector, SelectionKey.OP_READ);
        }
    }

    /**
     * Timer callback method
     * This method is invoked when the time-out occur
     */
    public synchronized void timerTimedOut() {
        continueReading = false;
        stdoutSelector.wakeup();
        if (stderrSelector != null) {
            stderrSelector.wakeup();
        }
    }

    /**
     * Timer callback method
     * This method is invoked by the Timer, when the timer thread
     * receives an interrupted exception
     * @param reason Why we were interrupted
     */
    public void timerInterrupted(InterruptedException reason) {
        continueReading = false;
        stdoutSelector.wakeup();
        if (stderrSelector != null) {
            stderrSelector.wakeup();
        }
    } 

    /**
     * @return true if the last expect() or expectErr() method 
     * returned because of a time out rather then a match against 
     * the output of the process. 
     */
    public boolean isLastExpectTimeOut() {
        return !continueReading;
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
        expect(pattern, lTimeOutSeconds, stdoutSelector);
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
            if(slave.isClosed()) {
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
     * @param selector A selector covering only the channel we should read from
     * @throws IOException on IO trouble waiting for pattern
     * @throws TimeoutException on timeout waiting for pattern
     */
    private void expect(String pattern, long lTimeOutSeconds, Selector selector)
    throws IOException, TimeoutException
    {
        if (lTimeOutSeconds < -1) {
            throw new IllegalArgumentException("Timeout must be >= -1, was "
                                               + lTimeOutSeconds);
        }
        
        if (selector.keys().size() != 1) {
            throw new IllegalArgumentException("Selector key set size must be 1, was "
                                               + selector.keys().size());
        }
        // If this cast fails somebody gave us the wrong selector.
        Pipe.SourceChannel readMe =
            (Pipe.SourceChannel)selector.keys().iterator().next().channel();
        
        debug.print("SpawnedProcess.expect(" + pattern + ")");               
        continueReading = true;
        boolean found = false;
        StringBuilder line = new StringBuilder();
        Date runUntil = null;
        if (lTimeOutSeconds > 0) {
            runUntil = new Date(new Date().getTime() + lTimeOutSeconds * 1000);
        }
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while(continueReading) {
            if (runUntil == null) {
                selector.select();
            } else {
                long msLeft = runUntil.getTime() - new Date().getTime();
                if (msLeft > 0) {
                    selector.select(msLeft);
                } else {
                    continueReading = false;
                    break;
                }
            }
            if (selector.selectedKeys().size() == 0) {
                // Woke up with nothing selected, try again
                continue;
            }
            
            buffer.rewind();
            if (readMe.read(buffer) == -1) {
                // End of stream
                break;
            }
            buffer.rewind();
            for (int i = 0; i < buffer.limit(); i++) {
                line.append((char)buffer.get(i));
            }
            if (line.toString().trim().toUpperCase().indexOf(pattern.toUpperCase()) != -1) {
                debug.print("Matched for " + pattern + ":" 
                            + line);
                found = true;
                break;
            }
            while (line.indexOf("\n") != -1) {
                line.delete(0, line.indexOf("\n") + 1);
            }
        }
        debug.print("expect Over");
        debug.print("Found: " + found);
        debug.print("Continue Reading:" + continueReading );
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
        expect(pattern, lTimeOutSeconds, stderrSelector);
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
        return slave.isClosed();
    }

    /**
     * @return the exit code of the process if the process has 
     * already exited.
     * @throws ExpectJException if the spawn is still running.
     */
    public int getExitValue()
    throws ExpectJException
    {
        return slave.getExitValue();
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
                                     System.in, slave.getOutputStream());
        interactIn.start();
        interactOut = new StreamPiper(null, 
                                      Channels.newInputStream(slave.getSourceChannel()),
                                      System.out);
        interactOut.start();
        interactErr = new StreamPiper(null, 
                                      Channels.newInputStream(slave.getErrorSourceChannel()),
                                      System.err);
        interactErr.start();
        slave.stopPipingToStandardOut();
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
        slave.stop();
    } 

    /**
     * @return the available contents of Standard Out
     */
    public String getCurrentStandardOutContents() {
        return slave.getCurrentStandardOutContents();
    }
    
    /**
     * @return the available contents of Standard Err
     */
    public String getCurrentStandardErrContents() {
        return slave.getCurrentStandardErrContents();
    }
}
