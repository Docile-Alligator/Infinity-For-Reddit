package Settings;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;

import ml.docilealligator.infinityforreddit.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.notification_preferences, rootKey);
    }
}
