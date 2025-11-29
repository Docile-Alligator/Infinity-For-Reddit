package ml.docilealligator.infinityforreddit.utils;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to manage domains that should always open in external browser
 */
public class ExternalBrowserDomainUtils {

    /**
     * Get all domains that should open in external browser
     */
    public static Set<String> getExternalBrowserDomains(SharedPreferences sharedPreferences) {
        Set<String> domains = new HashSet<>();
        String domainsJson = sharedPreferences.getString(SharedPreferencesUtils.EXTERNAL_BROWSER_DOMAINS, "[]");

        try {
            JSONArray jsonArray = new JSONArray(domainsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                domains.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return domains;
    }

    /**
     * Check if a domain should open in external browser
     */
    public static boolean isExternalBrowserDomain(SharedPreferences sharedPreferences, String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }

        // Normalize domain (remove www prefix)
        String normalizedDomain = normalizeDomain(domain);
        Set<String> domains = getExternalBrowserDomains(sharedPreferences);

        return domains.contains(normalizedDomain);
    }

    /**
     * Add a domain to external browser list
     */
    public static void addDomain(SharedPreferences sharedPreferences, String domain) {
        if (domain == null || domain.isEmpty()) {
            return;
        }

        String normalizedDomain = normalizeDomain(domain);

        // Don't add duplicate
        if (isExternalBrowserDomain(sharedPreferences, normalizedDomain)) {
            return;
        }

        Set<String> domains = getExternalBrowserDomains(sharedPreferences);
        domains.add(normalizedDomain);
        saveDomains(sharedPreferences, domains);
    }

    /**
     * Remove a domain from external browser list
     */
    public static void removeDomain(SharedPreferences sharedPreferences, String domain) {
        if (domain == null || domain.isEmpty()) {
            return;
        }

        String normalizedDomain = normalizeDomain(domain);
        Set<String> domains = getExternalBrowserDomains(sharedPreferences);
        domains.remove(normalizedDomain);
        saveDomains(sharedPreferences, domains);
    }

    /**
     * Save domains to SharedPreferences as JSON
     */
    private static void saveDomains(SharedPreferences sharedPreferences, Set<String> domains) {
        JSONArray jsonArray = new JSONArray(domains);
        sharedPreferences.edit()
                .putString(SharedPreferencesUtils.EXTERNAL_BROWSER_DOMAINS, jsonArray.toString())
                .apply();
    }

    /**
     * Normalize domain by removing www prefix for consistent comparison
     */
    private static String normalizeDomain(String domain) {
        if (domain.startsWith("www.")) {
            return domain.substring(4);
        }
        return domain;
    }

    /**
     * Get domain from URL authority (handles www prefix)
     */
    public static String extractDomain(String authority) {
        if (authority == null) {
            return null;
        }
        return normalizeDomain(authority);
    }
}
