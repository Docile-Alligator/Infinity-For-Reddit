package ml.docilealligator.infinityforreddit.adapters;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.TrendingSearch;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.AspectRatioGifImageView;
import ml.docilealligator.infinityforreddit.post.Post;

public class TrendingSearchRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private BaseActivity activity;
    private ArrayList<TrendingSearch> trendingSearches;
    private CustomThemeWrapper customThemeWrapper;
    private RequestManager glide;
    private int imageViewWidth;
    private boolean dataSavingMode;
    private boolean disableImagePreview;
    private float mScale;
    private ItemClickListener itemClickListener;

    public TrendingSearchRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                             int imageViewWidth, boolean dataSavingMode, boolean disableImagePreview,
                                             ItemClickListener itemClickListener) {
        this.activity = activity;
        this.customThemeWrapper = customThemeWrapper;
        this.glide = Glide.with(activity);
        this.imageViewWidth = imageViewWidth;
        this.dataSavingMode = dataSavingMode;
        this.disableImagePreview = disableImagePreview;
        mScale = activity.getResources().getDisplayMetrics().density;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TrendingSearchViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trending_search, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TrendingSearchViewHolder) {
            TrendingSearch trendingSearch = trendingSearches.get(position);
            if (dataSavingMode && disableImagePreview) {
                ((TrendingSearchViewHolder) holder).noPreviewLinkImageView.setVisibility(View.VISIBLE);
            } else {
                Post.Preview preview = getSuitablePreview(trendingSearch.previews);
                if (preview != null) {
                    ((TrendingSearchViewHolder) holder).imageWrapperRelativeLayout.setVisibility(View.VISIBLE);
                    if (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0) {
                        int height = (int) (400 * mScale);
                        ((TrendingSearchViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ((TrendingSearchViewHolder) holder).imageView.getLayoutParams().height = height;
                        preview.setPreviewWidth(imageViewWidth);
                        preview.setPreviewHeight(height);
                    } else {
                        ((TrendingSearchViewHolder) holder).imageView
                                .setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                    }
                    loadImage((TrendingSearchViewHolder) holder, preview);
                } else {
                    ((TrendingSearchViewHolder) holder).noPreviewLinkImageView.setVisibility(View.VISIBLE);
                }
            }

            ((TrendingSearchViewHolder) holder).titleTextView.setText(trendingSearch.displayString);
        }
    }

    @Override
    public int getItemCount() {
        return trendingSearches == null ? 0 : trendingSearches.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof TrendingSearchViewHolder) {
            glide.clear(((TrendingSearchViewHolder) holder).imageView);
            ((TrendingSearchViewHolder) holder).imageWrapperRelativeLayout.setVisibility(View.GONE);
            ((TrendingSearchViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
            ((TrendingSearchViewHolder) holder).noPreviewLinkImageView.setVisibility(View.GONE);
            ((TrendingSearchViewHolder) holder).progressBar.setVisibility(View.GONE);
        }
    }

    @Nullable
    private Post.Preview getSuitablePreview(ArrayList<Post.Preview> previews) {
        Post.Preview preview;
        if (!previews.isEmpty()) {
            int previewIndex;
            if (dataSavingMode && previews.size() > 2) {
                previewIndex = previews.size() / 2;
            } else {
                previewIndex = 0;
            }
            preview = previews.get(previewIndex);
            if (preview.getPreviewWidth() * preview.getPreviewHeight() > 10_000_000) {
                for (int i = previews.size() - 1; i >= 1; i--) {
                    preview = previews.get(i);
                    if (imageViewWidth >= preview.getPreviewWidth()) {
                        if (preview.getPreviewWidth() * preview.getPreviewHeight() <= 10_000_000) {
                            return preview;
                        }
                    } else {
                        int height = imageViewWidth / preview.getPreviewWidth() * preview.getPreviewHeight();
                        if (imageViewWidth * height <= 10_000_000) {
                            return preview;
                        }
                    }
                }
            }

            if (preview.getPreviewWidth() * preview.getPreviewHeight() > 10_000_000) {
                int divisor = 2;
                do {
                    preview.setPreviewWidth(preview.getPreviewWidth() / divisor);
                    preview.setPreviewHeight(preview.getPreviewHeight() / divisor);
                    divisor *= 2;
                } while (preview.getPreviewWidth() * preview.getPreviewHeight() > 10_000_000);
            }
            return preview;
        }

        return null;
    }

    private void loadImage(final TrendingSearchViewHolder holder, @NonNull Post.Preview preview) {
        String url = preview.getPreviewUrl();
        RequestBuilder<Drawable> imageRequestBuilder = glide.load(url).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                holder.progressBar.setVisibility(View.GONE);
                holder.errorRelativeLayout.setVisibility(View.VISIBLE);
                holder.errorRelativeLayout.setOnClickListener(view -> {
                    holder.progressBar.setVisibility(View.VISIBLE);
                    holder.errorRelativeLayout.setVisibility(View.GONE);
                    loadImage(holder, preview);
                });
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                holder.errorRelativeLayout.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.GONE);
                return false;
            }
        });

        if (imageViewWidth > preview.getPreviewWidth()) {
            imageRequestBuilder.override(preview.getPreviewWidth(), preview.getPreviewHeight()).into(holder.imageView);
        } else {
            imageRequestBuilder.into(holder.imageView);
        }
    }

    public void setTrendingSearches(ArrayList<TrendingSearch> trendingSearches) {
        if (trendingSearches != null) {
            this.trendingSearches = trendingSearches;
            notifyItemRangeInserted(0, trendingSearches.size());
        } else {
            int size = this.trendingSearches == null ? 0 : this.trendingSearches.size();
            this.trendingSearches = null;
            notifyItemRangeRemoved(0, size);
        }
    }

    class TrendingSearchViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_wrapper_relative_layout_item_trending_search)
        RelativeLayout imageWrapperRelativeLayout;
        @BindView(R.id.progress_bar_item_trending_search)
        ProgressBar progressBar;
        @BindView(R.id.image_view_item_trending_search)
        AspectRatioGifImageView imageView;
        @BindView(R.id.load_image_error_relative_layout_item_trending_search)
        RelativeLayout errorRelativeLayout;
        @BindView(R.id.load_image_error_text_view_item_trending_search)
        TextView errorTextView;
        @BindView(R.id.image_view_no_preview_gallery_item_trending_search)
        ImageView noPreviewLinkImageView;
        @BindView(R.id.title_text_view_item_trending_search)
        TextView titleTextView;

        public TrendingSearchViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            noPreviewLinkImageView.setBackgroundColor(customThemeWrapper.getNoPreviewPostTypeBackgroundColor());
            noPreviewLinkImageView.setColorFilter(customThemeWrapper.getNoPreviewPostTypeIconTint(), android.graphics.PorterDuff.Mode.SRC_IN);
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(customThemeWrapper.getColorAccent()));
            errorTextView.setTextColor(customThemeWrapper.getPrimaryTextColor());

            if (activity.typeface != null) {
                titleTextView.setTypeface(activity.typeface);
                errorTextView.setTypeface(activity.typeface);
            }

            itemView.setOnClickListener(view -> {
                itemClickListener.onClick(trendingSearches.get(getBindingAdapterPosition()));
            });
        }
    }

    public interface ItemClickListener {
        void onClick(TrendingSearch trendingSearch);
    }
}
