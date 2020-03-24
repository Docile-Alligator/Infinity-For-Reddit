package ml.docilealligator.infinityforreddit.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Adapter.CustomThemeListingRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeViewModel;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class CustomThemeListingActivity extends BaseActivity {

    @BindView(R.id.appbar_layout_customize_theme_listing_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_customize_theme_listing_activity)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_customize_theme_listing_activity)
    RecyclerView recyclerView;
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
                CustomThemeWrapper.getPredifinedThemes(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        customThemeViewModel = new ViewModelProvider(this,
                new CustomThemeViewModel.Factory(redditDataRoomDatabase))
                .get(CustomThemeViewModel.class);
        customThemeViewModel.getAllCustomThemes().observe(this, customThemes -> adapter.setUserThemes(customThemes));
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
}
