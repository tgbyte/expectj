package expectj;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.mockito.Mockito;

import expectj.test.StagedSpawnable;

import junit.framework.TestCase;

/**
 * Verify that the different expect() methods of {@link Spawn} work as expected.
 * @author johan.walles@gmail.com
 */
public class TestExpect extends TestCase
{
    /**
     * Generate a Spawn producing the indicated output on stdout.
     * @param strings The strings to print to stdout.  Strings will be produced with
     * 500ms between each.
     * @return A new Spawn.
     * @throws Exception when things go wrong.
     */
    private Spawn getSpawn(String strings[]) throws Exception {
        return new ExpectJ().spawn(new StagedSpawnable(strings));
    }

    /**
     * Test that we can find simple strings.
     * @throws Exception if things go wrong.
     */
    public void testExpectStrings() throws Exception {
        Spawn testMe = getSpawn(new String[] {"flaska", "gris"});
        testMe.expect("flaska");
        testMe.expect("gris");
    }

    /**
     * Test that we can find simple strings with an unexpected string in between.
     * @throws Exception if things go wrong.
     */
    public void testExpectStringsWithExtraData() throws Exception {
        Spawn testMe =
            getSpawn(new String[] {"flaska", "nyckel", "gris"});
        testMe.expect("flaska");
        testMe.expect("gris");
    }

    /**
     * Test that we get notified about closes.
     * @throws Exception if things go wrong.
     */
    public void testExpectClose() throws Exception {
        Spawn testMe = getSpawn(new String[] {"flaska", "gris"});
        testMe.expectClose();
    }

    /**
     * Test that we time out properly when we don't find what we're looking for.
     * @throws Exception if things go wrong.
     */
    public void testTimeout() throws Exception {
        // Test longer duration output than timeout
        Spawn testMe =
            getSpawn(new String[] {"flaska", "nyckel", "gris", "hink", "bil", "stork"});
        Date beforeTimeout = new Date();
        try {
            testMe.expect("klubba", 1);
            fail("expect() should have timed out");
        } catch (TimeoutException expected) {
            // Ignoring expected exception
        }
        Date afterTimeout = new Date();

        long msElapsed = afterTimeout.getTime() - beforeTimeout.getTime();
        if (msElapsed < 900 || msElapsed > 1100) {
            fail("expect() should have timed out after 1s, timed out in "
                 + msElapsed
                 + "ms");
        }

        testMe =
            getSpawn(new String[] {"flaska", "nyckel", "gris", "hink", "bil", "stork"});
        beforeTimeout = new Date();
        try {
            testMe.expectClose(1);
            fail("expectClose() should have timed out");
        } catch (TimeoutException expected) {
            // Ignoring expected exception
        }
        afterTimeout = new Date();

        msElapsed = afterTimeout.getTime() - beforeTimeout.getTime();
        if (msElapsed < 900 || msElapsed > 1100) {
            fail("expectClose() should have timed out after 1s, timed out in "
                 + msElapsed
                 + "ms");
        }
    }

    /**
     * Create a spawned process. This method hopefully works on Windows as well.
     * @return a Spawn representing a process
     * @throws Exception on trouble
     */
    private Spawn getSpawnedProcess() throws Exception {
        IOException throwMe = null;

        // Try a couple of different FTP binaries
        try {
            return new ExpectJ(5).spawn("/bin/ftp");
        } catch (IOException e) {
            // IOException probably means "binary not found"
            throwMe = e;
        }

        try {
            return new ExpectJ(5).spawn("ftp.exe");
        } catch (IOException e) {
            // This exception intentionally ignored
        }

        try {
            return new ExpectJ(5).spawn("/usr/bin/ftp");
        } catch (IOException e) {
            // This exception intentionally ignored
        }

        try {
            return new ExpectJ(5).spawn("/usr/bin/lftp");
        } catch (IOException e) {
            // This exception intentionally ignored
        }

        // Report problem
        throw throwMe;
    }

    /**
     * Verify that stopping a process spawn works as it should.
     * @throws Exception on trouble
     */
    public void testStopProcess() throws Exception {
        Spawn process = getSpawnedProcess();
        assertFalse(process.isClosed());

        // Process should be closed after it has been stopped
        process.stop();
        assertTrue("Process didn't close after calling stop()",
                   process.isClosed());

        // A closed process should return at once on expectClose()
        Date beforeExpectClose = new Date();
        process.expectClose();
        Date afterExpectClose = new Date();
        long dms = afterExpectClose.getTime() - beforeExpectClose.getTime();
        assertTrue(dms < 10);

        // Process should still be closed
        assertTrue(process.isClosed());
    }

    /**
     * Verify that waiting for a process spawn to finish works as it should..
     * @throws Exception on trouble
     */
    public void testFinishProcess() throws Exception {
        Spawn process = getSpawnedProcess();
        assertFalse(process.isClosed());

        // Process should be closed after it finishes
        process.send("quit\n");
        process.expectClose();
        process.stop();
        assertTrue("Process wasn't closed after finishing", process.isClosed());
    }

    /**
     * Spawn a ton of stuff in the hope that we'll get an exception if we leak
     * resources somewhere.
     * @throws Exception on trouble.
     */
    public void testSpawnLeaks1() throws Exception {
        Spawnable dummySpawn = (Spawnable)Mockito.mock(Spawnable.class);
        Mockito.when(Boolean.valueOf(dummySpawn.isClosed())).thenReturn(Boolean.TRUE);
        InputStream empty = new ByteArrayInputStream(new byte[0]);
        Mockito.when(dummySpawn.getStdout()).thenReturn(empty);
        for (int i = 0; i < 1024; i++) {
            try {
                new ExpectJ().spawn(dummySpawn).expectClose(1);
            } catch (Exception e) {
                throw new Exception("Leak test 1 failed after " + i + " iterations", e);
            }
        }
    }

    /**
     * Spawn a ton of stuff in the hope that we'll get an exception if we leak
     * resources somewhere.
     * @throws Exception on trouble.
     */
    public void testSpawnLeaks2() throws Exception {
        for (int i = 0; i < 1024; i++) {
            try {
                getSpawn(new String[0]).expectClose(1);
            } catch (Exception e) {
                throw new Exception("Leak test 2 failed after " + i + " iterations", e);
            }
        }
    }
}
