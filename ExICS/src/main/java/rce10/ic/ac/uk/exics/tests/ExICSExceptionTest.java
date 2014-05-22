package rce10.ic.ac.uk.exics.tests;

import android.test.InstrumentationTestCase;

import rce10.ic.ac.uk.exics.Model.ExICSException;

/**
 * Created by Rich on 22/05/2014.
 */
public class ExICSExceptionTest extends InstrumentationTestCase {
    public void testException() {
        try {
            throw new ExICSException("Exception Test");
        } catch (ExICSException e) {
            assertNotNull(e);
        }
    }
}
