package ml.docilealligator.infinityforreddit.settings;

import android.os.Bundle;

import androidx.preference.PreferenceManager;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class PostDetailsPreferenceFragment extends CustomFontPreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(SharedPreferencesUtils.POST_DETAILS_SHARED_PREFERENCES_FILE);
        setPreferencesFromResource(R.xml.post_details_preferences, rootKey);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }
    }
}
