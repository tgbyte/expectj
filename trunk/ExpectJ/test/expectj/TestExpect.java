package expectj;

import junit.framework.TestCase;
import expectj.test.StagedSpawnable;

/**
 * Verify that the different expect() methods of {@link SpawnedProcess} work as expected.
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
    private SpawnedProcess getSpawnedProcess(final String ... strings)
    throws Exception
    {
        return new ExpectJ("/dev/null", -1).spawn(new StagedSpawnable(strings));
    }
    
    /**
     * Test that we can find a simple string.
     * @throws Exception if things go wrong.
     */
    public void testExpectString()
    throws Exception
    {
        SpawnedProcess testMe = getSpawnedProcess("flaska");
        testMe.expect("flaska");
    }
}
