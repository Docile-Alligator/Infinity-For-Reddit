package ml.docilealligator.infinityforreddit.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.linkify.LinkifyPlugin;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.CommentData;
import ml.docilealligator.infinityforreddit.Event.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SendComment;
import retrofit2.Retrofit;

public class CommentActivity extends BaseActivity {

    public static final String EXTRA_COMMENT_PARENT_TEXT_KEY = "ECPTK";
    public static final String EXTRA_PARENT_FULLNAME_KEY = "EPFK";
    public static final String EXTRA_COMMENT_DATA_KEY = "ECDK";
    public static final String EXTRA_PARENT_DEPTH_KEY = "EPDK";
    public static final String EXTRA_PARENT_POSITION_KEY = "EPPK";
    public static final String EXTRA_IS_REPLYING_KEY = "EIRK";
    public static final int WRITE_COMMENT_REQUEST_CODE = 1;

    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";

    @BindView(R.id.coordinator_layout_comment_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar_comment_activity)
    Toolbar toolbar;
    @BindView(R.id.comment_parent_markwon_view_comment_activity)
    TextView commentParentMarkwonView;
    @BindView(R.id.comment_edit_text_comment_activity)
    EditText commentEditText;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    SharedPreferences mSharedPreferences;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String parentFullname;
    private int parentDepth;
    private int parentPosition;
    private boolean isReplying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        if (savedInstanceState == null) {
            getCurrentAccount();
        } else {
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccount();
            }
        }

        Intent intent = getIntent();
        Markwon markwon = Markwon.builder(this)
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                        builder.linkResolver((view, link) -> {
                            Intent intent = new Intent(CommentActivity.this, LinkResolverActivity.class);
                            Uri uri = Uri.parse(link);
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(link));
                            } else {
                                intent.setData(uri);
                            }
                            startActivity(intent);
                        });
                    }
                })
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .build();
        markwon.setMarkdown(commentParentMarkwonView, intent.getStringExtra(EXTRA_COMMENT_PARENT_TEXT_KEY));
        parentFullname = intent.getStringExtra(EXTRA_PARENT_FULLNAME_KEY);
        parentDepth = intent.getExtras().getInt(EXTRA_PARENT_DEPTH_KEY);
        parentPosition = intent.getExtras().getInt(EXTRA_PARENT_POSITION_KEY);
        isReplying = intent.getExtras().getBoolean(EXTRA_IS_REPLYING_KEY);
        if (isReplying) {
            toolbar.setTitle(getString(R.string.comment_activity_label_is_replying));
        }

        setSupportActionBar(toolbar);

        commentEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    private void getCurrentAccount() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if (account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
            }
        }).execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.comment_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send_comment_activity:
                if (commentEditText.getText() == null || commentEditText.getText().toString().equals("")) {
                    Snackbar.make(coordinatorLayout, R.string.comment_content_required, Snackbar.LENGTH_SHORT).show();
                    return true;
                }

                item.setEnabled(false);
                item.getIcon().setAlpha(130);
                Snackbar sendingSnackbar = Snackbar.make(coordinatorLayout, R.string.sending_comment, Snackbar.LENGTH_INDEFINITE);
                sendingSnackbar.show();

                SendComment.sendComment(commentEditText.getText().toString(), parentFullname, parentDepth,
                        getResources().getConfiguration().locale, mOauthRetrofit,
                        mAccessToken,
                        new SendComment.SendCommentListener() {
                            @Override
                            public void sendCommentSuccess(CommentData commentData) {
                                Toast.makeText(CommentActivity.this, R.string.send_comment_success, Toast.LENGTH_SHORT).show();
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra(EXTRA_COMMENT_DATA_KEY, commentData);
                                returnIntent.putExtra(EXTRA_PARENT_FULLNAME_KEY, parentFullname);
                                if (isReplying) {
                                    returnIntent.putExtra(EXTRA_PARENT_POSITION_KEY, parentPosition);
                                }
                                setResult(RESULT_OK, returnIntent);
                                finish();
                            }

                            @Override
                            public void sendCommentFailed(@Nullable String errorMessage) {
                                sendingSnackbar.dismiss();
                                item.setEnabled(true);
                                item.getIcon().setAlpha(255);

                                if (errorMessage == null) {
                                    Snackbar.make(coordinatorLayout, R.string.send_comment_failed, Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
                return true;
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }
}
