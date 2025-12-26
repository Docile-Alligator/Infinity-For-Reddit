package ml.docilealligator.infinityforreddit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.ActionState
import ml.docilealligator.infinityforreddit.ActionStateError
import ml.docilealligator.infinityforreddit.DataLoadState
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customviews.compose.AppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.CustomLoadingIndicator
import ml.docilealligator.infinityforreddit.customviews.compose.CustomTextField
import ml.docilealligator.infinityforreddit.customviews.compose.LocalAppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryIcon
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryText
import ml.docilealligator.infinityforreddit.customviews.compose.SwitchRow
import ml.docilealligator.infinityforreddit.customviews.compose.ThemedTopAppBar
import ml.docilealligator.infinityforreddit.customviews.compose.ToolbarIcon
import ml.docilealligator.infinityforreddit.multireddit.ExpandedSubredditInMultiReddit
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit
import ml.docilealligator.infinityforreddit.repositories.CopyMultiRedditActivityRepositoryImpl
import ml.docilealligator.infinityforreddit.viewmodels.CopyMultiRedditActivityViewModel
import ml.docilealligator.infinityforreddit.viewmodels.CopyMultiRedditActivityViewModel.Companion.provideFactory
import retrofit2.Retrofit
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

@OptIn(ExperimentalMaterial3Api::class)
class CopyMultiRedditActivity : BaseActivity() {
    @Inject
    @Named("oauth")
    lateinit var mOauthRetrofit: Retrofit
    @Inject
    lateinit var mRedditDataRoomDatabase: RedditDataRoomDatabase
    @Inject
    @Named("default")
    lateinit var mSharedPreferences: SharedPreferences
    @Inject
    @Named("current_account")
    lateinit var mCurrentAccountSharedPreferences: SharedPreferences
    @Inject
    lateinit var mCustomThemeWrapper: CustomThemeWrapper
    @Inject
    lateinit var mExecutor: Executor
    lateinit var copyMultiRedditActivityViewModel: CopyMultiRedditActivityViewModel

    companion object {
        private const val EXTRA_MULTIPATH = "EM"

        fun start(context: Context, multipath: String) {
            val intent = Intent(context, CopyMultiRedditActivity::class.java).apply {
                putExtra(EXTRA_MULTIPATH, multipath)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ((application) as Infinity).appComponent.inject(this)

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isImmersiveInterfaceRespectForcedEdgeToEdge()) {
                enableEdgeToEdge()
            }
        }

        val multipath = intent.getStringExtra(EXTRA_MULTIPATH) ?: ""

        copyMultiRedditActivityViewModel = ViewModelProvider.create(
            this,
            provideFactory(multipath, CopyMultiRedditActivityRepositoryImpl(mOauthRetrofit, mRedditDataRoomDatabase, accessToken ?: ""))
        )[CopyMultiRedditActivityViewModel::class.java]

        copyMultiRedditActivityViewModel.fetchMultiRedditInfo()

        setContent {
            AppTheme(customThemeWrapper.themeType) {
                val context = LocalContext.current
                val scrollBehavior = enterAlwaysScrollBehavior()
                val multiRedditState by copyMultiRedditActivityViewModel.multiRedditState.collectAsStateWithLifecycle()
                val copyMultiRedditState by copyMultiRedditActivityViewModel.copyMultiRedditState.collectAsStateWithLifecycle()
                val name by copyMultiRedditActivityViewModel.name.collectAsStateWithLifecycle()
                val description by copyMultiRedditActivityViewModel.description.collectAsStateWithLifecycle()

                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }
                val copyingMultiRedditMessage = stringResource(R.string.copying_multi_reddit)

                LaunchedEffect(copyMultiRedditState) {
                    when (copyMultiRedditState) {
                        is ActionState.Error -> {
                            val error = (copyMultiRedditState as ActionState.Error).error
                            scope.launch {
                                when (error) {
                                    is ActionStateError.Message -> snackbarHostState.showSnackbar(error.message)
                                    is ActionStateError.MessageRes -> snackbarHostState.showSnackbar(context.getString(error.resId))
                                }
                            }
                        }
                        is ActionState.Idle -> {

                        }
                        is ActionState.Running -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(copyingMultiRedditMessage)
                            }
                        }
                        is ActionState.Success<*> -> {
                            startActivity(Intent(this@CopyMultiRedditActivity, ViewMultiRedditDetailActivity::class.java).apply {
                                val data = (copyMultiRedditState as ActionState.Success<*>).data
                                if (data is MultiReddit) {
                                    putExtra(ViewMultiRedditDetailActivity.EXTRA_MULTIREDDIT_PATH, data.path)
                                }
                            })
                            finish()
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        ThemedTopAppBar(
                            titleStringResId = R.string.copy_multireddit_activity_label,
                            respectTopInsets = isImmersiveInterfaceEnabled,
                            scrollBehavior = scrollBehavior,
                            actions = {
                                IconButton(onClick = {
                                    if (multiRedditState is DataLoadState.Success) {
                                        copyMultiRedditActivityViewModel.copyMultiRedditInfo()
                                    }
                                }) {
                                    ToolbarIcon(
                                        drawableId = R.drawable.ic_check_circle_toolbar_24dp,
                                        contentDescription = stringResource(R.string.action_copy_multi_reddit)
                                    )
                                }
                            }
                        ) {
                            finish()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .imePadding(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                    contentWindowInsets = if (isImmersiveInterfaceEnabled) WindowInsets.safeDrawing else WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                ) { innerPadding ->
                    when(multiRedditState) {
                        is DataLoadState.Loading -> {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .background(Color(LocalAppTheme.current.backgroundColor))
                                .padding(innerPadding)) {
                                CustomLoadingIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                        is DataLoadState.Error -> {
                            val interactionSource = remember { MutableInteractionSource() }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(LocalAppTheme.current.backgroundColor))
                                    .padding(innerPadding)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) {
                                        copyMultiRedditActivityViewModel.fetchMultiRedditInfo()
                                    }
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                PrimaryIcon(
                                    drawableId = R.drawable.ic_error_outline_black_day_night_24dp,
                                    contentDescription = stringResource(R.string.cannot_fetch_multireddit_tap_to_retry)
                                )

                                PrimaryText(
                                    R.string.cannot_fetch_multireddit_tap_to_retry,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        is DataLoadState.Success -> {
                            LazyColumn(
                                modifier = Modifier.background(Color(LocalAppTheme.current.backgroundColor)),
                                contentPadding = innerPadding
                            ) {
                                item {
                                    CustomTextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .padding(top = 16.dp, bottom = 8.dp),
                                        value = name,
                                        placeholder = stringResource(R.string.multi_reddit_name_hint)
                                    ) {
                                        copyMultiRedditActivityViewModel.setName(it)
                                    }
                                }

                                item {
                                    CustomTextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .padding(top = 8.dp, bottom = 8.dp),
                                        value = description,
                                        placeholder = stringResource(R.string.multi_reddit_description_hint)
                                    ) {
                                        copyMultiRedditActivityViewModel.setDescription(it)
                                    }
                                }

                                items((multiRedditState as DataLoadState.Success).data.subreddits) { subreddit ->
                                    SubredditRow(subreddit)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun SubredditRow(expandedSubredditInMultiReddit: ExpandedSubredditInMultiReddit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    startActivity(Intent(this, ViewSubredditDetailActivity::class.java).apply {
                        putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, expandedSubredditInMultiReddit.name)
                    })
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlideImage(
                modifier = Modifier
                    .padding(end = 32.dp)
                    .size(36.dp),
                model = expandedSubredditInMultiReddit.iconUrl,
                failure = placeholder(R.drawable.subreddit_default_icon),
                contentDescription = expandedSubredditInMultiReddit.name
            ) {
                it.apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(72, 0)))
            }

            PrimaryText(expandedSubredditInMultiReddit.name)
        }
    }

    override fun getDefaultSharedPreferences(): SharedPreferences? {
        return mSharedPreferences
    }

    override fun getCurrentAccountSharedPreferences(): SharedPreferences? {
        return mCurrentAccountSharedPreferences
    }

    override fun getCustomThemeWrapper(): CustomThemeWrapper? {
        return mCustomThemeWrapper
    }

    override fun applyCustomTheme() {

    }
}