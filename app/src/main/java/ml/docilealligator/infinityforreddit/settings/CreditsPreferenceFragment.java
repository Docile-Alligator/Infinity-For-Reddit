package ml.docilealligator.infinityforreddit.settings;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

/**
 * A simple {@link PreferenceFragmentCompat} subclass.
 */
public class CreditsPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.credits_preferences, rootKey);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }

        Preference iconForegroundPreference = findPreference(SharedPreferencesUtils.ICON_FOREGROUND_KEY);
        Preference iconBackgroundPreference = findPreference(SharedPreferencesUtils.ICON_BACKGROUND_KEY);
        Preference errorImagePreference = findPreference(SharedPreferencesUtils.ERROR_IMAGE_KEY);
        Preference gildedIconPreference = findPreference(SharedPreferencesUtils.GILDED_ICON_KEY);
        Preference crosspostIconPreference = findPreference(SharedPreferencesUtils.CROSSPOST_ICON_KEY);
        Preference thumbtackIconPreference = findPreference(SharedPreferencesUtils.THUMBTACK_ICON_KEY);
        Preference bestRocketIconPreference = findPreference(SharedPreferencesUtils.BEST_ROCKET_ICON_KEY);
        Preference materialIconsPreference = findPreference(SharedPreferencesUtils.MATERIAL_ICONS_KEY);
        Preference nationalFlagsPreference = findPreference(SharedPreferencesUtils.NATIONAL_FLAGS);
        Preference ufoAndCowPreference = findPreference(SharedPreferencesUtils.UFO_CAPTURING_ANIMATION);
        Preference loveAnimationPreference = findPreference(SharedPreferencesUtils.LOVE_ANIMATION);
        Preference lockScreenPreference = findPreference(SharedPreferencesUtils.LOCK_SCREEN_ANIMATION);

        if (iconForegroundPreference != null) {
            iconForegroundPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://www.freepik.com/free-photos-vectors/technology"));
                startActivity(intent);
                return true;
            });
        }

        if (iconBackgroundPreference != null) {
            iconBackgroundPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://www.freepik.com/free-photos-vectors/background"));
                startActivity(intent);
                return true;
            });
        }

        if (errorImagePreference != null) {
            errorImagePreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://www.freepik.com/free-photos-vectors/technology"));
                startActivity(intent);
                return true;
            });
        }

        if (gildedIconPreference != null) {
            gildedIconPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://br.flaticon.com/icone-gratis/medalha_1007239"));
                startActivity(intent);
                return true;
            });
        }

        if (crosspostIconPreference != null) {
            crosspostIconPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://www.flaticon.com/free-icon/crossed-arrows_2291"));
                startActivity(intent);
                return true;
            });
        }

        if (thumbtackIconPreference != null) {
            thumbtackIconPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://www.flaticon.com/free-icon/tack-save-button_61845#term=thumbtack&page=1&position=3"));
                startActivity(intent);
                return true;
            });
        }

        if (bestRocketIconPreference != null) {
            bestRocketIconPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://www.flaticon.com/free-icon/spring-swing-rocket_2929322?term=space%20ship&page=1&position=18"));
                startActivity(intent);
                return true;
            });
        }

        if (materialIconsPreference != null) {
            materialIconsPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://material.io/resources/icons/"));
                startActivity(intent);
                return true;
            });
        }

        if (nationalFlagsPreference != null) {
            nationalFlagsPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://www.flaticon.com/packs/countrys-flags"));
                startActivity(intent);
                return true;
            });
        }

        if (ufoAndCowPreference != null) {
            ufoAndCowPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://lottiefiles.com/33858-ufo-capturing-animation"));
                startActivity(intent);
                return true;
            });
        }

        if (loveAnimationPreference != null) {
            loveAnimationPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://lottiefiles.com/52103-love"));
                startActivity(intent);
                return true;
            });
        }

        if (lockScreenPreference != null) {
            lockScreenPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://lottiefiles.com/69178-cool"));
                startActivity(intent);
                return true;
            });
        }
    }
}
