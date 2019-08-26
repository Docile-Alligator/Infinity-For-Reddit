package CustomView;

import android.content.Context;
import android.text.Spanned;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.commonmark.node.Node;

import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonPlugin;

public class CustomMarkwonView extends Markwon {

    public void setMarkdown(@Nullable String markdown, Context context) {

    }

    @NonNull
    @Override
    public Node parse(@NonNull String input) {
        return null;
    }

    @NonNull
    @Override
    public Spanned render(@NonNull Node node) {
        return null;
    }

    @NonNull
    @Override
    public Spanned toMarkdown(@NonNull String input) {
        return null;
    }

    @Override
    public void setMarkdown(@NonNull TextView textView, @NonNull String markdown) {

    }

    @Override
    public void setParsedMarkdown(@NonNull TextView textView, @NonNull Spanned markdown) {

    }

    @Override
    public boolean hasPlugin(@NonNull Class<? extends MarkwonPlugin> plugin) {
        return false;
    }

    @Nullable
    @Override
    public <P extends MarkwonPlugin> P getPlugin(@NonNull Class<P> type) {
        return null;
    }

    @NonNull
    @Override
    public <P extends MarkwonPlugin> P requirePlugin(@NonNull Class<P> type) {
        return null;
    }

    @NonNull
    @Override
    public List<? extends MarkwonPlugin> getPlugins() {
        return null;
    }
}
