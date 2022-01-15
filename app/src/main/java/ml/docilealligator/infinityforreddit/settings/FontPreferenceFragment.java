package ml.docilealligator.infinityforreddit.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.events.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.font.ContentFontFamily;
import ml.docilealligator.infinityforreddit.font.FontFamily;
import ml.docilealligator.infinityforreddit.font.TitleFontFamily;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class FontPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    private static final int CUSTOM_FONT_FAMILY_REQUEST_CODE = 20;
    private static final int CUSTOM_TITLE_FONT_FAMILY_REQUEST_CODE = 21;
    private static final int CUSTOM_CONTENT_FONT_FAMILY_REQUEST_CODE = 22;

    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    Executor executor;
    private Preference customFontFamilyPreference;
    private Preference customTitleFontFamilyPreference;
    private Preference customContentFontFamilyPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.font_preferences, rootKey);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }

        ListPreference fontFamilyPreference = findPreference(SharedPreferencesUtils.FONT_FAMILY_KEY);
        customFontFamilyPreference = findPreference(SharedPreferencesUtils.CUSTOM_FONT_FAMILY_KEY);
        ListPreference titleFontFamilyPreference = findPreference(SharedPreferencesUtils.TITLE_FONT_FAMILY_KEY);
        customTitleFontFamilyPreference = findPreference(SharedPreferencesUtils.CUSTOM_TITLE_FONT_FAMILY_KEY);
        ListPreference contentFontFamilyPreference = findPreference(SharedPreferencesUtils.CONTENT_FONT_FAMILY_KEY);
        customContentFontFamilyPreference = findPreference(SharedPreferencesUtils.CUSTOM_CONTENT_FONT_FAMILY_KEY);
        ListPreference fontSizePreference = findPreference(SharedPreferencesUtils.FONT_SIZE_KEY);
        ListPreference titleFontSizePreference = findPreference(SharedPreferencesUtils.TITLE_FONT_SIZE_KEY);
        ListPreference contentFontSizePreference = findPreference(SharedPreferencesUtils.CONTENT_FONT_SIZE_KEY);

        if (customFontFamilyPreference != null) {
            if (sharedPreferences.getString(SharedPreferencesUtils.FONT_FAMILY_KEY, FontFamily.Default.name()).equals(FontFamily.Custom.name())) {
                customFontFamilyPreference.setVisible(true);
            }

            customFontFamilyPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_a_ttf_font)), CUSTOM_FONT_FAMILY_REQUEST_CODE);
                return true;
            });
        }

        if (fontFamilyPreference != null) {
            fontFamilyPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                ActivityCompat.recreate(activity);
                return true;
            });
        }

        if (customTitleFontFamilyPreference != null) {
            if (sharedPreferences.getString(SharedPreferencesUtils.TITLE_FONT_FAMILY_KEY, TitleFontFamily.Default.name()).equals(TitleFontFamily.Custom.name())) {
                customTitleFontFamilyPreference.setVisible(true);
            }

            customTitleFontFamilyPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_a_ttf_font)), CUSTOM_TITLE_FONT_FAMILY_REQUEST_CODE);
                return true;
            });
        }

        if (titleFontFamilyPreference != null) {
            titleFontFamilyPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });
        }

        if (customContentFontFamilyPreference != null) {
            if (sharedPreferences.getString(SharedPreferencesUtils.CONTENT_FONT_FAMILY_KEY, ContentFontFamily.Default.name()).equals(ContentFontFamily.Custom.name())) {
                customContentFontFamilyPreference.setVisible(true);
            }

            customContentFontFamilyPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_a_ttf_font)), CUSTOM_CONTENT_FONT_FAMILY_REQUEST_CODE);
                return true;
            });
        }

        if (contentFontFamilyPreference != null) {
            contentFontFamilyPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });
        }

        if (fontSizePreference != null) {
            fontSizePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                ActivityCompat.recreate(activity);
                return true;
            });
        }

        if (titleFontSizePreference != null) {
            titleFontSizePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });
        }

        if (contentFontSizePreference != null) {
            contentFontSizePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == CUSTOM_FONT_FAMILY_REQUEST_CODE) {
                copyFontToInternalStorage(data.getData(), 0);
                if (customFontFamilyPreference != null) {
                    customFontFamilyPreference.setSummary(data.getDataString());
                }
            } else if (requestCode == CUSTOM_TITLE_FONT_FAMILY_REQUEST_CODE) {
                copyFontToInternalStorage(data.getData(), 1);
            } else if (requestCode == CUSTOM_CONTENT_FONT_FAMILY_REQUEST_CODE) {
                copyFontToInternalStorage(data.getData(), 2);
                if (customContentFontFamilyPreference != null) {
                    customContentFontFamilyPreference.setSummary(data.getDataString());
                }
            }
        }
    }

    private void copyFontToInternalStorage(Uri uri, int type) {
        String destinationFontName;
        switch (type) {
            case 1:
                destinationFontName = "title_font_family.ttf";
                break;
            case 2:
                destinationFontName = "content_font_family.ttf";
                break;
            default:
                destinationFontName = "font_family.ttf";
        }
        File fontDestinationPath = activity.getExternalFilesDir("fonts");

        Handler handler = new Handler();

        executor.execute(() -> {
            File destinationFontFile = new File(fontDestinationPath, destinationFontName);
            try (InputStream in = activity.getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(destinationFontFile)) {
                if (in != null) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    try {
                        switch (type) {
                            case 1:
                                ((Infinity) activity.getApplication()).titleTypeface = Typeface.createFromFile(destinationFontFile);
                                break;
                            case 2:
                                ((Infinity) activity.getApplication()).contentTypeface = Typeface.createFromFile(destinationFontFile);
                                break;
                            default:
                                ((Infinity) activity.getApplication()).typeface = Typeface.createFromFile(destinationFontFile);
                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        handler.post(() -> Toast.makeText(activity, R.string.unable_to_load_font, Toast.LENGTH_SHORT).show());
                        return;
                    }
                } else {
                    handler.post(() -> Toast.makeText(activity, R.string.unable_to_get_font_file, Toast.LENGTH_SHORT).show());
                    return;
                }
                handler.post(() -> {
                    EventBus.getDefault().post(new RecreateActivityEvent());
                    ActivityCompat.recreate(activity);
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    Toast.makeText(activity, R.string.unable_to_copy_font_file, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
