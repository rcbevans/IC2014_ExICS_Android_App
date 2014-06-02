package rce10.ic.ac.uk.exics.Utilities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import rce10.ic.ac.uk.exics.Model.BroadcastTags;
import rce10.ic.ac.uk.exics.Model.ExICSData;
import rce10.ic.ac.uk.exics.Model.ExICSException;
import rce10.ic.ac.uk.exics.Model.ExICSMessageType;
import rce10.ic.ac.uk.exics.Model.ExICSProtocol;
import rce10.ic.ac.uk.exics.Model.Exam;
import rce10.ic.ac.uk.exics.Model.PauseResumePair;
import rce10.ic.ac.uk.exics.Model.User;

/**
 * Created by Rich on 21/05/2014.
 */
public class wsCommunicationManager {

    private static final String TAG = wsCommunicationManager.class.getName();

    private static final WebSocketConnection mConnection = new WebSocketConnection();

    private static wsCommunicationManager instance = null;

    private static Context context = null;

    private static ExICSData exICSData = ExICSData.getInstance();

    private wsCommunicationManager(Context ctx) {
        this.context = ctx;
    }

    public static wsCommunicationManager getInstance(Context ctx) {
        synchronized (wsCommunicationManager.class) {
            if (instance == null) {
                instance = new wsCommunicationManager(ctx);
            }
            return instance;
        }
    }

    public static void connectToServer(final String hostname, final int portNumber) {
        try {
            Log.i(TAG, "Connecting to " + "ws://" + hostname + ":" + Integer.toString(portNumber));
            mConnection.connect("ws://" + hostname + ":" + Integer.toString(portNumber), new WebSocketHandler() {
                @Override
                public void onOpen() {
                    super.onOpen();
                    exICSData.appendToChatLog("Connected to server " + "ws://" + hostname + ":" + Integer.toString(portNumber), context);
                    Log.i(TAG, "Websocket Connection Opened");
                    sendProtocolHandshake(exICSData.getUsername(), exICSData.getPassword());
                }

                @Override
                public void onTextMessage(String payload) {
                    super.onTextMessage(payload);
                    Log.i(TAG, "Websocket Text Messaged Receieved " + payload);
                    handleMessage(payload);
                }

                @Override
                public void onClose(int code, String reason) {
                    super.onClose(code, reason);
                    exICSData.appendToChatLog("Lost connection to server " + "ws://" + hostname + ":" + Integer.toString(portNumber), context);
                    Log.i(TAG, "Websocket Connection Closed");
                    broadcastConnectionClosed(reason);
                }
            });

        } catch (WebSocketException e) {
            Log.e(TAG, "WebsocketExceptionOccurred", e);
        }
    }

    private static void sendProtocolHandshake(String username, String password) {
        try {
            JSONObject header = new JSONObject();
            header.put(ExICSProtocol.TAG_MESSAGE_TYPE, ExICSMessageType.PROTOCOL_HANDSHAKE);
            header.put(ExICSProtocol.TAG_SENDER, username);

            JSONObject payload = new JSONObject();
            payload.put(ExICSProtocol.TAG_USERNAME, username);
            payload.put(ExICSProtocol.TAG_PASSWORD, password);

            JSONObject message = new JSONObject();
            message.put(ExICSProtocol.TAG_HEADER, header);
            message.put(ExICSProtocol.TAG_PAYLOAD, payload);

            mConnection.sendTextMessage(message.toString());
        } catch (JSONException e) {
            broadcastFailure(e.getLocalizedMessage());
        }
    }

    private static void handleMessage(String message) {
        try {
            JSONObject messageObject = new JSONObject(message);
            JSONObject messageHeader = messageObject.getJSONObject(ExICSProtocol.TAG_HEADER);
            JSONObject messagePayload = messageObject.getJSONObject(ExICSProtocol.TAG_PAYLOAD);

            int messageType = messageHeader.getInt(ExICSProtocol.TAG_MESSAGE_TYPE);

            String username, sender, examCode, msg;
            int roomNum;

            switch (messageType) {
                case ExICSMessageType.PROTOCOL_HANDSHAKE:
                    exICSData.appendToChatLog("Successfully Authenticated", context);
                    broadcastAuthSuccessful();
                    break;

                case ExICSMessageType.SYSTEM_STATE:
                    exICSData.clearSystemState();
                    processSystemData(messagePayload);
                    exICSData.appendToChatLog("System State Updated", context);
                    broadcastDataUpdated();
                    break;

                case ExICSMessageType.EXAM_START:
                    username = messagePayload.getString(ExICSProtocol.TAG_USERNAME);
                    roomNum = messagePayload.getInt(ExICSProtocol.TAG_ROOM);
                    examCode = messagePayload.getString(ExICSProtocol.TAG_EXAM);
                    exICSData.appendToChatLog(username + " started " + examCode + " in room " + roomNum, context);
                    break;

                case ExICSMessageType.EXAM_PAUSE:
                    username = messagePayload.getString(ExICSProtocol.TAG_USERNAME);
                    roomNum = messagePayload.getInt(ExICSProtocol.TAG_ROOM);
                    examCode = messagePayload.getString(ExICSProtocol.TAG_EXAM);
                    Exam changed = exICSData.getExam(roomNum, examCode);
                    if (changed.isPaused()) {
                        exICSData.appendToChatLog(username + " paused " + examCode + " in room " + roomNum, context);
                    } else {
                        exICSData.appendToChatLog(username + " resumed " + examCode + " in room " + roomNum, context);
                    }
                    break;

                case ExICSMessageType.EXAM_STOP:
                    username = messagePayload.getString(ExICSProtocol.TAG_USERNAME);
                    roomNum = messagePayload.getInt(ExICSProtocol.TAG_ROOM);
                    examCode = messagePayload.getString(ExICSProtocol.TAG_EXAM);
                    exICSData.appendToChatLog(username + " stopped " + examCode + " in room " + roomNum, context);
                    break;

                case ExICSMessageType.SEND_MESSAGE_ALL:
                    sender = messageHeader.getString(ExICSProtocol.TAG_SENDER);
                    msg = messagePayload.getString(ExICSProtocol.TAG_MESSAGE);
                    exICSData.appendToChatLog(String.format("%s in room %d says: %s", sender, exICSData.getUserRoom(sender), msg), context);
                    break;

                case ExICSMessageType.SEND_MESSAGE_ROOM:
                    sender = messageHeader.getString(ExICSProtocol.TAG_SENDER);
                    msg = messagePayload.getString(ExICSProtocol.TAG_MESSAGE);
                    exICSData.appendToChatLog(String.format("%s in room %d says: %s", sender, exICSData.getUserRoom(sender), msg), context);
                    break;

                case ExICSMessageType.SEND_MESSAGE_USER:
                    sender = messageHeader.getString(ExICSProtocol.TAG_SENDER);
                    msg = messagePayload.getString(ExICSProtocol.TAG_MESSAGE);
                    exICSData.appendToChatLog(String.format("%s in room %d says: %s", sender, exICSData.getUserRoom(sender), msg), context);
                    break;

                case ExICSMessageType.FAILURE:
                    broadcastFailure(messagePayload.getString(ExICSProtocol.TAG_REASON));
                    break;

                default:
                    throw new ExICSException("Unknown Message Type Received");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse received message " + message, e);
        } catch (ExICSException e) {
            Log.e(TAG, "Excaption Handling Message Occurred", e);
        }
    }

    private static void processSystemData(JSONObject payload) {
        try {
            JSONArray users = payload.getJSONArray(ExICSProtocol.TAG_USERS);
            addUsers(users);
            JSONObject exams = payload.getJSONObject(ExICSProtocol.TAG_EXAMS);
            addExams(exams);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to Process System Data", e);
            broadcastFailure(e.getLocalizedMessage());
        }
    }

    private static void addUsers(JSONArray users) {
        try {
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                User newUser = new User(user.getString(ExICSProtocol.TAG_NAME), user.getInt(ExICSProtocol.TAG_ROOM));
                exICSData.addUser(newUser);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to Add Users", e);
            broadcastFailure(e.getLocalizedMessage());
        }
    }

    private static void addExams(JSONObject exams) {
        try {
            Iterator<?> rooms = exams.keys();
            while (rooms.hasNext()) {
                String roomNumber = (String) rooms.next();
                JSONArray room = exams.getJSONArray(roomNumber);
                for (int i = 0; i < room.length(); i++) {
                    JSONObject exam = room.getJSONObject(i);
                    String examCode = exam.getString(ExICSProtocol.TAG_EXAM_SUBEXAM);
                    String examTitle = exam.getString(ExICSProtocol.TAG_TITLE);
                    int numQs = exam.getInt(ExICSProtocol.TAG_NUM_QUESTIONS);
                    int examDuration = exam.getInt(ExICSProtocol.TAG_DURATION);
                    String scheduledStart = exam.getString(ExICSProtocol.TAG_DATE);
                    Boolean running = exam.getBoolean(ExICSProtocol.TAG_RUNNING);
                    Boolean paused = exam.getBoolean(ExICSProtocol.TAG_PAUSED);
                    String start = exam.getString(ExICSProtocol.TAG_START);
                    String finish = exam.getString(ExICSProtocol.TAG_FINISH);
                    int extraTime = exam.getInt(ExICSProtocol.TAG_EXTRA_TIME);
                    String examRoomString = exam.getString(ExICSProtocol.TAG_ROOM);
                    int examRoom = Integer.parseInt(examRoomString);

                    ArrayList<PauseResumePair> pauseResumeTimings = new ArrayList<PauseResumePair>();
                    JSONArray pauseResumePairArray = exam.getJSONArray(ExICSProtocol.TAG_PAUSE_TIMINGS);
                    for (int j = 0; j < pauseResumePairArray.length(); j++) {
                        JSONObject pauseResumePairJSON = pauseResumePairArray.getJSONObject(j);
                        PauseResumePair pair = new PauseResumePair(pauseResumePairJSON.getString(ExICSProtocol.TAG_PAUSED), pauseResumePairJSON.getString(ExICSProtocol.TAG_RESUMED));
                        pauseResumeTimings.add(pair);
                    }

                    Exam newExam = new Exam(examCode, examTitle, numQs, examDuration, extraTime, examRoom, scheduledStart, start, finish, running, paused, pauseResumeTimings);
                    exICSData.addExam(newExam);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to add Exams", e);
            broadcastFailure(e.getLocalizedMessage());
        }
    }

    private static void broadcastAuthSuccessful() {
        Intent broadcast = new Intent(BroadcastTags.TAG_AUTH_SUCCESSFUL);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
    }

    private static void broadcastDataUpdated() {
        Intent broadcast = new Intent(BroadcastTags.TAG_DATA_UPDATED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
    }

    private static void broadcastFailure(String reason) {
        Intent broadcast = new Intent(BroadcastTags.TAG_FAILURE_OCCURRED);
        broadcast.putExtra(ExICSProtocol.TAG_REASON, reason);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
    }

    private static void broadcastConnectionClosed(String reason) {
        Intent broadcast = new Intent(BroadcastTags.TAG_CONNECTION_CLOSED);
        broadcast.putExtra(ExICSProtocol.TAG_REASON, reason);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
    }

    public static Boolean isConnected() {
        return mConnection.isConnected();
    }

    public static void disconnect() {
        mConnection.disconnect();
    }

    public static void updateRoom(int roomNum) {
        try {
            JSONObject header = new JSONObject();
            header.put(ExICSProtocol.TAG_MESSAGE_TYPE, ExICSMessageType.CHANGE_ROOM);
            header.put(ExICSProtocol.TAG_SENDER, exICSData.getUsername());

            JSONObject payload = new JSONObject();
            payload.put(ExICSProtocol.TAG_ROOM, roomNum);

            JSONObject message = new JSONObject();
            message.put(ExICSProtocol.TAG_HEADER, header);
            message.put(ExICSProtocol.TAG_PAYLOAD, payload);

            mConnection.sendTextMessage(message.toString());
        } catch (JSONException e) {
            broadcastFailure(e.getLocalizedMessage());
        }
    }

    public static void startExam(int room, String examCode) {
        try {
            JSONObject header = new JSONObject();
            header.put(ExICSProtocol.TAG_MESSAGE_TYPE, ExICSMessageType.EXAM_START);
            header.put(ExICSProtocol.TAG_SENDER, exICSData.getUsername());

            JSONObject payload = new JSONObject();
            payload.put(ExICSProtocol.TAG_ROOM, room);
            payload.put(ExICSProtocol.TAG_EXAM, examCode);

            JSONObject message = new JSONObject();
            message.put(ExICSProtocol.TAG_HEADER, header);
            message.put(ExICSProtocol.TAG_PAYLOAD, payload);

            mConnection.sendTextMessage(message.toString());
        } catch (JSONException e) {
            broadcastFailure(e.getLocalizedMessage());
            Log.e(TAG, "Failed to Start Exam", e);
        }
    }

    public static void pauseExam(int room, String examCode) {
        try {
            JSONObject header = new JSONObject();
            header.put(ExICSProtocol.TAG_MESSAGE_TYPE, ExICSMessageType.EXAM_PAUSE);
            header.put(ExICSProtocol.TAG_SENDER, exICSData.getUsername());

            JSONObject payload = new JSONObject();
            payload.put(ExICSProtocol.TAG_ROOM, room);
            payload.put(ExICSProtocol.TAG_EXAM, examCode);

            JSONObject message = new JSONObject();
            message.put(ExICSProtocol.TAG_HEADER, header);
            message.put(ExICSProtocol.TAG_PAYLOAD, payload);

            mConnection.sendTextMessage(message.toString());
        } catch (JSONException e) {
            broadcastFailure(e.getLocalizedMessage());
            Log.e(TAG, "Failed to Pause/Resume Exam", e);
        }
    }

    public static void stopExam(int room, String examCode) {
        try {
            JSONObject header = new JSONObject();
            header.put(ExICSProtocol.TAG_MESSAGE_TYPE, ExICSMessageType.EXAM_STOP);
            header.put(ExICSProtocol.TAG_SENDER, exICSData.getUsername());

            JSONObject payload = new JSONObject();
            payload.put(ExICSProtocol.TAG_ROOM, room);
            payload.put(ExICSProtocol.TAG_EXAM, examCode);

            JSONObject message = new JSONObject();
            message.put(ExICSProtocol.TAG_HEADER, header);
            message.put(ExICSProtocol.TAG_PAYLOAD, payload);

            mConnection.sendTextMessage(message.toString());
        } catch (JSONException e) {
            broadcastFailure(e.getLocalizedMessage());
            Log.e(TAG, "Failed to Start Exam", e);
        }
    }

    public static void sendMessageToAll(String msg) {
        try {
            JSONObject header = new JSONObject();
            header.put(ExICSProtocol.TAG_MESSAGE_TYPE, ExICSMessageType.SEND_MESSAGE_ALL);
            header.put(ExICSProtocol.TAG_SENDER, exICSData.getUsername());

            JSONObject payload = new JSONObject();
            payload.put(ExICSProtocol.TAG_MESSAGE, msg);

            JSONObject message = new JSONObject();
            message.put(ExICSProtocol.TAG_HEADER, header);
            message.put(ExICSProtocol.TAG_PAYLOAD, payload);

            mConnection.sendTextMessage(message.toString());
            exICSData.appendToChatLog(String.format("Sent message to all users: %s", msg), context);
        } catch (JSONException e) {
            broadcastFailure(e.getLocalizedMessage());
            Log.e(TAG, "Failed to Send Message To All", e);
        }
    }

    public static void sendMessageToAllInRoom(int room, String msg) {
        try {
            JSONObject header = new JSONObject();
            header.put(ExICSProtocol.TAG_MESSAGE_TYPE, ExICSMessageType.SEND_MESSAGE_ROOM);
            header.put(ExICSProtocol.TAG_SENDER, exICSData.getUsername());

            JSONObject payload = new JSONObject();
            payload.put(ExICSProtocol.TAG_ROOM, room);
            payload.put(ExICSProtocol.TAG_MESSAGE, msg);

            JSONObject message = new JSONObject();
            message.put(ExICSProtocol.TAG_HEADER, header);
            message.put(ExICSProtocol.TAG_PAYLOAD, payload);

            mConnection.sendTextMessage(message.toString());
            exICSData.appendToChatLog(String.format("Sent message to room %d: %s", room, msg), context);
        } catch (JSONException e) {
            broadcastFailure(e.getLocalizedMessage());
            Log.e(TAG, "Failed to Send Message To All in Room", e);
        }
    }

    public static void sendMessageToUser(String user, String msg) {
        try {
            JSONObject header = new JSONObject();
            header.put(ExICSProtocol.TAG_MESSAGE_TYPE, ExICSMessageType.SEND_MESSAGE_USER);
            header.put(ExICSProtocol.TAG_SENDER, exICSData.getUsername());

            JSONObject payload = new JSONObject();
            payload.put(ExICSProtocol.TAG_USERNAME, user);
            payload.put(ExICSProtocol.TAG_MESSAGE, msg);

            JSONObject message = new JSONObject();
            message.put(ExICSProtocol.TAG_HEADER, header);
            message.put(ExICSProtocol.TAG_PAYLOAD, payload);

            mConnection.sendTextMessage(message.toString());
            exICSData.appendToChatLog(String.format("Sent message to user %s: %s", user, msg), context);
        } catch (JSONException e) {
            broadcastFailure(e.getLocalizedMessage());
            Log.e(TAG, "Failed to Send Message To All in Room", e);
        }
    }
}
