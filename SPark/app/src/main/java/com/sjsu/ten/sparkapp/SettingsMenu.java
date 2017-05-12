package com.sjsu.ten.sparkapp;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by gagan on 1/8/2017.
 */

public class SettingsMenu extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            Log.d("Log", "BACK PRESSED!!!");
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
