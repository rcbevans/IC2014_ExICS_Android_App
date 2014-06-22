package rce10.ic.ac.uk.exics.Activities;

import android.animation.Animator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import rce10.ic.ac.uk.exics.Adapters.PresetMessagesSpinnerAdapter;
import rce10.ic.ac.uk.exics.Adapters.RoomSelectSpinnerAdapter;
import rce10.ic.ac.uk.exics.Fragments.LogHistoryFragment;
import rce10.ic.ac.uk.exics.Fragments.NavigationDrawerFragment;
import rce10.ic.ac.uk.exics.Fragments.PlaceholderFragment;
import rce10.ic.ac.uk.exics.Fragments.RoomListFragment;
import rce10.ic.ac.uk.exics.Fragments.SeatingPlanFragment;
import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Child_Fragment_Interface;
import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Fragment_Interface;
import rce10.ic.ac.uk.exics.Model.BroadcastTags;
import rce10.ic.ac.uk.exics.Model.ExICSMessage;
import rce10.ic.ac.uk.exics.Model.ExICSPresetMessages;
import rce10.ic.ac.uk.exics.Model.ExICSProtocol;
import rce10.ic.ac.uk.exics.Model.PresetMessage;
import rce10.ic.ac.uk.exics.R;
import rce10.ic.ac.uk.exics.Utilities.OnSwipeTouchListener;
import rce10.ic.ac.uk.exics.Utilities.wsCommunicationManager;
import rce10.ic.ac.uk.exics.ViewModel.ExICSData;

public class ExICS_Main extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, ExICS_Main_Fragment_Interface {

    private static final String TAG_CHAT_FRAGMENT = "CHAT_FRAGMENT";
    private static final String TAG_ROOM_LIST_FRAGMENT = "ROOM_LIST_FRAGMENT";
    private static final String TAG_SEATING_PLAN_FRAGMENT = "SEATING_PLAN_FRAGMENT";

    private static final String TAG = ExICS_Main.class.getName();
    private BroadcastReceiver onChatLogUpdated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                ((ExICS_Main_Child_Fragment_Interface) getFragmentManager().findFragmentById(R.id.flChatWindow)).refreshView();
            } catch (ClassCastException e) {
                Log.e(TAG, "Child Fragments Must Implement ExICS_Main_Child_Fragment_Interface", e);
            }
        }
    };
    private static final String TAG_EXICS_MAIN_SHARED_PREFS = "EXICS_MAIN_SHARED_PREFS";
    private static final String TAG_CONFIRM_QUIT_SHOWING = "CONFIRM_QUIT_SHOWING";
    private static final String TAG_PROGRESS_SHOWING = "PROGRESS_SHOWING";
    private static final String TAG_PROGRESS_TEXT = "PROGRESS_TEXT";
    private static ExICSData exicsData = ExICSData.getInstance();
    private static wsCommunicationManager wsCM = null;
    private static String loadingSpinnerMessage = "";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private AlertDialog confirmQuitDialog;
    private Boolean holdWSOpen = false;
    private Boolean quitting = false;
    private Boolean chatPaneShowing = false;
    private Dialog roomSelectDialog = null;
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
            Log.i(TAG, "Received Broadcast onFailure");
            if (loadingSpinner != null && loadingSpinner.isShowing()) {
                loadingSpinner.dismiss();
                String reason = intent.getStringExtra(ExICSProtocol.TAG_REASON);
                Toast.makeText(ExICS_Main.this, "A Failure has occurred... Reason Given: " + reason, Toast.LENGTH_LONG).show();
                quitToLogin();
            }
            String reason = intent.getStringExtra(ExICSProtocol.TAG_REASON);
            Toast.makeText(ExICS_Main.this, "A Failure has occurred... Reason Given: " + reason, Toast.LENGTH_LONG).show();
        }
    };
    private BroadcastReceiver onConnectionClosed = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received Broadcast onConnectionClosed");
            if (loadingSpinner != null && loadingSpinner.isShowing()) {
                loadingSpinner.dismiss();
                String reason = intent.getStringExtra(ExICSProtocol.TAG_REASON);
                Toast.makeText(ExICS_Main.this, "The connection to the server could not be reopened... Reason Given: " + reason, Toast.LENGTH_LONG).show();
                quitToLogin();
            }
            if (!quitting) {
                Toast.makeText(ExICS_Main.this, "The connection has dropped... Attempting to reconnect...", Toast.LENGTH_LONG).show();
                attemptWSReconnect();
            }
        }
    };
    private BroadcastReceiver onDataUpdated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received Broadcast onDataUpdated");

            if (loadingSpinner != null && loadingSpinner.isShowing()) {
                loadingSpinner.dismiss();
                Toast.makeText(ExICS_Main.this, "Successfully restored connection to the server", Toast.LENGTH_LONG).show();
            }

            try {
                ((ExICS_Main_Child_Fragment_Interface) getFragmentManager().findFragmentById(R.id.flMainContent)).refreshView();
            } catch (ClassCastException e) {
                Log.e(TAG, "Child Fragments Must Implement ExICS_Main_Child_Fragment_Interface", e);
            }
        }
    };

    private ArrayList<ExICSMessage> messages = new ArrayList<ExICSMessage>();
    private AlertDialog messageAlertDialog;
    private BroadcastReceiver onNewMessageReceived = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onNewMessageReceived");
            String sender = intent.getStringExtra(ExICSProtocol.TAG_SENDER);
            int room = intent.getIntExtra(ExICSProtocol.TAG_ROOM, -1);
            Calendar timeReceived = (Calendar) intent.getSerializableExtra(ExICSProtocol.TAG_TIME);
            String message = intent.getStringExtra(ExICSProtocol.TAG_MESSAGE);
            ExICSMessage msgObject = new ExICSMessage(sender, room, timeReceived, message);
            messages.add(msgObject);
            if ((messageAlertDialog == null) || (messageAlertDialog != null && !(messageAlertDialog.isShowing()))) {
                messageAlertDialog = createMessageAlert(msgObject);
                messageAlertDialog.show();
                wakeDevice();
            }
        }
    };
    private PowerManager.WakeLock fullWakeLock, partialWakeLock;

    private void removeExICSMessage(ExICSMessage msg) {
        messages.remove(msg);
    }

    private AlertDialog createMessageAlert(final ExICSMessage message) {
        AlertDialog.Builder messageAlertBuilder = new AlertDialog.Builder(this);
        messageAlertBuilder.setTitle("Message Received");
        View messageAlertView = getLayoutInflater().inflate(R.layout.message_alert_dialog, null, false);
        TextView tvSender = (TextView) messageAlertView.findViewById(R.id.tvMessageAlertSender);
        TextView tvRoom = (TextView) messageAlertView.findViewById(R.id.tvMessageAlertRoom);
        TextView tvMessage = (TextView) messageAlertView.findViewById(R.id.tvMessageAlertMessage);
        final TextView tvPreview = (TextView) messageAlertView.findViewById(R.id.tvMessageAlertPreview);
        final Spinner spReplyMessage = (Spinner) messageAlertView.findViewById(R.id.spMessageAlertReply);
        final EditText etCustomReply = (EditText) messageAlertView.findViewById(R.id.etMessageAlertCustomResponse);

        final LinearLayout llPresetPreview = (LinearLayout) messageAlertView.findViewById(R.id.llMessageAlertMessagePreview);
        final LinearLayout llCustomMessageBox = (LinearLayout) messageAlertView.findViewById(R.id.llMessageAlertCustomReply);

        llCustomMessageBox.setVisibility(View.GONE);
        llPresetPreview.setVisibility(View.VISIBLE);

        tvSender.setText(message.getSender());
        tvRoom.setText(String.valueOf(message.getRoom()));
        tvMessage.setText(message.getMessage());

        ArrayList<PresetMessage> presetResponses = new ExICSPresetMessages().getPresetResponses();
        PresetMessage[] responses = new PresetMessage[presetResponses.size()];
        responses = presetResponses.toArray(responses);
        spReplyMessage.setAdapter(new PresetMessagesSpinnerAdapter(this, android.R.layout.simple_spinner_item, responses));
        spReplyMessage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PresetMessage selected = (PresetMessage) parent.getSelectedItem();

                if (selected.getTitle().contentEquals("Custom")) {
                    llCustomMessageBox.setVisibility(View.VISIBLE);
                    llPresetPreview.setVisibility(View.GONE);
                } else {
                    llCustomMessageBox.setVisibility(View.GONE);
                    llPresetPreview.setVisibility(View.VISIBLE);
                    tvPreview.setText(selected.getMessage());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                llCustomMessageBox.setVisibility(View.GONE);
                llPresetPreview.setVisibility(View.VISIBLE);
                tvPreview.setText("Please Select A Response Type Above");
            }
        });
        messageAlertBuilder.setView(messageAlertView);
        messageAlertBuilder.setCancelable(false);
        messageAlertBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                removeExICSMessage(message);
                if (messages.size() > 0) {
                    messageAlertDialog = createMessageAlert(messages.get(0));
                    messageAlertDialog.show();
                }
            }
        });
        messageAlertBuilder.setPositiveButton("Reply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PresetMessage selected = (PresetMessage) spReplyMessage.getSelectedItem();
                String response;
                if (selected.getTitle().contentEquals("Custom")) {
                    response = etCustomReply.getText().toString();
                } else {
                    response = selected.getMessage();
                }
                if (response.length() > 0) {
                    dialog.dismiss();
                    wsCM.sendMessageToUser(message.getSender(), response);
                    removeExICSMessage(message);
                    if (messages.size() > 0) {
                        messageAlertDialog = createMessageAlert(messages.get(0));
                        messageAlertDialog.show();
                    }
                } else {
                    Toast.makeText(ExICS_Main.this, "Please enter a valid message to be send", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return messageAlertBuilder.create();
    }

    protected void createWakeLocks() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        fullWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "Loneworker - FULL WAKE LOCK");
        partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Loneworker - PARTIAL WAKE LOCK");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_ex_ics_main);

        createWakeLocks();

        wsCM = wsCM.getInstance(ExICS_Main.this);

        final SharedPreferences sp = getSharedPreferences(TAG_EXICS_MAIN_SHARED_PREFS, MODE_PRIVATE);

        loadingSpinner = new ProgressDialog(ExICS_Main.this);
        loadingSpinner.setTitle("Connecting...");
        loadingSpinner.setCanceledOnTouchOutside(false);

        confirmQuitDialog = createQuitConfirmationDialog(savedInstanceState);

        attachFragmentSwipeListeners();

        if (savedInstanceState != null) {
            Boolean confirmQuitShowing = savedInstanceState.getBoolean(TAG_CONFIRM_QUIT_SHOWING, false);

            Boolean progressShowing = savedInstanceState.getBoolean(TAG_PROGRESS_SHOWING, false);
            String progressText = savedInstanceState.getString(TAG_PROGRESS_TEXT);

            if (confirmQuitShowing) {
                confirmQuitDialog.show();
                sp.edit().remove(TAG_CONFIRM_QUIT_SHOWING).commit();
            }

            if (progressShowing && wsCM.isConnected()) {
                loadingSpinnerMessage = progressText;
                loadingSpinner.setMessage(loadingSpinnerMessage);
                loadingSpinner.show();
            }
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        setUpChatWindow();

        registerBroadcastReceivers();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");

        if (fullWakeLock != null && fullWakeLock.isHeld()) {
            fullWakeLock.release();
        }
        if (partialWakeLock != null && partialWakeLock.isHeld()) {
            partialWakeLock.release();
        }

        SharedPreferences sp = getSharedPreferences(TAG_EXICS_MAIN_SHARED_PREFS, MODE_PRIVATE);

        Boolean progressShowing = sp.getBoolean(TAG_PROGRESS_SHOWING, false);
        String progressText = sp.getString(TAG_PROGRESS_TEXT, "");

        if (progressShowing && wsCM.isConnected()) {
            loadingSpinnerMessage = progressText;
            loadingSpinner.setMessage(loadingSpinnerMessage);
            loadingSpinner.show();
        }

        if (!wsCM.isConnected()) {
            if (loadingSpinner != null && loadingSpinner.isShowing()) {
                loadingSpinner.dismiss();
            }
            attemptWSReconnect();
        }

        if (confirmQuitDialog == null)
            confirmQuitDialog = createQuitConfirmationDialog(null);

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed() Backstack: " + getFragmentManager().getBackStackEntryCount());
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0 && !chatPaneShowing) {
            fm.popBackStack();
        } else {
            if (confirmQuitDialog != null && !(confirmQuitDialog.isShowing()))
                confirmQuitDialog.show();
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        unregisterBroadcastReceivers();
        if (!holdWSOpen) {
            if (wsCM.isConnected())
                wsCM.disconnect();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");

        outState.putBoolean(TAG_CONFIRM_QUIT_SHOWING, confirmQuitDialog != null && confirmQuitDialog.isShowing());

        holdWSOpen = true;

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        clearFragmentBackStack(fragmentManager);
        if (position == 0) {
            fragmentManager.beginTransaction()
                    .replace(R.id.flMainContent, RoomListFragment.newInstance(), TAG_ROOM_LIST_FRAGMENT)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        } else if (position == 1) {
            fragmentManager.beginTransaction()
                    .replace(R.id.flMainContent, SeatingPlanFragment.newInstance(), TAG_SEATING_PLAN_FRAGMENT)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.flMainContent, PlaceholderFragment.newInstance(position + 1))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }

        if (chatPaneShowing) {
            hideChatLog();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = "Overview";
                break;
            case 2:
                mTitle = "SeatingPlan";
                break;
            case 3:
                mTitle = "Invigilation Plan";
                break;
            default:
                mTitle = "King Potato of the clans";
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.ex_ics_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.change_room) {
            roomSelectDialog = createRoomSelectDialog();
            roomSelectDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Dialog createRoomSelectDialog() {
        final AlertDialog.Builder roomSelectBuilder = new AlertDialog.Builder(ExICS_Main.this);
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
        RoomSelectSpinnerAdapter roomListAdapter = new RoomSelectSpinnerAdapter(ExICS_Main.this, android.R.layout.simple_spinner_dropdown_item, adapterData);
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
            }
        });
        roomSelectBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        roomSelectBuilder.setView(roomSelectDialogView);
        Dialog roomSelect = roomSelectBuilder.create();
        roomSelect.setCanceledOnTouchOutside(false);
        return roomSelect;
    }

    private void showChatLog() {
        final View contentWindow = findViewById(R.id.flMainContent);
        View chatWindow = findViewById(R.id.flChatWindow);

        chatWindow.setAlpha(0f);
        chatWindow.setVisibility(View.VISIBLE);

        chatWindow.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null);

        contentWindow.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        contentWindow.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
        chatPaneShowing = true;
    }

    private void hideChatLog() {
        final View contentWindow = findViewById(R.id.flMainContent);
        final View chatWindow = findViewById(R.id.flChatWindow);

        contentWindow.setAlpha(0f);
        contentWindow.setVisibility(View.VISIBLE);

        contentWindow.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null);

        chatWindow.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        chatWindow.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
        chatPaneShowing = false;
    }

    private AlertDialog createQuitConfirmationDialog(Bundle savedInstanceState) {
        final SharedPreferences sp = getSharedPreferences(TAG_EXICS_MAIN_SHARED_PREFS, MODE_PRIVATE);

        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(ExICS_Main.this);
        confirmBuilder.setTitle("Quit?");
        confirmBuilder.setMessage("Would you really like to quit the application?  Your connection to the server will be closed...");
        confirmBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                sp.edit().remove(TAG_CONFIRM_QUIT_SHOWING).commit();
            }
        });
        confirmBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                quitting = true;
                if (wsCM.isConnected())
                    wsCM.disconnect();
                sp.edit().remove(TAG_CONFIRM_QUIT_SHOWING).commit();
                Intent Login = new Intent(ExICS_Main.this, rce10.ic.ac.uk.exics.Activities.Login.class);
                startActivity(Login);
                finish();
            }
        });
        return confirmBuilder.create();
    }

    private void attachFragmentSwipeListeners() {
        int screenOrientation = getResources().getConfiguration().orientation;
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i(TAG, "Attaching Fragment Swipe Listeners");
            FrameLayout mainContent = (FrameLayout) findViewById(R.id.flMainContent);
            mainContent.setOnTouchListener(new OnSwipeTouchListener(ExICS_Main.this) {
                @Override
                public void onSwipeLeft() {
                    showChatLog();
                    super.onSwipeLeft();
                }
            });

            FrameLayout chatWindow = (FrameLayout) findViewById(R.id.flChatWindow);
            chatWindow.setOnTouchListener(new OnSwipeTouchListener(ExICS_Main.this) {
                @Override
                public void onSwipeRight() {
                    hideChatLog();
                    super.onSwipeRight();
                }
            });

        }
    }

    private void setUpChatWindow() {
        Fragment chatFragment = getFragmentManager().findFragmentByTag(TAG_CHAT_FRAGMENT);
        if (chatFragment == null || !(chatFragment instanceof LogHistoryFragment)) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.flChatWindow, LogHistoryFragment.newInstance(), TAG_CHAT_FRAGMENT)
                    .commit();
        }
    }

    private void attemptWSReconnect() {
        loadingSpinner.setMessage("Attemping to reconnect...");
        loadingSpinner.show();
        wsCM.connectToServer(exicsData.getServerHostname(), exicsData.getServerPort());
    }

    private void registerBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(onAuthSuccessful, new IntentFilter(BroadcastTags.TAG_AUTH_SUCCESSFUL));
        LocalBroadcastManager.getInstance(this).registerReceiver(onDataUpdated, new IntentFilter(BroadcastTags.TAG_DATA_UPDATED));
        LocalBroadcastManager.getInstance(this).registerReceiver(onChatLogUpdated, new IntentFilter(BroadcastTags.TAG_LOG_UPDATED));
        LocalBroadcastManager.getInstance(this).registerReceiver(onFailure, new IntentFilter(BroadcastTags.TAG_FAILURE_OCCURRED));
        LocalBroadcastManager.getInstance(this).registerReceiver(onConnectionClosed, new IntentFilter(BroadcastTags.TAG_CONNECTION_CLOSED));
        LocalBroadcastManager.getInstance(this).registerReceiver(onNewMessageReceived, new IntentFilter(BroadcastTags.TAG_MESSAGE_RECEIVED));

    }

    private void unregisterBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onAuthSuccessful);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onDataUpdated);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onChatLogUpdated);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onFailure);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onConnectionClosed);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNewMessageReceived);
    }

    private void quitToLogin() {
        Intent backToLogin = new Intent(ExICS_Main.this, Login.class);
        startActivity(backToLogin);
        finish();
    }

    private void clearFragmentBackStack(FragmentManager fm) {
        fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void updateContentFragment(Fragment frag) {
        Log.i(TAG, "updateContentFragment" + frag.toString());
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.flMainContent, frag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .commit();
    }

    public void wakeDevice() {
        fullWakeLock.acquire();

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
        keyguardLock.disableKeyguard();
    }

    @Override
    public void fragmentViewUnavailable() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0 && !chatPaneShowing) {
            fm.popBackStack();
        } else {
            onNavigationDrawerItemSelected(mNavigationDrawerFragment.getCurrentSelectedPosition());
        }
    }

    @Override
    public Context getActivityContext() {
        return this;
    }

    @Override
    public void onFragmentSwipedLeft() {
        Log.i(TAG, "onFragmentSwipedLeft");
        int screenOrientation = getResources().getConfiguration().orientation;
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!chatPaneShowing) {
                showChatLog();
            }
        }
    }

    @Override
    public void onFragmentSwipedRight() {
        Log.i(TAG, "onFragmentSwipedRight");
        int screenOrientation = getResources().getConfiguration().orientation;
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (chatPaneShowing) {
                hideChatLog();
            }
        }
    }
}
