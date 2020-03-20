package ml.docilealligator.infinityforreddit.Activity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Adapter.CustomizeThemeRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCustomThemeAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.InsertCustomThemeAsyncTask;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomTheme;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeSettingsItem;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeViewModel;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils;

public class CustomizeThemeActivity extends BaseActivity {

    public static final String EXTRA_THEME_TYPE = "ETT";
    public static final int EXTRA_LIGHT_THEME = 0;
    public static final int EXTRA_DARK_THEME = 1;
    public static final int EXTRA_AMOLED_THEME = 2;
    public static final String EXTRA_THEME_NAME = "ETN";

    @BindView(R.id.appbar_layout_customize_theme_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_customize_theme_activity)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_customize_theme_activity)
    RecyclerView recyclerView;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;
    @Inject
    CustomThemeWrapper customThemeWrapper;

    public CustomThemeViewModel customThemeViewModel;

    private int themeType;
    private String themeName;
    private ArrayList<CustomThemeSettingsItem> customThemeSettingsItems;
    private CustomizeThemeRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_theme);

        ButterKnife.bind(this);

        applyCustomTheme();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new CustomizeThemeRecyclerViewAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        customThemeSettingsItems = new ArrayList<>();

        int androidVersion = Build.VERSION.SDK_INT;
        if (getIntent().hasExtra(EXTRA_THEME_TYPE)) {
            customThemeViewModel = new ViewModelProvider(this, new CustomThemeViewModel.Factory(redditDataRoomDatabase))
                    .get(CustomThemeViewModel.class);
            LiveData<CustomTheme> customThemeLiveData;
            themeType = getIntent().getIntExtra(EXTRA_THEME_TYPE, EXTRA_LIGHT_THEME);
            switch (themeType) {
                case EXTRA_DARK_THEME:
                    setTitle(getString(R.string.customize_dark_theme_fragment_title));
                    customThemeLiveData = customThemeViewModel.getDarkCustomTheme();
                    break;
                case EXTRA_AMOLED_THEME:
                    setTitle(getString(R.string.customize_amoled_theme_fragment_title));
                    customThemeLiveData = customThemeViewModel.getAmoledCustomTheme();
                    break;
                default:
                    setTitle(getString(R.string.customize_light_theme_fragment_title));
                    customThemeLiveData = customThemeViewModel.getLightCustomTheme();
                    break;
            }

            customThemeLiveData.observe(this, customTheme -> {
                if (customTheme == null) {
                    switch (themeType) {
                        case EXTRA_DARK_THEME:
                            customThemeSettingsItems = CustomThemeSettingsItem.convertCustomThemeToSettingsItem(
                                    CustomizeThemeActivity.this,
                                    CustomThemeWrapper.getIndigoDark(CustomizeThemeActivity.this));
                            break;
                        case EXTRA_AMOLED_THEME:
                            customThemeSettingsItems = CustomThemeSettingsItem.convertCustomThemeToSettingsItem(
                                    CustomizeThemeActivity.this,
                                    CustomThemeWrapper.getIndigoAmoled(CustomizeThemeActivity.this));
                            break;
                        default:
                            customThemeSettingsItems = CustomThemeSettingsItem.convertCustomThemeToSettingsItem(
                                    CustomizeThemeActivity.this,
                                    CustomThemeWrapper.getIndigo(CustomizeThemeActivity.this));
                    }
                } else {
                    customThemeSettingsItems = CustomThemeSettingsItem.convertCustomThemeToSettingsItem(CustomizeThemeActivity.this, customTheme);
                }

                if (androidVersion < Build.VERSION_CODES.O) {
                    customThemeSettingsItems.get(customThemeSettingsItems.size() - 2).itemDetails = getString(R.string.theme_item_available_on_android_8);
                }
                if (androidVersion < Build.VERSION_CODES.M) {
                    customThemeSettingsItems.get(customThemeSettingsItems.size() - 3).itemDetails = getString(R.string.theme_item_available_on_android_6);
                }

                adapter.setCustomThemeSettingsItem(customThemeSettingsItems);
            });
        } else {
            themeName = getIntent().getStringExtra(EXTRA_THEME_NAME);
            setTitle(themeName);
            new GetCustomThemeAsyncTask(redditDataRoomDatabase, themeName,
                    customTheme -> {
                customThemeSettingsItems = CustomThemeSettingsItem.convertCustomThemeToSettingsItem(CustomizeThemeActivity.this, customTheme);
                adapter.setCustomThemeSettingsItem(customThemeSettingsItems);
                    }).execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.customize_theme_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save_customize_theme_activity:
                if (themeName == null) {
                    switch (themeType) {
                        case CustomThemeSharedPreferencesUtils.DARK:
                            themeName = "Indigo Dark";
                            break;
                        case CustomThemeSharedPreferencesUtils.AMOLED:
                            themeName = "Indigo Amoled";
                            break;
                        default:
                            themeName = "Indigo";
                    }
                }

                CustomTheme customTheme = CustomTheme.convertSettingsItemsToCustomTheme(customThemeSettingsItems, themeName);

                switch (themeType) {
                    case CustomThemeSharedPreferencesUtils.DARK:
                        customTheme.isLightTheme = false;
                        customTheme.isDarkTheme = true;
                        customTheme.isAmoledTheme = false;
                        break;
                    case CustomThemeSharedPreferencesUtils.AMOLED:
                        customTheme.isLightTheme = false;
                        customTheme.isDarkTheme = false;
                        customTheme.isAmoledTheme = true;
                        break;
                    default:
                        customTheme.isLightTheme = true;
                        customTheme.isDarkTheme = false;
                        customTheme.isAmoledTheme = false;
                }

                new InsertCustomThemeAsyncTask(redditDataRoomDatabase, customTheme, () -> {
                    Toast.makeText(CustomizeThemeActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
                    finish();
                }).execute();

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
