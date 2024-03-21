package ml.docilealligator.infinityforreddit.markdown;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
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
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.MediaMetadata;
import ml.docilealligator.infinityforreddit.SaveMemoryCenterInisdeDownsampleStrategy;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.databinding.MarkdownImageAndGifBlockBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class ImageAndGifEntry extends MarkwonAdapter.Entry<ImageAndGifBlock, ImageAndGifEntry.Holder> {
    private final BaseActivity baseActivity;
    private final RequestManager glide;
    private final SaveMemoryCenterInisdeDownsampleStrategy saveMemoryCenterInsideDownsampleStrategy;
    private final OnItemClickListener onItemClickListener;
    private boolean dataSavingMode;
    private final boolean disableImagePreview;
    private boolean blurImage;
    private final int colorAccent;
    private final int primaryTextColor;
    private final int postContentColor;
    private final int linkColor;

    public ImageAndGifEntry(BaseActivity baseActivity, RequestManager glide,
                            OnItemClickListener onItemClickListener) {
        this.baseActivity = baseActivity;
        this.glide = glide;
        SharedPreferences sharedPreferences = baseActivity.getDefaultSharedPreferences();
        this.saveMemoryCenterInsideDownsampleStrategy = new SaveMemoryCenterInisdeDownsampleStrategy(
                Integer.parseInt(sharedPreferences.getString(SharedPreferencesUtils.POST_FEED_MAX_RESOLUTION, "5000000")));
        this.onItemClickListener = onItemClickListener;
        colorAccent = baseActivity.getCustomThemeWrapper().getColorAccent();
        primaryTextColor = baseActivity.getCustomThemeWrapper().getPrimaryTextColor();
        postContentColor = baseActivity.getCustomThemeWrapper().getPostContentColor();
        linkColor = baseActivity.getCustomThemeWrapper().getLinkColor();

        String dataSavingModeString = sharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
        if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ALWAYS)) {
            dataSavingMode = true;
        } else if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
            dataSavingMode = Utils.getConnectedNetwork(baseActivity) == Utils.NETWORK_TYPE_CELLULAR;
        }
        disableImagePreview = sharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_IMAGE_PREVIEW, false);
    }

    public ImageAndGifEntry(BaseActivity baseActivity, RequestManager glide, boolean blurImage,
                            OnItemClickListener onItemClickListener) {
        this(baseActivity, glide, onItemClickListener);
        this.blurImage = blurImage;
    }

    public ImageAndGifEntry(BaseActivity baseActivity, RequestManager glide, boolean dataSavingMode,
                            boolean disableImagePreview, boolean blurImage,
                            OnItemClickListener onItemClickListener) {
        this.baseActivity = baseActivity;
        this.glide = glide;
        this.dataSavingMode = dataSavingMode;
        this.disableImagePreview = disableImagePreview;
        this.blurImage = blurImage;
        SharedPreferences sharedPreferences = baseActivity.getDefaultSharedPreferences();
        this.saveMemoryCenterInsideDownsampleStrategy = new SaveMemoryCenterInisdeDownsampleStrategy(
                Integer.parseInt(sharedPreferences.getString(SharedPreferencesUtils.POST_FEED_MAX_RESOLUTION, "5000000")));
        this.onItemClickListener = onItemClickListener;
        colorAccent = baseActivity.getCustomThemeWrapper().getColorAccent();
        primaryTextColor = baseActivity.getCustomThemeWrapper().getPrimaryTextColor();
        postContentColor = baseActivity.getCustomThemeWrapper().getPostContentColor();
        linkColor = baseActivity.getCustomThemeWrapper().getLinkColor();
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
            if (disableImagePreview) {
                holder.binding.imageWrapperRelativeLayoutMarkdownImageAndGifBlock.setVisibility(View.GONE);
                holder.binding.captionTextViewMarkdownImageAndGifBlock.setVisibility(View.VISIBLE);
                holder.binding.captionTextViewMarkdownImageAndGifBlock.setGravity(Gravity.NO_GRAVITY);
                SpannableString spannableString = new SpannableString(node.mediaMetadata.caption == null ? node.mediaMetadata.original.url : node.mediaMetadata.caption);
                spannableString.setSpan(new URLSpan(node.mediaMetadata.original.url), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.binding.captionTextViewMarkdownImageAndGifBlock.setText(spannableString);
                return;
            } else {
                imageRequestBuilder = glide.load(node.mediaMetadata.downscaled.url).listener(holder.requestListener);
                holder.binding.imageViewMarkdownImageAndGifBlock.setRatio((float) node.mediaMetadata.downscaled.y / node.mediaMetadata.downscaled.x);
            }
        } else {
            imageRequestBuilder = glide.load(node.mediaMetadata.original.url).listener(holder.requestListener);
            holder.binding.imageViewMarkdownImageAndGifBlock.setRatio((float) node.mediaMetadata.original.y / node.mediaMetadata.original.x);
        }

        if (blurImage && !node.mediaMetadata.isGIF) {
            imageRequestBuilder
                    .apply(RequestOptions.bitmapTransform(
                            new MultiTransformation<>(
                                    new BlurTransformation(100, 4),
                                    new RoundedCornersTransformation(8, 0))))
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

        if (node.mediaMetadata.caption != null) {
            holder.binding.captionTextViewMarkdownImageAndGifBlock.setVisibility(View.VISIBLE);
            holder.binding.captionTextViewMarkdownImageAndGifBlock.setText(node.mediaMetadata.caption);
        }
    }

    @Override
    public void onViewRecycled(@NonNull Holder holder) {
        super.onViewRecycled(holder);
        holder.binding.imageWrapperRelativeLayoutMarkdownImageAndGifBlock.setVisibility(View.VISIBLE);
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
        holder.binding.captionTextViewMarkdownImageAndGifBlock.setVisibility(View.GONE);
        holder.binding.captionTextViewMarkdownImageAndGifBlock.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    public void setDataSavingMode(boolean dataSavingMode) {
        this.dataSavingMode = dataSavingMode;
    }

    public class Holder extends MarkwonAdapter.Holder {
        MarkdownImageAndGifBlockBinding binding;
        RequestListener<Drawable> requestListener;
        ImageAndGifBlock imageAndGifBlock;

        public Holder(@NonNull MarkdownImageAndGifBlockBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.progressBarMarkdownImageAndGifBlock.setIndeterminateTintList(ColorStateList.valueOf(colorAccent));
            binding.loadImageErrorTextViewMarkdownImageAndGifBlock.setTextColor(primaryTextColor);
            binding.captionTextViewMarkdownImageAndGifBlock.setTextColor(postContentColor);
            binding.captionTextViewMarkdownImageAndGifBlock.setLinkTextColor(linkColor);

            if (baseActivity.typeface != null) {
                binding.loadImageErrorTextViewMarkdownImageAndGifBlock.setTypeface(baseActivity.typeface);
            }
            if (baseActivity.contentTypeface != null) {
                binding.captionTextViewMarkdownImageAndGifBlock.setTypeface(baseActivity.contentTypeface);
            }

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

            binding.captionTextViewMarkdownImageAndGifBlock.setMovementMethod(
                    BetterLinkMovementMethod.newInstance()
                            .setOnLinkClickListener((textView, url) -> {
                                Intent intent = new Intent(baseActivity, LinkResolverActivity.class);
                                intent.setData(Uri.parse(url));
                                baseActivity.startActivity(intent);
                                return true;
                            })
                            .setOnLinkLongClickListener((textView, url) -> {
                                UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = UrlMenuBottomSheetFragment.newInstance(url);
                                urlMenuBottomSheetFragment.show(baseActivity.getSupportFragmentManager(), urlMenuBottomSheetFragment.getTag());
                                return true;
                            }));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(MediaMetadata mediaMetadata);
    }
}
