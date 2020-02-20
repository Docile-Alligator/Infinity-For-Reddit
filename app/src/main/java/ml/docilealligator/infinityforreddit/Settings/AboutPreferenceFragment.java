package ml.docilealligator.infinityforreddit.Settings;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import ml.docilealligator.infinityforreddit.Activity.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

/**
 * A simple {@link PreferenceFragmentCompat} subclass.
 */
public class AboutPreferenceFragment extends PreferenceFragmentCompat {

    private Activity activity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.about_preferences, rootKey);

        Preference openSourcePreference = findPreference(SharedPreferencesUtils.OPEN_SOURCE_KEY);
        Preference ratePreference = findPreference(SharedPreferencesUtils.RATE_KEY);
        Preference fDroidPreference = findPreference(SharedPreferencesUtils.F_DROID_KEY);
        Preference emailPreference = findPreference(SharedPreferencesUtils.EMAIL_KEY);
        Preference redditAccountPreference = findPreference(SharedPreferencesUtils.REDDIT_ACCOUNT_KEY);
        Preference subredditPreference = findPreference(SharedPreferencesUtils.SUBREDDIT_KEY);
        Preference sharePreference = findPreference(SharedPreferencesUtils.SHARE_KEY);

        if (openSourcePreference != null) {
            openSourcePreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://github.com/Docile-Alligator/Infinity-For-Reddit"));
                startActivity(intent);
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
                    startActivity(intent);
                }
                return true;
            });
        }

        if (fDroidPreference != null) {
            fDroidPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://www.reddit.com/r/Infinity_For_Reddit/comments/f23o0y/for_anyone_who_wants_to_use_fdroid/"));
                startActivity(intent);
                return true;
            });
        }

        if (emailPreference != null) {
            emailPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:docilealligator.app@gmail.com"));
                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(activity, R.string.no_email_client, Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        if (redditAccountPreference != null) {
            redditAccountPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://www.reddit.com/user/Hostilenemy"));
                startActivity(intent);
                return true;
            });
        }

        if (subredditPreference != null) {
            subredditPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://www.reddit.com/r/Infinity_For_Reddit"));
                startActivity(intent);
                return true;
            });
        }

        if (sharePreference != null) {
            sharePreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_this_app));
                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(activity, R.string.no_app, Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}
