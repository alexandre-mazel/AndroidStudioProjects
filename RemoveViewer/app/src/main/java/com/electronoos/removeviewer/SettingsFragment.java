package com.electronoos.removeviewer;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by AMazel on 20/12/15.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onPause() {
        // doesn't hide the stuffs...
        super.onDestroy();
    }
}