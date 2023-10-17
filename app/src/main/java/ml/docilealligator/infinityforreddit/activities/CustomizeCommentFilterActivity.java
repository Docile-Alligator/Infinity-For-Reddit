package ml.docilealligator.infinityforreddit.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ml.docilealligator.infinityforreddit.R;

public class CustomizeCommentFilterActivity extends AppCompatActivity {

    public static final String EXTRA_POST_FILTER = "EPF";
    public static final String EXTRA_FROM_SETTINGS = "EFS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_comment_filter);
    }
}