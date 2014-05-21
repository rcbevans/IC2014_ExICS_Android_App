package rce10.ic.ac.uk.exics.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import rce10.ic.ac.uk.exics.R;


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

    private static final int LOGIN_SETTINGS = 0;

    private Dialog aboutDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate()");

        Log.i(TAG, "Saved instance state?" + (savedInstanceState != null));

        setContentView(R.layout.activity_login);

        SharedPreferences sp = getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);

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

        aboutDialog = new Dialog(Login.this);
        aboutDialog.setContentView(R.layout.about);
        aboutDialog.setTitle("About ExICS");
        aboutDialog.setCanceledOnTouchOutside(true);

        if (savedInstanceState != null) {
            Log.i(TAG, "Restoring State");
            String storedUsername = savedInstanceState.getString(TAG_ENTERED_USERNAME);
            String storedPassword = savedInstanceState.getString(TAG_ENTERED_PASSWORD);
            Boolean aboutShowing = savedInstanceState.getBoolean(TAG_ABOUT_SHOWING);

            passwordBox.setText(storedPassword);
            passwordBox.setSelection(passwordBox.getText().length());
            usernameBox.setText(storedUsername);
            usernameBox.setSelection(usernameBox.getText().length());
            usernameBox.requestFocus();

            if (aboutShowing) aboutDialog.show();

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
                Toast.makeText(getApplicationContext(), "Not yet implemented... Come back later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume()");

        SharedPreferences sp = getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);

        Boolean showPassword = sp.getBoolean(SHOW_PASSWORD_PREFERENCE, false);
        Boolean rememberCredentials = sp.getBoolean(REMEMBER_CREDENTIALS_PREFERENCE, false);
        String enteredUsername = sp.getString(TAG_ENTERED_USERNAME, "");
        String enteredPassword = sp.getString(TAG_ENTERED_PASSWORD, "");
        Boolean aboutShowing = sp.getBoolean(TAG_ABOUT_SHOWING, false);

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
                .commit();

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        EditText passwordBox = (EditText) findViewById(R.id.etLoginPassword);
        EditText usernameBox = (EditText) findViewById(R.id.etLoginUsername);
        outState.putString(TAG_ENTERED_PASSWORD, passwordBox.getText().toString());
        outState.putString(TAG_ENTERED_USERNAME, usernameBox.getText().toString());
        outState.putBoolean(TAG_ABOUT_SHOWING, aboutDialog != null && aboutDialog.isShowing());

        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");

        SharedPreferences sp = getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);
        sp.edit().remove(TAG_ENTERED_USERNAME).remove(TAG_ENTERED_PASSWORD).remove(TAG_ABOUT_SHOWING).commit();

        if (aboutDialog != null && aboutDialog.isShowing()) aboutDialog.dismiss();

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
}
