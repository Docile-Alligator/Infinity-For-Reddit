package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;
import android.view.InflateException;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.FetchMyInfo;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.asynctasks.ParseAndInsertNewAccount;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityLoginBinding;
import ml.docilealligator.infinityforreddit.events.NewUserLoggedInEvent;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends BaseActivity {

    private static final String IS_AGREE_TO_USER_AGGREMENT_STATE = "IATUAS";

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
    private String authCode;
    private boolean isAgreeToUserAgreement = false;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());

        try {
            setContentView(binding.getRoot());
        } catch (InflateException ie) {
            Log.e("LoginActivity", "Failed to inflate LoginActivity: " + ie.getMessage());
            Toast.makeText(LoginActivity.this, R.string.no_system_webview_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        setSupportActionBar(binding.toolbarLoginActivity);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            isAgreeToUserAgreement = savedInstanceState.getBoolean(IS_AGREE_TO_USER_AGGREMENT_STATE);
        }

        binding.webviewLoginActivity.getSettings().setJavaScriptEnabled(true);

        Uri baseUri = Uri.parse(APIUtils.OAUTH_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter(APIUtils.CLIENT_ID_KEY, APIUtils.CLIENT_ID);
        uriBuilder.appendQueryParameter(APIUtils.RESPONSE_TYPE_KEY, APIUtils.RESPONSE_TYPE);
        uriBuilder.appendQueryParameter(APIUtils.STATE_KEY, APIUtils.STATE);
        uriBuilder.appendQueryParameter(APIUtils.REDIRECT_URI_KEY, APIUtils.REDIRECT_URI);
        uriBuilder.appendQueryParameter(APIUtils.DURATION_KEY, APIUtils.DURATION);
        uriBuilder.appendQueryParameter(APIUtils.SCOPE_KEY, APIUtils.SCOPE);

        String url = uriBuilder.toString();

        binding.fabLoginActivity.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginChromeCustomTabActivity.class);
            startActivity(intent);
            finish();
        });

        CookieManager.getInstance().removeAllCookies(aBoolean -> {
        });

        binding.webviewLoginActivity.loadUrl(url);
        binding.webviewLoginActivity.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("&code=") || url.contains("?code=")) {
                    Uri uri = Uri.parse(url);
                    String state = uri.getQueryParameter("state");
                    if (state.equals(APIUtils.STATE)) {
                        authCode = uri.getQueryParameter("code");

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
                                                            Toast.makeText(LoginActivity.this, R.string.parse_user_info_error, Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(LoginActivity.this, R.string.cannot_fetch_user_info, Toast.LENGTH_SHORT).show();
                                                        }

                                                        finish();
                                                    }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(LoginActivity.this, R.string.parse_json_response_error, Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, R.string.retrieve_token_error, Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                Toast.makeText(LoginActivity.this, R.string.retrieve_token_error, Toast.LENGTH_SHORT).show();
                                t.printStackTrace();
                                finish();
                            }
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } else if (url.contains("error=access_denied")) {
                    Toast.makeText(LoginActivity.this, R.string.access_denied, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    view.loadUrl(url);
                }

                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        if (!isAgreeToUserAgreement) {
            TextView messageTextView = new TextView(this);
            int padding = (int) Utils.convertDpToPixel(24, this);
            messageTextView.setPaddingRelative(padding, padding, padding, padding);
            SpannableString message = new SpannableString(getString(R.string.user_agreement_message, "https://www.redditinc.com/policies/user-agreement", "https://docile-alligator.github.io"));
            Linkify.addLinks(message, Linkify.WEB_URLS);
            messageTextView.setMovementMethod(BetterLinkMovementMethod.newInstance().setOnLinkClickListener(new BetterLinkMovementMethod.OnLinkClickListener() {
                @Override
                public boolean onClick(TextView textView, String url) {
                    Intent intent = new Intent(LoginActivity.this, LinkResolverActivity.class);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
            }));
            messageTextView.setLinkTextColor(getResources().getColor(R.color.colorAccent));
            messageTextView.setText(message);
            new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                    .setTitle(getString(R.string.user_agreement_dialog_title))
                    .setView(messageTextView)
                    .setPositiveButton(R.string.agree, (dialogInterface, i) -> isAgreeToUserAgreement = true)
                    .setNegativeButton(R.string.do_not_agree, (dialogInterface, i) -> finish())
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_AGREE_TO_USER_AGGREMENT_STATE, isAgreeToUserAgreement);
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutLoginActivity, null, binding.toolbarLoginActivity);
        binding.twoFaInfOTextViewLoginActivity.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        Drawable infoDrawable = Utils.getTintedDrawable(this, R.drawable.ic_info_preference_24dp, mCustomThemeWrapper.getPrimaryIconColor());
        binding.twoFaInfOTextViewLoginActivity.setCompoundDrawablesWithIntrinsicBounds(infoDrawable, null, null, null);
        applyFABTheme(binding.fabLoginActivity);
        if (typeface != null) {
            binding.twoFaInfOTextViewLoginActivity.setTypeface(typeface);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }
}
