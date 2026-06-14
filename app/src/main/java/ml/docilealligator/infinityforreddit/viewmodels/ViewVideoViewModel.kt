package ml.docilealligator.infinityforreddit.viewmodels

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.AppResult
import ml.docilealligator.infinityforreddit.LiveDataState
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.VReddItReturnType
import ml.docilealligator.infinityforreddit.activities.ViewVideoActivity
import ml.docilealligator.infinityforreddit.apis.StreamableAPIKt
import ml.docilealligator.infinityforreddit.extensions.getFileNameFromUrlString
import ml.docilealligator.infinityforreddit.fetchVideoLink
import ml.docilealligator.infinityforreddit.post.Post
import ml.docilealligator.infinityforreddit.thing.StreamableVideo
import ml.docilealligator.infinityforreddit.utils.getRandomString
import org.apache.commons.io.FilenameUtils
import retrofit2.Retrofit
import javax.inject.Provider

@UnstableApi
class ViewVideoViewModel(
    var post: Post? = null,
    videoUri: Uri? = null,
    var videoDownloadUrl: String? = null,
    private var videoFallbackDirectUrl: String? = null,
    var subredditName: String? = null,
    var id: String? = null,
    var isNSFW: Boolean = false,
    var resumePosition: Long = -1,
    var videoType: Int = 0,
    private var redgifsId: String?,
    private val vReddItUrl: String?,
    private var streamableShortCode: String?,
    var isDataSavingMode: Boolean = false,
    val dataSavingModeDefaultResolution: Int = 0,
    val nonDataSavingModeDefaultResolution: Int = 0,
    var playbackSpeed: Int
) : ViewModel() {
    var wasPlaying: Boolean = false
    var isDownloading: Boolean = false
    var isMute: Boolean = false
    var setDefaultResolutionAlready: Boolean = false

    private val _videoUri = MutableStateFlow(videoUri)
    val videoUriLiveData = _videoUri.asLiveData()

    private val _errorResId = MutableStateFlow<LiveDataState<Int?>>(LiveDataState.Uninitialized)
    val errorResId = _errorResId.asLiveData()

    val fileName: String
        get() {
            return if (redgifsId != null) {
                "Redgifs-$redgifsId.mp4"
            } else if (streamableShortCode != null) {
                "Streamable-$streamableShortCode.mp4"
            } else {
                post?.let {
                    if (it.isImgur) {
                        "Imgur-" + (videoDownloadUrl?.getFileNameFromUrlString() ?: (getRandomString() + ".mp4"));
                    } else {
                        if (videoType == ViewVideoActivity.VIDEO_TYPE_DIRECT) {
                            videoDownloadUrl?.getFileNameFromUrlString() ?: (getRandomString() + ".mp4")
                        } else {
                            it.subredditName + "-" + it.id + ".mp4";
                        }
                    }
                } ?: videoDownloadUrl?.getFileNameFromUrlString() ?: (getRandomString() + ".mp4")
            }
        }

    init {
        if (videoType == ViewVideoActivity.VIDEO_TYPE_DIRECT || videoType == ViewVideoActivity.VIDEO_TYPE_IMGUR) {
            videoDownloadUrl = videoUri?.toString()
        }
    }

    fun loadVideoLink(
        retrofit: Retrofit, vReddItRetrofit: Retrofit,
        redgifsRetrofit: Retrofit,
        streamableApiProvider: Provider<StreamableAPIKt>,
        currentAccountSharedPreferences: SharedPreferences,
    ) {
        viewModelScope.launch {
            val result = fetchVideoLink(
                retrofit, vReddItRetrofit, redgifsRetrofit, streamableApiProvider,
                currentAccountSharedPreferences, videoType, redgifsId, vReddItUrl,
                streamableShortCode
            )

            when (result) {
                is AppResult.Success -> {
                    when (val data = result.data) {
                        is StreamableVideo -> {
                            videoDownloadUrl = data.mp4?.url ?: data.mp4Mobile?.url
                            _videoUri.value = videoDownloadUrl?.toUri()
                        }

                        is Pair<*, *> -> {
                            // Redgifs
                            _videoUri.value = (data.first as? String)?.toUri()
                            videoDownloadUrl = data.first as? String
                        }

                        is VReddItReturnType -> {
                            redgifsId = data.newRedgifsId
                            streamableShortCode = data.newStreamableShortCode
                            videoFallbackDirectUrl = data.post.videoFallBackDirectUrl
                            post = data.post

                            val optionalResult = data.optionalResult
                            optionalResult?.let {
                                when (it) {
                                    is AppResult.Success -> {
                                        when (val optionalData = it.data) {
                                            is StreamableVideo -> {
                                                videoDownloadUrl = optionalData.mp4?.url ?: optionalData.mp4Mobile?.url
                                                _videoUri.value = videoDownloadUrl?.toUri()
                                            }

                                            is Pair<*, *> -> {
                                                // Redgifs or Imgur
                                                _videoUri.value = (optionalData.first as? String)?.toUri()
                                                videoDownloadUrl = optionalData.second as? String
                                            }
                                        }
                                    }

                                    is AppResult.Error -> {
                                        _errorResId.value = LiveDataState.Value(it.error as? Int ?: R.string.error_fetching_video)
                                    }
                                }
                            } ?: run {
                                _videoUri.value = data.post.videoUrl.toUri()
                            }
                        }
                    }
                }

                is AppResult.Error -> {
                    _errorResId.value = LiveDataState.Value(result.error as? Int ?: R.string.error_fetching_video)
                }
            }
        }
    }

    fun loadFallbackVideo(mediaItem: MediaItem?): Boolean {
        videoFallbackDirectUrl?.let { videoFallbackDirectUrl ->
            if (mediaItem == null || (mediaItem.localConfiguration != null && videoFallbackDirectUrl != mediaItem.localConfiguration?.uri?.toString())
            ) {
                videoType = ViewVideoActivity.VIDEO_TYPE_DIRECT
                videoDownloadUrl = videoFallbackDirectUrl
                _videoUri.value = videoFallbackDirectUrl.toUri()

                return true;
            }
        }

        return false;
    }

    companion object {
        fun provideFactory(
            post: Post?,
            videoUri: Uri?,
            videoDownloadUrl: String? = null,
            videoFallbackDirectUrl: String? = null,
            subredditName: String? = null,
            id: String? = null,
            isNSFW: Boolean = false,
            resumePosition: Long = -1,
            videoType: Int = 0,
            redgifsId: String?,
            vReddItUrl: String?,
            streamableShortCode: String?,
            isDataSavingMode: Boolean = false,
            dataSavingModeDefaultResolution: Int = 0,
            nonDataSavingModeDefaultResolution: Int = 0,
            playbackSpeed: Int
        ): ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return ViewVideoViewModel(post, videoUri,
                        videoDownloadUrl, videoFallbackDirectUrl, subredditName, id,
                        isNSFW, resumePosition, videoType, redgifsId, vReddItUrl, streamableShortCode,
                        isDataSavingMode, dataSavingModeDefaultResolution,
                        nonDataSavingModeDefaultResolution, playbackSpeed) as T
                }
            }
        }
    }
}