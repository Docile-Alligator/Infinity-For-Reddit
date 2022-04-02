package ml.docilealligator.infinityforreddit.customviews;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import org.commonmark.node.Node;

import java.util.Collections;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonReducer;
import io.noties.markwon.recycler.MarkwonAdapter;
import io.noties.markwon.recycler.SimpleEntry;
import ml.docilealligator.infinityforreddit.R;

public class CustomMarkwonAdapter extends MarkwonAdapter {
    private final SparseArray<Entry<Node, Holder>> entries;
    private final Entry<Node, Holder> defaultEntry;
    private final MarkwonReducer reducer;

    private LayoutInflater layoutInflater;

    private Markwon markwon;
    private List<Node> nodes;

    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;

    @SuppressWarnings("WeakerAccess")
    CustomMarkwonAdapter(
            @NonNull SparseArray<Entry<Node, Holder>> entries,
            @NonNull Entry<Node, Holder> defaultEntry,
            @NonNull MarkwonReducer reducer) {
        this.entries = entries;
        this.defaultEntry = defaultEntry;
        this.reducer = reducer;

        setHasStableIds(true);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    @NonNull
    public static CustomBuilderImpl builder(
            @LayoutRes int defaultEntryLayoutResId,
            @IdRes int defaultEntryTextViewResId
    ) {
        return builder(SimpleEntry.create(defaultEntryLayoutResId, defaultEntryTextViewResId));
    }

    @NonNull
    public static CustomBuilderImpl builder(@NonNull Entry<? extends Node, ? extends Holder> defaultEntry) {
        //noinspection unchecked
        return new CustomBuilderImpl((Entry<Node, Holder>) defaultEntry);
    }

    @Override
    public void setMarkdown(@NonNull Markwon markwon, @NonNull String markdown) {
        setParsedMarkdown(markwon, markwon.parse(markdown));
    }

    @Override
    public void setParsedMarkdown(@NonNull Markwon markwon, @NonNull Node document) {
        setParsedMarkdown(markwon, reducer.reduce(document));
    }

    @Override
    public void setParsedMarkdown(@NonNull Markwon markwon, @NonNull List<Node> nodes) {
        // clear all entries before applying

        defaultEntry.clear();

        for (int i = 0, size = entries.size(); i < size; i++) {
            entries.valueAt(i).clear();
        }

        this.markwon = markwon;
        this.nodes = nodes;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }

        final Entry<Node, Holder> entry = getEntry(viewType);

        return entry.createHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        final Node node = nodes.get(position);
        final int viewType = getNodeViewType(node.getClass());

        final Entry<Node, Holder> entry = getEntry(viewType);

        entry.bindHolder(markwon, holder, node);

        if (holder.itemView instanceof SpoilerOnClickTextView) {
            holder.itemView.setOnClickListener(onClickListener);
            holder.itemView.setOnLongClickListener(onLongClickListener);
        } else if (holder.itemView instanceof HorizontalScrollView) {
            TableLayout tableLayout = holder.itemView.findViewById(R.id.table_layout);
            if (tableLayout != null) {
                for (int i = 0; i < tableLayout.getChildCount(); i++) {
                    if (tableLayout.getChildAt(i) instanceof TableRow) {
                        TableRow tableRow = ((TableRow) tableLayout.getChildAt(i));
                        for (int j = 0; j < tableRow.getChildCount(); j++) {
                            if (tableRow.getChildAt(j) instanceof TextView) {
                                tableRow.getChildAt(j).setOnClickListener(onClickListener);
                                tableRow.getChildAt(j).setOnLongClickListener(onLongClickListener);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return nodes != null
                ? nodes.size()
                : 0;
    }

    @Override
    public void onViewRecycled(@NonNull Holder holder) {
        super.onViewRecycled(holder);

        final Entry<Node, Holder> entry = getEntry(holder.getItemViewType());
        entry.onViewRecycled(holder);
    }

    @SuppressWarnings("unused")
    @NonNull
    public List<Node> getItems() {
        return nodes != null
                ? Collections.unmodifiableList(nodes)
                : Collections.<Node>emptyList();
    }

    @Override
    public int getItemViewType(int position) {
        return getNodeViewType(nodes.get(position).getClass());
    }

    @Override
    public long getItemId(int position) {
        final Node node = nodes.get(position);
        final int type = getNodeViewType(node.getClass());
        final Entry<Node, Holder> entry = getEntry(type);
        return entry.id(node);
    }

    @Override
    public int getNodeViewType(@NonNull Class<? extends Node> node) {
        // if has registered -> then return it, else 0
        final int hash = node.hashCode();
        if (entries.indexOfKey(hash) > -1) {
            return hash;
        }
        return 0;
    }

    @NonNull
    private Entry<Node, Holder> getEntry(int viewType) {
        return viewType == 0
                ? defaultEntry
                : entries.get(viewType);
    }

    public static class CustomBuilderImpl implements Builder {

        private final SparseArray<Entry<Node, Holder>> entries = new SparseArray<>(3);

        private final Entry<Node, Holder> defaultEntry;

        private MarkwonReducer reducer;

        CustomBuilderImpl(@NonNull Entry<Node, Holder> defaultEntry) {
            this.defaultEntry = defaultEntry;
        }

        @NonNull
        @Override
        public <N extends Node> CustomBuilderImpl include(
                @NonNull Class<N> node,
                @NonNull Entry<? super N, ? extends Holder> entry) {
            //noinspection unchecked
            entries.append(node.hashCode(), (Entry<Node, Holder>) entry);
            return this;
        }

        @NonNull
        @Override
        public CustomBuilderImpl reducer(@NonNull MarkwonReducer reducer) {
            this.reducer = reducer;
            return this;
        }

        @NonNull
        @Override
        public CustomMarkwonAdapter build() {

            if (reducer == null) {
                reducer = MarkwonReducer.directChildren();
            }

            return new CustomMarkwonAdapter(entries, defaultEntry, reducer);
        }
    }
}
