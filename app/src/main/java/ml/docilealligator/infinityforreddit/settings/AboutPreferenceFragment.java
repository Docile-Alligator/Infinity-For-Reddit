package ml.docilealligator.infinityforreddit.settings;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import ml.docilealligator.infinityforreddit.BuildConfig;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

/**
 * A simple {@link PreferenceFragmentCompat} subclass.
 */
public class AboutPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.about_preferences, rootKey);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }

        Preference openSourcePreference = findPreference(SharedPreferencesUtils.OPEN_SOURCE_KEY);
        Preference ratePreference = findPreference(SharedPreferencesUtils.RATE_KEY);
        Preference emailPreference = findPreference(SharedPreferencesUtils.EMAIL_KEY);
        Preference redditAccountPreference = findPreference(SharedPreferencesUtils.REDDIT_ACCOUNT_KEY);
        Preference subredditPreference = findPreference(SharedPreferencesUtils.SUBREDDIT_KEY);
        Preference sharePreference = findPreference(SharedPreferencesUtils.SHARE_KEY);
        Preference versionPreference = findPreference(SharedPreferencesUtils.VERSION_KEY);

        if (openSourcePreference != null) {
            openSourcePreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://github.com/Docile-Alligator/Infinity-For-Reddit"));
                activity.startActivity(intent);
                return true;
            });
        }

        if (ratePreference != null) {
            ratePreference.setOnPreferenceClickListener(preference -> {
                Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
                playStoreIntent.setData(Uri.parse("market://details?id=ml.docilealligator.infinityforreddit"));
                if (playStoreIntent.resolveActivity(activity.getPackageManager()) != null) {
                    activity.startActivity(playStoreIntent);
                } else {
                    Intent intent = new Intent(activity, LinkResolverActivity.class);
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=ml.docilealligator.infinityforreddit"));
                    activity.startActivity(intent);
                }
                return true;
            });
        }

        if (emailPreference != null) {
            emailPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:docilealligator.app@gmail.com"));
                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(activity, R.string.no_email_client, Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        if (redditAccountPreference != null) {
            redditAccountPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://www.reddit.com/user/Hostilenemy"));
                activity.startActivity(intent);
                return true;
            });
        }

        if (subredditPreference != null) {
            subredditPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://www.reddit.com/r/Infinity_For_Reddit"));
                activity.startActivity(intent);
                return true;
            });
        }

        if (sharePreference != null) {
            sharePreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_this_app));
                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    activity.startActivity(intent);
                } else {
                    Toast.makeText(activity, R.string.no_app, Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        if (versionPreference != null) {
            versionPreference.setSummary(getString(R.string.settings_version_summary, BuildConfig.VERSION_NAME));

            versionPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                int clickedTimes = 0;

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    clickedTimes++;
                    if (clickedTimes > 6) {
                        Toast.makeText(activity, R.string.no_developer_easter_egg, Toast.LENGTH_SHORT).show();
                        clickedTimes = 0;
                    }
                    return true;
                }
            });
        }
    }
}
