package ml.docilealligator.infinityforreddit.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.PostFilterUsageRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.NewPostFilterUsageBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostFilterUsageOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.postfilter.DeletePostFilterUsage;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterUsage;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterUsageViewModel;
import ml.docilealligator.infinityforreddit.postfilter.SavePostFilterUsage;

public class PostFilterUsageListingActivity extends BaseActivity {

    public static final String EXTRA_POST_FILTER = "EPF";
    @BindView(R.id.coordinator_layout_post_filter_application_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_post_filter_application_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_post_filter_application_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_post_filter_application_activity)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_post_filter_application_activity)
    RecyclerView recyclerView;
    @BindView(R.id.fab_post_filter_application_activity)
    FloatingActionButton fab;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    @Inject
    Executor executor;
    public PostFilterUsageViewModel postFilterUsageViewModel;
    private PostFilterUsageRecyclerViewAdapter adapter;
    private PostFilter postFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_filter_application);

        ButterKnife.bind(this);

        applyCustomTheme();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postFilter = getIntent().getParcelableExtra(EXTRA_POST_FILTER);

        setTitle(postFilter.name);

        fab.setOnClickListener(view -> {
            NewPostFilterUsageBottomSheetFragment newPostFilterUsageBottomSheetFragment = new NewPostFilterUsageBottomSheetFragment();
            newPostFilterUsageBottomSheetFragment.show(getSupportFragmentManager(), newPostFilterUsageBottomSheetFragment.getTag());
        });

        adapter = new PostFilterUsageRecyclerViewAdapter(this, postFilterUsage -> {
            PostFilterUsageOptionsBottomSheetFragment postFilterUsageOptionsBottomSheetFragment = new PostFilterUsageOptionsBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(PostFilterUsageOptionsBottomSheetFragment.EXTRA_POST_FILTER_USAGE, postFilterUsage);
            postFilterUsageOptionsBottomSheetFragment.setArguments(bundle);
            postFilterUsageOptionsBottomSheetFragment.show(getSupportFragmentManager(), postFilterUsageOptionsBottomSheetFragment.getTag());
        });
        recyclerView.setAdapter(adapter);

        postFilterUsageViewModel = new ViewModelProvider(this,
                new PostFilterUsageViewModel.Factory(redditDataRoomDatabase, postFilter.name)).get(PostFilterUsageViewModel.class);

        postFilterUsageViewModel.getPostFilterUsageListLiveData().observe(this, postFilterUsages -> adapter.setPostFilterUsages(postFilterUsages));
    }

    public void newPostFilterUsage(int type) {
        switch (type) {
            case PostFilterUsage.HOME_TYPE:
            case PostFilterUsage.SEARCH_TYPE:
                PostFilterUsage postFilterUsage = new PostFilterUsage(postFilter.name, type, PostFilterUsage.NO_USAGE);
                SavePostFilterUsage.savePostFilterUsage(redditDataRoomDatabase, executor, postFilterUsage);
                break;
            case PostFilterUsage.SUBREDDIT_TYPE:
            case PostFilterUsage.USER_TYPE:
            case PostFilterUsage.MULTIREDDIT_TYPE:
                editAndPostFilterUsageNameOfUsage(type, null);
                break;
        }
    }

    private void editAndPostFilterUsageNameOfUsage(int type, String nameOfUsage) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_post_filter_name_of_usage, null);
        TextInputLayout textInputLayout = dialogView.findViewById(R.id.text_input_layout_edit_post_filter_name_of_usage_dialog);
        TextInputEditText textInputEditText = dialogView.findViewById(R.id.text_input_edit_text_edit_post_filter_name_of_usage_dialog);
        int primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        textInputLayout.setBoxStrokeColor(primaryTextColor);
        textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        textInputEditText.setTextColor(primaryTextColor);
        if (nameOfUsage != null && !nameOfUsage.equals(PostFilterUsage.NO_USAGE)) {
            textInputEditText.setText(nameOfUsage);
        }
        textInputEditText.requestFocus();
        int titleStringId = R.string.subreddit;
        switch (type) {
            case PostFilterUsage.SUBREDDIT_TYPE:
                textInputEditText.setHint(R.string.settings_tab_subreddit_name);
                break;
            case PostFilterUsage.USER_TYPE:
                textInputEditText.setHint(R.string.settings_tab_username);
                titleStringId = R.string.user;
                break;
            case PostFilterUsage.MULTIREDDIT_TYPE:
                textInputEditText.setHint(R.string.settings_tab_multi_reddit_name);
                titleStringId = R.string.multi_reddit;
                break;
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(titleStringId)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (editTextDialogInterface, i1)
                        -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(textInputEditText.getWindowToken(), 0);
                    }

                    PostFilterUsage postFilterUsage;
                    if (textInputEditText.getText().toString().equals("")) {
                        postFilterUsage = new PostFilterUsage(postFilter.name, type, PostFilterUsage.NO_USAGE);
                    } else {
                        postFilterUsage = new PostFilterUsage(postFilter.name, type, textInputEditText.getText().toString());
                    }

                    SavePostFilterUsage.savePostFilterUsage(redditDataRoomDatabase, executor, postFilterUsage);
                })
                .setNegativeButton(R.string.cancel, null)
                .setOnDismissListener(editTextDialogInterface -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(textInputEditText.getWindowToken(), 0);
                    }
                })
                .show();
    }

    public void editPostFilterUsage(PostFilterUsage postFilterUsage) {
        editAndPostFilterUsageNameOfUsage(postFilterUsage.usage, postFilterUsage.nameOfUsage);
    }

    public void deletePostFilterUsage(PostFilterUsage postFilterUsage) {
        DeletePostFilterUsage.deletePostFilterUsage(redditDataRoomDatabase, executor, postFilterUsage);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    @Override
    protected SharedPreferences getDefaultSharedPreferences() {
        return sharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return customThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, collapsingToolbarLayout, toolbar);
    }
}