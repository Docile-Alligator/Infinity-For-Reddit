package ml.docilealligator.infinityforreddit.Activity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import retrofit2.Retrofit;

public class SendPrivateMessageActivity extends BaseActivity {
    public static final String EXTRA_RECIPIENT_USERNAME = "ERU";
    @BindView(R.id.coordinator_layout_send_private_message_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_send_private_message_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_send_private_message_activity)
    Toolbar toolbar;
    @BindView(R.id.username_edit_text_send_private_message_activity)
    EditText usernameEditText;
    @BindView(R.id.divider_1_send_private_message_activity)
    View divider1;
    @BindView(R.id.subjet_edit_text_send_private_message_activity)
    EditText subjectEditText;
    @BindView(R.id.divider_2_send_private_message_activity)
    View divider2;
    @BindView(R.id.content_edit_text_send_private_message_activity)
    EditText messageEditText;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_private_message);

        ButterKnife.bind(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(appBarLayout);
        }

        setSupportActionBar(toolbar);

        String username = getIntent().getStringExtra(EXTRA_RECIPIENT_USERNAME);
        if (username != null) {
            usernameEditText.setText(username);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_private_message_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_send_send_private_message_activity) {

        }
        return false;
    }

    @Override
    protected SharedPreferences getDefaultSharedPreferences() {
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
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        usernameEditText.setTextColor(primaryTextColor);
        subjectEditText.setTextColor(primaryTextColor);
        messageEditText.setTextColor(primaryTextColor);
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        usernameEditText.setHintTextColor(secondaryTextColor);
        subjectEditText.setHintTextColor(secondaryTextColor);
        messageEditText.setHintTextColor(secondaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        divider1.setBackgroundColor(dividerColor);
        divider2.setBackgroundColor(dividerColor);
    }
}