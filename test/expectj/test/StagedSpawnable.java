package expectj.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import expectj.ExpectJException;
import expectj.Spawnable;

/**
 * A spawnable implementation using a {@link StagedStringProducer} to produce data on
 * its stdout stream.
 *
 * @author johan.walles@gmail.com
 */
public class StagedSpawnable implements Spawnable
{
    /**
     * We use this to produce strings.
     */
    private StagedStringProducer stringProducer;

    /**
     * Construct a staged string spawn.  The spawn will produce the requested strings
     * on its {@link #getStdout()} stream.
     * <p>
     * Strings will be produced with a 500ms delay between each.  There will be no delay
     * before the first or after the last one.  A null entry means "don't create any
     * string here".
     *
     * @see #StagedSpawnable(String...)
     * @param stringsToProduce The strings to produce.
     * @throws IOException on trouble creating the string spawn.
     */
    public StagedSpawnable(String stringsToProduce[])
    throws IOException
    {
        this.stringProducer = new StagedStringProducer(stringsToProduce);
    }

    public void stop() {
        // This method intentionally left blank
    }

    public void start() {
        // This method intentionally left blank
    }

    public boolean isClosed() {
        return stringProducer.done();
    }

    public OutputStream getStdin() {
        return null;
    }

    public InputStream getStdout() {
        return this.stringProducer.getStringStream();
    }

    public int getExitValue() throws ExpectJException {
        return 0;
    }

    public InputStream getStderr() {
        return null;
    }
}
