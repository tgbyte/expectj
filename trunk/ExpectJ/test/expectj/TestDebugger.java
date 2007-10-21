package expectj;

import junit.framework.TestCase;

/**
 * @author Johan Walles, johan.walles@gmail.com
 */
public class TestDebugger extends TestCase
{
    /**
     * Test class to name conversion.
     */
    public void testClassToName() {
        assertEquals("Object", Debugger.classToName(Object.class));
    }
}
