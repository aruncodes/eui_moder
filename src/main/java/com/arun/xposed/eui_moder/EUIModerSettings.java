package com.arun.xposed.eui_moder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class EUIModerSettings extends PreferenceActivity {


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {

        getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.prefs);

        SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                      String key) {

                    if(key.equals("set_standard_dpi")) {
                        boolean std_mode = sharedPreferences.getBoolean(key, false);
                        try {
                            Process su = Runtime.getRuntime().exec("su");
                            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

                            if(std_mode) {
                                outputStream.writeBytes("/system/bin/wm density 420 \n");
                            }
                            else {
                                outputStream.writeBytes("/system/bin/wm density 480 \n");
                            }
                            outputStream.writeBytes("exit \n");
                            outputStream.flush();

                            su.waitFor();

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),"DPI Changing failed!",Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }

                    Toast.makeText(getApplicationContext(),"Settings updated! Please reboot to see changes.",Toast.LENGTH_SHORT).show();
                }
            };

        PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).registerOnSharedPreferenceChangeListener(spChanged);
    }

}
