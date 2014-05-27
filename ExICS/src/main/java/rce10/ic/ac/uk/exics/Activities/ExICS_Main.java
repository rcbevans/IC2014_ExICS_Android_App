package rce10.ic.ac.uk.exics.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import rce10.ic.ac.uk.exics.Fragments.ExICS_Log_History;
import rce10.ic.ac.uk.exics.Fragments.NavigationDrawerFragment;
import rce10.ic.ac.uk.exics.Fragments.PlaceholderFragment;
import rce10.ic.ac.uk.exics.Model.ExICSData;
import rce10.ic.ac.uk.exics.R;
import rce10.ic.ac.uk.exics.Utilities.OnSwipeTouchListener;
import rce10.ic.ac.uk.exics.Utilities.wsCommunicationManager;

public class ExICS_Main extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final String TAG_CHAT_FRAGMENT = "CHAT_FRAGMENT";

    private static final String TAG = ExICS_Main.class.getName();
    private static final String TAG_EXICS_MAIN_SHARED_PREFS = "EXICS_MAIN_SHARED_PREFS";
    private static final String TAG_CONFIRM_QUIT_SHOWING = "CONFIRM_QUIT_SHOWING";
    private static ExICSData exicsData = ExICSData.getInstance();
    private static wsCommunicationManager wsCM = null;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_ex_ics_main);

        wsCM = wsCM.getInstance(ExICS_Main.this);

        final SharedPreferences sp = getSharedPreferences(TAG_EXICS_MAIN_SHARED_PREFS, MODE_PRIVATE);

        confirmQuitDialog = createQuitConfirmationDialog(savedInstanceState);

        attachFragmentSwipeListeners();

        if (savedInstanceState != null) {
            //TO_DO
            Boolean confirmQuitShowing = savedInstanceState.getBoolean(TAG_CONFIRM_QUIT_SHOWING, false);

            if (confirmQuitShowing) {
                confirmQuitDialog.show();
                sp.edit().remove(TAG_CONFIRM_QUIT_SHOWING).commit();
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
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");

        if (confirmQuitDialog == null)
            confirmQuitDialog = createQuitConfirmationDialog(null);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed()");
        if (confirmQuitDialog != null && !(confirmQuitDialog.isShowing()))
            confirmQuitDialog.show();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
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
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.flMainContent, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChatLog() {
        View contentWindow = findViewById(R.id.flMainContent);
        View chatWindow = findViewById(R.id.flChatWindow);
        contentWindow.setVisibility(View.GONE);
        chatWindow.setVisibility(View.VISIBLE);
    }

    private void hideChatLog() {
        View contentWindow = findViewById(R.id.flMainContent);
        View chatWindow = findViewById(R.id.flChatWindow);
        contentWindow.setVisibility(View.VISIBLE);
        chatWindow.setVisibility(View.GONE);
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
            View mainContent = findViewById(R.id.flMainContent);
            mainContent.setOnTouchListener(new OnSwipeTouchListener(ExICS_Main.this) {
                @Override
                public void onSwipeLeft() {
                    showChatLog();
                    super.onSwipeLeft();
                }
            });

            View chatWindow = findViewById(R.id.flChatWindow);
            chatWindow.setOnTouchListener(new OnSwipeTouchListener(ExICS_Main.this) {
                @Override
                public void onSwipeRight() {
                    hideChatLog();
                    super.onSwipeRight();
                }
            });

        } else {
            View mainContent = findViewById(R.id.flMainContent);
            mainContent.setOnTouchListener(new OnSwipeTouchListener(ExICS_Main.this) {
                @Override
                public void onSwipeLeft() {
                    showChatLog();
                    super.onSwipeLeft();
                }
            });

            View chatWindow = findViewById(R.id.flChatWindow);
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
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.flChatWindow, ExICS_Log_History.newInstance(), TAG_CHAT_FRAGMENT)
                .commit();
    }
}
