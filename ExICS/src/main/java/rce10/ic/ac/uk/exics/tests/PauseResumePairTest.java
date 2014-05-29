package rce10.ic.ac.uk.exics.tests;

import android.test.InstrumentationTestCase;

import java.util.Calendar;
import java.util.GregorianCalendar;

import rce10.ic.ac.uk.exics.Model.PauseResumePair;
import rce10.ic.ac.uk.exics.Utilities.ISO8601DateParser;

/**
 * Created by Rich on 29/05/2014.
 */
public class PauseResumePairTest extends InstrumentationTestCase {
    private PauseResumePair setUpPair() {
        Calendar startCal = new GregorianCalendar();
        Calendar finCal = (Calendar) startCal.clone();
        finCal.add(Calendar.MINUTE, 10);

        ISO8601DateParser parser = new ISO8601DateParser();
        return new PauseResumePair(parser.toString(startCal), parser.toString(finCal));
    }

    public void testConstructor() throws Exception {
        Calendar startCal = new GregorianCalendar();
        Calendar finCal = (Calendar) startCal.clone();
        finCal.add(Calendar.MINUTE, 10);

        ISO8601DateParser parser = new ISO8601DateParser();
        PauseResumePair testPair = new PauseResumePair(parser.toString(startCal), parser.toString(finCal));

        assertNotNull(testPair);

        PauseResumePair testPair2 = new PauseResumePair(parser.toString(startCal), "null");
        assertNotNull(testPair2);
    }

    public void testGetPauseTime() throws Exception {
        Calendar startCal = new GregorianCalendar();
        Calendar finCal = (Calendar) startCal.clone();
        finCal.add(Calendar.MINUTE, 10);

        ISO8601DateParser parser = new ISO8601DateParser();
        PauseResumePair testPair = new PauseResumePair(parser.toString(startCal), parser.toString(finCal));
        assertEquals(startCal, testPair.getTimePaused());
    }
}
