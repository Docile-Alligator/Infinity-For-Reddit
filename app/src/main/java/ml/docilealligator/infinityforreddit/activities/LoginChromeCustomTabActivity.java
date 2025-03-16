package ml.docilealligator.infinityforreddit.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsService;

import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.account.FetchMyInfo;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.asynctasks.ParseAndInsertNewAccount;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityLoginChromeCustomTabBinding;
import ml.docilealligator.infinityforreddit.events.NewUserLoggedInEvent;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginChromeCustomTabActivity extends BaseActivity {

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private ActivityLoginChromeCustomTabBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        binding = ActivityLoginChromeCustomTabBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        /*ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/

        applyCustomTheme();

        setSupportActionBar(binding.toolbarLoginChromeCustomTabActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        openLoginPage();

        binding.openWebpageButtonLoginChromeCustomTabActivity.setOnClickListener(view -> {
            openLoginPage();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri == null) {
            binding.openWebpageButtonLoginChromeCustomTabActivity.setVisibility(View.VISIBLE);
            return;
        }

        binding.openWebpageButtonLoginChromeCustomTabActivity.setVisibility(View.GONE);

        String authCode = uri.getQueryParameter("code");
        if (authCode != null) {
            String state = uri.getQueryParameter("state");
            if (APIUtils.STATE.equals(state)) {
                Map<String, String> params = new HashMap<>();
                params.put(APIUtils.GRANT_TYPE_KEY, "authorization_code");
                params.put("code", authCode);
                params.put(APIUtils.REDIRECT_URI_KEY, APIUtils.REDIRECT_URI);

                RedditAPI api = mRetrofit.create(RedditAPI.class);
                Call<String> accessTokenCall = api.getAccessToken(APIUtils.getHttpBasicAuthHeader(), params);
                accessTokenCall.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            try {
                                String accountResponse = response.body();
                                if (accountResponse == null) {
                                    //Handle error
                                    return;
                                }

                                JSONObject responseJSON = new JSONObject(accountResponse);
                                String accessToken = responseJSON.getString(APIUtils.ACCESS_TOKEN_KEY);
                                String refreshToken = responseJSON.getString(APIUtils.REFRESH_TOKEN_KEY);

                                FetchMyInfo.fetchAccountInfo(mOauthRetrofit, mRedditDataRoomDatabase,
                                        accessToken, new FetchMyInfo.FetchMyInfoListener() {
                                            @Override
                                            public void onFetchMyInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma) {
                                                mCurrentAccountSharedPreferences.edit().putString(SharedPreferencesUtils.ACCESS_TOKEN, accessToken)
                                                        .putString(SharedPreferencesUtils.ACCOUNT_NAME, name)
                                                        .putString(SharedPreferencesUtils.ACCOUNT_IMAGE_URL, profileImageUrl).apply();
                                                mCurrentAccountSharedPreferences.edit().remove(SharedPreferencesUtils.SUBSCRIBED_THINGS_SYNC_TIME).apply();
                                                ParseAndInsertNewAccount.parseAndInsertNewAccount(mExecutor, new Handler(), name, accessToken, refreshToken, profileImageUrl, bannerImageUrl,
                                                        karma, authCode, mRedditDataRoomDatabase.accountDao(),
                                                        () -> {
                                                            EventBus.getDefault().post(new NewUserLoggedInEvent());
                                                            finish();
                                                        });
                                            }

                                            @Override
                                            public void onFetchMyInfoFailed(boolean parseFailed) {
                                                if (parseFailed) {
                                                    Toast.makeText(LoginChromeCustomTabActivity.this, R.string.parse_user_info_error, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(LoginChromeCustomTabActivity.this, R.string.cannot_fetch_user_info, Toast.LENGTH_SHORT).show();
                                                }

                                                finish();
                                            }
                                        });
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(LoginChromeCustomTabActivity.this, R.string.parse_json_response_error, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(LoginChromeCustomTabActivity.this, R.string.retrieve_token_error, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(LoginChromeCustomTabActivity.this, R.string.retrieve_token_error, Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                        finish();
                    }
                });
            } else {
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                finish();
            }

        } else if ("access_denied".equals(uri.getQueryParameter("error"))) {
            Toast.makeText(this, R.string.access_denied, Toast.LENGTH_SHORT).show();
            finish();
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutLoginChromeCustomTabActivity, null, binding.toolbarLoginChromeCustomTabActivity);
        binding.openWebpageButtonLoginChromeCustomTabActivity.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        binding.openWebpageButtonLoginChromeCustomTabActivity.setBackgroundColor(mCustomThemeWrapper.getColorPrimaryLightTheme());
        if (typeface != null) {
            binding.openWebpageButtonLoginChromeCustomTabActivity.setTypeface(typeface);
        }
    }

    private void openLoginPage() {
        ArrayList<ResolveInfo> resolveInfos = getCustomTabsPackages(getPackageManager());
        if (!resolveInfos.isEmpty()) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            // add share action to menu list
            builder.setShareState(CustomTabsIntent.SHARE_STATE_ON);
            builder.setDefaultColorSchemeParams(
                    new CustomTabColorSchemeParams.Builder()
                            .setToolbarColor(mCustomThemeWrapper.getColorPrimary())
                            .build());
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setPackage(resolveInfos.get(0).activityInfo.packageName);
            customTabsIntent.intent.putExtra("com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB", true);

            try {
                Uri.Builder uriBuilder = Uri.parse(APIUtils.OAUTH_URL).buildUpon();
                uriBuilder.appendQueryParameter(APIUtils.CLIENT_ID_KEY, APIUtils.CLIENT_ID);
                uriBuilder.appendQueryParameter(APIUtils.RESPONSE_TYPE_KEY, APIUtils.RESPONSE_TYPE);
                uriBuilder.appendQueryParameter(APIUtils.STATE_KEY, APIUtils.STATE);
                uriBuilder.appendQueryParameter(APIUtils.REDIRECT_URI_KEY, APIUtils.REDIRECT_URI);
                uriBuilder.appendQueryParameter(APIUtils.DURATION_KEY, APIUtils.DURATION);
                uriBuilder.appendQueryParameter(APIUtils.SCOPE_KEY, APIUtils.SCOPE);

                customTabsIntent.launchUrl(this, uriBuilder.build());
            } catch (ActivityNotFoundException e) {
                Snackbar.make(binding.getRoot(), R.string.custom_tab_not_available, Snackbar.LENGTH_LONG).show();
            }
        } else {
            Snackbar.make(binding.getRoot(), R.string.custom_tab_not_available, Snackbar.LENGTH_LONG).show();
        }
    }

    private ArrayList<ResolveInfo> getCustomTabsPackages(PackageManager pm) {
        // Get default VIEW intent handler.
        Intent activityIntent = new Intent()
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.fromParts("http", "", null));

        // Get all apps that can handle VIEW intents.
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        ArrayList<ResolveInfo> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            // Check if this package also resolves the Custom Tabs service.
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info);
            }
        }
        return packagesSupportingCustomTabs;
    }
}