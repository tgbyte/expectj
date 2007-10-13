package expectj.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;


/**
 * Write some stuff to a stream, with 500ms pauses between each write.
 * <p>
 * For testing ExpectJ.
 * @author johan.walles@gmail.com
 */
public class StagedStringProducer
{
    /**
     * The user will read data from here.
     */
    private PipedInputStream inputStream;
    
    /**
     * Thread that writes data to the pipe in stages.
     * @author johan.walles@gmail.com
     */
    private class ProducerThread
    extends Thread {
        /**
         * Write these strings to the output stream.
         */
        private String writeUs[];
        
        /**
         * Write strings to here.
         */
        OutputStream destination;
        
        /**
         * @param destination Where to write data to.
         * @param stringsToWrite The data to write.
         */
        public ProducerThread(OutputStream destination, String ... stringsToWrite) {
            this.destination = destination;
            this.writeUs = stringsToWrite;
        }
        
        @Override
        public void run() {
            PrintStream output = new PrintStream(this.destination);
            boolean justStarting = true;
            for (String writeMe: writeUs) {
                if (!justStarting) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // This exception intentionally ignored
                    }
                    if (writeMe != null) {
                        output.append(writeMe);
                    }
                }
            }
            output.close();
        }
    }
    
    /**
     * Construct a staged string producer.
     * <p>
     * Strings will be produced with a 500ms delay between each.  There will be no delay
     * before the first or after the last one.  A null entry means "don't create any
     * string here".
     * <p>
     * Strings can be read from the {@link #getInputStream()} stream.
     * 
     * @param stringsToProduce The strings to produce.
     * @exception IOException If an IO error occurs.
     */
    public StagedStringProducer(String ... stringsToProduce)
    throws IOException
    {
        this.inputStream = new PipedInputStream();
        OutputStream outputStream = new PipedOutputStream(this.inputStream);
        new ProducerThread(outputStream, stringsToProduce).start();
    }
    
    /**
     * @return A reference to the stream on which we'll produce data.
     */
    public InputStream getInputStream() {
        return this.inputStream;
    }
}
