package rce10.ic.ac.uk.exics.ViewModel;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import rce10.ic.ac.uk.exics.Model.BroadcastTags;
import rce10.ic.ac.uk.exics.Model.Exam;
import rce10.ic.ac.uk.exics.Model.User;

/**
 * Created by Rich on 22/05/2014.
 */
public class ExICSData {

    private static final String TAG = ExICSData.class.getName();

    private static String username;
    private static String password;
    private static int room;

    private static String serverHostname;
    private static int serverPort;

    private static HashMap<Integer, ArrayList<Exam>> currentSession = new HashMap<Integer, ArrayList<Exam>>();
    private static HashMap<String, User> users = new HashMap<String, User>();

    private static String chatLog = new String();

    private static ExICSData instance = null;

    private ExICSData() {
    }

    public static ExICSData getInstance() {
        synchronized (ExICSData.class) {
            if (instance == null) {
                instance = new ExICSData();
            }
            return instance;
        }
    }

    ;

    private static void broadcastLogUpdated(Context context) {
        Intent broadcast = new Intent(BroadcastTags.TAG_LOG_UPDATED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
    }

    //Member methods
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String uname) {
        this.username = uname;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRoom() {
        return this.room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public boolean inRoom(int room) {
        return this.room == room;
    }

    public String getServerHostname() {
        return this.serverHostname;
    }

    public void setServerHostname(String hostname) {
        this.serverHostname = hostname;
    }

    public int getServerPort() {
        return this.serverPort;
    }

    public void setServerPort(int port) {
        this.serverPort = port;
    }

    // /Current Session Methods
    public synchronized int getNumRooms() {
        return currentSession.size();
    }

    public synchronized Set<Integer> getAllRooms() {
        return currentSession.keySet();
    }

    public synchronized ArrayList<Exam> getExams(int room) {
        return currentSession.get(room);
    }

    public synchronized Exam getExam(int room, String courseCode) {
        ArrayList<Exam> examList = currentSession.get(room);
        if (examList == null) {
            return null;
        } else {
            for (Exam exam : examList) {
                if (exam.getExamSubModule().contentEquals(courseCode)) {
                    return exam;
                }
            }
            return null;
        }
    }

    public synchronized void addExam(Exam exam) {
        ArrayList<Exam> examList = currentSession.get(exam.getRoom());
        if (examList == null)
            examList = new ArrayList<Exam>();
        examList.add(exam);
        currentSession.put(exam.getRoom(), examList);
    }

    public synchronized int getNumExamsInRoom(int room) {
        ArrayList<Exam> examList = currentSession.get(room);
        if (examList == null) {
            return 0;
        } else {
            return examList.size();
        }
    }

    public synchronized int getNumStartedExamsInRoom(int room) {
        ArrayList<Exam> examList = currentSession.get(room);
        int numExamsStarted = 0;
        if (examList == null) {
            return 0;
        } else {
            for (Exam exam : examList) {
                if (exam.isRunning()) {
                    numExamsStarted++;
                }
            }
            return numExamsStarted;
        }
    }

    public synchronized int getNumPausedExamsInRoom(int room) {
        ArrayList<Exam> examList = currentSession.get(room);
        int numExamsPaused = 0;
        if (examList == null) {
            return 0;
        } else {
            for (Exam exam : examList) {
                if (exam.isPaused()) {
                    numExamsPaused++;
                }
            }
            return numExamsPaused;
        }
    }

    public synchronized boolean removeExam(int room, String courseCode) {
        ArrayList<Exam> examList = currentSession.get(room);
        if (examList == null) {
            return false;
        } else {
            for (Exam exam : examList) {
                if (exam.getExamSubModule().contentEquals(courseCode)) {
                    examList.remove(exam);
                    if (examList.size() == 0) {
                        currentSession.remove(room);
                    } else {
                        currentSession.put(room, examList);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public synchronized int getLowestRoomStatus(int room) {
        ArrayList<Exam> examList = currentSession.get(room);
        int status = 2;
        if (examList == null) {
            return 0;
        } else {
            for (Exam exam : examList) {
                if (!exam.isRunning()) {
                    status = 0;
                    break;
                }
                if (exam.isPaused()) {
                    if (1 < status)
                        status = 1;
                }
            }
            return status;
        }
    }

    public synchronized boolean removeRoom(int room) {
        return currentSession.remove(room) != null;
    }

    public synchronized void clearCurrentSession() {
        currentSession.clear();
    }

    public synchronized void clearCurrentUsers() {
        users.clear();
    }

    public synchronized int getNumUsers() {
        return users.size();
    }

    public synchronized Set<String> getAllUsers() {
        return users.keySet();
    }

    public synchronized void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    public synchronized boolean removeUser(String name) {
        return users.remove(name) != null;
    }

    public synchronized int getUserRoom(String name) {
        User user = users.get(name);
        if (user == null) {
            return 0;
        } else {
            return user.getRoom();
        }
    }

    public synchronized void clearSystemState() {
        clearCurrentSession();
        clearCurrentUsers();
    }

    public synchronized void resetExICSData() {
        clearCurrentSession();
        clearCurrentUsers();
        clearChatLog();
    }

    public synchronized String getChatLog() {
        return this.chatLog;
    }

    public synchronized void appendToChatLog(String line, Context context) {
        this.chatLog += android.text.format.DateFormat.format("hh:mm", new java.util.Date()) + " " + line + "\n";
        broadcastLogUpdated(context);
    }

    public synchronized void clearChatLog() {
        this.chatLog = new String();
    }

    public synchronized int getNumUsersInRoom(int room) {
        int numUsers = 0;
        for (String user : getAllUsers()) {
            User userData = users.get(user);
            if (userData.getRoom() == room)
                numUsers++;
        }
        return numUsers;
    }
}
