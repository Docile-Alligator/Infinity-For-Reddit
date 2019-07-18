package CustomView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.ViewUserDetailActivity;
import ru.noties.markwon.SpannableConfiguration;
import ru.noties.markwon.view.MarkwonView;

public class CustomMarkwonView extends MarkwonView {

    public CustomMarkwonView(Context context) {
        super(context);
    }

    public CustomMarkwonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMarkdown(@Nullable String markdown, Context conte) {
        SpannableConfiguration configuration = SpannableConfiguration.builder(conte).linkResolver((view, link) -> {
            if(link.startsWith("/u/") || link.startsWith("u/")) {
                Intent intent = new Intent(conte, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, link.substring(3));
                conte.startActivity(intent);
            } else if(link.startsWith("/r/") || link.startsWith("r/")) {
                Intent intent = new Intent(conte, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, link.substring(3));
                conte.startActivity(intent);
            } else {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                // add share action to menu list
                builder.addDefaultShareMenuItem();
                builder.setToolbarColor(conte.getResources().getColor(R.color.colorPrimary));
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(conte, Uri.parse(link));
            }
        }).build();

        super.setMarkdown(configuration, markdown);
    }
}
