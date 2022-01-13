package ml.docilealligator.infinityforreddit.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.crazylegend.crashyreporter.CrashyReporter;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
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
        }
        return false;
    }

    @SuppressLint("RestrictedApi")
    protected boolean applyMenuItemTheme(Menu menu) {
        if (mCustomThemeWrapper != null) {
            int size = Math.min(menu.size(), 2);
            for (int i = 0; i < size; i++) {
                MenuItem item = menu.getItem(i);
                if (((MenuItemImpl) item).requestsActionButton()) {
                    Drawable drawable = item.getIcon();
                    if (drawable != null) {
                        DrawableCompat.setTint(drawable, mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
                        item.setIcon(drawable);
                    }
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