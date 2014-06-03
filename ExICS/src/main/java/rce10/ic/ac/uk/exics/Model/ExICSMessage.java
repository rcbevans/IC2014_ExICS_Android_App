package rce10.ic.ac.uk.exics.Model;

import java.util.Calendar;

/**
 * Created by Rich on 03/06/2014.
 */
public class ExICSMessage {
    private String sender;
    private int room;
    private Calendar timeReceived;
    private String message;

    public ExICSMessage(String sender, int room, Calendar timeReceived, String message) {
        this.sender = sender;
        this.room = room;
        this.timeReceived = timeReceived;
        this.message = message;
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Calendar getTimeReceived() {
        return timeReceived;
    }

    public void setTimeReceived(Calendar timeReceived) {
        this.timeReceived = timeReceived;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRoom() {
        return room;

    }

    public void setRoom(int room) {
        this.room = room;
    }

}
