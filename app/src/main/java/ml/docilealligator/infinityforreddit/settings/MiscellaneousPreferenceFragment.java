package ml.docilealligator.infinityforreddit.settings;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.preference.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.events.ChangePostFeedMaxResolutionEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSavePostFeedScrolledPositionEvent;
import ml.docilealligator.infinityforreddit.events.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.utils.ExternalBrowserDomainUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class MiscellaneousPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Inject
    @Named("post_feed_scrolled_position_cache")
    SharedPreferences cache;

    public MiscellaneousPreferenceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.miscellaneous_preferences, rootKey);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        ListPreference mainPageBackButtonActionListPreference = findPreference(SharedPreferencesUtils.MAIN_PAGE_BACK_BUTTON_ACTION);
        SwitchPreference savePostFeedScrolledPositionSwitch = findPreference(SharedPreferencesUtils.SAVE_FRONT_PAGE_SCROLLED_POSITION);
        ListPreference languageListPreference = findPreference(SharedPreferencesUtils.LANGUAGE);
        EditTextPreference postFeedMaxResolution = findPreference(SharedPreferencesUtils.POST_FEED_MAX_RESOLUTION);
        Preference manageExternalBrowserDomainsPreference = findPreference("manage_external_browser_domains");

        if (mainPageBackButtonActionListPreference != null) {
            mainPageBackButtonActionListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });
        }

        if (savePostFeedScrolledPositionSwitch != null) {
            savePostFeedScrolledPositionSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!(Boolean) newValue) {
                    cache.edit().clear().apply();
                }
                EventBus.getDefault().post(new ChangeSavePostFeedScrolledPositionEvent((Boolean) newValue));
                return true;
            });
        }

        if (languageListPreference != null) {
            languageListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });
        }

        if (postFeedMaxResolution != null) {
            postFeedMaxResolution.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    int resolution = Integer.parseInt((String) newValue);
                    if (resolution <= 0) {
                        Toast.makeText(mActivity, R.string.not_a_valid_number, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    EventBus.getDefault().post(new ChangePostFeedMaxResolutionEvent(resolution));
                } catch (NumberFormatException e) {
                    Toast.makeText(mActivity, R.string.not_a_valid_number, Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            });
        }

        if (manageExternalBrowserDomainsPreference != null) {
            manageExternalBrowserDomainsPreference.setOnPreferenceClickListener(preference -> {
                showManageExternalBrowserDomainsDialog();
                return true;
            });
        }
    }

    private void showManageExternalBrowserDomainsDialog() {
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        Set<String> domains = ExternalBrowserDomainUtils.getExternalBrowserDomains(sharedPreferences);
        List<String> domainList = new ArrayList<>(domains);

        if (domainList.isEmpty()) {
            Toast.makeText(activity, R.string.no_external_browser_domains, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean[] checkedItems = new boolean[domainList.size()];
        new AlertDialog.Builder(activity)
                .setTitle(R.string.external_browser_domains_dialog_title)
                .setMultiChoiceItems(domainList.toArray(new String[0]), checkedItems,
                        (dialog, which, isChecked) -> {
                            checkedItems[which] = isChecked;
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.remove_selected, (dialog, which) -> {
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) {
                            ExternalBrowserDomainUtils.removeDomain(sharedPreferences, domainList.get(i));
                        }
                    }
                    Toast.makeText(activity, R.string.domains_removed, Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}