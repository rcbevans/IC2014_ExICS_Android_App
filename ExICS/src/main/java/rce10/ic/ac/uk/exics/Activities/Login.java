package rce10.ic.ac.uk.exics.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import rce10.ic.ac.uk.exics.Adapters.RoomSelectSpinnerAdapter;
import rce10.ic.ac.uk.exics.Model.BroadcastTags;
import rce10.ic.ac.uk.exics.Model.ExICSData;
import rce10.ic.ac.uk.exics.Model.ExICSProtocol;
import rce10.ic.ac.uk.exics.R;
import rce10.ic.ac.uk.exics.Utilities.wsCommunicationManager;

public class Login extends Activity {

    private static final String TAG = Login.class.getName();

    private static final String LOGIN_PREFERENCES = "LOGIN_PREFERENCES";

    private static final String SHOW_PASSWORD_PREFERENCE = "SHOW_PASSWORD";
    private static final String REMEMBER_CREDENTIALS_PREFERENCE = "REMEMBER_CREDENTIALS";

    private static final String TAG_USERNAME = "USERNAME";
    private static final String TAG_PASSWORD = "PASSWORD";

    private static final String TAG_ENTERED_USERNAME = "ENTERED_USERNAME";
    private static final String TAG_ENTERED_PASSWORD = "ENTERED_PASSWORD";

    private static final String TAG_ABOUT_SHOWING = "ABOUT_SHOWING";

    private static final String TAG_ROOM_SELECT_SHOWING = "ROOM_SELECT_SHOWING";
    private static final String TAG_ROOM_SELECT_SELECTED = "ROOM_SELECT_SELECTED";

    private static final String TAG_PROGRESS_SHOWING = "PROGRESS_SHOWING";
    private static final String TAG_PROGRESS_TEXT = "PROGRESS_TEXT";

    private static final String TAG_SERVER_HOSTNAME = "EXICS_HOSTNAME";
    private static final String TAG_SERVER_PORT = "EXICS_PORT";

    private static final int LOGIN_SETTINGS = 0;

    private static ExICSData exicsData = ExICSData.getInstance();
    private static wsCommunicationManager wsCM = null;
    private static String loadingSpinnerMessage = "";
    private AlertDialog aboutDialog = null;
    private AlertDialog roomSelect = null;
    private int roomSelectSpinnerItem = 0;
    private ProgressDialog loadingSpinner = null;
    private BroadcastReceiver onAuthSuccessful = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received Broadcast onAuthSuccessful");
            if (loadingSpinner != null && loadingSpinner.isShowing()) {
                loadingSpinnerMessage = "Fetching System Data";
                loadingSpinner.setMessage(loadingSpinnerMessage);
            }
        }
    };
    private BroadcastReceiver onFailure = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received Broadcast onReceive");
            if (loadingSpinner != null && loadingSpinner.isShowing()) {
                loadingSpinner.dismiss();
            }
            if (roomSelect != null && roomSelect.isShowing()) {
                roomSelect.dismiss();
            }
            String reason = intent.getStringExtra(ExICSProtocol.TAG_REASON);
            Toast.makeText(Login.this, "A Failure has occurred... Reason Given: " + reason, Toast.LENGTH_LONG).show();
        }
    };
    private BroadcastReceiver onConnectionClosed = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received Broadcast onConnectionClosed");
            if (loadingSpinner != null && loadingSpinner.isShowing()) {
                loadingSpinner.dismiss();
                String reason = intent.getStringExtra(ExICSProtocol.TAG_REASON);
                Toast.makeText(Login.this, "The connection to the server was closed... Reason Given: " + reason + "... Are the credentials you provided correct?", Toast.LENGTH_LONG).show();
            }
            if (roomSelect != null && roomSelect.isShowing()) {
                roomSelect.dismiss();
                String reason = intent.getStringExtra(ExICSProtocol.TAG_REASON);
                Toast.makeText(Login.this, "The connection to the server was closed... Reason Given: " + reason, Toast.LENGTH_LONG).show();
            }
        }
    };
    private Boolean holdWSOpen = false;
    private BroadcastReceiver onDataUpdated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received Broadcast onDataUpdated");

            CheckBox rememberCredentials = (CheckBox) findViewById(R.id.cbLoginRememberCredentials);
            final SharedPreferences sp = getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);

            if (rememberCredentials.isChecked()) {
                sp.edit().putString(TAG_USERNAME, exicsData.getUsername()).commit();
                sp.edit().putString(TAG_PASSWORD, exicsData.getPassword()).commit();
            }

            if (loadingSpinner != null && loadingSpinner.isShowing()) {
                loadingSpinner.dismiss();

                final AlertDialog.Builder roomSelectBuilder = new AlertDialog.Builder(Login.this);
                roomSelectBuilder.setTitle("Room Select");
                View roomSelectDialogView = getLayoutInflater().inflate(R.layout.room_select_dialog, null);
                final Spinner roomList = (Spinner) roomSelectDialogView.findViewById(R.id.spRoomSpinner);
                ArrayList<String> rooms = new ArrayList<String>();
                Set<Integer> roomsWithExams = exicsData.getAllRooms();
                for (int room : roomsWithExams) {
                    rooms.add(Integer.toString(room));
                }
                rooms.add("Delocalised");
                String[] adapterData = new String[rooms.size()];
                adapterData = rooms.toArray(adapterData);
                RoomSelectSpinnerAdapter roomListAdapter = new RoomSelectSpinnerAdapter(Login.this, android.R.layout.simple_spinner_dropdown_item, adapterData);
                roomList.setAdapter(roomListAdapter);
                roomList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        roomSelectSpinnerItem = position;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        roomSelectSpinnerItem = 0;
                    }
                });
                roomSelectBuilder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Spinner roomSelectSpinner = (Spinner) ((AlertDialog) dialog).findViewById(R.id.spRoomSpinner);
                        String room = (String) roomSelectSpinner.getSelectedItem();
                        if (room.contentEquals("Delocalised")) {
                            wsCM.updateRoom(-3);
                            exicsData.setRoom(-1);
                        } else {
                            wsCM.updateRoom(Integer.parseInt(room));
                            exicsData.setRoom(Integer.parseInt(room));
                        }
                        if (dialog != null) {
                            dialog.dismiss();
                            sp.edit().remove(TAG_ROOM_SELECT_SHOWING).remove(TAG_ROOM_SELECT_SELECTED).commit();
                            holdWSOpen = true;
                            Intent ExICSMain = new Intent(Login.this, ExICS_Main.class);
                            startActivity(ExICSMain);
                            finish();
                        }
                    }
                });
                roomSelectBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            if (wsCM.isConnected())
                                wsCM.disconnect();
                            dialog.dismiss();
                            sp.edit().remove(TAG_ROOM_SELECT_SHOWING).remove(TAG_ROOM_SELECT_SELECTED).commit();
                        }
                    }
                });
                roomSelectBuilder.setView(roomSelectDialogView);
                roomSelect = roomSelectBuilder.create();
                roomSelect.setCanceledOnTouchOutside(false);
                roomSelect.show();
                Log.i(TAG, "Room Select Showing onDataUpdated");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate()");

        Log.i(TAG, "Saved instance state?" + (savedInstanceState != null));

        setContentView(R.layout.activity_login);
        wsCM = wsCommunicationManager.getInstance(this);

        holdWSOpen = false;

        final SharedPreferences sp = getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);

        Boolean showPassword = sp.getBoolean(SHOW_PASSWORD_PREFERENCE, false);
        Boolean rememberCredentials = sp.getBoolean(REMEMBER_CREDENTIALS_PREFERENCE, false);

        EditText usernameBox = (EditText) findViewById(R.id.etLoginUsername);
        EditText passwordBox = (EditText) findViewById(R.id.etLoginPassword);
        CheckBox showPasswordCheckBox = (CheckBox) findViewById(R.id.cbLoginShowPassword);
        CheckBox rememberCredentialsCheckBox = (CheckBox) findViewById(R.id.cbLoginRememberCredentials);
        Button loginButton = (Button) findViewById(R.id.bLogin);

        showPasswordCheckBox.setChecked(showPassword);
        rememberCredentialsCheckBox.setChecked(rememberCredentials);

        sp.edit().remove(TAG_ENTERED_USERNAME).remove(TAG_ENTERED_PASSWORD).remove(TAG_ABOUT_SHOWING).commit();

        AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(Login.this);
        aboutBuilder.setTitle("About ExICS");
        aboutBuilder.setView(getLayoutInflater().inflate(R.layout.about, null));
        aboutDialog = aboutBuilder.create();
        aboutDialog.setCanceledOnTouchOutside(true);

        loadingSpinner = new ProgressDialog(Login.this);
        loadingSpinner.setTitle("Connecting...");
        loadingSpinner.setCanceledOnTouchOutside(false);

        if (savedInstanceState != null) {
            Log.i(TAG, "Restoring State");

            String storedUsername = savedInstanceState.getString(TAG_ENTERED_USERNAME);
            String storedPassword = savedInstanceState.getString(TAG_ENTERED_PASSWORD);
            Boolean aboutShowing = savedInstanceState.getBoolean(TAG_ABOUT_SHOWING, false);
            Boolean roomSelectedShowing = savedInstanceState.getBoolean(TAG_ROOM_SELECT_SHOWING, false);
            roomSelectSpinnerItem = savedInstanceState.getInt(TAG_ROOM_SELECT_SELECTED, 0);
            Boolean progressShowing = savedInstanceState.getBoolean(TAG_PROGRESS_SHOWING, false);
            String progressText = savedInstanceState.getString(TAG_PROGRESS_TEXT);

            passwordBox.setText(storedPassword);
            passwordBox.setSelection(passwordBox.getText().length());
            usernameBox.setText(storedUsername);
            usernameBox.setSelection(usernameBox.getText().length());
            usernameBox.requestFocus();

            if (aboutShowing) aboutDialog.show();

            if (roomSelectedShowing) {
                final AlertDialog.Builder roomSelectBuilder = new AlertDialog.Builder(Login.this);
                roomSelectBuilder.setTitle("Room Select");
                View roomSelectDialogView = getLayoutInflater().inflate(R.layout.room_select_dialog, null);
                final Spinner roomList = (Spinner) roomSelectDialogView.findViewById(R.id.spRoomSpinner);
                ArrayList<String> rooms = new ArrayList<String>();
                Set<Integer> roomsWithExams = exicsData.getAllRooms();
                for (int room : roomsWithExams) {
                    rooms.add(Integer.toString(room));
                }
                rooms.add("Delocalised");
                String[] adapterData = new String[rooms.size()];
                adapterData = rooms.toArray(adapterData);
                RoomSelectSpinnerAdapter roomListAdapter = new RoomSelectSpinnerAdapter(Login.this, android.R.layout.simple_spinner_dropdown_item, adapterData);
                roomList.setAdapter(roomListAdapter);
                roomList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        roomSelectSpinnerItem = position;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        roomSelectSpinnerItem = 0;
                    }
                });
                roomSelectBuilder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Spinner roomSelectSpinner = (Spinner) ((AlertDialog) dialog).findViewById(R.id.spRoomSpinner);
                        String room = (String) roomSelectSpinner.getSelectedItem();
                        if (room.contentEquals("Delocalised")) {
                            wsCM.updateRoom(-3);
                            exicsData.setRoom(-1);
                        } else {
                            wsCM.updateRoom(Integer.parseInt(room));
                            exicsData.setRoom(Integer.parseInt(room));
                        }
                        if (dialog != null) {
                            dialog.dismiss();
                            sp.edit().remove(TAG_ROOM_SELECT_SHOWING).remove(TAG_ROOM_SELECT_SELECTED).commit();
                            holdWSOpen = true;
                            Intent ExICSMain = new Intent(Login.this, ExICS_Main.class);
                            startActivity(ExICSMain);
                            finish();
                        }
                    }
                });
                roomSelectBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            if (wsCM.isConnected())
                                wsCM.disconnect();
                            dialog.dismiss();
                            sp.edit().remove(TAG_ROOM_SELECT_SHOWING).remove(TAG_ROOM_SELECT_SELECTED).commit();
                        }
                    }
                });
                roomSelectBuilder.setView(roomSelectDialogView);
                roomList.setSelection(roomSelectSpinnerItem);
                roomSelect = roomSelectBuilder.create();
                roomSelect.setCanceledOnTouchOutside(false);
                roomSelect.show();
                Log.i(TAG, "Room Select Showing onCreate");
            }

            if (progressShowing && wsCM.isConnected()) {
                loadingSpinnerMessage = progressText;
                loadingSpinner.setMessage(loadingSpinnerMessage);
                loadingSpinner.show();
            }

            sp.edit().remove(TAG_ABOUT_SHOWING).remove(TAG_ROOM_SELECT_SHOWING).remove(TAG_PROGRESS_SHOWING).commit();

        } else {
            if (rememberCredentials) {
                Log.i(TAG, "rememberCredentials()");
                String storedUsername = sp.getString(TAG_USERNAME, "");
                String storedPassword = sp.getString(TAG_PASSWORD, "");
                usernameBox.setText(storedUsername);
                usernameBox.setSelection(usernameBox.getText().length());
                passwordBox.setText(storedPassword);
                passwordBox.setSelection(passwordBox.getText().length());
                loginButton.requestFocus();
            } else {
                usernameBox.requestFocus();
            }
        }

        toggleShowPassword(showPassword);

        showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);
                sp.edit().putBoolean(SHOW_PASSWORD_PREFERENCE, isChecked).commit();
                toggleShowPassword(isChecked);
            }
        });

        rememberCredentialsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);
                sp.edit().putBoolean(REMEMBER_CREDENTIALS_PREFERENCE, isChecked).commit();
                if (!isChecked) {
                    sp.edit().putString(TAG_PASSWORD, "").putString(TAG_USERNAME, "").commit();
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingSpinnerMessage = "Connecting to server...";
                loadingSpinner.setMessage(loadingSpinnerMessage);

                EditText usernameBox = (EditText) findViewById(R.id.etLoginUsername);
                EditText passwordBox = (EditText) findViewById(R.id.etLoginPassword);

                SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(Login.this);
                exicsData.setServerHostname(dsp.getString(TAG_SERVER_HOSTNAME, "192.0.0.1"));
                exicsData.setServerPort(Integer.parseInt(dsp.getString(TAG_SERVER_PORT, "")));
                exicsData.setUsername(usernameBox.getText().toString());
                exicsData.setPassword(passwordBox.getText().toString());

                if (exicsData.getUsername().length() > 0 && exicsData.getPassword().length() > 0) {
                    loadingSpinner.show();
                    wsCM.connectToServer(exicsData.getServerHostname(), exicsData.getServerPort());
                } else
                    Toast.makeText(Login.this, "Please enter your IC Login Credentials", Toast.LENGTH_LONG).show();

            }
        });

        registerBroadcastReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume()");

        final SharedPreferences sp = getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);

        Boolean showPassword = sp.getBoolean(SHOW_PASSWORD_PREFERENCE, false);
        Boolean rememberCredentials = sp.getBoolean(REMEMBER_CREDENTIALS_PREFERENCE, false);
        String enteredUsername = sp.getString(TAG_ENTERED_USERNAME, "");
        String enteredPassword = sp.getString(TAG_ENTERED_PASSWORD, "");
        Boolean aboutShowing = sp.getBoolean(TAG_ABOUT_SHOWING, false);

        Boolean roomSelectedShowing = sp.getBoolean(TAG_ROOM_SELECT_SHOWING, false);
        roomSelectSpinnerItem = sp.getInt(TAG_ROOM_SELECT_SELECTED, 0);

        Boolean progressShowing = sp.getBoolean(TAG_PROGRESS_SHOWING, false);
        String progressText = sp.getString(TAG_PROGRESS_TEXT, "");

        CheckBox rememberCredentialsCheckBox = (CheckBox) findViewById(R.id.cbLoginRememberCredentials);
        CheckBox showPasswordCheckBox = (CheckBox) findViewById(R.id.cbLoginShowPassword);

        if (!enteredUsername.contentEquals("")) {
            Log.i(TAG, "enteredUsername.contentEquals(\"\") " + enteredUsername);

            EditText usernameBox = (EditText) findViewById(R.id.etLoginUsername);
            usernameBox.setText(enteredUsername);
            usernameBox.setSelection(usernameBox.getText().length());
            usernameBox.requestFocus();
        }
        if (!enteredPassword.contentEquals("")) {
            EditText passwordBox = (EditText) findViewById(R.id.etLoginPassword);
            passwordBox.setText(enteredPassword);
            passwordBox.setSelection(passwordBox.getText().length());
            passwordBox.requestFocus();
        }

        rememberCredentialsCheckBox.setChecked(rememberCredentials);
        showPasswordCheckBox.setChecked(showPassword);
        toggleShowPassword(showPassword);

        if (aboutShowing) aboutDialog.show();

        if (roomSelectedShowing) {
            final AlertDialog.Builder roomSelectBuilder = new AlertDialog.Builder(Login.this);
            roomSelectBuilder.setTitle("Room Select");
            View roomSelectDialogView = getLayoutInflater().inflate(R.layout.room_select_dialog, null);
            final Spinner roomList = (Spinner) roomSelectDialogView.findViewById(R.id.spRoomSpinner);
            ArrayList<String> rooms = new ArrayList<String>();
            Set<Integer> roomsWithExams = exicsData.getAllRooms();
            for (int room : roomsWithExams) {
                rooms.add(Integer.toString(room));
            }
            rooms.add("Delocalised");
            String[] adapterData = new String[rooms.size()];
            adapterData = rooms.toArray(adapterData);
            RoomSelectSpinnerAdapter roomListAdapter = new RoomSelectSpinnerAdapter(Login.this, android.R.layout.simple_spinner_dropdown_item, adapterData);
            roomList.setAdapter(roomListAdapter);
            roomList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    roomSelectSpinnerItem = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    roomSelectSpinnerItem = 0;
                }
            });
            roomSelectBuilder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Spinner roomSelectSpinner = (Spinner) ((AlertDialog) dialog).findViewById(R.id.spRoomSpinner);
                    String room = (String) roomSelectSpinner.getSelectedItem();
                    if (room.contentEquals("Delocalised")) {
                        wsCM.updateRoom(-3);
                        exicsData.setRoom(-1);
                    } else {
                        wsCM.updateRoom(Integer.parseInt(room));
                        exicsData.setRoom(Integer.parseInt(room));
                    }
                    if (dialog != null) {
                        dialog.dismiss();
                        sp.edit().remove(TAG_ROOM_SELECT_SHOWING).remove(TAG_ROOM_SELECT_SELECTED).commit();
                        holdWSOpen = true;
                        Intent ExICSMain = new Intent(Login.this, ExICS_Main.class);
                        startActivity(ExICSMain);
                        finish();
                    }
                }
            });
            roomSelectBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (dialog != null) {
                        if (wsCM.isConnected())
                            wsCM.disconnect();
                        dialog.dismiss();
                        sp.edit().remove(TAG_ROOM_SELECT_SHOWING).remove(TAG_ROOM_SELECT_SELECTED).commit();
                    }
                }
            });
            roomSelectBuilder.setView(roomSelectDialogView);
            roomList.setSelection(roomSelectSpinnerItem);
            roomSelect = roomSelectBuilder.create();
            roomSelect.setCanceledOnTouchOutside(false);
            roomSelect.show();
            Log.i(TAG, "Room Select Showing onResume");
        }

        if (progressShowing && wsCM.isConnected()) {
            loadingSpinner.setMessage(progressText);
            loadingSpinner.show();
        }
    }

    @Override
    public void onBackPressed() {
        if (wsCM.isConnected())
            wsCM.disconnect();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");

        SharedPreferences sp = getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);
        EditText passwordBox = (EditText) findViewById(R.id.etLoginPassword);
        EditText usernameBox = (EditText) findViewById(R.id.etLoginUsername);
        sp.edit().putString(TAG_ENTERED_PASSWORD, passwordBox.getText().toString())
                .putString(TAG_ENTERED_USERNAME, usernameBox.getText().toString())
                .putBoolean(TAG_ABOUT_SHOWING, aboutDialog != null && aboutDialog.isShowing())
                .putBoolean(TAG_ROOM_SELECT_SHOWING, roomSelect != null && roomSelect.isShowing())
                .putInt(TAG_ROOM_SELECT_SELECTED, roomSelectSpinnerItem)
                .putBoolean(TAG_PROGRESS_SHOWING, loadingSpinner != null && loadingSpinner.isShowing())
                .putString(TAG_PROGRESS_TEXT, loadingSpinnerMessage)
                .commit();

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");

        EditText passwordBox = (EditText) findViewById(R.id.etLoginPassword);
        EditText usernameBox = (EditText) findViewById(R.id.etLoginUsername);
        outState.putString(TAG_ENTERED_PASSWORD, passwordBox.getText().toString());
        outState.putString(TAG_ENTERED_USERNAME, usernameBox.getText().toString());
        outState.putBoolean(TAG_ABOUT_SHOWING, aboutDialog != null && aboutDialog.isShowing());
        outState.putBoolean(TAG_ROOM_SELECT_SHOWING, roomSelect != null && roomSelect.isShowing());
        outState.putInt(TAG_ROOM_SELECT_SELECTED, roomSelectSpinnerItem);
        outState.putBoolean(TAG_PROGRESS_SHOWING, loadingSpinner != null && loadingSpinner.isShowing());
        outState.putString(TAG_PROGRESS_TEXT, loadingSpinnerMessage);

        holdWSOpen = true;

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");

        SharedPreferences sp = getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);
        sp.edit().remove(TAG_ENTERED_USERNAME).remove(TAG_ENTERED_PASSWORD).remove(TAG_ABOUT_SHOWING).commit();

        if (aboutDialog != null && aboutDialog.isShowing()) aboutDialog.dismiss();
        if (loadingSpinner != null && loadingSpinner.isShowing()) loadingSpinner.dismiss();
        if (roomSelect != null && roomSelect.isShowing()) roomSelect.dismiss();

        unregisterBroadcastReceivers();

        if (wsCM.isConnected() && !(holdWSOpen))
            wsCM.disconnect();

        super.onDestroy();
    }

    private void toggleShowPassword(Boolean showPassword) {
        EditText passwordBox = (EditText) findViewById(R.id.etLoginPassword);
        if (showPassword) {
            passwordBox.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordBox.setSelection(passwordBox.getText().length());
        } else {
            passwordBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordBox.setSelection(passwordBox.getText().length());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(Login.this, Login_Settings.class);
                startActivityForResult(settingsIntent, LOGIN_SETTINGS);
                return true;

            case R.id.action_about:
                aboutDialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LOGIN_SETTINGS:
                Toast.makeText(Login.this, "Settings Updated", Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(Login.this, "Ruh-roh... What's happened here!?", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void registerBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(onAuthSuccessful, new IntentFilter(BroadcastTags.TAG_AUTH_SUCCESSFUL));
        LocalBroadcastManager.getInstance(this).registerReceiver(onDataUpdated, new IntentFilter(BroadcastTags.TAG_DATA_UPDATED));
        LocalBroadcastManager.getInstance(this).registerReceiver(onFailure, new IntentFilter(BroadcastTags.TAG_FAILURE_OCCURRED));
        LocalBroadcastManager.getInstance(this).registerReceiver(onConnectionClosed, new IntentFilter(BroadcastTags.TAG_CONNECTION_CLOSED));

    }

    private void unregisterBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onAuthSuccessful);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onDataUpdated);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onFailure);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onConnectionClosed);
    }
}
