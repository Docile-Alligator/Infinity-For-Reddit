package ml.docilealligator.infinityforreddit.settings;

import android.os.Bundle;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;

public class CommentPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.comment_preferences, rootKey);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }
    }
}