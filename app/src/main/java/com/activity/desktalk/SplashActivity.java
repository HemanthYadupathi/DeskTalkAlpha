package com.activity.desktalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.utils.desktalk.Constants;

public class SplashActivity extends AppCompatActivity {

    private Thread mSplashThread;
    private SharedPreferences sharedpreferences;
    private final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The thread to wait for splash screen events
        mSplashThread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        // Wait given period of time or exit on touch
                        wait(1500);
                    }
                } catch (InterruptedException ex) {
                }

                finish();
                launchNextActivity();
            }
        };

        mSplashThread.start();


    }

    private void launchNextActivity() {
        Class nextActivity = LoginActivity.class;
        sharedpreferences = getSharedPreferences(Constants.PREFERENCE_LOGIN_DETAILS, Context.MODE_PRIVATE);
        if (sharedpreferences != null) {
            String username = sharedpreferences.getString(Constants.PREFERENCE_KEY_USER_NAME, "");
            String pwd = sharedpreferences.getString(Constants.PREFERENCE_KEY_USER_PWD, "");
            String deviceToken = sharedpreferences.getString(Constants.PREFERENCE_KEY_DEVICE_TOKEN, "");

            if (username != null && (!username.contentEquals("")) && pwd != null && (!pwd.contentEquals(""))
                    && deviceToken != null && (!deviceToken.contentEquals(""))) {
                Log.i(TAG, "USER Logged IN " + username + " " + deviceToken);
                nextActivity = AutoLoginActivity.class;
            }
        }
        Intent intent = new Intent();
        intent.setClass(SplashActivity.this, nextActivity);
        startActivity(intent);
    }
}
