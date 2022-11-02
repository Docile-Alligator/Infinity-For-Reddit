package ml.docilealligator.infinityforreddit.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.crazylegend.crashyreporter.CrashyReporter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.BuildConfig;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.adapters.CrashReportsRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CrashReportsFragment extends Fragment {

    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private SettingsActivity activity;

    public CrashReportsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_crash_reports, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        setHasOptionsMenu(true);

        recyclerView.setAdapter(new CrashReportsRecyclerViewAdapter(activity, CrashyReporter.INSTANCE.getLogsAsStrings()));

        recyclerView.setBackgroundColor(activity.customThemeWrapper.getBackgroundColor());

        return recyclerView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.crash_reports_fragment, menu);
        applyMenuItemTheme(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete_logs_crash_reports_fragment) {
            CrashyReporter.INSTANCE.purgeLogs();
            Toast.makeText(activity, R.string.crash_reports_deleted, Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_export_logs_crash_reports_fragment) {
            return createGithubIssueWithLogs();
        }
        return false;
    }

    /**
     * Fetch the logs from CrashyReporter and open browser to create GitHub issue page.
     * Issue will have logs, device model, app version, and Android version prefilled.
     * @return if successful
     */
    private boolean createGithubIssueWithLogs() {
        Intent intent = new Intent(getContext(), LinkResolverActivity.class);
        String logs, model, appVersion, androidVersion;
        try {
            List<String> logLines = CrashyReporter.INSTANCE.getLogsAsStrings();
            if (logLines == null) {
                return false;
            }
            logs = String.join("\n", logLines);
            // limit size to 6800 characters to avoid `414 URI Too Long`
            logs = URLEncoder.encode("```\n" + (logs.length() > 0 ? logs.substring(0, Math.min(6800, logs.length())) : "No logs found.") + "\n```", "UTF-8");
            model = URLEncoder.encode(Build.MANUFACTURER + " " + Build.MODEL, "UTF-8");
            appVersion = URLEncoder.encode(BuildConfig.VERSION_NAME, "UTF-8");
            androidVersion = URLEncoder.encode(Build.VERSION.RELEASE, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return false;
        }
        Uri githubIssueUri = Uri.parse(String.format("https://github.com/Docile-Alligator/Infinity-For-Reddit/issues/new?labels=possible-bug&device=%s&version=%s&android_version=%s&logs=%s&&template=BUG_REPORT.yml", model, appVersion, androidVersion, logs));
        intent.setData(githubIssueUri);
        startActivity(intent);
        return true;
    }

    @SuppressLint("RestrictedApi")
    protected boolean applyMenuItemTheme(Menu menu) {
        if (mCustomThemeWrapper != null) {
            int size = Math.min(menu.size(), 2);
            for (int i = 0; i < size; i++) {
                MenuItem item = menu.getItem(i);
                if (((MenuItemImpl) item).requestsActionButton()) {
                    MenuItemCompat.setIconTintList(item, ColorStateList
                            .valueOf(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor()));
                }
                Utils.setTitleWithCustomFontToMenuItem(activity.typeface, item, null);
            }
        }
        return true;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (SettingsActivity) context;
    }
}