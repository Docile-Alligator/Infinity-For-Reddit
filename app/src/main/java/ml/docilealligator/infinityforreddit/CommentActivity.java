package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

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

public class CommentActivity extends AppCompatActivity {

    static final String COMMENT_PARENT_TEXT = "CPT";
    static final String PARENT_FULLNAME = "PF";
    static final String EXTRA_COMMENT_DATA = "ECD";
    static final int WRITE_COMMENT_REQUEST_CODE = 1;

    @BindView(R.id.coordinator_layout_comment_activity) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar_comment_activity) Toolbar toolbar;
    @BindView(R.id.comment_parent_text_view_comment_activity) TextView commentParentTextView;
    @BindView(R.id.comment_edit_text_comment_activity) EditText commentEditText;

    private String parentFullname;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    @Named("auth_info")
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        ButterKnife.bind(this);

        ((Infinity) getApplication()).getmAppComponent().inject(this);

        setSupportActionBar(toolbar);

        commentParentTextView.setText(getIntent().getExtras().getString(COMMENT_PARENT_TEXT));
        parentFullname = getIntent().getExtras().getString(PARENT_FULLNAME);
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
                CommentData commentData = null;
                SendComment.sendComment(commentEditText.getText().toString(), parentFullname, mOauthRetrofit,
                        sharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, ""),
                        new SendComment.SendCommentListener() {
                            @Override
                            public void sendCommentSuccess() {
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra(EXTRA_COMMENT_DATA, commentData);
                                setResult(RESULT_OK, returnIntent);
                                finish();
                            }

                            @Override
                            public void sendCommentFailed() {
                                Snackbar.make(coordinatorLayout, R.string.send_comment_failed, Snackbar.LENGTH_SHORT).show();
                            }
                        });
        }
        return super.onOptionsItemSelected(item);
    }
}
