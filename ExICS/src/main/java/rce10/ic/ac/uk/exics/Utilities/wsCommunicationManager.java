package rce10.ic.ac.uk.exics.Utilities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import rce10.ic.ac.uk.exics.Model.BroadcastTags;
import rce10.ic.ac.uk.exics.Model.ExICSData;
import rce10.ic.ac.uk.exics.Model.ExICSException;
import rce10.ic.ac.uk.exics.Model.ExICSMessageType;
import rce10.ic.ac.uk.exics.Model.ExICSProtocol;

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

    public static void connectToServer(String hostname, int portNumber) {
        try {
            Log.i(TAG, "Connecting to " + "ws://" + hostname + ":" + Integer.toString(portNumber));
            mConnection.connect("ws://" + hostname + ":" + Integer.toString(portNumber), new WebSocketHandler() {
                @Override
                public void onOpen() {
                    super.onOpen();
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

            Log.i(TAG, "Sending " + message.toString(4));

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

            switch (messageType) {
                case ExICSMessageType.PROTOCOL_HANDSHAKE:
                    broadcastAuthSuccessful();
                    break;

                case ExICSMessageType.SYSTEM_STATE:
                    broadcastDataUpdated();
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

}
