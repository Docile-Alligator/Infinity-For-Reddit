package ml.docilealligator.infinityforreddit.markdown;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.commonmark.node.Node;

import java.util.Collections;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonReducer;
import io.noties.markwon.recycler.MarkwonAdapter;
import io.noties.markwon.recycler.SimpleEntry;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.SpoilerOnClickTextView;

public class CustomMarkwonAdapter extends MarkwonAdapter {
    private BaseActivity activity;
    private final SparseArray<Entry<Node, Holder>> entries;
    private final Entry<Node, Holder> defaultEntry;
    private final MarkwonReducer reducer;

    private LayoutInflater layoutInflater;

    private Markwon markwon;
    private List<Node> nodes;

    @Nullable
    private View.OnClickListener onClickListener;
    @Nullable
    private View.OnLongClickListener onLongClickListener;

    @SuppressWarnings("WeakerAccess")
    CustomMarkwonAdapter(
            @NonNull BaseActivity activity,
            @NonNull SparseArray<Entry<Node, Holder>> entries,
            @NonNull Entry<Node, Holder> defaultEntry,
            @NonNull MarkwonReducer reducer) {
        this.activity = activity;
        this.entries = entries;
        this.defaultEntry = defaultEntry;
        this.reducer = reducer;

        setHasStableIds(true);
    }

    public void setOnClickListener(@Nullable View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(@Nullable View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    @NonNull
    public static CustomBuilderImpl builder(
            @NonNull BaseActivity activity,
            @LayoutRes int defaultEntryLayoutResId,
            @IdRes int defaultEntryTextViewResId
    ) {
        return builder(activity, SimpleEntry.create(defaultEntryLayoutResId, defaultEntryTextViewResId));
    }

    @NonNull
    public static CustomBuilderImpl builder(@NonNull BaseActivity activity, @NonNull Entry<? extends Node, ? extends Holder> defaultEntry) {
        //noinspection unchecked
        return new CustomBuilderImpl(activity, (Entry<Node, Holder>) defaultEntry);
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
            SpoilerOnClickTextView textView = (SpoilerOnClickTextView) holder.itemView;
            holder.itemView.setOnClickListener(view -> {
                if (onClickListener != null && textView.getSelectionStart() == -1 && textView.getSelectionEnd() == -1) {
                    onClickListener.onClick(view);
                }
            });
            holder.itemView.setOnLongClickListener(view -> {
                if (onLongClickListener != null && textView.getSelectionStart() == -1 && textView.getSelectionEnd() == -1) {
                    return onLongClickListener.onLongClick(view);
                }
                return false;
            });
        } else if (holder.itemView instanceof HorizontalScrollView) {
            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                boolean isSliderPanelLockedAlready;
                boolean isViewPager2UserInputEnabledAlready;
                @Override
                public boolean onTouch(View v, MotionEvent motionEvent) {
                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                        if (activity.mSliderPanel != null) {
                            activity.mSliderPanel.requestDisallowInterceptTouchEvent(false);
                        }

                        if (activity.mViewPager2 != null && isViewPager2UserInputEnabledAlready) {
                            activity.mViewPager2.setUserInputEnabled(true);
                        }

                        if (!isSliderPanelLockedAlready) {
                            activity.unlockSwipeRightToGoBack();
                        }
                    } else {
                        if (activity.mSliderPanel != null) {
                            isSliderPanelLockedAlready = activity.mSliderPanel.isLocked();
                            activity.mSliderPanel.requestDisallowInterceptTouchEvent(true);
                        }
                        if (activity.mViewPager2 != null) {
                            isViewPager2UserInputEnabledAlready = activity.mViewPager2.isUserInputEnabled();
                            activity.mViewPager2.setUserInputEnabled(false);
                        }
                        activity.lockSwipeRightToGoBack();
                    }

                    return false;
                }
            });

            TableLayout tableLayout = holder.itemView.findViewById(R.id.table_layout);
            if (tableLayout != null) {
                for (int i = 0; i < tableLayout.getChildCount(); i++) {
                    if (tableLayout.getChildAt(i) instanceof TableRow) {
                        TableRow tableRow = ((TableRow) tableLayout.getChildAt(i));
                        for (int j = 0; j < tableRow.getChildCount(); j++) {
                            if (tableRow.getChildAt(j) instanceof TextView) {
                                TextView textView = (TextView) tableRow.getChildAt(j);
                                tableRow.getChildAt(j).setOnClickListener(view -> {
                                    if (onClickListener != null && textView.getSelectionStart() == -1 && textView.getSelectionEnd() == -1) {
                                        onClickListener.onClick(view);
                                    }
                                });
                                tableRow.getChildAt(j).setOnLongClickListener(view -> {
                                    if (onLongClickListener != null && textView.getSelectionStart() == -1 && textView.getSelectionEnd() == -1) {
                                        onLongClickListener.onLongClick(view);
                                        return true;
                                    }
                                    return false;
                                });
                            }
                        }
                    }
                }
            }
        }

        if (node instanceof ImageAndGifBlock) {
            if (onClickListener != null) {
                holder.itemView.setOnClickListener(onClickListener);
            }
            if (onLongClickListener != null) {
                holder.itemView.setOnLongClickListener(onLongClickListener);
            }

            if (holder instanceof ImageAndGifEntry.Holder) {
                ((ImageAndGifEntry.Holder) holder).binding.captionTextViewMarkdownImageAndGifBlock.setOnClickListener(view -> {
                    if (onClickListener != null
                            && ((ImageAndGifEntry.Holder) holder).binding.captionTextViewMarkdownImageAndGifBlock.getSelectionStart() == -1
                            && ((ImageAndGifEntry.Holder) holder).binding.captionTextViewMarkdownImageAndGifBlock.getSelectionEnd() == -1) {
                        onClickListener.onClick(view);
                    }
                });
                ((ImageAndGifEntry.Holder) holder).binding.captionTextViewMarkdownImageAndGifBlock.setOnLongClickListener(view -> {
                    if (onLongClickListener != null
                            && ((ImageAndGifEntry.Holder) holder).binding.captionTextViewMarkdownImageAndGifBlock.getSelectionStart() == -1
                            && ((ImageAndGifEntry.Holder) holder).binding.captionTextViewMarkdownImageAndGifBlock.getSelectionEnd() == -1) {
                        return onLongClickListener.onLongClick(view);
                    }
                    return false;
                });
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
                : Collections.emptyList();
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

        private final BaseActivity activity;

        private final SparseArray<Entry<Node, Holder>> entries = new SparseArray<>(3);

        private final Entry<Node, Holder> defaultEntry;

        private MarkwonReducer reducer;

        CustomBuilderImpl(@NonNull BaseActivity activity, @NonNull Entry<Node, Holder> defaultEntry) {
            this.activity = activity;
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

            return new CustomMarkwonAdapter(activity, entries, defaultEntry, reducer);
        }
    }
}
