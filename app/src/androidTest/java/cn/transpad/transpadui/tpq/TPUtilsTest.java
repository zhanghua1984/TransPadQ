package cn.transpad.transpadui.tpq;

import android.test.InstrumentationTestCase;

import cn.transpad.transpadui.util.L;

/**
 * Created by user on 2015/9/17.
 */
public class TPUtilsTest extends InstrumentationTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testLog() {
        for (int i = 0; i < 10; i++) {
            L.v("TPUtilsTest", "testLog", "i=" + i);
            assertEquals(1, i);
        }

    }
}
