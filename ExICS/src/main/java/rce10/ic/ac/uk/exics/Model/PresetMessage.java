package rce10.ic.ac.uk.exics.Model;

/**
 * Created by Rich on 02/06/2014.
 */
public class PresetMessage {
    String title;
    String message;

    public PresetMessage(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
