package rce10.ic.ac.uk.exics.tests;

import android.test.InstrumentationTestCase;

import rce10.ic.ac.uk.exics.Model.User;

/**
 * Created by Rich on 21/05/2014.
 */
public class UserClassTest extends InstrumentationTestCase {

    private User createUser() {
        String name = "rce10";
        int room = 503;
        return new User(name, room);
    }

    public void testConstructor() throws Exception {
        User testUser = createUser();
        assertNotNull(testUser);
    }

    public void testGetUsername() throws Exception {
        User testUser = createUser();
        assertEquals(testUser.getUsername(), "rce10");
    }

    public void testSetUsername() throws Exception {
        User testUser = createUser();
        testUser.setUsername("rmb209");
        assertEquals(testUser.getUsername(), "rmb209");
    }

    public void testGetRoom() throws Exception {
        User testUser = createUser();
        assertEquals(testUser.getRoom(), 503);
    }

    public void testSetRoom() throws Exception {
        User testUser = createUser();
        testUser.setRoom(209);
        assertEquals(testUser.getRoom(), 209);
    }
}
