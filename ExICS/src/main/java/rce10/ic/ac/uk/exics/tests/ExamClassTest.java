package rce10.ic.ac.uk.exics.tests;

import android.test.InstrumentationTestCase;

import java.util.Calendar;

import rce10.ic.ac.uk.exics.Model.Exam;
import rce10.ic.ac.uk.exics.Utilities.ISO8601DateParser;

;

/**
 * Created by Rich on 21/05/2014.
 */
public class ExamClassTest extends InstrumentationTestCase {

    private Exam setUpExam() {
        String examCode = "C231=MC231";
        String title = "Intro to AI";
        int numQs = 2;
        int duration = 75;
        int xTime = 0;
        int room = 341;
        String date = "2013-04-29T09:00:00.000Z";
        String start = "2013-04-29T09:00:00.000Z";
        String finish = "null";
        Boolean running = false;

        return new Exam(examCode, title, numQs, duration, xTime, room, date, start, finish, running);
    }

    public void testConstructor() throws Exception {
        Exam testExam = setUpExam();
        assertNotNull(testExam);
    }

    public void testGetExamCode() throws Exception {
        Exam testExam = setUpExam();
        assertEquals(testExam.getExamSubModule(), "C231=MC231");
    }

    public void testSetExamCode() throws Exception {
        Exam testExam = setUpExam();
        testExam.setExamSubModule("NEW_CODE");
        assertEquals(testExam.getExamSubModule(), "NEW_CODE");
    }

    public void testGetExamTitle() throws Exception {
        Exam testExam = setUpExam();
        assertEquals(testExam.getTitle(), "Intro to AI");
    }

    public void testSetExamTitle() throws Exception {
        Exam testExam = setUpExam();
        testExam.setTitle("NEW_TITLE");
        assertEquals(testExam.getTitle(), "NEW_TITLE");
    }

    public void testGetNumQs() throws Exception {
        Exam testExam = setUpExam();
        assertEquals(testExam.getNumQuestions(), 2);
    }

    public void testSetNumQs() throws Exception {
        Exam testExam = setUpExam();
        testExam.setNumQuestions(4);
        assertEquals(testExam.getNumQuestions(), 4);
    }

    public void testGetDuration() throws Exception {
        Exam testExam = setUpExam();
        assertEquals(testExam.getDuration(), 75);
    }

    public void testSetDuration() throws Exception {
        Exam testExam = setUpExam();
        testExam.setDuration(120);
        assertEquals(testExam.getDuration(), 120);
    }

    public void testGetExtraTime() throws Exception {
        Exam testExam = setUpExam();
        assertEquals(testExam.getExtraTime(), 0);
    }

    public void testSetExtraTime() throws Exception {
        Exam testExam = setUpExam();
        testExam.setExtraTime(15);
        assertEquals(testExam.getExtraTime(), 15);
    }

    public void testGetRoom() throws Exception {
        Exam testExam = setUpExam();
        assertEquals(testExam.getRoom(), 341);
    }

    public void testSetRoom() throws Exception {
        Exam testExam = setUpExam();
        testExam.setRoom(109);
        assertEquals(testExam.getRoom(), 109);
    }

    public void testGetRunning() throws Exception {
        Exam testExam = setUpExam();
        assertFalse(testExam.isRunning());
    }

    public void testSetRunning() throws Exception {
        Exam testExam = setUpExam();
        testExam.setRunning(true);
        assertTrue(testExam.isRunning());
    }

    public void testGetDate() throws Exception {
        Exam testExam = setUpExam();
        String date = "2013-04-29T09:00:00.000Z";
        ISO8601DateParser dateParser = new ISO8601DateParser();
        Calendar directParsedDate = dateParser.parse(date);
        assertEquals(directParsedDate, testExam.getScheduledStart());
    }

    public void testSetDate() throws Exception {
        Exam testExam = setUpExam();
        Calendar examStart = testExam.getScheduledStart();
        examStart.set(Calendar.HOUR_OF_DAY, 12);
        testExam.setScheduledStart(examStart);

        assertEquals(testExam.getScheduledStart(), examStart);
        Calendar newCal = testExam.getScheduledStart();
        assertEquals(newCal.get(Calendar.HOUR_OF_DAY), 12);
    }
}
