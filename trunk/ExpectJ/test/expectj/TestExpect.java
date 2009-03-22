package expectj;

import java.util.Date;
import java.util.concurrent.TimeoutException;

import expectj.test.StagedSpawnable;

import junit.framework.TestCase;

/**
 * Verify that the different expect() methods of {@link Spawn} work as expected.
 * @author johan.walles@gmail.com
 */
public class TestExpect extends TestCase
{
    /**
     * Generate a SpawnedProcess producing the indicated output on stdout.
     * @param strings The strings to print to stdout.  Strings will be produced with
     * 500ms between each.
     * @return A new SpawnedProcess.
     * @throws Exception when things go wrong.
     */
    private Spawn getSpawnedProcess(String strings[])
    throws Exception
    {
        return new ExpectJ().spawn(new StagedSpawnable(strings));
    }

    /**
     * Test that we can find simple strings.
     * @throws Exception if things go wrong.
     */
    public void testExpectStrings()
    throws Exception
    {
        Spawn testMe = getSpawnedProcess(new String[] {"flaska", "gris"});
        testMe.expect("flaska");
        testMe.expect("gris");
    }

    /**
     * Test that we can find simple strings with an unexpected string in between.
     * @throws Exception if things go wrong.
     */
    public void testExpectStringsWithExtraData()
    throws Exception
    {
        Spawn testMe =
            getSpawnedProcess(new String[] {"flaska", "nyckel", "gris"});
        testMe.expect("flaska");
        testMe.expect("gris");
    }

    /**
     * Test that we get notified about closes.
     * @throws Exception if things go wrong.
     */
    public void testExpectClose()
    throws Exception
    {
        Spawn testMe = getSpawnedProcess(new String[] {"flaska", "gris"});
        testMe.expectClose();
    }

    /**
     * Test that we time out properly when we don't find what we're looking for.
     * @throws Exception if things go wrong.
     */
    public void testTimeout()
    throws Exception
    {
        // Test longer duration output than timeout
        Spawn testMe =
            getSpawnedProcess(new String[] {"flaska", "nyckel", "gris", "hink", "bil", "stork"});
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
            getSpawnedProcess(new String[] {"flaska", "nyckel", "gris", "hink", "bil", "stork"});
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
}
