package expectj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is responsible for piping the output of one stream to the
 * other. Optionally it also copies the content to standard out or
 * standard err
 *
 * @author	Sachin Shekar Shetty
 */

class StreamPiper extends Thread implements Runnable {
    /**
     * Log messages go here.
     */
    private final static Log LOG = LogFactory.getLog(StreamPiper.class);

    /**
     * Read data from here.
     */
    private InputStream pi = null;

    /**
     * Write data to here.
     */
    private OutputStream po = null;

    /**
     * Optionally send a copy of all piped data to here.
     */
    private PrintStream copyStream = null;

    /**
     * When true we drop data from {@link #pi} rather than passing it to {@link #po}.
     */
    volatile boolean stopPiping = false;

    /**
     * When this turns false, we shut down.  All accesses to this variable should be
     * synchronized.
     */
    private boolean continueProcessing = true;

    /**
     * String Buffer to hold the contents of output and err.
     */
    private volatile StringBuffer sCurrentOut = new StringBuffer();

    /**
     * @param copyStream Stream to copy the contents to before piping
     * the data to another stream. When this parameter is null, it does
     * not copy the contents
     * @param pi Input stream to read the data
     * @param po Output stream to write the data
     */
    StreamPiper(PrintStream copyStream, InputStream pi, OutputStream po) {
        if (pi == null) {
            throw new NullPointerException("Input stream must not be null");
        }
        this.pi = pi;
        this.po = po;
        this.copyStream = copyStream;
        // So that JVM does not wait for these threads
        this.setDaemon(true);
    }

    /**
     * This method is used to stop copying on to Standard out and err.
     * This is used after interact.
     */
    public synchronized void stopPipingToStandardOut() {
        stopPiping = true;
    }

    synchronized void startPipingToStandardOut() {
        stopPiping = false;
    }

    /**
     * This is used to stop the thread, after the process is killed
     */
    public synchronized void stopProcessing() {
        setContinueProcessing(false);
    }

    /**
     * @return the entire available contents read from the stream
     */
    synchronized String getCurrentContents() {
        return sCurrentOut.toString();
    }

    /**
     * Thread method that reads from the stream and writes to the other.
     */
    public void run() {
        byte[] buffer = new byte[512];
        int bytes_read;

        try {
            while(shouldContinueProcessing()) {
                bytes_read = pi.read(buffer);
                if (bytes_read == -1) {
                    LOG.debug("Stream ended, closing");
                    pi.close();
                    po.close();
                    return;
                }
                po.write(buffer, 0, bytes_read);
                sCurrentOut.append(new String(buffer, 0, bytes_read));
                if (copyStream != null && !stopPiping) {
                    copyStream.write(buffer, 0, bytes_read);
                    copyStream.flush();
                }
                po.flush();
            }
        } catch (IOException e) {
            if (shouldContinueProcessing()) {
                LOG.error("Trouble while pushing data between streams", e);
            }
        }
    }

    /**
     * Set to false to terminate data shuffling.
     * @param continueProcessing Whether or not to continue shuffling data.
     */
    private synchronized void setContinueProcessing(boolean continueProcessing)
    {
        this.continueProcessing = continueProcessing;
    }

    /**
     * Are we still supposed to keep shuffling data?
     * @return True if we should continue.  False otherwise.
     */
    private synchronized boolean shouldContinueProcessing()
    {
        return continueProcessing;
    }

}
