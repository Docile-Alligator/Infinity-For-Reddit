package ml.docilealligator.infinityforreddit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.view.WindowInsetsControllerCompat
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customviews.compose.AppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.CustomFilledButton
import ml.docilealligator.infinityforreddit.customviews.compose.LocalAppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.LocalTypography
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryText
import ml.docilealligator.infinityforreddit.customviews.compose.SecondaryText
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.roundToInt

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
    @Named("internal")
    lateinit var mInternalSharedPreferences: SharedPreferences
    @Inject
    lateinit var mCustomThemeWrapper: CustomThemeWrapper

    private lateinit var onboardingPageData: Array<OnboardingPageData>

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
        windowInsetsController.isAppearanceLightStatusBars =
            (getResources().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO

        onboardingPageData = arrayOf(
            OnboardingPageData(
                getString(R.string.filter_what_you_see),
                getString(R.string.filter_what_you_see_description),
                R.drawable.onboarding_filter_what_you_see,
                getString(R.string.content_description_filter_what_you_see)
            ),
            OnboardingPageData(
                getString(R.string.post_like_a_pro),
                getString(R.string.post_like_a_pro_description),
                R.drawable.onboarding_post_like_a_pro,
                getString(R.string.content_description_post_like_a_pro)
            ),
            OnboardingPageData(
                getString(R.string.make_it_yours),
                getString(R.string.make_it_yours_description),
                R.drawable.onboarding_make_it_yours,
                getString(R.string.content_description_make_it_yours)
            ),
            OnboardingPageData(
                getString(R.string.private_browsing),
                getString(R.string.private_browsing_description),
                R.drawable.onboarding_private_browsing,
                getString(R.string.content_description_private_browsing)
            ),
            OnboardingPageData(
                getString(R.string.more_features),
                getString(R.string.more_features_description),
                R.drawable.onboarding_more_features,
                getString(R.string.content_description_more_features)
            )
        )

        setContent {
            AppTheme(mCustomThemeWrapper.themeType, mSharedPreferences) {
                val context = LocalContext.current
                val pagerState = rememberPagerState(pageCount = {
                    onboardingPageData.size + 1
                })
                var continueButtonText by remember { mutableStateOf(context.getString(R.string.take_a_quick_tour)) }
                val coroutineScope = rememberCoroutineScope()
                val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
                val isCompactHeight = windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT

                LaunchedEffect(pagerState.currentPage) {
                    continueButtonText = when (pagerState.currentPage) {
                        0 -> {
                            context.getString(R.string.take_a_quick_tour)
                        }

                        onboardingPageData.size -> {
                            context.getString(R.string.get_started)
                        }

                        else -> {
                            context.getString(R.string.next)
                        }
                    }
                }

                val alphaAnimation = remember { Animatable(0f) }
                val yAnimation = remember { Animatable(20f) }

                LaunchedEffect(Unit) {
                    launch {
                        alphaAnimation.animateTo(1f, animationSpec = spring(stiffness = Spring.StiffnessLow))
                    }

                    launch {
                        yAnimation.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessLow))
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentWindowInsets = if (isImmersiveInterfaceEnabled) WindowInsets.safeDrawing else WindowInsets.navigationBars.only(
                        WindowInsetsSides.Bottom
                    )
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painterResource(R.drawable.onboarding_background),
                            modifier = Modifier.fillMaxSize(),
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
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(
                                            top = innerPadding.calculateTopPadding(),
                                            start = innerPadding.calculateStartPadding(
                                                LocalLayoutDirection.current
                                            ),
                                            end = innerPadding.calculateEndPadding(
                                                LocalLayoutDirection.current
                                            )
                                        )
                                        .padding(
                                            horizontal = 32.dp
                                        ),
                                ) {
                                    if (page == 0) {
                                        WelcomePage(
                                            if (isCompactHeight) 16.dp else 32.dp,
                                            windowSizeClass,
                                            IntOffset(x = 0, y = yAnimation.value.roundToInt()),
                                            alphaAnimation.value
                                        )
                                    } else {
                                        OnboardingPage(
                                            page,
                                            if (isCompactHeight) 16.dp else 32.dp,
                                            windowSizeClass
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        bottom = innerPadding.calculateBottomPadding(),
                                        start = innerPadding.calculateStartPadding(
                                            LocalLayoutDirection.current
                                        ),
                                        end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
                                    )
                                    .padding(horizontal = 32.dp)
                                    .padding(bottom = if (isCompactHeight) 16.dp else 32.dp)
                            ) {
                                CustomFilledButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset {
                                            IntOffset(x = 0, y = yAnimation.value.roundToInt())
                                        }
                                        .graphicsLayer {
                                            alpha = alphaAnimation.value
                                        },
                                    text = continueButtonText
                                ) {
                                    if (pagerState.currentPage < pagerState.pageCount - 1) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    } else {
                                        mInternalSharedPreferences.edit {
                                            putBoolean(
                                                SharedPreferencesUtils.ONBOARDING_FINISHED,
                                                true
                                            )
                                        }
                                        startActivity(Intent(context, MainActivity::class.java))
                                        finish()
                                    }
                                }

                                Spacer(Modifier.height(if (isCompactHeight) 8.dp else 16.dp))

                                SecondaryText(
                                    text = buildAnnotatedString {
                                        append(stringResource(R.string.by_continuing_1))

                                        withLink(
                                            LinkAnnotation.Url(
                                                "https://docile-alligator.github.io/",
                                                TextLinkStyles(
                                                    style = SpanStyle(
                                                        color = Color(
                                                            LocalAppTheme.current.linkColor
                                                        )
                                                    )
                                                )
                                            )
                                        ) {
                                            append(stringResource(R.string.privacy_policy))
                                        }

                                        append(stringResource(R.string.by_continuing_2))

                                        withLink(
                                            LinkAnnotation.Url(
                                                "https://redditinc.com/policies/user-agreement",
                                                TextLinkStyles(
                                                    style = SpanStyle(
                                                        color = Color(
                                                            LocalAppTheme.current.linkColor
                                                        )
                                                    )
                                                )
                                            )
                                        ) {
                                            append(stringResource(R.string.reddit_user_agreement))
                                        }

                                        append(stringResource(R.string.by_continuing_3))
                                    },
                                    modifier = Modifier
                                        .offset(y = yAnimation.value.dp)
                                        .graphicsLayer {
                                            alpha = alphaAnimation.value
                                        },
                                    fontFamily = LocalTypography.current.fontFamily,
                                    fontSize = 12.sp,
                                    lineHeight = if (isCompactHeight) 12.sp else TextUnit.Unspecified
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun WelcomePage(verticalPadding: Dp, windowSizeClass: WindowSizeClass, offset: IntOffset, alphaValue: Float) {
        val isTablet = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED &&
                windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.EXPANDED

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(verticalPadding))

            Image(
                painterResource(R.drawable.onboarding_icon),
                contentDescription = stringResource(R.string.content_description_infinity_icon),
                modifier = Modifier
                    .width(
                        if (
                            !(windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT ||
                                    windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT)
                        ) 200.dp else if (windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT) 70.dp else 100.dp
                    )
                    .offset {
                        offset
                    }
                    .graphicsLayer {
                        alpha = alphaValue
                    }
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTablet) {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Column(
                    horizontalAlignment = if (isTablet) Alignment.CenterHorizontally else Alignment.Start
                ) {
                    SecondaryText(
                        R.string.onboarding_welcome_to,
                        modifier = Modifier
                            .offset {
                                offset
                            }
                            .graphicsLayer {
                                alpha = alphaValue
                            },
                        fontSize = if (isTablet) 30.sp else 22.sp
                    )

                    if (isTablet) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    PrimaryText(
                        R.string.onboarding_infinity_for_reddit,
                        modifier = Modifier
                            .offset {
                                offset
                            }
                            .graphicsLayer {
                                alpha = alphaValue
                            },
                        fontSize = if (isTablet) 48.sp else 36.sp,
                        lineHeight = if (isTablet) 48.sp else 36.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryText(
                        R.string.infinitely_better_experience,
                        modifier = Modifier
                            .offset {
                                offset
                            }
                            .graphicsLayer {
                                alpha = alphaValue
                            },
                        fontSize = if (isTablet) 28.sp else 18.sp
                    )
                }

                if (isTablet) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            if (isTablet) {
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(verticalPadding))
        }
    }

    @Composable
    fun OnboardingPage(page: Int, verticalPadding: Dp, windowSizeClass: WindowSizeClass) {
        if (windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT) {
            Row(
                modifier = Modifier.padding(vertical = verticalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painterResource(onboardingPageData[page - 1].drawableResId),
                    contentDescription = onboardingPageData[page - 1].contentDescription,
                    modifier = if (windowSizeClass.windowHeightSizeClass != WindowHeightSizeClass.COMPACT) Modifier
                        .heightIn(max = 700.dp)
                        .widthIn(max = 500.dp)
                        .fillMaxSize(0.7f)
                        .weight(1f) else Modifier
                        .weight(1f)
                )

                Spacer(modifier = Modifier.width(36.dp))

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.weight(1f))

                    PrimaryText(
                        onboardingPageData[page - 1].title,
                        fontSize = 36.sp,
                        lineHeight = 36.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryText(
                        onboardingPageData[page - 1].subtitle,
                        fontSize = LocalTypography.current.fontSize.size18,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.weight(1f))
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painterResource(onboardingPageData[page - 1].drawableResId),
                    contentDescription = onboardingPageData[page - 1].contentDescription,
                    modifier = Modifier
                        .weight(1f)
                )

                Spacer(modifier = Modifier.height(36.dp))

                PrimaryText(
                    onboardingPageData[page - 1].title,
                    fontSize = 36.sp,
                    lineHeight = 36.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                PrimaryText(
                    onboardingPageData[page - 1].subtitle,
                    fontSize = LocalTypography.current.fontSize.size18,
                    textAlign = TextAlign.Center
                )
            }
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

    private class OnboardingPageData(
        val title: String,
        val subtitle: String,
        val drawableResId: Int,
        val contentDescription: String
    )
}