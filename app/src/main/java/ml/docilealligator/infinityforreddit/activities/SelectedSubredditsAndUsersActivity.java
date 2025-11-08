package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.thing.SelectThingReturnKey;
import ml.docilealligator.infinityforreddit.adapters.SelectedSubredditsRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SelectSubredditsOrUsersOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivitySelectedSubredditsBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class SelectedSubredditsAndUsersActivity extends BaseActivity implements ActivityToolbarInterface {

    public static final String EXTRA_SELECTED_SUBREDDITS = "ESS";
    public static final String EXTRA_RETURN_SELECTED_SUBREDDITS = "ERSS";
    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 1;
    private static final int USER_SELECTION_REQUEST_CODE = 2;
    private static final String SELECTED_SUBREDDITS_STATE = "SSS";

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private LinearLayoutManagerBugFixed linearLayoutManager;
    private SelectedSubredditsRecyclerViewAdapter adapter;
    private ArrayList<String> subreddits;
    private ActivitySelectedSubredditsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicableBelowAndroid16();

        super.onCreate(savedInstanceState);

        binding = ActivitySelectedSubredditsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (isImmersiveInterface()) {
            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutSelectedSubredditsAndUsersActivity);
            }

            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, false);

                    setMargins(binding.toolbarSelectedSubredditsAndUsersActivity,
                            allInsets.left,
                            allInsets.top,
                            allInsets.right,
                            BaseActivity.IGNORE_MARGIN);

                    binding.recyclerViewSelectedSubredditsAndUsersActivity.setPadding(
                            allInsets.left,
                            0,
                            allInsets.right,
                            allInsets.bottom);

                    setMargins(binding.fabSelectedSubredditsAndUsersActivity,
                            BaseActivity.IGNORE_MARGIN,
                            BaseActivity.IGNORE_MARGIN,
                            (int) Utils.convertDpToPixel(16, SelectedSubredditsAndUsersActivity.this) + allInsets.right,
                            (int) Utils.convertDpToPixel(16, SelectedSubredditsAndUsersActivity.this) + allInsets.bottom);

                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        setSupportActionBar(binding.toolbarSelectedSubredditsAndUsersActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(binding.toolbarSelectedSubredditsAndUsersActivity);

        if (savedInstanceState != null) {
            subreddits = savedInstanceState.getStringArrayList(SELECTED_SUBREDDITS_STATE);
        } else {
            subreddits = getIntent().getStringArrayListExtra(EXTRA_SELECTED_SUBREDDITS);
        }

        Collections.sort(subreddits);

        adapter = new SelectedSubredditsRecyclerViewAdapter(this, mCustomThemeWrapper, subreddits);
        linearLayoutManager = new LinearLayoutManagerBugFixed(this);
        binding.recyclerViewSelectedSubredditsAndUsersActivity.setLayoutManager(linearLayoutManager);
        binding.recyclerViewSelectedSubredditsAndUsersActivity.setAdapter(adapter);
        binding.recyclerViewSelectedSubredditsAndUsersActivity.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    binding.fabSelectedSubredditsAndUsersActivity.hide();
                } else {
                    binding.fabSelectedSubredditsAndUsersActivity.show();
                }
            }
        });

        binding.fabSelectedSubredditsAndUsersActivity.setOnClickListener(view -> {
            SelectSubredditsOrUsersOptionsBottomSheetFragment selectSubredditsOrUsersOptionsBottomSheetFragment = new SelectSubredditsOrUsersOptionsBottomSheetFragment();
            selectSubredditsOrUsersOptionsBottomSheetFragment.show(getSupportFragmentManager(), selectSubredditsOrUsersOptionsBottomSheetFragment.getTag());
        });
    }

    public void selectSubreddits() {
        Intent intent = new Intent(this, SubredditMultiselectionActivity.class);
        startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
    }

    public void selectUsers() {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_USERS, true);
        startActivityForResult(intent, USER_SELECTION_REQUEST_CODE);
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
        if (resultCode == RESULT_OK) {
            if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
                if (data != null) {
                    if (subreddits == null) {
                        subreddits = new ArrayList<>();
                    }
                    subreddits = data.getStringArrayListExtra(SubredditMultiselectionActivity.EXTRA_RETURN_SELECTED_SUBREDDITS);
                    adapter.addSubreddits(subreddits);
                }
            } else if (requestCode == USER_SELECTION_REQUEST_CODE) {
                if (data != null) {
                    if (subreddits == null) {
                        subreddits = new ArrayList<>();
                    }
                    adapter.addUserInSubredditType("u_" + data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME));
                }
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
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        binding.getRoot().setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutSelectedSubredditsAndUsersActivity,
                binding.collapsingToolbarLayoutSelectedSubredditsAndUsersActivity, binding.toolbarSelectedSubredditsAndUsersActivity);
        applyFABTheme(binding.fabSelectedSubredditsAndUsersActivity);
    }

    @Override
    public void onLongPress() {
        if (linearLayoutManager != null) {
            linearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }
}
