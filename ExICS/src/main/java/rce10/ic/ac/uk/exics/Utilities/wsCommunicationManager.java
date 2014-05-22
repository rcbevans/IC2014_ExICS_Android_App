package rce10.ic.ac.uk.exics.Utilities;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
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

    private wsCommunicationManager() {
    }

    public static wsCommunicationManager getInstance() {
        synchronized (wsCommunicationManager.class) {
            if (instance == null) {
                instance = new wsCommunicationManager();
            }
            return instance;
        }
    }

    public static void connectToServer(String hostname, int portNumber) {
        try {
            mConnection.connect("ws://" + hostname + ":" + Integer.toString(portNumber), new WebSocketHandler() {
                @Override
                public void onOpen() {
                    super.onOpen();
                    Log.i(TAG, "Websocket Connection Opened");
                }

                @Override
                public void onTextMessage(String payload) {
                    super.onTextMessage(payload);
                    Log.i(TAG, "Websocket Text Messaged Receieved " + payload);
                }

                @Override
                public void onClose(int code, String reason) {
                    super.onClose(code, reason);
                    Log.i(TAG, "Websocket Connection Closed");
                }
            });

        } catch (WebSocketException e) {
            Log.e(TAG, "WebsocketExceptionOccurred", e);
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

}
