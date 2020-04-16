package me.yohanesgultom.sms2email;


import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {

    public static final String PREFS_NAME = "SMS2EMAIL_CONFIG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }
}
