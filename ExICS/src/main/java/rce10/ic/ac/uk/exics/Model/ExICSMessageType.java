package rce10.ic.ac.uk.exics.Model;

/**
 * Created by Rich on 22/05/2014.
 */
abstract public class ExICSMessageType {
    public static final int PROTOCOL_HANDSHAKE = 0;
    public static final int USER_CONNECTED = 1;
    public static final int USER_DISCONNECTED = 2;
    public static final int SYSTEM_STATE = 3;
    public static final int CHANGE_ROOM = 4;
    public static final int EXAM_START = 5;
    public static final int EXAM_PAUSE = 6;
    public static final int EXAM_STOP = 7;
    public static final int EXAM_XTIME = 8;
    public static final int SEND_MESSAGE_ALL = 9;
    public static final int SEND_MESSAGE_ROOM = 10;
    public static final int SEND_MESSAGE_USER = 11;
    public static final int SUCCESS = 69;
    public static final int FAILURE = 1;
    public static final int TERMINATE_CONNECTION = -2;
}
