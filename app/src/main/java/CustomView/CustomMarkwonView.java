package CustomView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import ml.docilealligator.infinityforreddit.LinkResolverActivity;
import ru.noties.markwon.SpannableConfiguration;
import ru.noties.markwon.view.MarkwonView;

public class CustomMarkwonView extends MarkwonView {

    public CustomMarkwonView(Context context) {
        super(context);
    }

    public CustomMarkwonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMarkdown(@Nullable String markdown, Context context) {
        SpannableConfiguration configuration = SpannableConfiguration.builder(context).linkResolver((view, link) -> {
            Intent intent = new Intent(context, LinkResolverActivity.class);
            Uri uri = Uri.parse(link);
            if(uri.getScheme() == null && uri.getHost() == null) {
                intent.setData(LinkResolverActivity.getRedditUriByPath(link));
            } else {
                intent.setData(uri);
            }
            context.startActivity(intent);
        }).build();

        super.setMarkdown(configuration, markdown);
    }
}
