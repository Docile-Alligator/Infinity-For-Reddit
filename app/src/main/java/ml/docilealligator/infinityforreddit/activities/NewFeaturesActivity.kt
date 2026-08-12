package ml.docilealligator.infinityforreddit.activities

import android.R.attr.onClick
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.view.WindowInsetsControllerCompat
import androidx.window.core.layout.WindowHeightSizeClass
import ml.docilealligator.infinityforreddit.BuildConfig
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customviews.compose.AppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.ColorAccentText
import ml.docilealligator.infinityforreddit.customviews.compose.CustomFilledButton
import ml.docilealligator.infinityforreddit.customviews.compose.CustomNeutralTextButton
import ml.docilealligator.infinityforreddit.customviews.compose.LocalAppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.LocalTypography
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryIcon
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryText
import ml.docilealligator.infinityforreddit.customviews.compose.SecondaryText
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class NewFeaturesActivity: BaseActivity() {
    @Inject
    @Named("no_oauth")
    lateinit var mRetrofit: Retrofit
    @Inject
    @Named("oauth")
    lateinit var mOauthRetrofit: Retrofit
    @Inject
    lateinit var mRedditDataRoomDatabase: RedditDataRoomDatabase
    @Inject
    @Named("default")
    lateinit var mSharedPreferences: SharedPreferences
    @Inject
    @Named("post_layout")
    lateinit var mPostLayoutSharedPreferences: SharedPreferences
    @Inject
    @Named("current_account")
    lateinit var mCurrentAccountSharedPreferences: SharedPreferences
    @Inject
    @Named("internal")
    lateinit var mInternalSharedPreferences: SharedPreferences
    @Inject
    lateinit var mCustomThemeWrapper: CustomThemeWrapper

    companion object {
        fun startNewFeaturesActivity(context: Context) {
            context.startActivity(Intent(context, NewFeaturesActivity::class.java))
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        ((application) as Infinity).appComponent.inject(this)

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isImmersiveInterfaceRespectForcedEdgeToEdge()) {
                enableEdgeToEdge()
            }
        }

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars =
            (getResources().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO

        setContent {
            AppTheme(mCustomThemeWrapper.themeType, mSharedPreferences) {
                val context = LocalContext.current
                var continueButtonText by remember { mutableStateOf(context.getString(R.string.take_a_quick_tour)) }
                val coroutineScope = rememberCoroutineScope()
                val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
                val isCompactHeight = windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {},
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                            ),
                            navigationIcon = {
                                IconButton(onClick = {
                                    triggerBackPress()
                                }) {
                                    PrimaryIcon(
                                        drawableId = R.drawable.ic_close_24dp,
                                        contentDescription = stringResource(R.string.action_back_content_description)
                                    )
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize(),
                    contentWindowInsets = if (isImmersiveInterfaceEnabled) WindowInsets.safeDrawing else WindowInsets.navigationBars.only(
                        WindowInsetsSides.Bottom
                    )
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(LocalAppTheme.current.backgroundColor))
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            PrimaryText(
                                R.string.important_info_post_comments_rework_title,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                fontSize = LocalTypography.current.fontSize.size20,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            SecondaryText(
                                R.string.important_info_post_comments_rework_description,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        ColorAccentText(
                            R.string.important_info_post_comments_rework_join_subreddit,
                            modifier = Modifier
                                .clickable {
                                    mInternalSharedPreferences.edit {
                                        putInt(
                                            SharedPreferencesUtils.CURRENT_VERSION,
                                            BuildConfig.VERSION_CODE
                                        )
                                    }

                                    startActivity(Intent(
                                        this@NewFeaturesActivity,
                                        ViewSubredditDetailActivity::class.java
                                    ).apply {
                                        putExtra(
                                            ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                            "Infinity_For_Reddit"
                                        )
                                    })

                                    finish()
                                }
                                .padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        CustomFilledButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            stringResId = R.string.continue_to_app
                        ) {
                            triggerBackPress()
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mInternalSharedPreferences.edit {
                    putInt(SharedPreferencesUtils.CURRENT_VERSION, BuildConfig.VERSION_CODE)
                }
                isEnabled = false
                triggerBackPress()
            }
        })
    }

    override fun getDefaultSharedPreferences(): SharedPreferences {
        return mSharedPreferences
    }

    override fun getCurrentAccountSharedPreferences(): SharedPreferences {
        return mCurrentAccountSharedPreferences
    }

    override fun getCustomThemeWrapper(): CustomThemeWrapper {
        return mCustomThemeWrapper
    }

    override fun applyCustomTheme() {

    }
}