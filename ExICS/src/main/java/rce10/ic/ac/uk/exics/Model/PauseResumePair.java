package rce10.ic.ac.uk.exics.Model;

import android.util.Log;

import java.text.ParseException;
import java.util.Calendar;

import rce10.ic.ac.uk.exics.Utilities.ISO8601DateParser;

/**
 * Created by Rich on 29/05/2014.
 */
public class PauseResumePair {
    private static final String TAG = PauseResumePair.class.getName();
    private Calendar timePaused;
    private Calendar timeResumed;

    public PauseResumePair(String timePaused, String timeResumed) {
        ISO8601DateParser parser = new ISO8601DateParser();
        if (!timePaused.contentEquals("null")) {
            try {
                this.timePaused = parser.parse(timePaused);
            } catch (ParseException e) {
                Log.e(TAG, "Shit just got torn up", e);
            }
        }
        if (!timeResumed.contentEquals("null")) {
            try {
                this.timeResumed = parser.parse(timeResumed);
            } catch (ParseException e) {
                Log.e(TAG, "Shit just got torn up", e);
            }
        }
    }

    public Calendar getTimePaused() {
        return this.timePaused;
    }

    public void setTimePaused(Calendar cal) {
        this.timePaused = cal;
    }

    public Calendar getTimeResumed() {
        return this.timeResumed;
    }

    public void setTimeResumed(Calendar cal) {
        this.timeResumed = cal;
    }
}
