package com.clevergo.vcode;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    public static boolean refresh = false;
    public static boolean _updateInfo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.setting));
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private SharedPreferences sharedPreferences;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
            sharedPreferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
                if (key.equals("pref_codeviewThemes") ||
                        key.equals("pref_wrapLines_codeView") ||
                        key.equals("pref_pinchZoom") ||
                        key.equals("pref_lineNumber_codeView") ||
                        key.equals("pref_textSize_codeView")) refresh = true;

                if(key.equals("pref_organizeFileInfo")) _updateInfo = true;
            });
        }
    }
}
