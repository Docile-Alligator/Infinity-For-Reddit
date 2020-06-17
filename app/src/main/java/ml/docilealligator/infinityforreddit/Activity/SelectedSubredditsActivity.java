package ml.docilealligator.infinityforreddit.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Adapter.SelectedSubredditsRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

public class SelectedSubredditsActivity extends BaseActivity implements ActivityToolbarInterface {

    public static final String EXTRA_SELECTED_SUBREDDITS = "ESS";
    public static final String EXTRA_RETURN_SELECTED_SUBREDDITS = "ERSS";
    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 1;
    private static final String SELECTED_SUBREDDITS_STATE = "SSS";

    @BindView(R.id.coordinator_layout_selected_subreddits_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_selected_subreddits_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_selected_subreddits_activity)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_selected_subreddits_activity)
    RecyclerView recyclerView;
    @BindView(R.id.fab_selected_subreddits_activity)
    FloatingActionButton fab;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private LinearLayoutManager linearLayoutManager;
    private SelectedSubredditsRecyclerViewAdapter adapter;
    private ArrayList<String> subreddits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_subreddits);

        ButterKnife.bind(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK_FROM_POST_DETAIL, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(appBarLayout);
            }

            if (isImmersiveInterface()) {
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                adjustToolbar(toolbar);
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(toolbar);

        if (savedInstanceState != null) {
            subreddits = savedInstanceState.getStringArrayList(SELECTED_SUBREDDITS_STATE);
        } else {
            subreddits = getIntent().getStringArrayListExtra(EXTRA_SELECTED_SUBREDDITS);
        }

        adapter = new SelectedSubredditsRecyclerViewAdapter(mCustomThemeWrapper, subreddits);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    fab.hide();
                } else {
                    fab.show();
                }
            }
        });

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubredditMultiselectionActivity.class);
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.selected_subreddits_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save_selected_subreddits_activity) {
            if (adapter != null) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(EXTRA_RETURN_SELECTED_SUBREDDITS, adapter.getSubreddits());
                setResult(Activity.RESULT_OK, returnIntent);
            }
            finish();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                if (subreddits == null) {
                    subreddits = new ArrayList<>();
                }
                subreddits = data.getStringArrayListExtra(SubredditMultiselectionActivity.EXTRA_RETURN_SELECTED_SUBREDDITS);
                adapter.addSubreddits(subreddits);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter != null) {
            outState.putStringArrayList(SELECTED_SUBREDDITS_STATE, adapter.getSubreddits());
        }
    }

    @Override
    protected SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
        applyFABTheme(fab);
    }

    @Override
    public void onLongPress() {
        if (linearLayoutManager != null) {
            linearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }
}
