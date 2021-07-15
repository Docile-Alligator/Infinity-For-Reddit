package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.libRG.CustomTextView;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import ml.docilealligator.infinityforreddit.Flair;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FlairBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class PostGalleryActivity extends BaseActivity implements FlairBottomSheetFragment.FlairSelectionCallback {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";

    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String SUBREDDIT_ICON_STATE = "SIS";
    private static final String SUBREDDIT_SELECTED_STATE = "SSS";
    private static final String SUBREDDIT_IS_USER_STATE = "SIUS";
    private static final String LOAD_SUBREDDIT_ICON_STATE = "LSIS";
    private static final String IS_POSTING_STATE = "IPS";
    private static final String FLAIR_STATE = "FS";
    private static final String IS_SPOILER_STATE = "ISS";
    private static final String IS_NSFW_STATE = "INS";

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 2;

    @BindView(R.id.coordinator_layout_post_gallery_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_post_gallery_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_post_gallery_activity)
    Toolbar toolbar;
    @BindView(R.id.subreddit_icon_gif_image_view_post_gallery_activity)
    GifImageView iconGifImageView;
    @BindView(R.id.subreddit_name_text_view_post_gallery_activity)
    TextView subredditNameTextView;
    @BindView(R.id.rules_button_post_gallery_activity)
    MaterialButton rulesButton;
    @BindView(R.id.divider_1_post_gallery_activity)
    View divider1;
    @BindView(R.id.flair_custom_text_view_post_gallery_activity)
    CustomTextView flairTextView;
    @BindView(R.id.spoiler_custom_text_view_post_gallery_activity)
    CustomTextView spoilerTextView;
    @BindView(R.id.nsfw_custom_text_view_post_gallery_activity)
    CustomTextView nsfwTextView;
    @BindView(R.id.divider_2_post_gallery_activity)
    View divider2;
    @BindView(R.id.receive_post_reply_notifications_linear_layout_post_gallery_activity)
    LinearLayout receivePostReplyNotificationsLinearLayout;
    @BindView(R.id.receive_post_reply_notifications_text_view_post_gallery_activity)
    TextView receivePostReplyNotificationsTextView;
    @BindView(R.id.receive_post_reply_notifications_switch_material_post_gallery_activity)
    SwitchMaterial receivePostReplyNotificationsSwitchMaterial;
    @BindView(R.id.divider_3_post_gallery_activity)
    View divider3;
    @BindView(R.id.post_title_edit_text_post_gallery_activity)
    EditText titleEditText;
    @BindView(R.id.divider_4_post_gallery_activity)
    View divider4;
    @BindView(R.id.images_recycler_view_post_gallery_activity)
    RecyclerView imagesRecyclerView;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("upload_media")
    Retrofit mUploadMediaRetrofit;
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
    private String mAccessToken;
    private String mAccountName;
    private String iconUrl;
    private String subredditName;
    private boolean subredditSelected = false;
    private boolean subredditIsUser;
    private boolean loadSubredditIconSuccessful = true;
    private boolean isPosting;
    private Uri imageUri;
    private int primaryTextColor;
    private int flairBackgroundColor;
    private int flairTextColor;
    private int spoilerBackgroundColor;
    private int spoilerTextColor;
    private int nsfwBackgroundColor;
    private int nsfwTextColor;
    private Flair flair;
    private boolean isSpoiler = false;
    private boolean isNSFW = false;
    private Resources resources;
    private Menu mMemu;
    private RequestManager mGlide;
    private FlairBottomSheetFragment flairSelectionBottomSheetFragment;
    private Snackbar mPostingSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_gallery);
    }

    @Override
    protected SharedPreferences getDefaultSharedPreferences() {
        return null;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return null;
    }

    @Override
    protected void applyCustomTheme() {

    }

    @Override
    public void flairSelected(Flair flair) {

    }
}