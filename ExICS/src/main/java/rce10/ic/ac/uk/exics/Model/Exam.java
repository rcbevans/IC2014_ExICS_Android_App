package rce10.ic.ac.uk.exics.Model;

import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import rce10.ic.ac.uk.exics.Utilities.ISO8601DateParser;

/**
 * Created by Rich on 21/05/2014.
 */
public class Exam {

    private static final String TAG = Exam.class.getName();

    private String examSubModule;
    private String title;
    private int numQuestions;
    private int duration;
    private int extraTime;
    private int room;
    private Calendar scheduledStart;
    private Calendar actualStart;
    private Calendar finish;
    private Boolean running;
    private Boolean paused;
    private ArrayList<PauseResumePair> pauseResumePairs;

    public Exam(String examCode, String examTitle, int numQs, int duration, int xtime, int room, String scheduledStartJSON, String actualStartJSON, String finishJSON, Boolean examRunning, Boolean paused, ArrayList<PauseResumePair> pauseResumePairs) {
        this.examSubModule = examCode;
        this.title = examTitle;
        this.numQuestions = numQs;
        this.duration = duration;
        this.extraTime = xtime;
        this.room = room;
        ISO8601DateParser dateParser = new ISO8601DateParser();
        try {
            this.scheduledStart = dateParser.parse(scheduledStartJSON);
            if (!actualStartJSON.contentEquals("null")) {
                this.actualStart = dateParser.parse(actualStartJSON);
            }
            if (!finishJSON.contentEquals("null"))
                this.finish = dateParser.parse(finishJSON);
            else
                this.finish = null;
        } catch (ParseException pe) {
            Log.e(TAG, "Failed to parse exam date provided by server! PANIC!" + pe.getLocalizedMessage());
            StackTraceElement[] stes = pe.getStackTrace();
            for (StackTraceElement ste : stes) {
                Log.e(TAG, ste.toString());
            }
        }

        //this.running = examRunning.contentEquals("true") ? true : false;
        this.running = examRunning;
        //NEED TO IMPLEMENT THIS PROPERLY!
        this.paused = paused;
        this.pauseResumePairs = pauseResumePairs;
    }

    public String getExamSubModule() {
        return this.examSubModule;
    }

    public void setExamSubModule(String esm) {
        this.examSubModule = esm;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumQuestions() {
        return this.numQuestions;
    }

    public void setNumQuestions(int numQs) {
        this.numQuestions = numQs;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getExtraTime() {
        return this.extraTime;
    }

    public void setExtraTime(int xtraTime) {
        this.extraTime = xtraTime;
    }

    public int getRoom() {
        return this.room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public Calendar getScheduledStart() {
        return this.scheduledStart;
    }

    public void setScheduledStart(Calendar schedStart) {
        this.scheduledStart = schedStart;
    }

    public Calendar getActualStart() {
        return this.actualStart;
    }

    public void setActualStart(Calendar actStart) {
        this.actualStart = actStart;
    }

    public Calendar getFinish() {
        return this.finish;
    }

    public void setFinish(Calendar fin) {
        this.finish = fin;
    }

    public Boolean isRunning() {
        return this.running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }

    public Boolean isPaused() {
        return this.paused;
    }

    public void setPaused(Boolean paused) {
        this.paused = paused;
    }

    public int getTimePaused() {
        int timePausedMins = 0;
        for (PauseResumePair pair : pauseResumePairs) {
            if (pair.getTimeResumed() != null) {
                long diffMillis = pair.getTimeResumed().getTime().getTime() - pair.getTimePaused().getTime().getTime();
                timePausedMins += TimeUnit.MILLISECONDS.toMinutes(diffMillis);
            } else {
                Calendar now = new GregorianCalendar();
                long diffMillis = now.getTime().getTime() - pair.getTimePaused().getTime().getTime();
                timePausedMins += TimeUnit.MILLISECONDS.toMinutes(diffMillis);
            }
        }
        return timePausedMins;
    }

    public Calendar getExpectedFinish() {
        Calendar finish;
        if (isRunning()) {
            finish = (Calendar) this.actualStart.clone();
        } else {
            finish = (Calendar) this.scheduledStart.clone();
        }
        finish.add(Calendar.MINUTE, getDuration());
        finish.add(Calendar.MINUTE, getExtraTime());
        finish.add(Calendar.MINUTE, getTimePaused());

        return finish;
    }
}
