package rce10.ic.ac.uk.exics.Model;

/**
 * Created by Rich on 21/05/2014.
 */
public class User {
    private String username;
    private int room;

    public User(String uname, int room) {
        this.username = uname;
        this.room = room;
    }

    ;

    public String getUsername() {
        return this.username;
    }

    ;

    public void setUsername(String uname) {
        this.username = uname;
    }

    ;

    public int getRoom() {
        return this.room;
    }

    ;

    public void setRoom(int room) {
        this.room = room;
    }

    ;
}
