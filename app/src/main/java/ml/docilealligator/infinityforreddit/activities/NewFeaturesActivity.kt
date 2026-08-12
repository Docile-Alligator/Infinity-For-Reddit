package ml.docilealligator.infinityforreddit.activities

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.view.WindowInsetsControllerCompat
import androidx.window.core.layout.WindowWidthSizeClass
import ml.docilealligator.infinityforreddit.BuildConfig
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customviews.compose.AppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.ColorAccentText
import ml.docilealligator.infinityforreddit.customviews.compose.CustomFilledButton
import ml.docilealligator.infinityforreddit.customviews.compose.LocalAppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.LocalTypography
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryIcon
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryText
import ml.docilealligator.infinityforreddit.customviews.compose.SecondaryText
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils
import javax.inject.Inject
import javax.inject.Named

class NewFeaturesActivity: BaseActivity() {
    @Inject
    @Named("default")
    lateinit var mSharedPreferences: SharedPreferences
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
                val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
                val isCompactWidth = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
                        || windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM

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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(LocalAppTheme.current.backgroundColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row (
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(max = 1200.dp)
                                .padding(innerPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(horizontal = if (isCompactWidth) 0.dp else 16.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    if (!isCompactWidth) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }

                                    PrimaryText(
                                        R.string.important_info_post_comments_rework_title,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        fontSize = if (isCompactWidth) LocalTypography.current.fontSize.size20 else 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = if (isCompactWidth) TextUnit.Unspecified else 56.sp
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    SecondaryText(
                                        R.string.important_info_post_comments_rework_description,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        fontSize = if (isCompactWidth) LocalTypography.current.fontSize.default else LocalTypography.current.fontSize.size20,
                                        lineHeight = if (isCompactWidth) TextUnit.Unspecified else 24.sp
                                    )

                                    if (!isCompactWidth) {
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }

                                if (isCompactWidth) {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    JoinSubredditButton(true)

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ContinueToAppButton(true)

                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            if (!isCompactWidth) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    JoinSubredditButton(false)

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ContinueToAppButton(false)
                                }
                            }
                        }
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

    @Composable
    private fun JoinSubredditButton(isCompactWidth: Boolean) {
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

                    startActivity(
                        Intent(
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
            textAlign = TextAlign.Center,
            fontSize = if (isCompactWidth) LocalTypography.current.fontSize.default else LocalTypography.current.fontSize.size18
        )
    }

    @Composable
    private fun ContinueToAppButton(isCompactWidth: Boolean) {
        CustomFilledButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            stringResId = R.string.continue_to_app,
            fontSize = if (isCompactWidth) LocalTypography.current.fontSize.default else LocalTypography.current.fontSize.size18
        ) {
            triggerBackPress()
        }
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