package ml.docilealligator.infinityforreddit.settings;

import android.os.Bundle;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;

public class NumberOfColumnsInPostFeedPreferenceFragment extends CustomFontPreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.number_of_columns_in_post_feed_preferences, rootKey);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }
    }
}
