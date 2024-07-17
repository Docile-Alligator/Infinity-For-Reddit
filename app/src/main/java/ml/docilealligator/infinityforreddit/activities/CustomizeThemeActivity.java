package ml.docilealligator.infinityforreddit.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.CustomizeThemeRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.OnlineCustomThemeAPI;
import ml.docilealligator.infinityforreddit.asynctasks.GetCustomTheme;
import ml.docilealligator.infinityforreddit.asynctasks.InsertCustomTheme;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeSettingsItem;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customtheme.OnlineCustomThemeMetadata;
import ml.docilealligator.infinityforreddit.databinding.ActivityCustomizeThemeBinding;
import ml.docilealligator.infinityforreddit.events.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.utils.CustomThemeSharedPreferencesUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CustomizeThemeActivity extends BaseActivity {

    public static final String EXTRA_THEME_TYPE = "ETT";
    public static final int EXTRA_LIGHT_THEME = CustomThemeSharedPreferencesUtils.LIGHT;
    public static final int EXTRA_DARK_THEME = CustomThemeSharedPreferencesUtils.DARK;
    public static final int EXTRA_AMOLED_THEME = CustomThemeSharedPreferencesUtils.AMOLED;
    public static final String EXTRA_THEME_NAME = "ETN";
    public static final String EXTRA_ONLINE_CUSTOM_THEME_METADATA = "EOCTM";
    public static final String EXTRA_INDEX_IN_THEME_LIST = "EIITL";
    public static final String EXTRA_IS_PREDEFIINED_THEME = "EIPT";
    public static final String EXTRA_CREATE_THEME = "ECT";
    public static final String RETURN_EXTRA_THEME_NAME = "RETN";
    public static final String RETURN_EXTRA_PRIMARY_COLOR = "REPC";
    public static final String RETURN_EXTRA_INDEX_IN_THEME_LIST = "REIITL";
    private static final String CUSTOM_THEME_SETTINGS_ITEMS_STATE = "CTSIS";
    private static final String THEME_NAME_STATE = "TNS";

    @Inject
    @Named("online_custom_themes")
    Retrofit onlineCustomThemesRetrofit;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("light_theme")
    SharedPreferences lightThemeSharedPreferences;
    @Inject
    @Named("dark_theme")
    SharedPreferences darkThemeSharedPreferences;
    @Inject
    @Named("amoled_theme")
    SharedPreferences amoledThemeSharedPreferences;
    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    @Inject
    Executor mExecutor;

    private String themeName;
    private OnlineCustomThemeMetadata onlineCustomThemeMetadata;
    private boolean isPredefinedTheme;
    private ArrayList<CustomThemeSettingsItem> customThemeSettingsItems;
    private CustomizeThemeRecyclerViewAdapter adapter;
    private ActivityCustomizeThemeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivityCustomizeThemeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        setSupportActionBar(binding.toolbarCustomizeThemeActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getBooleanExtra(EXTRA_CREATE_THEME, false)) {
            setTitle(R.string.customize_theme_activity_create_theme_label);
        }

        if (savedInstanceState != null) {
            customThemeSettingsItems = savedInstanceState.getParcelableArrayList(CUSTOM_THEME_SETTINGS_ITEMS_STATE);
            themeName = savedInstanceState.getString(THEME_NAME_STATE);
        }

        int androidVersion = Build.VERSION.SDK_INT;

        if (customThemeSettingsItems == null) {
            if (getIntent().hasExtra(EXTRA_THEME_TYPE)) {
                int themeType = getIntent().getIntExtra(EXTRA_THEME_TYPE, EXTRA_LIGHT_THEME);
                GetCustomTheme.getCustomTheme(mExecutor, new Handler(), redditDataRoomDatabase, themeType, customTheme -> {
                    if (customTheme == null) {
                        isPredefinedTheme = true;
                        switch (themeType) {
                            case EXTRA_DARK_THEME:
                                customThemeSettingsItems = CustomThemeSettingsItem.convertCustomThemeToSettingsItem(
                                        CustomizeThemeActivity.this,
                                        CustomThemeWrapper.getIndigoDark(CustomizeThemeActivity.this),
                                        androidVersion);
                                themeName = getString(R.string.theme_name_indigo_dark);
                                break;
                            case EXTRA_AMOLED_THEME:
                                customThemeSettingsItems = CustomThemeSettingsItem.convertCustomThemeToSettingsItem(
                                        CustomizeThemeActivity.this,
                                        CustomThemeWrapper.getIndigoAmoled(CustomizeThemeActivity.this),
                                        androidVersion);
                                themeName = getString(R.string.theme_name_indigo_amoled);
                                break;
                            default:
                                customThemeSettingsItems = CustomThemeSettingsItem.convertCustomThemeToSettingsItem(
                                        CustomizeThemeActivity.this,
                                        CustomThemeWrapper.getIndigo(CustomizeThemeActivity.this),
                                        androidVersion);
                                themeName = getString(R.string.theme_name_indigo);
                        }
                    } else {
                        customThemeSettingsItems = CustomThemeSettingsItem.convertCustomThemeToSettingsItem(
                                CustomizeThemeActivity.this, customTheme, androidVersion);
                        themeName = customTheme.name;
                    }

                    adapter = new CustomizeThemeRecyclerViewAdapter(this, customThemeWrapper, themeName);
                    binding.recyclerViewCustomizeThemeActivity.setAdapter(adapter);
                    adapter.setCustomThemeSettingsItem(customThemeSettingsItems);
                });
            } else {
                isPredefinedTheme = getIntent().getBooleanExtra(EXTRA_IS_PREDEFIINED_THEME, false);
                themeName = getIntent().getStringExtra(EXTRA_THEME_NAME);
                onlineCustomThemeMetadata = getIntent().getParcelableExtra(EXTRA_ONLINE_CUSTOM_THEME_METADATA);

                adapter = new CustomizeThemeRecyclerViewAdapter(this, customThemeWrapper, themeName);
                binding.recyclerViewCustomizeThemeActivity.setAdapter(adapter);
                if (isPredefinedTheme) {
                    customThemeSettingsItems = CustomThemeSettingsItem.convertCustomThemeToSettingsItem(
                            CustomizeThemeActivity.this,
                            CustomThemeWrapper.getPredefinedCustomTheme(this, themeName),
                            androidVersion);

                    adapter = new CustomizeThemeRecyclerViewAdapter(this, customThemeWrapper, themeName);
                    binding.recyclerViewCustomizeThemeActivity.setAdapter(adapter);
                    adapter.setCustomThemeSettingsItem(customThemeSettingsItems);
                } else {
                    if (onlineCustomThemeMetadata != null) {
                        binding.progressBarCustomizeThemeActivity.setVisibility(View.VISIBLE);
                        onlineCustomThemesRetrofit.create(OnlineCustomThemeAPI.class)
                                .getCustomTheme(onlineCustomThemeMetadata.name, onlineCustomThemeMetadata.username)
                                .enqueue(new Callback<>() {
                                    @Override
                                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                        if (response.isSuccessful()) {
                                            customThemeSettingsItems = CustomThemeSettingsItem.convertCustomThemeToSettingsItem(
                                                    CustomizeThemeActivity.this,
                                                    CustomTheme.fromJson(response.body()),
                                                    androidVersion);

                                            adapter.setCustomThemeSettingsItem(customThemeSettingsItems);

                                            binding.progressBarCustomizeThemeActivity.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(CustomizeThemeActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                                        Toast.makeText(CustomizeThemeActivity.this, R.string.cannot_download_theme_data, Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                    } else {
                        GetCustomTheme.getCustomTheme(mExecutor, new Handler(), redditDataRoomDatabase,
                                themeName, customTheme -> {
                                    customThemeSettingsItems = CustomThemeSettingsItem.convertCustomThemeToSettingsItem(
                                            CustomizeThemeActivity.this, customTheme, androidVersion);

                                    adapter.setCustomThemeSettingsItem(customThemeSettingsItems);
                                });
                    }
                }
            }
        } else {
            adapter = new CustomizeThemeRecyclerViewAdapter(this, customThemeWrapper, themeName);
            binding.recyclerViewCustomizeThemeActivity.setAdapter(adapter);
            adapter.setCustomThemeSettingsItem(customThemeSettingsItems);
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
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_preview_customize_theme_activity) {
            Intent intent = new Intent(this, CustomThemePreviewActivity.class);
            intent.putParcelableArrayListExtra(CustomThemePreviewActivity.EXTRA_CUSTOM_THEME_SETTINGS_ITEMS, customThemeSettingsItems);
            startActivity(intent);

            return true;
        } else if (itemId == R.id.action_save_customize_theme_activity) {
            if (adapter != null) {
                themeName = adapter.getThemeName();
                if (themeName.equals("")) {
                    Snackbar.make(binding.coordinatorCustomizeThemeActivity, R.string.no_theme_name, Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                CustomTheme customTheme = CustomTheme.convertSettingsItemsToCustomTheme(customThemeSettingsItems, themeName);
                if (onlineCustomThemeMetadata != null && onlineCustomThemeMetadata.username.equals(accountName)) {
                    // This custom theme is uploaded by the current user
                    final int[] option = {0};
                    new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                            .setTitle(R.string.save_theme_options_title)
                            //.setMessage(R.string.save_theme_options_message)
                            .setSingleChoiceItems(R.array.save_theme_options, 0, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    option[0] = which;
                                }
                            })
                            .setPositiveButton(R.string.ok, (dialogInterface, which) -> {
                                switch (option[0]) {
                                    case 0:
                                        saveThemeLocally(customTheme);
                                        break;
                                    case 1:
                                        saveThemeOnline(customTheme);
                                        break;
                                    case 2:
                                        saveThemeLocally(customTheme);
                                        saveThemeOnline(customTheme);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                } else {
                    saveThemeLocally(customTheme);
                }
            }

            return true;
        }

        return false;
    }

    private void saveThemeLocally(CustomTheme customTheme) {
        InsertCustomTheme.insertCustomTheme(mExecutor, new Handler(), redditDataRoomDatabase, lightThemeSharedPreferences,
                darkThemeSharedPreferences, amoledThemeSharedPreferences, customTheme,
                false, () -> {
                    Toast.makeText(CustomizeThemeActivity.this, R.string.theme_saved_locally, Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(new RecreateActivityEvent());
                    finish();
                });
    }

    private void saveThemeOnline(CustomTheme customTheme) {
        onlineCustomThemesRetrofit.create(OnlineCustomThemeAPI.class).modifyTheme(
                onlineCustomThemeMetadata.id, customTheme.name,
                customTheme.getJSONModel(),
                ('#' + Integer.toHexString(customTheme.colorPrimary)).toUpperCase()
        ).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CustomizeThemeActivity.this, R.string.theme_saved_online, Toast.LENGTH_SHORT).show();
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(RETURN_EXTRA_INDEX_IN_THEME_LIST, getIntent().getIntExtra(EXTRA_INDEX_IN_THEME_LIST, -1));
                    returnIntent.putExtra(RETURN_EXTRA_THEME_NAME, customTheme.name);
                    returnIntent.putExtra(RETURN_EXTRA_PRIMARY_COLOR, '#' + Integer.toHexString(customTheme.colorPrimary));
                    setResult(RESULT_OK, returnIntent);

                    finish();
                } else {
                    Toast.makeText(CustomizeThemeActivity.this, R.string.upload_theme_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                Toast.makeText(CustomizeThemeActivity.this, R.string.upload_theme_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter != null) {
            outState.putParcelableArrayList(CUSTOM_THEME_SETTINGS_ITEMS_STATE, customThemeSettingsItems);
            outState.putString(THEME_NAME_STATE, adapter.getThemeName());
        }
    }

    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.discard)
                .setPositiveButton(R.string.discard_dialog_button, (dialogInterface, i)
                        -> super.onBackPressed())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return sharedPreferences;
    }

    @Override
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return customThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutCustomizeThemeActivity, binding.collapsingToolbarLayoutCustomizeThemeActivity, binding.toolbarCustomizeThemeActivity);
        binding.coordinatorCustomizeThemeActivity.setBackgroundColor(customThemeWrapper.getBackgroundColor());
        binding.progressBarCustomizeThemeActivity.setIndeterminateTintList(ColorStateList.valueOf(customThemeWrapper.getColorAccent()));
    }
}
