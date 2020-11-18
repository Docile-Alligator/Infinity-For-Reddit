package ml.docilealligator.infinityforreddit.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import ml.docilealligator.infinityforreddit.activities.SubredditFilterPopularAndAllActivity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class FilterPreferenceFragment extends PreferenceFragmentCompat {

    private Activity activity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.filter_preferences, rootKey);

        Preference subredditFilterPopularAndAllPreference = findPreference(SharedPreferencesUtils.SUBREDDIT_FILTER_POPULAR_AND_ALL);

        subredditFilterPopularAndAllPreference.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(activity, SubredditFilterPopularAndAllActivity.class);
            activity.startActivity(intent);
            return true;
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}