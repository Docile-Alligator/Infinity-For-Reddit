package ml.docilealligator.infinityforreddit.markdown;

import android.content.Context;
import android.text.util.Linkify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.commonmark.ext.gfm.tables.TableBlock;

import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.inlineparser.BangInlineProcessor;
import io.noties.markwon.inlineparser.HtmlInlineProcessor;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.movement.MovementMethodPlugin;
import io.noties.markwon.recycler.MarkwonAdapter;
import io.noties.markwon.recycler.table.TableEntry;
import io.noties.markwon.recycler.table.TableEntryPlugin;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomMarkwonAdapter;

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
                    plugin.excludeInlineProcessor(HtmlInlineProcessor.class);
                    plugin.excludeInlineProcessor(BangInlineProcessor.class);
                }))
                .usePlugin(miscPlugin)
                .usePlugin(SuperscriptPlugin.create())
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
                    plugin.excludeInlineProcessor(HtmlInlineProcessor.class);
                    plugin.excludeInlineProcessor(BangInlineProcessor.class);
                }))
                .usePlugin(miscPlugin)
                .usePlugin(MovementMethodPlugin.create(BetterLinkMovementMethod.newInstance().setOnLinkLongClickListener(onLinkLongClickListener)))
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .build();
    }

    /**
     * Creates a MarkwonAdapter configured with support for tables.
     */
    @NonNull
    public static MarkwonAdapter createTablesAdapter() {
        return MarkwonAdapter.builder(R.layout.adapter_default_entry, R.id.text)
                .include(TableBlock.class, TableEntry.create(builder -> builder
                        .tableLayout(R.layout.adapter_table_block, R.id.table_layout)
                        .textLayoutIsRoot(R.layout.view_table_entry_cell)))
                .build();
    }

    /**
     * Creates a CustomMarkwonAdapter configured with support for tables.
     */
    @NonNull
    public static CustomMarkwonAdapter createCustomTablesAdapter() {
        return CustomMarkwonAdapter.builder(R.layout.adapter_default_entry, R.id.text)
                .include(TableBlock.class, TableEntry.create(builder -> builder
                        .tableLayout(R.layout.adapter_table_block, R.id.table_layout)
                        .textLayoutIsRoot(R.layout.view_table_entry_cell)))
                .build();
    }
}
