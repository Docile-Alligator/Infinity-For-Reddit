package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import java.util.List;

public class LinkResolverActivity extends AppCompatActivity {

    private static final String POST_PATTERN = "/r/\\w+/comments/\\w+/*[\\w+]*/*";
    private static final String SUBREDDIT_PATTERN = "/r/\\w+/*";
    private static final String USER_PATTERN = "/user/\\w+/*";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        String path = uri.getPath();
        if(path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if(path.matches(POST_PATTERN)) {
            List<String> segments = uri.getPathSegments();
            int commentsIndex = segments.lastIndexOf("comments");
            if(commentsIndex >=0 && commentsIndex < segments.size() - 1) {
                Intent intent = new Intent(this, ViewPostDetailActivity.class);
                intent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, segments.get(commentsIndex + 1));
                startActivity(intent);
            } else {
                deepLinkError(uri);
            }
        } else if(path.matches(SUBREDDIT_PATTERN)) {
            String subredditName = path.substring(3);
            if(subredditName.equals("popular") || subredditName.equals("all")) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(MainActivity.EXTRA_POST_TYPE, subredditName);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, path.substring(3));
                startActivity(intent);
            }
        } else if(path.matches(USER_PATTERN)) {
            Intent intent = new Intent(this, ViewUserDetailActivity.class);
            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, path.substring(6));
            startActivity(intent);
        } else {
            deepLinkError(uri);
        }

        finish();
    }

    private void deepLinkError(Uri uri) {
        //Deep link error handling
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        // add share action to menu list
        builder.addDefaultShareMenuItem();
        builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, uri);
    }
}
