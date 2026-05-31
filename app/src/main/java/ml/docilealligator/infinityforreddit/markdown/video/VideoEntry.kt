package ml.docilealligator.infinityforreddit.markdown.video

import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import io.noties.markwon.Markwon
import io.noties.markwon.recycler.MarkwonAdapter
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import ml.docilealligator.infinityforreddit.SaveMemoryCenterInisdeDownsampleStrategy
import ml.docilealligator.infinityforreddit.activities.BaseActivity
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment
import ml.docilealligator.infinityforreddit.databinding.MarkdownVideoBlockBinding
import ml.docilealligator.infinityforreddit.managers.VideoMuteManager
import ml.docilealligator.infinityforreddit.thing.MediaMetadata
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils
import ml.docilealligator.infinityforreddit.videoautoplay.ExoCreator

class VideoEntry(
    private val baseActivity: BaseActivity,
    embeddedMediaType: Int,
    /*private val exoCreator: ExoCreator,
    private val autoplayVideo: Boolean,
    private val autoplayNsfwVideos: Boolean,
    private val isNSFW: Boolean,
    private var nonDataSavingModeDefaultResolution: Int = 0,
    private var dataSavingModeDefaultResolution: Int = 0,
    private var dataSavingMode: Boolean,
    private val videoMuteManager: VideoMuteManager,*/
    private val onItemClickListener: OnItemClickListener
): MarkwonAdapter.Entry<VideoBlock, VideoEntry.Holder>() {
    private val saveMemoryCenterInsideDownsampleStrategy: SaveMemoryCenterInisdeDownsampleStrategy
    private val colorAccent: Int
    private val primaryTextColor: Int
    private val postContentColor: Int
    private val linkColor: Int
    private val canShowImage: Boolean
    private val canShowGif: Boolean

    init {
        val sharedPreferences = baseActivity.getDefaultSharedPreferences()
        this.saveMemoryCenterInsideDownsampleStrategy = SaveMemoryCenterInisdeDownsampleStrategy(
            sharedPreferences.getString(
                SharedPreferencesUtils.POST_FEED_MAX_RESOLUTION,
                "5000000"
            )!!.toInt()
        )
        colorAccent = baseActivity.getCustomThemeWrapper().getColorAccent()
        primaryTextColor = baseActivity.getCustomThemeWrapper().getPrimaryTextColor()
        postContentColor = baseActivity.getCustomThemeWrapper().getPostContentColor()
        linkColor = baseActivity.getCustomThemeWrapper().getLinkColor()
        canShowImage = SharedPreferencesUtils.canShowImage(embeddedMediaType)
        canShowGif = SharedPreferencesUtils.canShowGif(embeddedMediaType)
    }

    override fun createHolder(inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(MarkdownVideoBlockBinding.inflate(inflater, parent, false))
    }

    override fun bindHolder(markwon: Markwon, holder: Holder, node: VideoBlock) {
        holder.videoBlock = node

        if (node.mediaMetadata.caption != null) {
            holder.binding.captionTextViewMarkdownVideoBlock.visibility = View.VISIBLE
            holder.binding.captionTextViewMarkdownVideoBlock.text = node.mediaMetadata.caption
        }

        /*if (dataSavingMode) {
            showVideoAsUrl(holder, node)
        } else {
            if ((autoplayVideo && !isNSFW) || (autoplayNsfwVideos && isNSFW)) {
                if (node.mediaMetadata.caption != null) {
                    holder.binding.captionTextViewMarkdownVideoBlock.setVisibility(View.VISIBLE)
                    holder.binding.captionTextViewMarkdownVideoBlock.setText(node.mediaMetadata.caption)
                }
            } else {
                showVideoAsUrl(holder, node)
            }
        }*/
    }

    /*private fun showVideoAsUrl(holder: Holder, node: VideoBlock) {
        holder.binding.playerViewMarkdownVideoBlock.visibility = View.GONE
        holder.binding.captionTextViewMarkdownVideoBlock.visibility = View.VISIBLE
        holder.binding.captionTextViewMarkdownVideoBlock.setGravity(Gravity.NO_GRAVITY)
        val spannableString = SpannableString(node.mediaMetadata.caption ?: node.mediaMetadata.original.url)
        spannableString.setSpan(
            URLSpan(node.mediaMetadata.original.url),
            0,
            spannableString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        holder.binding.captionTextViewMarkdownVideoBlock.text = spannableString
    }*/

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        //holder.binding.errorLoadingVideoImageViewMarkdownVideoBlock.setVisibility(View.GONE)
        holder.binding.captionTextViewMarkdownVideoBlock.visibility = View.GONE
        holder.binding.captionTextViewMarkdownVideoBlock.setGravity(Gravity.CENTER_HORIZONTAL)
    }

    /*fun setDataSavingMode(dataSavingMode: Boolean) {
        this.dataSavingMode = dataSavingMode
    }*/

    inner class Holder(
        val binding: MarkdownVideoBlockBinding
    ) : MarkwonAdapter.Holder(binding.getRoot()) {
        var videoBlock: VideoBlock? = null
        /*var container: Container? = null
        var helper: ExoPlayerViewHelper? = null
        var volume = 0f
        var isManuallyPaused = false
        val playDrawable: Drawable
        val pauseDrawable: Drawable
        var setDefaultResolutionAlready: Boolean = false*/

        init {
            /*playDrawable = AppCompatResources.getDrawable(baseActivity, R.drawable.ic_play_arrow_24dp)!!
            pauseDrawable = AppCompatResources.getDrawable(baseActivity, R.drawable.ic_pause_24dp)!!*/

            binding.captionTextViewMarkdownVideoBlock.setTextColor(postContentColor)
            binding.captionTextViewMarkdownVideoBlock.setLinkTextColor(linkColor)

            if (baseActivity.contentTypeface != null) {
                binding.captionTextViewMarkdownVideoBlock.setTypeface(baseActivity.contentTypeface)
            }

            binding.frameLayoutMarkdownVideoBlock.setOnClickListener {
                onItemClickListener.onItemClick(videoBlock?.mediaMetadata)
            }

            binding.captionTextViewMarkdownVideoBlock.movementMethod = BetterLinkMovementMethod.newInstance()
                .setOnLinkClickListener { _, url: String ->
                    val intent = Intent(baseActivity, LinkResolverActivity::class.java)
                    intent.setData(url.toUri())
                    baseActivity.startActivity(intent)
                    true
                }
                .setOnLinkLongClickListener { _, url: String ->
                    val urlMenuBottomSheetFragment = UrlMenuBottomSheetFragment.newInstance(url)
                    urlMenuBottomSheetFragment.show(
                        baseActivity.supportFragmentManager,
                        urlMenuBottomSheetFragment.tag
                    )
                    true
                }
        }

        /*override fun getPlayerView(): View {
            return binding.playerViewMarkdownVideoBlock
        }

        override fun getCurrentPlaybackInfo(): PlaybackInfo {
            return helper?.latestPlaybackInfo ?: PlaybackInfo()
        }

        override fun initialize(
            container: Container,
            playbackInfo: PlaybackInfo
        ) {
            videoBlock?.mediaMetadata?.original?.url?.let { url ->
                if (this.container == null) {
                    this.container = container
                }
                if (helper == null) {
                    helper = ExoPlayerViewHelper(this, url.toUri(), null, exoCreator)
                    helper?.addEventListener(object : DefaultEventListener() {
                        override fun onEvents(player: Player, events: Player.Events) {
                            if (events.containsAny(
                                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                                    Player.EVENT_PLAYBACK_SUPPRESSION_REASON_CHANGED
                                )
                            ) {
                                playPauseButton.setImageDrawable(if (Util.shouldShowPlayButton(player)) playDrawable else pauseDrawable)
                            }
                        }

                        override fun onTracksChanged(tracks: Tracks) {
                            val trackGroups: ImmutableList<Tracks.Group> = tracks.getGroups()
                            if (!trackGroups.isEmpty()) {
                                videoQualityButton.setVisibility(View.VISIBLE)
                                videoQualityButton.setOnClickListener(View.OnClickListener { view: View? ->
                                    val builder = TrackSelectionDialogBuilder(
                                        baseActivity,
                                        baseActivity.getString(R.string.select_video_quality),
                                        helper.getPlayer(),
                                        C.TRACK_TYPE_VIDEO
                                    ).apply {
                                        setShowDisableOption(true)
                                        setAllowAdaptiveSelections(false)
                                    }
                                    val dialog =
                                        builder.setTheme(R.style.MaterialAlertDialogTheme).build()
                                    dialog.show()
                                    if (dialog is AlertDialog) {
                                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                            .setTextColor(primaryTextColor)
                                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                            .setTextColor(primaryTextColor)
                                    }
                                })

                                if (!setDefaultResolutionAlready) {
                                    var desiredResolution = 0
                                    if (dataSavingMode) {
                                        if (dataSavingModeDefaultResolution > 0) {
                                            desiredResolution = dataSavingModeDefaultResolution
                                        }
                                    } else if (nonDataSavingModeDefaultResolution > 0) {
                                        desiredResolution = nonDataSavingModeDefaultResolution
                                    }

                                    if (desiredResolution > 0) {
                                        var trackSelectionOverride: TrackSelectionOverride? = null
                                        var bestTrackIndex = -1
                                        var bestResolution = -1
                                        var worstResolution = Int.Companion.MAX_VALUE
                                        var worstTrackIndex = -1
                                        var bestTrackGroup: Tracks.Group? = null
                                        var worstTrackGroup: Tracks.Group? = null
                                        for (trackGroup in tracks.getGroups()) {
                                            if (trackGroup.type == C.TRACK_TYPE_VIDEO) {
                                                for (trackIndex in 0..<trackGroup.length) {
                                                    val trackResolution = min(
                                                        trackGroup.getTrackFormat(trackIndex).height,
                                                        trackGroup.getTrackFormat(trackIndex).width
                                                    )
                                                    if (trackResolution in (bestResolution + 1)..desiredResolution) {
                                                        bestTrackIndex = trackIndex
                                                        bestResolution = trackResolution
                                                        bestTrackGroup = trackGroup
                                                    }
                                                    if (trackResolution < worstResolution) {
                                                        worstTrackIndex = trackIndex
                                                        worstResolution = trackResolution
                                                        worstTrackGroup = trackGroup
                                                    }
                                                }
                                            }
                                        }

                                        if (bestTrackIndex != -1 && bestTrackGroup != null) {
                                            trackSelectionOverride = TrackSelectionOverride(
                                                bestTrackGroup.getMediaTrackGroup(),
                                                ImmutableList.of<Int?>(bestTrackIndex)
                                            )
                                        } else if (worstTrackIndex != -1 && worstTrackGroup != null) {
                                            trackSelectionOverride = TrackSelectionOverride(
                                                worstTrackGroup.getMediaTrackGroup(),
                                                ImmutableList.of<Int?>(worstTrackIndex)
                                            )
                                        }

                                        if (trackSelectionOverride != null) {
                                            helper?.let {
                                                it.player.trackSelectionParameters =
                                                    it.player.trackSelectionParameters
                                                        .buildUpon()
                                                        .addOverride(trackSelectionOverride)
                                                        .build()
                                            }
                                        }
                                    }
                                    setDefaultResolutionAlready = true
                                }

                                for (i in trackGroups.indices) {
                                    val mimeType = trackGroups[i]!!.getTrackFormat(0).sampleMimeType
                                    if (mimeType != null && mimeType.contains("audio")) {
                                        videoMuteManager.getMasterMutingOption()?.let {
                                            volume = if (it) 0f else 1f
                                        }
                                        helper?.volume = volume
                                        muteButton.setVisibility(View.VISIBLE)
                                        if (volume != 0f) {
                                            muteButton.setImageDrawable(baseActivity.getDrawable(R.drawable.ic_unmute_24dp))
                                        } else {
                                            muteButton.setImageDrawable(baseActivity.getDrawable(R.drawable.ic_mute_24dp))
                                        }
                                        break
                                    }
                                }
                            } else {
                                muteButton.setVisibility(View.GONE)
                            }
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            binding.errorLoadingVideoImageViewMarkdownVideoBlock.setVisibility(View.VISIBLE)
                        }
                    })
                }
                helper?.initialize(container, playbackInfo)
            }
        }

        override fun play() {
            helper?.let { helper ->
                if (!isPlaying && isManuallyPaused) {
                    helper.play()
                    pause()
                    helper.volume = volume
                } else {
                    helper.play()
                }
                baseActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        override fun pause() {
            helper?.let { helper ->
                helper.pause()
                baseActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        override fun isPlaying(): Boolean {
            return helper?.isPlaying() ?: false
        }

        override fun release() {
            if (helper != null) {
                helper?.release()
                helper = null
            }
            container = null
        }

        override fun wantsToPlay(): Boolean {
            return canPlayVideo && ToroUtil.visibleAreaOffset(
                this,
                itemView.parent
            ) >= mStartAutoplayVisibleAreaOffset
        }

        override fun getPlayerOrder(): Int {
            TODO("Not yet implemented")
        }*/
    }

    interface OnItemClickListener {
        fun onItemClick(mediaMetadata: MediaMetadata?)
    }
}