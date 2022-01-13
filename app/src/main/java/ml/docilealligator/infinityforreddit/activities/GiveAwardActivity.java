package ml.docilealligator.infinityforreddit.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.r0adkll.slidr.Slidr;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.AwardRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.award.GiveAward;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class GiveAwardActivity extends BaseActivity {

    public static final String EXTRA_THING_FULLNAME = "ETF";
    public static final String EXTRA_ITEM_POSITION = "EIP";
    public static final String EXTRA_RETURN_ITEM_POSITION = "ERIP";
    public static final String EXTRA_RETURN_NEW_AWARDS = "ERNA";
    public static final String EXTRA_RETURN_NEW_AWARDS_COUNT = "ERNAC";

    @BindView(R.id.coordinator_layout_give_award_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_give_award_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_give_award_activity)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_give_award_activity)
    RecyclerView recyclerView;
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
    private String thingFullname;
    private int itemPosition;
    private String mAccessToken;
    private AwardRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_give_award);

        ButterKnife.bind(this);

        applyCustomTheme();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(appBarLayout);
            }
        }

        thingFullname = getIntent().getStringExtra(EXTRA_THING_FULLNAME);
        itemPosition = getIntent().getIntExtra(EXTRA_ITEM_POSITION, 0);

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);

        bindView();
    }

    private void bindView() {
        adapter = new AwardRecyclerViewAdapter(this, mCustomThemeWrapper, award -> {
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_give_award, null);
            SwitchMaterial switchMaterial = layout.findViewById(R.id.switch_material_give_award_dialog);
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.give_award_dialog_title)
                    .setView(layout)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        boolean isAnonymous = switchMaterial.isChecked();

                        GiveAward.giveAwardV2(mExecutor, new Handler(), mOauthRetrofit, mAccessToken,
                                thingFullname, award.getId(), isAnonymous, new GiveAward.GiveAwardListener() {
                                    @Override
                                    public void success(String awardsHTML, int awardCount) {
                                        Intent data = new Intent();
                                        data.putExtra(EXTRA_RETURN_ITEM_POSITION, itemPosition);
                                        data.putExtra(EXTRA_RETURN_NEW_AWARDS, awardsHTML);
                                        data.putExtra(EXTRA_RETURN_NEW_AWARDS_COUNT, awardCount);
                                        setResult(RESULT_OK, data);
                                        finish();
                                    }

                                    @Override
                                    public void failed(int code, String message) {
                                        View layout = inflater.inflate(R.layout.copy_text_material_dialog, null);
                                        TextView textView = layout.findViewById(R.id.text_view_copy_text_material_dialog);
                                        String text = getString(R.string.give_award_error_message, code, message == null ? "" : message);
                                        textView.setText(text);
                                        new MaterialAlertDialogBuilder(GiveAwardActivity.this, R.style.CopyTextMaterialAlertDialogTheme)
                                                .setTitle(R.string.give_award_failed)
                                                .setView(layout)
                                                .setPositiveButton(R.string.copy_all, (dialogInterface, i) -> {
                                                    ClipboardManager clipboard = (ClipboardManager) GiveAwardActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                                    if (clipboard != null) {
                                                        ClipData clip = ClipData.newPlainText("simple text", text);
                                                        clipboard.setPrimaryClip(clip);
                                                        Toast.makeText(GiveAwardActivity.this, R.string.copy_success, Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(GiveAwardActivity.this, R.string.copy_failed, Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .setNegativeButton(R.string.cancel, null)
                                                .show();
                                    }
                                });
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManagerBugFixed(this));
        recyclerView.setAdapter(adapter);
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
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, null, toolbar);
    }
}