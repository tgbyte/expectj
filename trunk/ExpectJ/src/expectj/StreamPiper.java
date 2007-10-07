package expectj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This class is responsible for piping the output of one stream to the
 * other. Optionally it also copies the content to standard out or
 * standard err
 *
 * @author	Sachin Shekar Shetty  
 */

class StreamPiper extends Thread implements Runnable {

    // Streams to read and write
    private InputStream pi = null;
    private OutputStream po = null;    
    // Stream to copy the content
    private PrintStream copyStream = null;
   
    // When true, StreamPiper does not copy the contents to Standard Out
    volatile boolean stopPiping = false;

    // To indicate stops processing the streams
    volatile boolean continueProcessing = true;

    // String Buffer to hold the contents of output and err
    private volatile StringBuffer sCurrentOut = new StringBuffer();

    // Debugger
    private Debugger debug = new Debugger("StreamPiper", true);
    
    /**
     * Constructor
     *
     * @param copyStream Stream to copy the contents to before piping
     * the data to another stream. When this parameter is null, it does
     * not copy the contents
     * @param pi Input stream to read the data
     * @param po Output stream to write the data
     * 
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

        continueProcessing = false;          

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
            while(continueProcessing) {
                bytes_read = pi.read(buffer);
                //bytes_read = pi.read();
                if (bytes_read == -1) { 
                    debug.print("Closing Streams");
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
            e.printStackTrace();
        }
    }

}
