package rce10.ic.ac.uk.exics.tests;

import android.test.InstrumentationTestCase;

import java.util.Calendar;

import rce10.ic.ac.uk.exics.Utilities.ISO8601DateParser;

/**
 * Created by Rich on 21/05/2014.
 */
public class ISO8601DateParserTest extends InstrumentationTestCase {
    public void testISO8601DateParserParseTest() throws Exception {
        String date = "2013-04-29T09:00:00.000Z";
        ISO8601DateParser dateParser = new ISO8601DateParser();
        Calendar result = dateParser.parse(date);
        assertNotNull(result);
    }

    public void testCorrectnessOfDateObject() throws Exception {
        String date = "2013-04-29T09:00:00.000Z";
        ISO8601DateParser dateParser = new ISO8601DateParser();
        Calendar myCalendar = dateParser.parse(date);
        assertEquals(myCalendar.get(Calendar.HOUR_OF_DAY), 10);
        assertEquals(myCalendar.get(Calendar.MINUTE), 0);
        assertEquals(myCalendar.get(Calendar.DATE), 29);
        assertEquals(myCalendar.get(Calendar.MONTH), Calendar.APRIL);
        assertEquals(myCalendar.get(Calendar.YEAR), 2013);
    }

    public void testISO8601DateParserToStringTest() throws Exception {
        String date = "2013-04-29T09:00:00.000Z";
        ISO8601DateParser dateParser = new ISO8601DateParser();
        Calendar result = dateParser.parse(date);
        String output = dateParser.toString(result);
        assertEquals(date, output);
    }
}
