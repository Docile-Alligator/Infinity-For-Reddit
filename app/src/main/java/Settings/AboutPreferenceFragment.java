package Settings;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SharedPreferencesUtils;

/**
 * A simple {@link PreferenceFragmentCompat} subclass.
 */
public class AboutPreferenceFragment extends PreferenceFragmentCompat {

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.about_preferences, rootKey);

    Preference openSourcePreference = findPreference(SharedPreferencesUtils.OPEN_SOURCE_KEY);
    Preference reviewPreference = findPreference(SharedPreferencesUtils.RATE_KEY);
    Preference emailPreference = findPreference(SharedPreferencesUtils.EMAIL_KEY);
    Preference redditAccountPreference = findPreference(SharedPreferencesUtils.REDDIT_ACCOUNT_KEY);

    Activity activity = getActivity();

    if (activity != null) {
      if (openSourcePreference != null) {
        openSourcePreference.setOnPreferenceClickListener(preference -> {
          Intent intent = new Intent(activity, LinkResolverActivity.class);
          intent.setData(Uri.parse("https://github.com/Docile-Alligator/Infinity-For-Reddit"));
          startActivity(intent);
          return true;
        });
      }

      if (reviewPreference != null) {
        reviewPreference.setOnPreferenceClickListener(preference -> {
          Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
          playStoreIntent
              .setData(Uri.parse("market://details?id=ml.docilealligator.infinityforreddit"));
          if (playStoreIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(playStoreIntent);
          } else {
            Intent intent = new Intent(activity, LinkResolverActivity.class);
            intent.setData(Uri.parse(
                "https://play.google.com/store/apps/details?id=ml.docilealligator.infinityforreddit"));
            startActivity(intent);
          }
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
    }
  }
}
