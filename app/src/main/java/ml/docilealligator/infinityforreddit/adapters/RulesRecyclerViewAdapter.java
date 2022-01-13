package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.html.tag.SuperScriptHandler;
import io.noties.markwon.inlineparser.AutolinkInlineProcessor;
import io.noties.markwon.inlineparser.BangInlineProcessor;
import io.noties.markwon.inlineparser.HtmlInlineProcessor;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.movement.MovementMethodPlugin;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Rule;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.markdown.SuperscriptInlineProcessor;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class RulesRecyclerViewAdapter extends RecyclerView.Adapter<RulesRecyclerViewAdapter.RuleViewHolder> {
    private BaseActivity activity;
    private Markwon markwon;
    private ArrayList<Rule> rules;
    private int mPrimaryTextColor;
    private int mSecondaryTextColor;

    public RulesRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper) {
        this.activity = activity;
        markwon = Markwon.builder(activity)
                .usePlugin(MarkwonInlineParserPlugin.create(plugin -> {
                    plugin.excludeInlineProcessor(AutolinkInlineProcessor.class);
                    plugin.excludeInlineProcessor(HtmlInlineProcessor.class);
                    plugin.excludeInlineProcessor(BangInlineProcessor.class);
                    plugin.addInlineProcessor(new SuperscriptInlineProcessor());
                }))
                .usePlugin(HtmlPlugin.create(plugin -> {
                    plugin.excludeDefaults(true).addHandler(new SuperScriptHandler());
                }))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @NonNull
                    @Override
                    public String processMarkdown(@NonNull String markdown) {
                        return Utils.fixSuperScript(markdown);
                    }

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
                })
                .usePlugin(MovementMethodPlugin.create(BetterLinkMovementMethod.linkify(Linkify.WEB_URLS).setOnLinkLongClickListener((textView, url) -> {
                    if (activity != null && !activity.isDestroyed() && !activity.isFinishing()) {
                        UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = new UrlMenuBottomSheetFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(UrlMenuBottomSheetFragment.EXTRA_URL, url);
                        urlMenuBottomSheetFragment.setArguments(bundle);
                        urlMenuBottomSheetFragment.show(activity.getSupportFragmentManager(), urlMenuBottomSheetFragment.getTag());
                    }
                    return true;
                })))
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .usePlugin(StrikethroughPlugin.create())
                .build();
        mPrimaryTextColor = customThemeWrapper.getPrimaryTextColor();
        mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
    }

    @NonNull
    @Override
    public RuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RuleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {
        holder.shortNameTextView.setText(rules.get(holder.getBindingAdapterPosition()).getShortName());
        if (rules.get(holder.getBindingAdapterPosition()).getDescriptionHtml() == null) {
            holder.descriptionMarkwonView.setVisibility(View.GONE);
        } else {
            markwon.setMarkdown(holder.descriptionMarkwonView, rules.get(holder.getBindingAdapterPosition()).getDescriptionHtml());
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
        TextView descriptionMarkwonView;

        RuleViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            shortNameTextView.setTextColor(mPrimaryTextColor);
            descriptionMarkwonView.setTextColor(mSecondaryTextColor);

            if (activity.typeface != null) {
                shortNameTextView.setTypeface(activity.typeface);
                descriptionMarkwonView.setTypeface(activity.typeface);
            }
        }
    }
}
