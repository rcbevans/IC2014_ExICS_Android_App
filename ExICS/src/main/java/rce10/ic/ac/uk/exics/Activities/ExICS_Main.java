package rce10.ic.ac.uk.exics.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import rce10.ic.ac.uk.exics.Fragments.NavigationDrawerFragment;
import rce10.ic.ac.uk.exics.Model.ExICSData;
import rce10.ic.ac.uk.exics.R;
import rce10.ic.ac.uk.exics.Utilities.wsCommunicationManager;

public class ExICS_Main extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

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
        confirmQuitDialog = confirmBuilder.create();

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
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
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
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
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
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.ex_ics__main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_exics_main_overview, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((ExICS_Main) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
