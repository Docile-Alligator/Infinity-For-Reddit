package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemTranslationContributorBinding;
import ml.docilealligator.infinityforreddit.settings.Translation;

public class TranslationFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final BaseActivity activity;
    private final int primaryTextColor;
    private final int secondaryTextColor;
    private final ArrayList<Translation> translationContributors;

    public TranslationFragmentRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper) {
        this.activity = activity;
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        secondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        translationContributors = Translation.getTranslationContributors();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TranslationContributorViewHolder(ItemTranslationContributorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TranslationContributorViewHolder) {
            Translation translation = translationContributors.get(position);
            if (translation.flagDrawableId < 0) {
                ((TranslationContributorViewHolder) holder).binding.countryFlagImageViewItemTranslationContributor.setImageDrawable(null);
            } else {
                ((TranslationContributorViewHolder) holder).binding.countryFlagImageViewItemTranslationContributor.setImageResource(translation.flagDrawableId);
            }
            ((TranslationContributorViewHolder) holder).binding.languageNameTextViewItemTranslationContributor.setText(translation.language);
            ((TranslationContributorViewHolder) holder).binding.contributorNamesTextViewItemTranslationContributor.setText(translation.contributors);
        }
    }

    @Override
    public int getItemCount() {
        return translationContributors.size();
    }

    class TranslationContributorViewHolder extends RecyclerView.ViewHolder {
        ItemTranslationContributorBinding binding;

        public TranslationContributorViewHolder(@NonNull ItemTranslationContributorBinding binding) {
            super(binding.getRoot());

            this.binding = binding;

            if (activity.typeface != null) {
                binding.languageNameTextViewItemTranslationContributor.setTypeface(activity.typeface);
                binding.contributorNamesTextViewItemTranslationContributor.setTypeface(activity.typeface);
            }

            binding.languageNameTextViewItemTranslationContributor.setTextColor(primaryTextColor);
            binding.contributorNamesTextViewItemTranslationContributor.setTextColor(secondaryTextColor);

            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://poeditor.com/join/project?hash=b2IRyfaJv6"));
                activity.startActivity(intent);
            });
        }
    }
}
