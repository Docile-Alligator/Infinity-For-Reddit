package ml.docilealligator.infinityforreddit.settings;

import static ml.docilealligator.infinityforreddit.utils.Utils.HOSTNAME_REGEX;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceManager;

import com.google.common.net.InetAddresses;

import java.util.regex.Pattern;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.preference.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class ProxyPreferenceFragment extends CustomFontPreferenceFragmentCompat  {
    public ProxyPreferenceFragment() {}

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(SharedPreferencesUtils.PROXY_SHARED_PREFERENCES_FILE);
        setPreferencesFromResource(R.xml.proxy_preferences, rootKey);
        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        EditTextPreference proxyHostnamePref = findPreference(SharedPreferencesUtils.PROXY_HOSTNAME);
        EditTextPreference proxyPortPref = findPreference(SharedPreferencesUtils.PROXY_PORT);

        if (proxyHostnamePref != null) {
            proxyHostnamePref.setOnPreferenceChangeListener(((preference, newValue) -> {
                boolean isHostname = Pattern.matches(HOSTNAME_REGEX, (String) newValue);
                boolean isInetAddress = InetAddresses.isInetAddress((String) newValue);

                if (!isInetAddress && !isHostname) {
                    Toast.makeText(activity, R.string.not_a_valid_ip_or_hostname, Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }));
        }

        if (proxyPortPref != null) {
            proxyPortPref.setOnPreferenceChangeListener(((preference, newValue) -> {
                try {
                    int port = Integer.parseInt((String) newValue);
                    if (port < 0 || port > 65535) {
                        Toast.makeText(activity, R.string.not_a_valid_port, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(activity, R.string.not_a_valid_number, Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }));
        }
    }
}
