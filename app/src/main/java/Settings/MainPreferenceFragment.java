package Settings;


import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import ml.docilealligator.infinityforreddit.R;

/**
 * A simple {@link PreferenceFragmentCompat} subclass.
 */
public class MainPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey);
    }
}
