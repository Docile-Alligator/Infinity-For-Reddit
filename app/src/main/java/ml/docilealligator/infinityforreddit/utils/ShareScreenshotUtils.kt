package ml.docilealligator.infinityforreddit.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColors
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogo
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorShapes
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.SaveMemoryCenterInisdeDownsampleStrategy
import ml.docilealligator.infinityforreddit.activities.BaseActivity
import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.databinding.SharedCommentBinding
import ml.docilealligator.infinityforreddit.databinding.SharedPostBinding
import ml.docilealligator.infinityforreddit.post.Post
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

fun sharePostAsScreenshot(
    baseActivity: BaseActivity, post: Post, customThemeWrapper: CustomThemeWrapper,
    locale: Locale, timeFormatPattern: String,
    saveMemoryCenterInsideDownsampleStrategy: SaveMemoryCenterInisdeDownsampleStrategy
) {
    //val binding: SharedPostBinding = SharedPostBinding.inflate(LayoutInflater.from(ContextThemeWrapper(baseActivity, R.style.AppTheme)))
    val binding: SharedPostBinding = SharedPostBinding.inflate(LayoutInflater.from(baseActivity))

    binding.titleTextViewSharedPost.text = post.title
    binding.subredditNameTextViewSharedPost.text = post.subredditNamePrefixed
    binding.userTextViewSharedPost.text = post.authorNamePrefixed
    binding.postTimeTextViewSharedPost.text = Utils.getFormattedTime(
        locale,
        post.postTimeMillis,
        timeFormatPattern
    )
    binding.scoreTextViewSharedPost.text = post.score.toString()
    binding.commentsCountTextViewSharedPost.text = post.nComments.toString()

    binding.root.setBackgroundTintList(ColorStateList.valueOf(customThemeWrapper.filledCardViewBackgroundColor))
    binding.titleTextViewSharedPost.setTextColor(customThemeWrapper.postTitleColor)
    binding.contentTextViewSharedPost.setTextColor(customThemeWrapper.postContentColor)
    binding.subredditNameTextViewSharedPost.setTextColor(customThemeWrapper.subreddit)
    binding.userTextViewSharedPost.setTextColor(customThemeWrapper.username)
    binding.postTimeTextViewSharedPost.setTextColor(customThemeWrapper.secondaryTextColor)
    binding.scoreTextViewSharedPost.setTextColor(customThemeWrapper.upvoted)
    binding.commentsCountTextViewSharedPost.setTextColor(customThemeWrapper.postIconAndInfoColor)
    binding.upvoteImageViewSharedPost.setColorFilter(
        customThemeWrapper.upvoted,
        PorterDuff.Mode.SRC_IN
    )
    binding.commentImageViewSharedPost.setColorFilter(
        customThemeWrapper.postIconAndInfoColor,
        PorterDuff.Mode.SRC_IN
    )

    binding.titleTextViewSharedPost.setTypeface(baseActivity.titleTypeface)
    binding.contentTextViewSharedPost.setTypeface(baseActivity.contentTypeface)
    binding.subredditNameTextViewSharedPost.setTypeface(baseActivity.titleTypeface)
    binding.userTextViewSharedPost.setTypeface(baseActivity.titleTypeface)
    binding.postTimeTextViewSharedPost.setTypeface(baseActivity.titleTypeface)
    binding.scoreTextViewSharedPost.setTypeface(baseActivity.titleTypeface)
    binding.commentsCountTextViewSharedPost.setTypeface(baseActivity.titleTypeface)

    binding.qrCodeImageViewSharedPost.setImageDrawable(generateQRCode(baseActivity, customThemeWrapper, post.permalink))

    when (post.postType) {
        Post.VIDEO_TYPE, Post.GIF_TYPE, Post.IMAGE_TYPE, Post.GALLERY_TYPE, Post.LINK_TYPE -> {
            binding.contentTextViewSharedPost.visibility = View.GONE
            val preview = if (post.previews.isNotEmpty()) post.previews[0] else null
            if (preview != null) {
                val height = (400 * baseActivity.resources.displayMetrics.density).toInt()
                binding.imageViewSharedPost.setScaleType(ImageView.ScaleType.CENTER_CROP)
                binding.imageViewSharedPost.layoutParams.height = height
                measureView(binding.getRoot())

                val blurImage = post.isNSFW || post.isSpoiler
                val url = preview.previewUrl
                val imageRequestBuilder = Glide.with(baseActivity).load(url)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.imageViewSharedPost.visibility = View.GONE
                            measureView(binding.getRoot())
                            shareScreenshot(baseActivity, getBitmapFromView(binding.getRoot()))
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            Handler(Looper.getMainLooper()).post {
                                shareScreenshot(baseActivity, getBitmapFromView(binding.getRoot()))
                            }

                            return false
                        }
                    })
                if (blurImage) {
                    imageRequestBuilder.apply(
                        RequestOptions.bitmapTransform(
                            MultiTransformation(
                                CenterCrop(),
                                BlurTransformation(50, 10),
                                RoundedCornersTransformation(4, 0)
                            )
                        )
                    ).into(binding.imageViewSharedPost)
                } else {
                    imageRequestBuilder.apply(RequestOptions.bitmapTransform(
                        MultiTransformation(
                            CenterCrop(),
                            RoundedCornersTransformation(50, 0)
                        )
                    )).downsample(
                        saveMemoryCenterInsideDownsampleStrategy
                    ).into(binding.imageViewSharedPost)
                }
                return
            } else {
                binding.imageViewSharedPost.visibility = View.GONE
            }
        }

        Post.NO_PREVIEW_LINK_TYPE -> {
            binding.contentTextViewSharedPost.text = post.url
            binding.imageViewSharedPost.visibility = View.GONE
        }

        else -> {
            if (post.selfTextPlainTrimmed != null && post.selfTextPlainTrimmed.isNotEmpty()) {
                binding.contentTextViewSharedPost.text = post.selfTextPlainTrimmed
            }
            binding.imageViewSharedPost.visibility = View.GONE
        }
    }

    measureView(binding.getRoot())
    shareScreenshot(baseActivity, getBitmapFromView(binding.getRoot()))
}

fun shareCommentAsScreenshot(
    baseActivity: BaseActivity, comment: Comment
) {
    val binding: SharedCommentBinding = SharedCommentBinding.inflate(LayoutInflater.from(ContextThemeWrapper(baseActivity, R.style.AppTheme)))
    val customThemeWrapper = baseActivity.customThemeWrapper

    binding.userTextViewSharedComment.text = "— u/" + comment.author
    binding.contentTextViewSharedComment.text = comment.commentRawText

    binding.root.setBackgroundTintList(ColorStateList.valueOf(customThemeWrapper.filledCardViewBackgroundColor))
    binding.userTextViewSharedComment.setTextColor(customThemeWrapper.username)
    binding.contentTextViewSharedComment.setTextColor(customThemeWrapper.commentColor)
    binding.quoteImageViewSharedComment.setColorFilter(
        customThemeWrapper.colorPrimary,
        PorterDuff.Mode.SRC_IN
    )

    binding.userTextViewSharedComment.setTypeface(baseActivity.typeface)
    binding.contentTextViewSharedComment.setTypeface(baseActivity.contentTypeface)

    binding.qrCodeImageViewSharedComment.setImageDrawable(generateQRCode(baseActivity, customThemeWrapper, comment.permalink))

    measureView(binding.getRoot())
    shareScreenshot(baseActivity, getBitmapFromView(binding.getRoot()))
}

private fun measureView(rootView: View) {
    val specWidth = View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
    val specHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    rootView.measure(specWidth, specHeight)
    rootView.layout(0, 0, rootView.measuredWidth, rootView.measuredHeight)
}

private fun getBitmapFromView(rootView: View): Bitmap {
    val bitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val bgDrawable = rootView.background
    if (bgDrawable != null) bgDrawable.draw(canvas)
    else canvas.drawColor(Color.WHITE)
    rootView.draw(canvas)

    return bitmap
}

private fun generateQRCode(baseActivity: BaseActivity, customThemeWrapper: CustomThemeWrapper, url: String): Drawable {
    val data: QrData.Url = QrData.Url(url)
    return QrCodeDrawable(
        data, QrVectorOptions.Builder()
            .setLogo(
                QrVectorLogo(
                    drawable = ContextCompat.getDrawable(baseActivity, R.mipmap.ic_launcher_round),
                    size = .3f,
                    padding = QrVectorLogoPadding.Natural(.1f),
                    shape = QrVectorLogoShape.Circle
                )
            )
            .setColors(
                QrVectorColors(
                    dark = QrVectorColor.Solid(customThemeWrapper.colorAccent),
                    ball = QrVectorColor.Solid(customThemeWrapper.colorAccent),
                    frame = QrVectorColor.Solid(customThemeWrapper.colorAccent)
                )
            )
            .setShapes(
                QrVectorShapes(
                    darkPixel = QrVectorPixelShape.RoundCorners(0.5f),
                    ball = QrVectorBallShape.RoundCorners(0.5f),
                    frame = QrVectorFrameShape.RoundCorners(0.25f),
                )
            ).build()
    )
}

private fun shareScreenshot(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.externalCacheDir, "images")
        if (!cachePath.exists()) {
            cachePath.mkdirs()
        }

        val file = File(cachePath, "shared_view.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("image/png")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.clipData = ClipData.newRawUri("", uri)
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share"))
    } catch (e: IOException) {
        e.printStackTrace()
    }
}