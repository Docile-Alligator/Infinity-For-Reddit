package ml.docilealligator.infinityforreddit.settings;

import static android.content.Intent.ACTION_OPEN_DOCUMENT_TREE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class DownloadLocationPreferenceFragment extends CustomFontPreferenceFragmentCompat {
    private static final int IMAGE_DOWNLOAD_LOCATION_REQUEST_CODE = 10;
    private static final int GIF_DOWNLOAD_LOCATION_REQUEST_CODE = 11;
    private static final int VIDEO_DOWNLOAD_LOCATION_REQUEST_CODE = 12;
    private static final int NSFW_DOWNLOAD_LOCATION_REQUEST_CODE = 13;

    Preference imageDownloadLocationPreference;
    Preference gifDownloadLocationPreference;
    Preference videoDownloadLocationPreference;
    Preference nsfwDownloadLocationPreference;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        ((Infinity) activity.getApplication()).getAppComponent().inject(this);
        setPreferencesFromResource(R.xml.download_location_preferences, rootKey);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }

        imageDownloadLocationPreference = findPreference(SharedPreferencesUtils.IMAGE_DOWNLOAD_LOCATION);
        gifDownloadLocationPreference = findPreference(SharedPreferencesUtils.GIF_DOWNLOAD_LOCATION);
        videoDownloadLocationPreference = findPreference(SharedPreferencesUtils.VIDEO_DOWNLOAD_LOCATION);
        nsfwDownloadLocationPreference = findPreference(SharedPreferencesUtils.NSFW_DOWNLOAD_LOCATION);

        if (nsfwDownloadLocationPreference != null) {
            String downloadLocation = sharedPreferences.getString(SharedPreferencesUtils.NSFW_DOWNLOAD_LOCATION, "");
            if (!downloadLocation.equals("")) {
                nsfwDownloadLocationPreference.setSummary(downloadLocation);
            }

            nsfwDownloadLocationPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(intent, NSFW_DOWNLOAD_LOCATION_REQUEST_CODE);
                return true;
            });
        }
        if (imageDownloadLocationPreference != null) {
            String downloadLocation = sharedPreferences.getString(SharedPreferencesUtils.IMAGE_DOWNLOAD_LOCATION, "");
            if (!downloadLocation.equals("")) {
                imageDownloadLocationPreference.setSummary(downloadLocation);
            }

            imageDownloadLocationPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(intent, IMAGE_DOWNLOAD_LOCATION_REQUEST_CODE);
                return true;
            });
        }

        if (gifDownloadLocationPreference != null) {
            String downloadLocation = sharedPreferences.getString(SharedPreferencesUtils.GIF_DOWNLOAD_LOCATION, "");
            if (!downloadLocation.equals("")) {
                gifDownloadLocationPreference.setSummary(downloadLocation);
            }

            gifDownloadLocationPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(intent, GIF_DOWNLOAD_LOCATION_REQUEST_CODE);
                return true;
            });
        }

        if (videoDownloadLocationPreference != null) {
            String downloadLocation = sharedPreferences.getString(SharedPreferencesUtils.VIDEO_DOWNLOAD_LOCATION, "");
            if (!downloadLocation.equals("")) {
                videoDownloadLocationPreference.setSummary(downloadLocation);
            }

            videoDownloadLocationPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(intent, VIDEO_DOWNLOAD_LOCATION_REQUEST_CODE);
                return true;
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == IMAGE_DOWNLOAD_LOCATION_REQUEST_CODE) {
                activity.getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                sharedPreferences.edit().putString(SharedPreferencesUtils.IMAGE_DOWNLOAD_LOCATION, data.getDataString()).apply();
                if (imageDownloadLocationPreference != null) {
                    imageDownloadLocationPreference.setSummary(data.getDataString());
                }
            } else if (requestCode == GIF_DOWNLOAD_LOCATION_REQUEST_CODE) {
                activity.getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                sharedPreferences.edit().putString(SharedPreferencesUtils.GIF_DOWNLOAD_LOCATION, data.getDataString()).apply();
                if (gifDownloadLocationPreference != null) {
                    gifDownloadLocationPreference.setSummary(data.getDataString());
                }
            } else if (requestCode == VIDEO_DOWNLOAD_LOCATION_REQUEST_CODE) {
                activity.getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                sharedPreferences.edit().putString(SharedPreferencesUtils.VIDEO_DOWNLOAD_LOCATION, data.getDataString()).apply();
                if (videoDownloadLocationPreference != null) {
                    videoDownloadLocationPreference.setSummary(data.getDataString());
                }
            } else if (requestCode == NSFW_DOWNLOAD_LOCATION_REQUEST_CODE) {
                activity.getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                sharedPreferences.edit().putString(SharedPreferencesUtils.NSFW_DOWNLOAD_LOCATION, data.getDataString()).apply();
                if (nsfwDownloadLocationPreference != null) {
                    nsfwDownloadLocationPreference.setSummary(data.getDataString());
                }
            }
        }
    }
}