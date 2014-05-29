package rce10.ic.ac.uk.exics.tests;

import android.test.InstrumentationTestCase;

import java.util.ArrayList;
import java.util.Set;

import rce10.ic.ac.uk.exics.Model.ExICSData;
import rce10.ic.ac.uk.exics.Model.Exam;
import rce10.ic.ac.uk.exics.Model.PauseResumePair;
import rce10.ic.ac.uk.exics.Model.User;

/**
 * Created by Rich on 22/05/2014.
 */
public class ExICSDataTest extends InstrumentationTestCase {

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
        Boolean paused = false;

        ArrayList<PauseResumePair> pairs = new ArrayList<PauseResumePair>();

        return new Exam(examCode, title, numQs, duration, xTime, room, date, start, finish, running, paused, pairs);
    }

    private User setUpUser() {
        String name = "rce10";
        int room = 69;
        return new User(name, room);
    }

    public void testGetInstance() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        assertNotNull(testObject);
    }

    public void testSingleton() throws Exception {
        ExICSData testObject1 = ExICSData.getInstance();
        ExICSData testObject2 = ExICSData.getInstance();
        assertEquals(testObject1, testObject2);
    }

    public void testGetSetUsername() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        String uname = "rce10";
        testObject.setUsername(uname);
        String objUname = testObject.getUsername();
        assertEquals(uname, objUname);
    }

    public void testGetSetPassword() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        String password = "password";
        testObject.setPassword(password);
        String objPassword = testObject.getPassword();
        assertEquals(password, objPassword);
    }

    public void testGetSetServerHostname() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        String hostname = "192.0.0.1";
        testObject.setServerHostname(hostname);
        String objHostname = testObject.getServerHostname();
        assertEquals(hostname, objHostname);
    }

    public void testGetSetServerPort() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        int port = 8081;
        testObject.setServerPort(port);
        int objPort = testObject.getServerPort();
        assertEquals(port, objPort);
    }

    public void testGetNumRooms() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        testObject.clearCurrentSession();
        assertEquals(0, testObject.getNumRooms());
        int room = 403;
        Exam mockExam = setUpExam();
        mockExam.setRoom(room);
        testObject.addExam(mockExam);
        assertEquals(testObject.getNumRooms(), 1);
        testObject.clearCurrentSession();
        assertEquals(testObject.getNumRooms(), 0);
    }

    public void testGetExams() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        testObject.clearCurrentSession();
        int room = 403;
        Exam mockExam = setUpExam();
        mockExam.setRoom(room);
        ArrayList<Exam> examList = new ArrayList<Exam>();
        examList.add(mockExam);
        testObject.addExam(mockExam);
        ArrayList<Exam> objExamList = testObject.getExams(room);
        assertEquals(objExamList.size(), examList.size());
    }

    public void testGetNumOfExamsInRoom() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        testObject.clearCurrentSession();
        int room = 400;
        assertEquals(0, testObject.getNumExamsInRoom(room));
        Exam mockExam = setUpExam();
        mockExam.setRoom(room);
        testObject.addExam(mockExam);
        assertEquals(1, testObject.getNumExamsInRoom(room));
    }

    public void testRemoveExam() throws Exception {
        String examCode = "C231=MC231";
        int room = 102;
        ExICSData testObject = ExICSData.getInstance();
        testObject.clearCurrentSession();
        assertFalse(testObject.removeExam(room, examCode));
        assertEquals(0, testObject.getNumRooms());
        assertEquals(0, testObject.getNumExamsInRoom(room));
        Exam mockExam = setUpExam();
        mockExam.setRoom(room);
        testObject.addExam(mockExam);
        assertEquals(1, testObject.getNumRooms());
        assertEquals(1, testObject.getNumExamsInRoom(room));
        assertTrue(testObject.removeExam(room, examCode));
        assertEquals(0, testObject.getNumRooms());
        assertEquals(0, testObject.getNumExamsInRoom(room));
    }

    public void testRemoveRoom() throws Exception {
        int room = 103;
        ExICSData testObject = ExICSData.getInstance();
        testObject.clearCurrentSession();
        assertFalse(testObject.removeRoom(room));
        assertEquals(0, testObject.getNumRooms());
        assertEquals(0, testObject.getNumExamsInRoom(room));
        Exam mockExam = setUpExam();
        mockExam.setRoom(room);
        testObject.addExam(mockExam);
        assertEquals(1, testObject.getNumRooms());
        assertEquals(1, testObject.getNumExamsInRoom(room));
        assertTrue(testObject.removeRoom(room));
        assertEquals(0, testObject.getNumRooms());
        assertEquals(0, testObject.getNumExamsInRoom(room));
    }

    public void testClearCurrentSession() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        testObject.clearCurrentSession();
        assertEquals(0, testObject.getNumRooms());
        Exam mockExam = setUpExam();
        int room = 101;
        mockExam.setRoom(room);
        testObject.addExam(mockExam);
        assertEquals(1, testObject.getNumRooms());
        testObject.clearCurrentSession();
        assertEquals(0, testObject.getNumRooms());
    }

    public void testGetAllRooms() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        testObject.clearCurrentSession();
        int room1 = 202;
        int room2 = 203;
        Exam mockExam1 = setUpExam();
        mockExam1.setRoom(room1);
        testObject.addExam(mockExam1);
        Exam mockExam2 = setUpExam();
        mockExam2.setRoom(room2);
        testObject.addExam(mockExam2);
        assertEquals(2, testObject.getNumRooms());
        assertEquals(1, testObject.getNumExamsInRoom(room1));
        Set<Integer> rooms = testObject.getAllRooms();
        assertEquals(2, rooms.size());
    }

    public void testGetNumUsers() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        testObject.clearCurrentUsers();
        assertEquals(0, testObject.getNumUsers());
        User mockUser = setUpUser();
        testObject.addUser(mockUser);
        assertEquals(1, testObject.getNumUsers());
    }

    public void testClearUsers() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        testObject.clearCurrentUsers();
        assertEquals(0, testObject.getNumUsers());
        User mockUser = setUpUser();
        testObject.addUser(mockUser);
        assertEquals(1, testObject.getNumUsers());
        testObject.clearCurrentUsers();
        assertEquals(0, testObject.getNumUsers());
    }

    public void testRemoveUser() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        testObject.clearCurrentUsers();
        assertEquals(0, testObject.getNumUsers());
        String rce10 = "rce10";
        assertFalse(testObject.removeUser(rce10));
        User mockUser = setUpUser();
        testObject.addUser(mockUser);
        assertEquals(1, testObject.getNumUsers());
        assertTrue(testObject.removeUser(rce10));
    }

    public void testGetUserRoom() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        String user = "rce10";
        int room = 69;
        testObject.clearCurrentUsers();
        assertEquals(0, testObject.getNumUsers());
        assertEquals(0, testObject.getUserRoom(user));
        testObject.addUser(setUpUser());
        assertEquals(room, testObject.getUserRoom(user));
    }

    public void testResetData() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        testObject.clearCurrentUsers();
        testObject.clearCurrentSession();
        assertEquals(0, testObject.getNumRooms());
        assertEquals(0, testObject.getNumUsers());
        User mockUser = setUpUser();
        Exam mockExam = setUpExam();
        mockExam.setRoom(44);
        testObject.addUser(mockUser);
        testObject.addExam(mockExam);
        assertEquals(1, testObject.getNumUsers());
        assertEquals(1, testObject.getNumRooms());
        testObject.resetData();
        assertEquals(0, testObject.getNumRooms());
        assertEquals(0, testObject.getNumUsers());
    }

    public void testGetAllUsers() throws Exception {
        ExICSData testObject = ExICSData.getInstance();
        testObject.clearCurrentUsers();
        assertEquals(0, testObject.getNumUsers());
        String user1 = "bill";
        String user2 = "ben";
        User u1 = setUpUser();
        u1.setUsername(user1);
        testObject.addUser(u1);
        User u2 = setUpUser();
        u2.setUsername(user2);
        testObject.addUser(u2);
        assertEquals(2, testObject.getNumUsers());
        Set<String> users = testObject.getAllUsers();
        assertEquals(2, users.size());
        testObject.removeUser(user1);
        users = testObject.getAllUsers();
        assertEquals(1, users.size());
    }
}
