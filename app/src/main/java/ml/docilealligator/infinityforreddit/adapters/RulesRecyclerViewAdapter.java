package ml.ino6962.postinfinityforreddit.adapters;

import android.content.Intent;
import android.net.Uri;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.recycler.MarkwonAdapter;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ml.ino6962.postinfinityforreddit.R;
import ml.ino6962.postinfinityforreddit.Rule;
import ml.ino6962.postinfinityforreddit.activities.BaseActivity;
import ml.ino6962.postinfinityforreddit.activities.LinkResolverActivity;
import ml.ino6962.postinfinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.ino6962.postinfinityforreddit.customtheme.CustomThemeWrapper;
import ml.ino6962.postinfinityforreddit.customviews.SwipeLockInterface;
import ml.ino6962.postinfinityforreddit.customviews.SwipeLockLinearLayoutManager;
import ml.ino6962.postinfinityforreddit.customviews.slidr.widget.SliderPanel;
import ml.ino6962.postinfinityforreddit.markdown.MarkdownUtils;

public class RulesRecyclerViewAdapter extends RecyclerView.Adapter<RulesRecyclerViewAdapter.RuleViewHolder> {
    private BaseActivity activity;
    private Markwon markwon;
    @Nullable
    private final SliderPanel sliderPanel;
    private ArrayList<Rule> rules;
    private int mPrimaryTextColor;

    public RulesRecyclerViewAdapter(@NonNull BaseActivity activity,
                                    @NonNull CustomThemeWrapper customThemeWrapper,
                                    @Nullable SliderPanel sliderPanel) {
        this.activity = activity;
        this.sliderPanel = sliderPanel;
        mPrimaryTextColor = customThemeWrapper.getPrimaryTextColor();
        int spoilerBackgroundColor = mPrimaryTextColor | 0xFF000000;
        MarkwonPlugin miscPlugin = new AbstractMarkwonPlugin() {
            @Override
            public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                if (activity.typeface != null) {
                    textView.setTypeface(activity.typeface);
                }

                textView.setTextColor(mPrimaryTextColor);
            }

            @Override
            public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                builder.linkResolver((view, link) -> {
                    Intent intent = new Intent(activity, LinkResolverActivity.class);
                    Uri uri = Uri.parse(link);
                    intent.setData(uri);
                    activity.startActivity(intent);
                });
            }

            @Override
            public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                builder.linkColor(customThemeWrapper.getLinkColor());
            }
        };
        BetterLinkMovementMethod.OnLinkLongClickListener onLinkLongClickListener = (textView, url) -> {
            if (activity != null && !activity.isDestroyed() && !activity.isFinishing()) {
                UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = UrlMenuBottomSheetFragment.newInstance(url);
                urlMenuBottomSheetFragment.show(activity.getSupportFragmentManager(), null);
            }
            return true;
        };
        markwon = MarkdownUtils.createFullRedditMarkwon(activity,
                miscPlugin, mPrimaryTextColor, spoilerBackgroundColor, onLinkLongClickListener);
    }

    @NonNull
    @Override
    public RuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RuleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {
        Rule rule = rules.get(holder.getBindingAdapterPosition());
        holder.shortNameTextView.setText(rule.getShortName());
        if (rule.getDescriptionHtml() == null) {
            holder.descriptionMarkwonView.setVisibility(View.GONE);
        } else {
            holder.markwonAdapter.setMarkdown(markwon, rule.getDescriptionHtml());
            //noinspection NotifyDatasetChanged
            holder.markwonAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return rules == null ? 0 : rules.size();
    }

    @Override
    public void onViewRecycled(@NonNull RuleViewHolder holder) {
        super.onViewRecycled(holder);
        holder.descriptionMarkwonView.setVisibility(View.VISIBLE);
    }

    public void changeDataset(ArrayList<Rule> rules) {
        this.rules = rules;
        notifyDataSetChanged();
    }

    class RuleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.short_name_text_view_item_rule)
        TextView shortNameTextView;
        @BindView(R.id.description_markwon_view_item_rule)
        RecyclerView descriptionMarkwonView;
        @NonNull
        final MarkwonAdapter markwonAdapter;

        RuleViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            shortNameTextView.setTextColor(mPrimaryTextColor);

            if (activity.typeface != null) {
                shortNameTextView.setTypeface(activity.typeface);
            }
            markwonAdapter = MarkdownUtils.createTablesAdapter();
            SwipeLockLinearLayoutManager swipeLockLinearLayoutManager = new SwipeLockLinearLayoutManager(activity,
                    new SwipeLockInterface() {
                @Override
                public void lockSwipe() {
                    if (sliderPanel != null) {
                        sliderPanel.lock();
                    }
                }

                @Override
                public void unlockSwipe() {
                    if (sliderPanel != null) {
                        sliderPanel.unlock();
                    }
                }
            });
            descriptionMarkwonView.setLayoutManager(swipeLockLinearLayoutManager);
            descriptionMarkwonView.setAdapter(markwonAdapter);
        }
    }
}
