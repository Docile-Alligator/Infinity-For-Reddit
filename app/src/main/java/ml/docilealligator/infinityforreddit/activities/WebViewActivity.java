package ml.docilealligator.infinityforreddit.activities;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityWebViewBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class WebViewActivity extends BaseActivity {

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private String url;
    private ActivityWebViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicableBelowAndroid16();

        super.onCreate(savedInstanceState);

        try {
            binding = ActivityWebViewBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
        } catch (InflateException ie) {
            Log.e("WebViewActivity", "Failed to inflate WebViewActivity: " + ie.getMessage());
            Toast.makeText(WebViewActivity.this, R.string.no_system_webview_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        applyCustomTheme();

        if (isImmersiveInterface()) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, true);

                    setMargins(binding.toolbarWebViewActivity,
                            allInsets.left,
                            allInsets.top,
                            allInsets.right,
                            BaseActivity.IGNORE_MARGIN);

                    binding.webViewWebViewActivity.setPadding(
                            allInsets.left,
                            0,
                            allInsets.right,
                            allInsets.bottom);

                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        setSupportActionBar(binding.toolbarWebViewActivity);

        if (mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null) == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.webViewWebViewActivity.setAnonymous(true);
        }

        binding.webViewWebViewActivity.getSettings().setJavaScriptEnabled(true);
        binding.webViewWebViewActivity.getSettings().setDomStorageEnabled(true);

        url = getIntent().getDataString();
        if (savedInstanceState == null) {
            binding.toolbarWebViewActivity.setTitle(url);
            binding.webViewWebViewActivity.loadUrl(url);
        }

        WebViewClient client = new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                WebViewActivity.this.url = url;
                binding.toolbarWebViewActivity.setTitle(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                binding.toolbarWebViewActivity.setTitle(view.getTitle());
            }
        };
        binding.webViewWebViewActivity.setWebViewClient(client);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.webViewWebViewActivity.canGoBack()) {
                    binding.webViewWebViewActivity.goBack();
                } else {
                    setEnabled(false);
                    triggerBackPress();
                }
            }
        });
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutWebViewActivity,
                null, binding.toolbarWebViewActivity);
        Drawable closeIcon = Utils.getTintedDrawable(this, R.drawable.ic_close_24dp, mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
        binding.toolbarWebViewActivity.setNavigationIcon(closeIcon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web_view_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_refresh_web_view_activity) {
            binding.webViewWebViewActivity.reload();
            return true;
        } else if (item.getItemId() == R.id.action_share_link_web_view_activity) {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, url);
                startActivity(Intent.createChooser(intent, getString(R.string.share)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.no_activity_found_for_share, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (item.getItemId() == R.id.action_copy_link_web_view_activity) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("simple text", url);
                clipboard.setPrimaryClip(clip);
                if (android.os.Build.VERSION.SDK_INT < 33) {
                    Toast.makeText(this, R.string.copy_success, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.copy_link_failed, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (item.getItemId() == R.id.action_open_external_browser_web_view_activity) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.no_activity_found_for_external_browser, Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.webViewWebViewActivity.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        binding.webViewWebViewActivity.restoreState(savedInstanceState);
    }
}