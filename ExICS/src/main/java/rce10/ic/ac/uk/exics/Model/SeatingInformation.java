package rce10.ic.ac.uk.exics.Model;

/**
 * Created by Rich on 04/06/2014.
 */
public class SeatingInformation {
    private int room;
    private String course;
    private String student_class;
    private int seat;
    private String CID;

    public SeatingInformation(int room, String course, String student_class, int seat, String cid) {
        this.room = room;
        this.course = course;
        this.student_class = student_class;
        this.seat = seat;
        this.CID = cid;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getStudent_class() {
        return student_class;
    }

    public void setStudent_class(String student_class) {
        this.student_class = student_class;
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public String getCID() {
        return CID;
    }

    public void setCID(String CID) {
        this.CID = CID;
    }
}
