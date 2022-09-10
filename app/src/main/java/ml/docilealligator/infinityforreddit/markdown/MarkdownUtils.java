package ml.docilealligator.infinityforreddit.markdown;

import android.content.Context;
import android.text.util.Linkify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.html.tag.SuperScriptHandler;
import io.noties.markwon.inlineparser.AutolinkInlineProcessor;
import io.noties.markwon.inlineparser.BangInlineProcessor;
import io.noties.markwon.inlineparser.HtmlInlineProcessor;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.movement.MovementMethodPlugin;
import io.noties.markwon.recycler.table.TableEntryPlugin;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;

public class MarkdownUtils {
    /**
     * Creates a Markwon instance with all the plugins required for processing Reddit's markdown.
     * @return configured Markwon instance
     */
    @NonNull
    public static Markwon createFullRedditMarkwon(@NonNull Context context,
                                                  @NonNull MarkwonPlugin miscPlugin,
                                                  int markdownColor,
                                                  int spoilerBackgroundColor,
                                                  @Nullable BetterLinkMovementMethod.OnLinkLongClickListener onLinkLongClickListener) {
        return Markwon.builder(context)
                .usePlugin(MarkwonInlineParserPlugin.create(plugin -> {
                    plugin.excludeInlineProcessor(AutolinkInlineProcessor.class);
                    plugin.excludeInlineProcessor(HtmlInlineProcessor.class);
                    plugin.excludeInlineProcessor(BangInlineProcessor.class);
                    plugin.addInlineProcessor(new SuperscriptInlineProcessor());
                }))
                .usePlugin(HtmlPlugin.create(plugin -> {
                    plugin.excludeDefaults(true).addHandler(new SuperScriptHandler());
                }))
                .usePlugin(miscPlugin)
                .usePlugin(SpoilerParserPlugin.create(markdownColor, spoilerBackgroundColor))
                .usePlugin(RedditHeadingPlugin.create())
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(MovementMethodPlugin.create(new SpoilerAwareMovementMethod()
                        .setOnLinkLongClickListener(onLinkLongClickListener)))
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .usePlugin(TableEntryPlugin.create(context))
                .build();
    }

    /**
     * Creates a Markwon instance that processes only the links.
     * @return configured Markwon instance
     */
    @NonNull
    public static Markwon createLinksOnlyMarkwon(@NonNull Context context,
                                                  @NonNull MarkwonPlugin miscPlugin,
                                                  @Nullable BetterLinkMovementMethod.OnLinkLongClickListener onLinkLongClickListener) {
        return Markwon.builder(context)
                .usePlugin(MarkwonInlineParserPlugin.create(plugin -> {
                    plugin.excludeInlineProcessor(AutolinkInlineProcessor.class);
                    plugin.excludeInlineProcessor(HtmlInlineProcessor.class);
                    plugin.excludeInlineProcessor(BangInlineProcessor.class);
                }))
                .usePlugin(miscPlugin)
                .usePlugin(MovementMethodPlugin.create(BetterLinkMovementMethod.linkify(Linkify.WEB_URLS).setOnLinkLongClickListener(onLinkLongClickListener)))
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .build();
    }
}
