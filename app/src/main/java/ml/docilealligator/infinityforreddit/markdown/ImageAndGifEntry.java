package ml.docilealligator.infinityforreddit.markdown;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import io.noties.markwon.Markwon;
import io.noties.markwon.recycler.MarkwonAdapter;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.MediaMetadata;
import ml.docilealligator.infinityforreddit.SaveMemoryCenterInisdeDownsampleStrategy;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.databinding.MarkdownImageAndGifBlockBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class ImageAndGifEntry extends MarkwonAdapter.Entry<ImageAndGifBlock, ImageAndGifEntry.Holder> {
    private BaseActivity baseActivity;
    private RequestManager glide;
    private SaveMemoryCenterInisdeDownsampleStrategy saveMemoryCenterInsideDownsampleStrategy;
    private OnItemClickListener onItemClickListener;
    private boolean dataSavingMode;
    private boolean blurImage;
    private int colorAccent;

    public ImageAndGifEntry(BaseActivity baseActivity, RequestManager glide,
                            OnItemClickListener onItemClickListener) {
        this.baseActivity = baseActivity;
        this.glide = glide;
        SharedPreferences sharedPreferences = baseActivity.getDefaultSharedPreferences();
        this.saveMemoryCenterInsideDownsampleStrategy = new SaveMemoryCenterInisdeDownsampleStrategy(
                Integer.parseInt(sharedPreferences.getString(SharedPreferencesUtils.POST_FEED_MAX_RESOLUTION, "5000000")));
        this.onItemClickListener = onItemClickListener;
        colorAccent = baseActivity.getCustomThemeWrapper().getColorAccent();
        String dataSavingModeString = sharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
        if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ALWAYS)) {
            dataSavingMode = true;
        } else if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
            dataSavingMode = Utils.getConnectedNetwork(baseActivity) == Utils.NETWORK_TYPE_CELLULAR;
        }
    }

    public ImageAndGifEntry(BaseActivity baseActivity, RequestManager glide, boolean dataSavingMode, boolean blurImage,
                            OnItemClickListener onItemClickListener) {
        this.baseActivity = baseActivity;
        this.glide = glide;

        SharedPreferences sharedPreferences = baseActivity.getDefaultSharedPreferences();
        this.saveMemoryCenterInsideDownsampleStrategy = new SaveMemoryCenterInisdeDownsampleStrategy(
                Integer.parseInt(sharedPreferences.getString(SharedPreferencesUtils.POST_FEED_MAX_RESOLUTION, "5000000")));
        this.onItemClickListener = onItemClickListener;
        this.dataSavingMode = dataSavingMode;
        this.blurImage = blurImage;
    }

    @NonNull
    @Override
    public Holder createHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new Holder(MarkdownImageAndGifBlockBinding.inflate(inflater, parent, false));
    }

    @Override
    public void bindHolder(@NonNull Markwon markwon, @NonNull Holder holder, @NonNull ImageAndGifBlock node) {
        holder.imageAndGifBlock = node;

        holder.binding.progressBarMarkdownImageAndGifBlock.setVisibility(View.VISIBLE);

        if (node.mediaMetadata.isGIF) {
            ViewGroup.LayoutParams params = holder.binding.imageViewMarkdownImageAndGifBlock.getLayoutParams();
            params.width = (int) Utils.convertDpToPixel(160, baseActivity);
            holder.binding.imageViewMarkdownImageAndGifBlock.setLayoutParams(params);

            FrameLayout.LayoutParams progressBarParams = (FrameLayout.LayoutParams) holder.binding.progressBarMarkdownImageAndGifBlock.getLayoutParams();
            progressBarParams.gravity = Gravity.CENTER_VERTICAL;
            progressBarParams.leftMargin = (int) Utils.convertDpToPixel(56, baseActivity);
            holder.binding.progressBarMarkdownImageAndGifBlock.setLayoutParams(progressBarParams);
        }

        RequestBuilder<Drawable> imageRequestBuilder;
        if (dataSavingMode) {
            imageRequestBuilder = glide.load(node.mediaMetadata.downscaled.url).listener(holder.requestListener);
            holder.binding.imageViewMarkdownImageAndGifBlock.setRatio((float) node.mediaMetadata.downscaled.y / node.mediaMetadata.downscaled.x);
        } else {
            imageRequestBuilder = glide.load(node.mediaMetadata.original.url).listener(holder.requestListener);
            holder.binding.imageViewMarkdownImageAndGifBlock.setRatio((float) node.mediaMetadata.original.y / node.mediaMetadata.original.x);
        }

        if (blurImage && !node.mediaMetadata.isGIF) {
            imageRequestBuilder
                    .apply(RequestOptions.bitmapTransform(
                            new MultiTransformation<>(
                                    new BlurTransformation(50, 10),
                                    new RoundedCornersTransformation(72, 0))))
                    .into(holder.binding.imageViewMarkdownImageAndGifBlock);
        } else {
            imageRequestBuilder
                    .apply(RequestOptions.bitmapTransform(
                            new MultiTransformation<>(
                                    new CenterInside(),
                                    new RoundedCornersTransformation(16, 0))))
                    .downsample(saveMemoryCenterInsideDownsampleStrategy)
                    .into(holder.binding.imageViewMarkdownImageAndGifBlock);
        }
    }

    @Override
    public void onViewRecycled(@NonNull Holder holder) {
        super.onViewRecycled(holder);
        ViewGroup.LayoutParams params = holder.binding.imageViewMarkdownImageAndGifBlock.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        holder.binding.imageViewMarkdownImageAndGifBlock.setLayoutParams(params);

        FrameLayout.LayoutParams progressBarParams = (FrameLayout.LayoutParams) holder.binding.progressBarMarkdownImageAndGifBlock.getLayoutParams();
        progressBarParams.gravity = Gravity.CENTER;
        progressBarParams.leftMargin = (int) Utils.convertDpToPixel(8, baseActivity);
        holder.binding.progressBarMarkdownImageAndGifBlock.setLayoutParams(progressBarParams);

        glide.clear(holder.binding.imageViewMarkdownImageAndGifBlock);
        holder.binding.progressBarMarkdownImageAndGifBlock.setVisibility(View.GONE);
        holder.binding.loadImageErrorTextViewMarkdownImageAndGifBlock.setVisibility(View.GONE);
    }

    public class Holder extends MarkwonAdapter.Holder {
        MarkdownImageAndGifBlockBinding binding;
        RequestListener<Drawable> requestListener;
        ImageAndGifBlock imageAndGifBlock;

        public Holder(@NonNull MarkdownImageAndGifBlockBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.progressBarMarkdownImageAndGifBlock.setIndeterminateTintList(ColorStateList.valueOf(colorAccent));

            requestListener = new RequestListener<>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    binding.progressBarMarkdownImageAndGifBlock.setVisibility(View.GONE);
                    binding.loadImageErrorTextViewMarkdownImageAndGifBlock.setVisibility(View.VISIBLE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    binding.progressBarMarkdownImageAndGifBlock.setVisibility(View.GONE);
                    return false;
                }
            };

            binding.imageViewMarkdownImageAndGifBlock.setOnClickListener(view -> {
                if (imageAndGifBlock != null) {
                    onItemClickListener.onItemClick(imageAndGifBlock.mediaMetadata);
                }
            });
        }

        /*public Holder(@NonNull AdapterGifEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.progressBar.setIndeterminateTintList(ColorStateList.valueOf(customThemeWrapper.getColorAccent()));
            binding.giphyWatermark.setTextColor(customThemeWrapper.getCommentColor());

            binding.gifLink.setTextColor(customThemeWrapper.getLinkColor());

        }*/

        /*void bindImage(ImageMetadata image) {
            binding.gifLink.setVisibility(View.GONE);
            binding.iv.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.giphyWatermark.setVisibility(View.GONE);

            ViewGroup.LayoutParams params = binding.iv.getLayoutParams();
            if (image.x > image.y) {
                params.height = dpToPx(160);
                params.width = params.height * image.x / image.y;
            } else {
                params.width = dpToPx(160);
                params.height = params.width * image.y / image.x;
            }
            binding.iv.setLayoutParams(params);

            // todo: check if waitForLayout is necessary here since we explicitly set width/height in LP
            Target<Drawable> target = new DrawableImageViewTarget(binding.iv)
                    .waitForLayout();
            glide.load(image.getUrl())
                    .addListener(requestListener)
                    .error(R.drawable.ic_error_outline_black_24dp)
                    .into(target);
        }

        void bindGif(GiphyGifMetadata gif) {
            if (!canLoadGif()) {
                // video autoplay is disabled, don't load gif
                binding.gifLink.setVisibility(View.VISIBLE);
                binding.iv.setVisibility(View.GONE);
                binding.progressBar.setVisibility(View.GONE);
                binding.giphyWatermark.setVisibility(View.GONE);

                binding.gifLink.setOnClickListener(v -> {
                    onClickListener.accept(Uri.parse(gif.getGifUrl()));
                });
                return;
            }
            binding.gifLink.setVisibility(View.GONE);
            binding.iv.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.giphyWatermark.setVisibility(View.VISIBLE);

            ViewGroup.LayoutParams params = binding.iv.getLayoutParams();
            if (gif.x > gif.y) {
                params.height = dpToPx(160);
                params.width = params.height * gif.x / gif.y;
            } else {
                params.width = dpToPx(160);
                params.height = params.width * gif.y / gif.x;
            }
            binding.iv.setLayoutParams(params);

            // todo: check if waitForLayout is necessary here since we explicitly set width/height in LP
            Target<Drawable> target = new DrawableImageViewTarget(binding.iv)
                    .waitForLayout();
            glide.load(gif.getGifUrl())
                    .addListener(requestListener)
                    .error(R.drawable.ic_error_outline_black_24dp)
                    .into(target);
        }*/
    }

    public interface OnItemClickListener {
        void onItemClick(MediaMetadata mediaMetadata);
    }
}
