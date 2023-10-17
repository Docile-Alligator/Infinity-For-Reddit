package ml.docilealligator.infinityforreddit.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ml.docilealligator.infinityforreddit.R;

public class CommentFilterUsageListingActivity extends AppCompatActivity {

    public static final String EXTRA_POST_FILTER = "EPF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_filter_usage_listing);
    }
}