package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;
import ru.noties.markwon.view.MarkwonView;

public class CommentActivity extends AppCompatActivity {

    static final String EXTRA_COMMENT_PARENT_TEXT_KEY = "ECPTK";
    static final String EXTRA_PARENT_FULLNAME_KEY = "EPFK";
    static final String EXTRA_COMMENT_DATA_KEY = "ECDK";
    static final String EXTRA_PARENT_DEPTH_KEY = "EPDK";
    static final String EXTRA_PARENT_POSITION_KEY = "EPPK";
    static final String EXTRA_IS_REPLYING_KEY = "EIRK";
    static final int WRITE_COMMENT_REQUEST_CODE = 1;

    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";

    @BindView(R.id.coordinator_layout_comment_activity) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar_comment_activity) Toolbar toolbar;
    @BindView(R.id.comment_parent_markwon_view_comment_activity) MarkwonView commentParentMarkwonView;
    @BindView(R.id.comment_edit_text_comment_activity) EditText commentEditText;

    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String parentFullname;
    private int parentDepth;
    private int parentPosition;
    private boolean isReplying;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        ButterKnife.bind(this);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        if(savedInstanceState == null) {
            getCurrentAccount();
        } else {
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            if(!mNullAccessToken && mAccessToken == null) {
                getCurrentAccount();
            }
        }

        Intent intent = getIntent();
        commentParentMarkwonView.setMarkdown(intent.getExtras().getString(EXTRA_COMMENT_PARENT_TEXT_KEY));
        parentFullname = intent.getExtras().getString(EXTRA_PARENT_FULLNAME_KEY);
        parentDepth = intent.getExtras().getInt(EXTRA_PARENT_DEPTH_KEY);
        parentPosition = intent.getExtras().getInt(EXTRA_PARENT_POSITION_KEY);
        isReplying = intent.getExtras().getBoolean(EXTRA_IS_REPLYING_KEY);
        if(isReplying) {
            toolbar.setTitle(getString(R.string.comment_activity_label_is_replying));
        }

        setSupportActionBar(toolbar);
    }

    private void getCurrentAccount() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if(account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
            }
        }).execute();
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
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra(EXTRA_COMMENT_DATA_KEY, commentData);
                                returnIntent.putExtra(EXTRA_PARENT_FULLNAME_KEY, parentFullname);
                                if(isReplying) {
                                    returnIntent.putExtra(EXTRA_PARENT_POSITION_KEY, parentPosition);
                                }
                                setResult(RESULT_OK, returnIntent);
                                finish();
                            }

                            @Override
                            public void sendCommentFailed() {
                                sendingSnackbar.dismiss();
                                item.setEnabled(true);
                                item.getIcon().setAlpha(255);
                                Snackbar.make(coordinatorLayout, R.string.send_comment_failed, Snackbar.LENGTH_SHORT).show();
                            }

                            @Override
                            public void parseSentCommentFailed() {
                                Intent returnIntent = new Intent();
                                setResult(RESULT_OK, returnIntent);
                                finish();
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
}
