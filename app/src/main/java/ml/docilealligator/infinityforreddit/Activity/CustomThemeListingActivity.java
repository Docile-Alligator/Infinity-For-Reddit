package ml.docilealligator.infinityforreddit.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Adapter.CustomThemeListingRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.AsyncTask.ChangeThemeNameAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.DeleteThemeAsyncTask;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeViewModel;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Fragment.CustomThemeOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Fragment.SelectBaseThemeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class CustomThemeListingActivity extends BaseActivity implements CustomThemeOptionsBottomSheetFragment.CustomThemeOptionsBottomSheetFragmentListener {

    @BindView(R.id.appbar_layout_customize_theme_listing_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_customize_theme_listing_activity)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_customize_theme_listing_activity)
    RecyclerView recyclerView;
    @BindView(R.id.fab_custom_theme_listing_activity)
    FloatingActionButton fab;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    public CustomThemeViewModel customThemeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_theme_listing);

        ButterKnife.bind(this);

        applyCustomTheme();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CustomThemeListingRecyclerViewAdapter adapter = new CustomThemeListingRecyclerViewAdapter(this,
                CustomThemeWrapper.getPredefinedThemes(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        customThemeViewModel = new ViewModelProvider(this,
                new CustomThemeViewModel.Factory(redditDataRoomDatabase))
                .get(CustomThemeViewModel.class);
        customThemeViewModel.getAllCustomThemes().observe(this, adapter::setUserThemes);

        fab.setOnClickListener(view -> {
            SelectBaseThemeBottomSheetFragment selectBaseThemeBottomSheetFragment = new SelectBaseThemeBottomSheetFragment();
            selectBaseThemeBottomSheetFragment.show(getSupportFragmentManager(), selectBaseThemeBottomSheetFragment.getTag());
        });
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
    protected SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return customThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
    }

    @Override
    public void changeName(String oldName) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_theme_name, null);
        EditText themeNameEditText = dialogView.findViewById(R.id.theme_name_edit_text_edit_theme_name_dialog);
        themeNameEditText.setText(oldName);
        themeNameEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.edit_theme_name)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (dialogInterface, i)
                        -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(themeNameEditText.getWindowToken(), 0);
                    }
                    new ChangeThemeNameAsyncTask(redditDataRoomDatabase, oldName, themeNameEditText.getText().toString());
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(themeNameEditText.getWindowToken(), 0);
                    }
                })
                .setOnDismissListener(dialogInterface -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(themeNameEditText.getWindowToken(), 0);
                    }
                })
                .show();
    }

    @Override
    public void delete(String name) {
        new DeleteThemeAsyncTask(redditDataRoomDatabase, name).execute();
    }
}
