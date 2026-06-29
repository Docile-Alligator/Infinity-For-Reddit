package ml.docilealligator.infinityforreddit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customviews.compose.AppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.CustomFilledButton
import ml.docilealligator.infinityforreddit.customviews.compose.LocalTypography
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryText
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class OnboardingActivity: BaseActivity() {
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
    lateinit var mCustomThemeWrapper: CustomThemeWrapper

    companion object {
        fun startOnboardingActivity(context: Context) {
            context.startActivity(Intent(context, OnboardingActivity::class.java))
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
        windowInsetsController.isAppearanceLightStatusBars = customThemeWrapper.isLightStatusBar

        setContent {
            AppTheme(customThemeWrapper.themeType, mSharedPreferences) {
                val context = LocalContext.current
                val scrollBehavior = enterAlwaysScrollBehavior()
                val pagerState = rememberPagerState(pageCount = {
                    4
                })

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .imePadding(),
                    contentWindowInsets = if (isImmersiveInterfaceEnabled) WindowInsets.safeDrawing else WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize(1f)) {
                        Image(
                            painterResource(R.drawable.onboarding_background),
                            modifier = Modifier.fillMaxSize(1f),
                            contentScale = ContentScale.Crop,
                            contentDescription = stringResource(R.string.content_description_background)
                        )

                        Column {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .weight(1f)
                            ) { page ->
                                Column(
                                    modifier = Modifier
                                        .wrapContentHeight()
                                        .padding(innerPadding)
                                        .padding(32.dp)
                                ) {
                                    WelcomePage()

                                    Spacer(modifier = Modifier.height(36.dp))
                                }
                            }

                            CustomFilledButton(
                                modifier = Modifier
                                    .fillMaxWidth(1f)
                                    .padding(
                                        bottom = innerPadding.calculateBottomPadding(),
                                        start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                                        end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
                                    )
                                    .padding(horizontal = 32.dp)
                                    .padding(bottom = 32.dp),
                                stringResId = R.string.take_a_quick_tour
                            ) {

                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ColumnScope.WelcomePage() {
        Image(
            painterResource(R.drawable.onboarding_icon),
            contentDescription = stringResource(R.string.content_description_infinity_icon),
            modifier = Modifier
                .width(100.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.weight(1f))

        PrimaryText(
            R.string.welcome_to_infinity_onboarding,
            fontSize = 36.sp,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryText(
            R.string.infinitely_better_experience,
            fontSize = LocalTypography.current.fontSize.size18
        )
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