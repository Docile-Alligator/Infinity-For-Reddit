package ml.docilealligator.infinityforreddit.activities

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spanned
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.request.RequestOptions
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.core.MarkwonTheme
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed
import ml.docilealligator.infinityforreddit.customviews.compose.AppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.CustomCircularWavyProgressIndicator
import ml.docilealligator.infinityforreddit.customviews.compose.CustomFilledButton
import ml.docilealligator.infinityforreddit.customviews.compose.CustomHorizontalDivider
import ml.docilealligator.infinityforreddit.customviews.compose.CustomLoadingIndicator
import ml.docilealligator.infinityforreddit.customviews.compose.CustomModalBottomSheet
import ml.docilealligator.infinityforreddit.customviews.compose.CustomPositiveTextButton
import ml.docilealligator.infinityforreddit.customviews.compose.CustomSpinner
import ml.docilealligator.infinityforreddit.customviews.compose.CustomSwitch
import ml.docilealligator.infinityforreddit.customviews.compose.CustomTextButton
import ml.docilealligator.infinityforreddit.customviews.compose.CustomTextField
import ml.docilealligator.infinityforreddit.customviews.compose.FilledLoadingButton
import ml.docilealligator.infinityforreddit.customviews.compose.IconTextRowButton
import ml.docilealligator.infinityforreddit.customviews.compose.LocalAppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.LocalTypography
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryIcon
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryText
import ml.docilealligator.infinityforreddit.customviews.compose.SecondaryText
import ml.docilealligator.infinityforreddit.customviews.compose.SendMessageIconButton
import ml.docilealligator.infinityforreddit.customviews.compose.SubredditText
import ml.docilealligator.infinityforreddit.customviews.compose.SwitchRow
import ml.docilealligator.infinityforreddit.customviews.compose.ThemedTopAppBar
import ml.docilealligator.infinityforreddit.customviews.compose.ToolbarIcon
import ml.docilealligator.infinityforreddit.customviews.compose.UserText
import ml.docilealligator.infinityforreddit.customviews.compose.shimmer.ShimmerContainer
import ml.docilealligator.infinityforreddit.customviews.compose.shimmer.ShimmerSurface
import ml.docilealligator.infinityforreddit.database.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils
import ml.docilealligator.infinityforreddit.moderation.modmail.Conversation
import ml.docilealligator.infinityforreddit.moderation.modmail.ModAction
import ml.docilealligator.infinityforreddit.moderation.modmail.ModActionType
import ml.docilealligator.infinityforreddit.moderation.modmail.ModMailRecentComment
import ml.docilealligator.infinityforreddit.moderation.modmail.ModMessage
import ml.docilealligator.infinityforreddit.moderation.modmail.RecentConvo
import ml.docilealligator.infinityforreddit.repositories.ModMailConversationRepository
import ml.docilealligator.infinityforreddit.state.DataLoadState
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils
import ml.docilealligator.infinityforreddit.utils.Utils
import ml.docilealligator.infinityforreddit.utils.getFormattedTime
import ml.docilealligator.infinityforreddit.viewmodels.ModConversationAction
import ml.docilealligator.infinityforreddit.viewmodels.ModMailConversationViewModel
import ml.docilealligator.infinityforreddit.viewmodels.ModMailState
import ml.docilealligator.infinityforreddit.viewmodels.ModUserAction
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalGlideComposeApi::class
)
class ModMailActivity : BaseActivity() {
    @Inject
    lateinit var mRedditDataRoomDatabase: RedditDataRoomDatabase
    @Inject
    @Named("oauth")
    lateinit var mOauthRetrofit: Retrofit
    @Inject
    @Named("default")
    lateinit var mSharedPreferences: SharedPreferences
    @Inject
    @Named("current_account")
    lateinit var mCurrentAccountSharedPreferences: SharedPreferences
    @Inject
    lateinit var mCustomThemeWrapper: CustomThemeWrapper
    lateinit var conversationViewModel: ModMailConversationViewModel
    lateinit var mTimeFormatPattern: String

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as Infinity).appComponent.inject(this)

        super.onCreate(savedInstanceState)

        if (accessToken == null) {
            Toast.makeText(this, R.string.login_first, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isImmersiveInterfaceRespectForcedEdgeToEdge()) {
                enableEdgeToEdge()
            }
        }

        conversationViewModel = ViewModelProvider.create(this, ModMailConversationViewModel.Factory(
            mRedditDataRoomDatabase, mOauthRetrofit, accessToken!!, accountName, mSharedPreferences))[ModMailConversationViewModel::class]

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = customThemeWrapper.isLightStatusBar

        mTimeFormatPattern = mSharedPreferences.getString(
            SharedPreferencesUtils.TIME_FORMAT_KEY,
            SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE
        ) ?: SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE

        setContent {
            AppTheme(customThemeWrapper.themeType, mSharedPreferences) {
                val state by conversationViewModel.currentState.collectAsStateWithLifecycle()

                key(state) {
                    val scrollBehavior = enterAlwaysScrollBehavior()

                    val scope = rememberCoroutineScope()
                    val pagerState = rememberPagerState { 4 }
                    val selectedTabIndex by remember { derivedStateOf { pagerState.currentPage } }
                    var isToolbarMenuOpen by remember { mutableStateOf(false) }

                    val isStateSpinnerShownState = remember { mutableStateOf(false) }

                    val unreadNavigator = rememberListDetailPaneScaffoldNavigator<Conversation>()
                    val recentNavigator = rememberListDetailPaneScaffoldNavigator<Conversation>()
                    val modNavigator = rememberListDetailPaneScaffoldNavigator<Conversation>()
                    val userNavigator = rememberListDetailPaneScaffoldNavigator<Conversation>()

                    val refreshConversationsTriggers = listOf(
                        remember { mutableStateOf(false) },
                        remember { mutableStateOf(false) },
                        remember { mutableStateOf(false) },
                        remember { mutableStateOf(false) }
                    )

                    val refreshConversationMessagesTriggers = listOf(
                        remember { mutableStateOf(false) },
                        remember { mutableStateOf(false) },
                        remember { mutableStateOf(false) },
                        remember { mutableStateOf(false) }
                    )

                    val showUserActions = listOf(
                        remember { mutableStateOf(false) },
                        remember { mutableStateOf(false) },
                        remember { mutableStateOf(false) },
                        remember { mutableStateOf(false) }
                    )

                    val showUserInfoStates = listOf(
                        remember { mutableStateOf(false) },
                        remember { mutableStateOf(false) },
                        remember { mutableStateOf(false) },
                        remember { mutableStateOf(false) }
                    )

                    var showSelectSubredditsSheet by remember { mutableStateOf(false) }

                    fun getNavigator(page: Int): ThreePaneScaffoldNavigator<Conversation> {
                        return when(page) {
                            0 -> unreadNavigator
                            1 -> recentNavigator
                            2 -> modNavigator
                            3 -> userNavigator
                            else -> unreadNavigator
                        }
                    }

                    LaunchedEffect(pagerState.currentPage) {
                        conversationViewModel.initialLoad(getRepository(pagerState.currentPage))
                    }

                    Scaffold(
                        topBar = {
                            ThemedTopAppBar(
                                titleStringResId = R.string.mod_mail_activity_label,
                                isImmersiveInterfaceEnabled = isImmersiveInterfaceEnabled,
                                scrollBehavior = scrollBehavior,
                                windowInsetsController = windowInsetsController,
                                actions = {
                                    CustomSpinner(
                                        isSpinnerShownState = isStateSpinnerShownState,
                                        spinnerButtonText = state.title,
                                        options = ModMailState.entries,
                                        optionName = { state ->
                                            state.title
                                        }
                                    ) { state ->
                                        conversationViewModel.switchState(state)
                                    }

                                    Box {
                                        IconButton(onClick = {
                                            isToolbarMenuOpen = true
                                        }) {
                                            ToolbarIcon(
                                                drawableId = R.drawable.ic_more_vert_grey_24dp,
                                                contentDescription = stringResource(R.string.content_description_toolbar_menu_icon)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = isToolbarMenuOpen,
                                            containerColor = Color(LocalAppTheme.current.backgroundColor),
                                            onDismissRequest = { isToolbarMenuOpen = false }
                                        ) {
                                            if (getNavigator(selectedTabIndex).scaffoldValue.get(ListDetailPaneScaffoldRole.List) != PaneAdaptedValue.Hidden) {
                                                DropdownMenuItem(
                                                    text = {
                                                        PrimaryText(
                                                            R.string.action_refresh_mod_mail_conversations
                                                        )
                                                    },
                                                    onClick = {
                                                        isToolbarMenuOpen = false
                                                        refreshConversationsTriggers[selectedTabIndex].value = !refreshConversationsTriggers[selectedTabIndex].value
                                                    }
                                                )
                                            }

                                            DropdownMenuItem(
                                                text = {
                                                    PrimaryText(
                                                        R.string.action_select_subreddits
                                                    )
                                                },
                                                onClick = {
                                                    isToolbarMenuOpen = false
                                                    showSelectSubredditsSheet = true
                                                }
                                            )

                                            if (getNavigator(selectedTabIndex).currentDestination?.contentKey != null) {
                                                DropdownMenuItem(
                                                    text = {
                                                        PrimaryText(
                                                            R.string.action_refresh_mod_mail_messages_in_conversation
                                                        )
                                                    },
                                                    onClick = {
                                                        isToolbarMenuOpen = false
                                                        refreshConversationMessagesTriggers[selectedTabIndex].value = !refreshConversationMessagesTriggers[selectedTabIndex].value
                                                    }
                                                )

                                                DropdownMenuItem(
                                                    text = {
                                                        PrimaryText(
                                                            R.string.action_user_mod_action
                                                        )
                                                    },
                                                    onClick = {
                                                        isToolbarMenuOpen = false
                                                        showUserActions[selectedTabIndex].value = true
                                                    }
                                                )

                                                DropdownMenuItem(
                                                    text = {
                                                        PrimaryText(
                                                            R.string.action_view_user_info
                                                        )
                                                    },
                                                    onClick = {
                                                        isToolbarMenuOpen = false
                                                        showUserInfoStates[selectedTabIndex].value = true
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            ) {
                                triggerBackPress()
                            }
                        },
                        containerColor = Color(mCustomThemeWrapper.backgroundColor),
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(
                                    top = innerPadding.calculateTopPadding()
                                )
                                .fillMaxSize()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(mCustomThemeWrapper.tabLayoutWithCollapsedCollapsingToolbarTabBackground))
                                    .padding(
                                        PaddingValues(
                                            start = innerPadding.calculateStartPadding(
                                                LocalLayoutDirection.current
                                            ),
                                            end = innerPadding.calculateEndPadding(
                                                LocalLayoutDirection.current
                                            )
                                        )
                                    ),
                            ) {
                                val tabTitles = listOf(getString(R.string.mod_mail_unread), getString(R.string.mod_mail_recent),
                                    getString(R.string.mod_mail_mod), getString(R.string.mod_mail_user))
                                PrimaryScrollableTabRow(
                                    selectedTabIndex = selectedTabIndex,
                                    containerColor = Color(mCustomThemeWrapper.tabLayoutWithCollapsedCollapsingToolbarTabBackground),
                                    modifier = Modifier.fillMaxWidth(),
                                    edgePadding = 0.dp,
                                    indicator = {
                                        TabRowDefaults.PrimaryIndicator(
                                            modifier = Modifier
                                                .tabIndicatorOffset(
                                                    selectedTabIndex,
                                                    matchContentSize = true
                                                )
                                                .background(Color(mCustomThemeWrapper.tabLayoutWithCollapsedCollapsingToolbarTabBackground)),
                                            width = Dp.Unspecified,
                                            color = Color(mCustomThemeWrapper.tabLayoutWithCollapsedCollapsingToolbarTabIndicator),
                                        )
                                    },
                                    divider = { }
                                ) {
                                    tabTitles.forEachIndexed { index, title ->
                                        Tab(
                                            selected = selectedTabIndex == index,
                                            onClick = {
                                                scope.launch {
                                                    pagerState.animateScrollToPage(index)
                                                }
                                            },
                                            selectedContentColor = Color(mCustomThemeWrapper.tabLayoutWithCollapsedCollapsingToolbarTabIndicator),
                                            unselectedContentColor = Color(mCustomThemeWrapper.tabLayoutWithCollapsedCollapsingToolbarTabIndicator),
                                            text = {
                                                Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        )
                                    }
                                }
                            }

                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(
                                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
                                )
                            ) { page ->
                                ConversationListDetailPaneView(
                                    page,
                                    refreshConversationsTriggers[page].value,
                                    refreshConversationMessagesTriggers[page],
                                    showUserActions[page],
                                    showUserInfoStates[page],
                                    getNavigator(page)
                                )
                            }
                        }

                        if (showSelectSubredditsSheet) {
                            val moderatedSubreddits by conversationViewModel.moderatedSubreddits.collectAsStateWithLifecycle()
                            val unselectedSubreddits by conversationViewModel.unselectedSubreddits.collectAsStateWithLifecycle()
                            val allSubredditsSelected by conversationViewModel.allSubredditsSelected.collectAsStateWithLifecycle()
                            val isLoadingModeratedSubreddits by conversationViewModel.isLoadingModeratedSubreddits.collectAsStateWithLifecycle()

                            LaunchedEffect(Unit) {
                                if (moderatedSubreddits == null) {
                                    conversationViewModel.loadModeratedSubreddits()
                                }
                            }

                            fun toggleSubreddit(subredditId: String) {
                                conversationViewModel.toggleSubreddit(subredditId)
                            }

                            CustomModalBottomSheet(onDismissRequest = { showSelectSubredditsSheet = false }) {
                                LazyColumn(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            PrimaryText(
                                                stringResource(R.string.select_subreddits),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 16.dp),
                                                fontSize = LocalTypography.current.fontSize.size18,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Box(
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CustomCircularWavyProgressIndicator(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .alpha(if (isLoadingModeratedSubreddits && moderatedSubreddits != null) 1f else 0f)
                                                )

                                                CustomPositiveTextButton(
                                                    stringResId = R.string.refresh,
                                                    modifier = Modifier.alpha(if (!isLoadingModeratedSubreddits && moderatedSubreddits != null) 1f else 0f)
                                                ) {
                                                    conversationViewModel.loadModeratedSubreddits()
                                                }
                                            }
                                        }

                                        SwitchRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            checked = allSubredditsSelected,
                                            title = stringResource(R.string.all_subreddits)
                                        ) {
                                            if (!allSubredditsSelected) {
                                                conversationViewModel.clearUnSelectedSubreddits()
                                            }
                                            conversationViewModel.setAllSubredditsSelected(!allSubredditsSelected)
                                        }
                                    }

                                    moderatedSubreddits?.let {
                                        items(
                                            count = it.size,
                                            contentType = { _ -> 1 },
                                            key = { index -> index }
                                        ) { index: Int ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        if (!allSubredditsSelected) {
                                                            toggleSubreddit(it[index].id)
                                                        }
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                GlideImage(
                                                    modifier = Modifier
                                                        .size(36.dp),
                                                    model = it[index].iconUrl,
                                                    failure = placeholder(R.drawable.subreddit_default_icon),
                                                    contentDescription = stringResource(R.string.content_description_subreddit_icon)
                                                ) {
                                                    it.apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(72, 0)))
                                                }

                                                PrimaryText(
                                                    it[index].name,
                                                    modifier = Modifier.padding(horizontal = 16.dp)
                                                )

                                                Spacer(modifier = Modifier.weight(1f))

                                                CustomSwitch(
                                                    checked = !unselectedSubreddits.contains(it[index].id),
                                                    enabled = !allSubredditsSelected,
                                                    onCheckedChange = { _ ->
                                                        toggleSubreddit(it[index].id)
                                                    }
                                                )
                                            }
                                        }
                                    } ?: run {
                                        if (isLoadingModeratedSubreddits) {
                                            item {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    CustomLoadingIndicator()
                                                }
                                            }
                                        }
                                    }
                                }

                                CustomFilledButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    stringResId = R.string.apply
                                ) {
                                    showSelectSubredditsSheet = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun getRepository(page: Int): ModMailConversationRepository {
        return when(page) {
            0 -> conversationViewModel.unreadModMailConversationRepository
            1 -> conversationViewModel.recentModMailConversationRepository
            2 -> conversationViewModel.modModMailConversationRepository
            3 -> conversationViewModel.userModMailConversationRepository
            else -> conversationViewModel.unreadModMailConversationRepository
        }
    }

    @Composable
    fun ConversationListDetailPaneView(
        page: Int,
        refreshConversationsTrigger: Boolean,
        refreshConversationMessagesTrigger: MutableState<Boolean>,
        showUserActions: MutableState<Boolean>,
        showUserInfo: MutableState<Boolean>,
        navigator: ThreePaneScaffoldNavigator<Conversation>
    ) {
        val listState: LazyListState = rememberLazyListState()
        var isInitial by remember { mutableStateOf(true) }

        val conversations by getRepository(page).conversations.collectAsStateWithLifecycle()
        val loadState by getRepository(page).loadState.collectAsStateWithLifecycle()
        val isRefreshingConversations by getRepository(page).isRefreshingConversations.collectAsStateWithLifecycle()
        val modMailUser by getRepository(page).modMailUser.collectAsStateWithLifecycle()

        val needPagination by remember {
            derivedStateOf {
                conversations?.let {
                    (it.isNotEmpty() && listState.firstVisibleItemIndex >= it.size - 5) || !listState.canScrollForward
                } ?: false
            }
        }

        var conversationForOptionsSheet by remember { mutableStateOf<Conversation?>(null) }
        var showBanBottomSheet by remember { mutableStateOf(false) }
        var showMuteBottomSheet by remember { mutableStateOf(false) }

        val markwon = remember {
            val miscPlugin: MarkwonPlugin = object : AbstractMarkwonPlugin() {
                override fun beforeSetText(textView: TextView, markdown: Spanned) {
                    if (this@ModMailActivity.contentTypeface != null) {
                        textView.setTypeface(this@ModMailActivity.contentTypeface)
                    }
                    textView.setTextColor(mCustomThemeWrapper.primaryTextColor)
                    textView.highlightColor = android.graphics.Color.TRANSPARENT
                }

                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder.linkResolver { view: View?, link: String? ->
                        val intent = Intent(this@ModMailActivity, LinkResolverActivity::class.java)
                        val uri = Uri.parse(link)
                        intent.setData(uri)
                        startActivity(intent)
                    }
                }

                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder.linkColor(mCustomThemeWrapper.linkColor)
                }
            }

            MarkdownUtils.createDescriptionMarkwon(
                this@ModMailActivity,
                miscPlugin
            ) { textView, url ->
                val urlMenuBottomSheetFragment = UrlMenuBottomSheetFragment()
                val bundle = Bundle()
                bundle.putString(UrlMenuBottomSheetFragment.EXTRA_URL, url)
                urlMenuBottomSheetFragment.setArguments(bundle)
                urlMenuBottomSheetFragment.show(
                    supportFragmentManager,
                    urlMenuBottomSheetFragment.tag
                )
                return@createDescriptionMarkwon true
            }
        }

        val scope = rememberCoroutineScope()

        LaunchedEffect(needPagination) {
            if (needPagination) {
                conversationViewModel.loadNextPage(getRepository(page))
            }
        }

        LaunchedEffect(refreshConversationsTrigger) {
            if (isInitial) {
                isInitial = false
                return@LaunchedEffect
            }

            conversationViewModel.refreshConversations(getRepository(page))
        }

        BackHandler(navigator.canNavigateBack()) {
            scope.launch {
                navigator.navigateBack()
            }
        }

        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    ConversationListView(
                        conversations,
                        loadState,
                        isRefreshingConversations,
                        navigator,
                        listState,
                        onRefresh = {
                            conversationViewModel.refreshConversations(getRepository(page))
                        },
                        onRetryPagination = {
                            conversationViewModel.loadNextPage(getRepository(page))
                        },
                        onConversationLongClick = { conversation ->
                            conversationForOptionsSheet = conversation
                        }
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    navigator.currentDestination?.contentKey?.let {
                        ConversationDetailsView(
                            page,
                            it,
                            markwon,
                            refreshConversationMessagesTrigger.value
                        )
                    }
                }
            }
        )

        if (showBanBottomSheet) {
            navigator.currentDestination?.contentKey?.let { conversation ->
                modMailUser?.let { modMailUser ->
                    conversation.owner?.displayName?.let { subreddit ->
                        var reason by remember { mutableStateOf("") }
                        var isPermanent by remember { mutableStateOf(false) }
                        var duration by remember { mutableStateOf("") }
                        var message by remember { mutableStateOf("") }
                        var modNote by remember { mutableStateOf("") }
                        var isBanningUser by remember { mutableStateOf(false) }

                        CustomModalBottomSheet(onDismissRequest = { showBanBottomSheet = false }) {
                            Column {
                                PrimaryText(
                                    stringResource(R.string.ban_user, "u/${modMailUser.name}"),
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = LocalTypography.current.fontSize.size18,
                                    fontWeight = FontWeight.Bold
                                )

                                CustomTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    value = reason,
                                    placeholder = stringResource(R.string.ban_reason_hint),
                                    maxLines = 3,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    reason = it
                                }

                                AnimatedVisibility(!isPermanent) {
                                    CustomTextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        value = duration,
                                        placeholder = stringResource(R.string.ban_duration_hint),
                                        maxLines = 1,
                                        shape = RoundedCornerShape(16.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    ) {
                                        if (it.isDigitsOnly()) {
                                            duration = it
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .clickable {
                                            isPermanent = !isPermanent
                                        }
                                        .padding(start = 20.dp, end = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PrimaryText(
                                        stringResource(R.string.perma_ban),
                                        modifier = Modifier.padding(end = 16.dp)
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    Checkbox(
                                        checked = isPermanent,
                                        onCheckedChange = { isPermanent = it }
                                    )
                                }

                                CustomTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    value = message,
                                    placeholder = stringResource(R.string.message),
                                    minLines = 3,
                                    maxLines = 5,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    message = it
                                }

                                CustomTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp)
                                        .padding(horizontal = 16.dp),
                                    value = modNote,
                                    placeholder = stringResource(R.string.mod_note_hint),
                                    maxLines = 3,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    modNote = it
                                }

                                SecondaryText(
                                    stringResource(R.string.mod_note_description),
                                    modifier = Modifier
                                        .padding(bottom = 16.dp)
                                        .padding(horizontal = 20.dp)
                                )
                            }

                            FilledLoadingButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .padding(horizontal = 16.dp),
                                stringResId = R.string.ban,
                                isLoading = isBanningUser
                            ) {
                                if (isBanningUser) {
                                    return@FilledLoadingButton
                                }

                                isBanningUser = true

                                scope.launch {
                                    val isPermanentTemp = isPermanent
                                    val durationInt = duration.toInt()
                                    val reasonTemp = reason

                                    try {
                                        conversationViewModel.executeUserAction(
                                            conversation,
                                            ModUserAction.TEMPBAN(
                                                subreddit,
                                                modMailUser.name,
                                                if (isPermanentTemp) 0 else durationInt,
                                                message,
                                                reasonTemp,
                                                modNote
                                            ),
                                            getRepository(page)
                                        )

                                        modMailUser.ban(durationInt, reasonTemp, isPermanentTemp)

                                        Toast.makeText(
                                            this@ModMailActivity,
                                            if (isPermanentTemp) getString(R.string.banned_permanently) else getString(R.string.banned_with_duration, durationInt),
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        showBanBottomSheet = false
                                    } catch (e: Exception) {
                                        if (e is CancellationException) {
                                            throw e
                                        }

                                        e.printStackTrace()

                                        Toast.makeText(this@ModMailActivity, e.message?.let { message ->
                                            getString(R.string.ban_failed_with_reason, message)
                                        } ?: getString(R.string.ban_failed), Toast.LENGTH_SHORT).show()
                                    }

                                    isBanningUser = false
                                }
                            }
                        }
                    }
                }
            }
        } else if (showMuteBottomSheet) {
            navigator.currentDestination?.contentKey?.let { conversation ->
                modMailUser?.let { modMailUser ->
                    conversation.owner?.displayName?.let { subreddit ->
                        var duration by remember { mutableStateOf(getString(R.string.mute_duration_3_days)) }
                        var durationHour by remember { mutableIntStateOf(72) }
                        var isMutingUser by remember { mutableStateOf(false) }
                        val muteDurations = stringArrayResource(R.array.mute_user_durations)
                        val isDurationSpinnerShown = remember { mutableStateOf(false) }

                        CustomModalBottomSheet(onDismissRequest = { showMuteBottomSheet = false }) {
                            Column {
                                PrimaryText(
                                    stringResource(R.string.mute_user, "u/${modMailUser.name}"),
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = LocalTypography.current.fontSize.size18,
                                    fontWeight = FontWeight.Bold
                                )

                                CustomSpinner(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp),
                                    isSpinnerShownState = isDurationSpinnerShown,
                                    options = muteDurations,
                                    spinnerButtonText = duration,
                                    optionName = {
                                        it
                                    }
                                ) {
                                    duration = it
                                    durationHour = when (it) {
                                        getString(R.string.mute_duration_3_days) -> 72
                                        getString(R.string.mute_duration_7_days) -> 168
                                        getString(R.string.mute_duration_28_days) -> 672
                                        else -> 72
                                    }
                                }
                            }

                            FilledLoadingButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                stringResId = R.string.mute,
                                isLoading = isMutingUser
                            ) {
                                if (isMutingUser) {
                                    return@FilledLoadingButton
                                }

                                isMutingUser = true

                                scope.launch {
                                    try {
                                        if (!conversationViewModel.executeUserAction(
                                                conversation,
                                                ModUserAction.MUTE(
                                                    durationHour
                                                ),
                                                getRepository(page)
                                            )
                                        ) {
                                            modMailUser.mute(durationHour)
                                        }

                                        Toast.makeText(
                                            this@ModMailActivity,
                                            getString(R.string.muted_with_duration, durationHour / 24),
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        showMuteBottomSheet = false
                                    } catch (e: Exception) {
                                        if (e is CancellationException) {
                                            throw e
                                        }

                                        e.printStackTrace()

                                        Toast.makeText(this@ModMailActivity, e.message?.let { message ->
                                            getString(R.string.mute_failed_with_reason, message)
                                        } ?: getString(R.string.mute_failed), Toast.LENGTH_SHORT).show()
                                    }

                                    isMutingUser = false
                                }
                            }
                        }
                    }
                }
            }
        } else if (showUserActions.value) {
            navigator.currentDestination?.contentKey?.let { conversation ->
                modMailUser?.let {
                    CustomModalBottomSheet(onDismissRequest = { showUserActions.value = false }) {
                        UserText(
                            "u/${it.name}",
                            modifier = Modifier.padding(16.dp),
                            fontSize = LocalTypography.current.fontSize.size18,
                            fontWeight = FontWeight.Bold
                        )

                        IconTextRowButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(if (it.isBanned()) R.string.unban else R.string.ban),
                            drawableResource = if (it.isBanned()) R.drawable.ic_unlock_24dp else R.drawable.ic_ban_rounded_24dp
                        ) {
                            if (!it.isBanned()) {
                                showUserActions.value = false
                                showBanBottomSheet = true
                                return@IconTextRowButton
                            }

                            scope.launch {
                                try {
                                    if (!conversationViewModel.executeUserAction(
                                        conversation,
                                        ModUserAction.UNBAN,
                                        getRepository(page)
                                    )) {
                                        it.unban()
                                    }

                                    Toast.makeText(
                                        this@ModMailActivity,
                                        getString(R.string.unbanned),
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    refreshConversationMessagesTrigger.value = !refreshConversationMessagesTrigger.value
                                } catch (e: Exception) {
                                    if (e is CancellationException) {
                                        throw e
                                    }

                                    e.printStackTrace()

                                    Toast.makeText(this@ModMailActivity, e.message?.let { message ->
                                        getString(R.string.unban_failed_with_reason, message)
                                    } ?: getString(R.string.unban_failed), Toast.LENGTH_SHORT).show()
                                }
                            }

                            showUserActions.value = false
                        }

                        IconTextRowButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(if (it.isMuted()) R.string.unmute else R.string.mute),
                            drawableResource = if (it.isMuted()) R.drawable.ic_unmute_24dp else R.drawable.ic_mute_24dp
                        ) {
                            if (!it.isMuted()) {
                                showUserActions.value = false
                                showMuteBottomSheet = true
                                return@IconTextRowButton
                            }

                            scope.launch {
                                try {
                                    if (!conversationViewModel.executeUserAction(
                                        conversation,
                                        ModUserAction.UNMUTE,
                                        getRepository(page)
                                    )) {
                                        it.unmute()
                                    }

                                    Toast.makeText(
                                        this@ModMailActivity,
                                        getString(R.string.unmuted),
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    refreshConversationMessagesTrigger.value = !refreshConversationMessagesTrigger.value
                                } catch (e: Exception) {
                                    if (e is CancellationException) {
                                        throw e
                                    }

                                    e.printStackTrace()

                                    Toast.makeText(this@ModMailActivity, e.message?.let { message ->
                                        getString(R.string.unmute_failed_with_reason, message)
                                    } ?: getString(R.string.unmute_failed), Toast.LENGTH_SHORT).show()
                                }
                            }

                            showUserActions.value = false
                        }

                        IconTextRowButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(if (it.isApproved()) R.string.remove_from_approved_users else R.string.approve),
                            drawableResource = if (it.isApproved()) R.drawable.ic_unapprove_rounded_24dp else R.drawable.ic_approve_rounded_24dp
                        ) {
                            scope.launch {
                                try {
                                    if (!conversationViewModel.executeUserAction(
                                        conversation,
                                        if (it.isApproved()) ModUserAction.DISAPPROVE else ModUserAction.APPROVE,
                                        getRepository(page)
                                    )) {
                                        it.setApproved(!it.isApproved())

                                        Toast.makeText(
                                            this@ModMailActivity,
                                            getString(if (it.isApproved()) R.string.approved else R.string.disapproved),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        // Still the old modMailUser, no new approval info.
                                        Toast.makeText(
                                            this@ModMailActivity,
                                            getString(if (it.isApproved()) R.string.disapproved else R.string.approved),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    refreshConversationMessagesTrigger.value = !refreshConversationMessagesTrigger.value
                                } catch (e: Exception) {
                                    if (e is CancellationException) {
                                        throw e
                                    }

                                    e.printStackTrace()

                                    Toast.makeText(this@ModMailActivity, e.message?.let { message ->
                                        getString(if (it.isApproved()) R.string.disapprove_failed_with_reason else R.string.approve_failed_with_reason, message)
                                    } ?: getString(if (it.isApproved()) R.string.disapprove_failed else R.string.approve_failed), Toast.LENGTH_SHORT).show()
                                }
                            }

                            showUserActions.value = false
                        }
                    }
                }
            }
        } else if (showUserInfo.value) {
            navigator.currentDestination?.contentKey?.let { conversation ->
                modMailUser?.let { modMailUser ->
                    CustomModalBottomSheet(onDismissRequest = { showUserInfo.value = false }) {
                        val userDataFlow = remember(conversationViewModel, modMailUser.name) {
                            conversationViewModel.getUserDataFlow(modMailUser.name)
                        }

                        val userData by userDataFlow.collectAsStateWithLifecycle(null)

                        val recentConvos = modMailUser.recentConvos.entries.toList().sortedByDescending { it.value.dateUTC }
                        val recentComments = modMailUser.recentComments.entries.toList().sortedByDescending { it.value.dateUTC }

                        LaunchedEffect(Unit) {
                            conversationViewModel.loadUserData(modMailUser.name)
                        }

                        LazyColumn {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    userData?.let { userData ->
                                        GlideImage(
                                            modifier = Modifier
                                                .size(72.dp),
                                            model = userData.iconUrl,
                                            failure = placeholder(R.drawable.subreddit_default_icon),
                                            contentDescription = stringResource(R.string.content_description_user_avatar)
                                        ) {
                                            it.apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(144, 0)))
                                        }

                                        UserText(
                                            "u/${modMailUser.name}",
                                            modifier = Modifier.padding(16.dp),
                                            fontSize = LocalTypography.current.fontSize.size18,
                                            fontWeight = FontWeight.Bold
                                        )

                                        CustomFilledButton(
                                            stringResId = R.string.view_profile
                                        ) {
                                            startActivity(Intent(this@ModMailActivity, ViewUserDetailActivity::class.java).apply {
                                                putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, modMailUser.name)
                                            })
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Column {
                                                PrimaryText(
                                                    R.string.post_karma,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                PrimaryText(
                                                    userData.linkKarma.toString()
                                                )
                                            }

                                            Spacer(modifier = Modifier.weight(1f))

                                            Column {
                                                PrimaryText(
                                                    R.string.comment_karma,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                PrimaryText(
                                                    userData.commentKarma.toString()
                                                )
                                            }
                                        }
                                    } ?: run {
                                        ShimmerSurface {
                                            ShimmerContainer(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth(),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(CircleShape)
                                                            .size(72.dp)
                                                            .fillMaxWidth()
                                                            .height(16.dp)
                                                            .background(Color.Gray)
                                                    )
                                                }
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                UserText(
                                                    "u/${modMailUser.name}",
                                                    modifier = Modifier.padding(16.dp),
                                                    fontSize = LocalTypography.current.fontSize.size18,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                CustomFilledButton(
                                                    stringResId = R.string.view_profile
                                                ) {
                                                    startActivity(Intent(this@ModMailActivity, ViewUserDetailActivity::class.java).apply {
                                                        putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, modMailUser.name)
                                                    })
                                                }
                                            }

                                            ShimmerContainer(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(16.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .weight(0.25f)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(16.dp))
                                                                .fillMaxWidth()
                                                                .height(18.dp)
                                                                .background(Color.Gray)
                                                        )

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(16.dp))
                                                                .fillMaxWidth()
                                                                .height(18.dp)
                                                                .background(Color.Gray)
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.weight(0.5f))

                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxHeight()
                                                            .weight(0.25f)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .fillMaxWidth()
                                                                .height(18.dp)
                                                                .background(Color.Gray)
                                                        )

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .fillMaxWidth()
                                                                .height(18.dp)
                                                                .background(Color.Gray)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        conversation.owner?.let { owner ->
                                            owner.displayName?.let { subreddit ->
                                                if (owner.type.equals("subreddit")) {
                                                    PrimaryText(
                                                        stringResource(R.string.subreddit_activity, subreddit),
                                                        modifier = Modifier
                                                            .padding(bottom = 8.dp),
                                                        fontSize = LocalTypography.current.fontSize.size20
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.padding(top = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            PrimaryIcon(
                                                modifier = Modifier.size(24.dp),
                                                drawableId = if (modMailUser.isApproved()) R.drawable.ic_approved_user_rounded_24dp else R.drawable.ic_disapproved_user_rounded_24dp,
                                                contentDescription = stringResource(R.string.content_description_approval_info_icon)
                                            )

                                            Spacer(modifier = Modifier.width(16.dp))

                                            PrimaryText(
                                                if (modMailUser.isApproved()) R.string.approved_user else R.string.not_approved_user
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.padding(top = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            PrimaryIcon(
                                                modifier = Modifier.size(24.dp),
                                                drawableId = if (modMailUser.isBanned()) R.drawable.ic_ban_rounded_24dp else R.drawable.ic_check_circle_day_night_24dp,
                                                contentDescription = stringResource(R.string.content_description_ban_info_icon)
                                            )

                                            Spacer(modifier = Modifier.width(16.dp))

                                            if (modMailUser.isBanned()) {
                                                val bannedUntil = remember { getFormattedTime(modMailUser.banStatus.endDateUTC, mTimeFormatPattern) }

                                                Column {
                                                    PrimaryText(
                                                        R.string.banned_user
                                                    )

                                                    PrimaryText(
                                                        if (modMailUser.banStatus.isPermanent) {
                                                            stringResource(R.string.permanent)
                                                        } else {
                                                            stringResource(R.string.banned_until, bannedUntil)
                                                        }
                                                    )

                                                    PrimaryText(
                                                        stringResource(R.string.ban_reason, modMailUser.banStatus.reason)
                                                    )
                                                }
                                            } else {
                                                PrimaryText(
                                                    R.string.not_banned_user
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.padding(top = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            PrimaryIcon(
                                                modifier = Modifier.size(24.dp),
                                                drawableId = if (modMailUser.isMuted()) R.drawable.ic_mute_24dp else R.drawable.ic_unmute_24dp,
                                                contentDescription = stringResource(R.string.content_description_mute_info_icon)
                                            )

                                            Spacer(modifier = Modifier.width(16.dp))

                                            Column {
                                                if (modMailUser.isMuted()) {
                                                    PrimaryText(
                                                        R.string.muted_user
                                                    )

                                                    modMailUser.muteStatus.endDate?.let {
                                                        val mutedUntil = remember { getFormattedTime(modMailUser.muteStatus.endDateUTC, mTimeFormatPattern) }

                                                        PrimaryText(
                                                            stringResource(R.string.muted_until, mutedUntil)
                                                        )
                                                    }
                                                } else {
                                                    PrimaryText(
                                                        R.string.not_muted_user
                                                    )
                                                }

                                                PrimaryText(
                                                    stringResource(R.string.mute_count_info, modMailUser.muteStatus.muteCount)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (recentConvos.isNotEmpty()) {
                                item {
                                    PrimaryText(
                                        R.string.recent_conversations,
                                        modifier = Modifier
                                            .padding(top = 16.dp, bottom = 4.dp)
                                            .padding(horizontal = 16.dp),
                                        fontSize = LocalTypography.current.fontSize.size20
                                    )
                                }

                                items(
                                    count = recentConvos.size,
                                    contentType = { _ -> 1 },
                                    key = { index -> recentConvos[index].value.id }
                                ) { index: Int ->
                                    RecentConvoView(recentConvos[index].value) {

                                    }
                                }
                            }

                            if (recentComments.isNotEmpty()) {
                                item {
                                    PrimaryText(
                                        R.string.recent_comments,
                                        modifier = Modifier
                                            .padding(top = 16.dp, bottom = 4.dp)
                                            .padding(horizontal = 16.dp),
                                        fontSize = LocalTypography.current.fontSize.size20
                                    )
                                }

                                items(
                                    count = recentComments.size,
                                    contentType = { _ -> 1 },
                                    key = { index -> recentComments[index].key }
                                ) { index: Int ->
                                    RecentCommentView(recentComments[index].value, markwon) {
                                        startActivity(
                                            Intent(this@ModMailActivity, LinkResolverActivity::class.java).apply {
                                                setData(recentComments[index].value.permalink.toUri())
                                            }
                                        )
                                    }
                                }
                            }

                            if (modMailUser.recentConvos.isNotEmpty() || modMailUser.recentComments.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else if (conversationForOptionsSheet != null) {
            CustomModalBottomSheet(onDismissRequest = { conversationForOptionsSheet = null }) {
                conversationForOptionsSheet?.let {
                    it.subject?.let { subject ->
                        PrimaryText(
                            subject,
                            modifier = Modifier.padding(16.dp),
                            fontSize = LocalTypography.current.fontSize.size18,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconTextRowButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(if (it.isArchived) R.string.unarchive else R.string.archive),
                        drawableResource = if (it.isArchived) R.drawable.ic_unarchive_rounded_24dp else R.drawable.ic_archive_rounded_24dp
                    ) {
                        scope.launch {
                            try {
                                conversationViewModel.executeModConversationAction(
                                    it,
                                    if (it.isArchived) ModConversationAction.UNARCHIVE else ModConversationAction.ARCHIVE,
                                    getRepository(page)
                                )

                                Toast.makeText(
                                    this@ModMailActivity,
                                    getString(if (it.isArchived) R.string.unarchived else R.string.archived),
                                    Toast.LENGTH_SHORT
                                ).show()

                                it.isArchived = !it.isArchived
                            } catch (e: Exception) {
                                if (e is CancellationException) {
                                    throw e
                                }

                                e.printStackTrace()

                                Toast.makeText(this@ModMailActivity, e.message?.let { message ->
                                    getString(if (it.isArchived) R.string.unarchive_failed_with_reason else R.string.archive_failed_with_reason, message)
                                } ?: getString(if (it.isArchived) R.string.unarchive_failed else R.string.archive_failed), Toast.LENGTH_SHORT).show()
                            }
                        }

                        conversationForOptionsSheet = null
                    }

                    IconTextRowButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(if (it.isHighlighted == true) R.string.unhighlight else R.string.highlight),
                        drawableResource = if (it.isHighlighted == true) R.drawable.ic_unhighlight_rounded_24dp else R.drawable.ic_highlight_rounded_24dp
                    ) {
                        scope.launch {
                            try {
                                conversationViewModel.executeModConversationAction(
                                    it,
                                    if (it.isHighlighted == true) ModConversationAction.UNHIGHLIGHT else ModConversationAction.HIGHLIGHT,
                                    getRepository(page)
                                )

                                Toast.makeText(
                                    this@ModMailActivity,
                                    getString(if (it.isHighlighted == true) R.string.unhighlighted else R.string.highlighted),
                                    Toast.LENGTH_SHORT
                                ).show()

                                it.isHighlighted = it.isHighlighted != true && it.isHighlighted == false
                            } catch (e: Exception) {
                                if (e is CancellationException) {
                                    throw e
                                }

                                e.printStackTrace()

                                Toast.makeText(this@ModMailActivity, e.message?.let { message ->
                                    getString(if (it.isHighlighted == true) R.string.unhighlight_failed_with_reason else R.string.highlight_failed_with_reason, message)
                                } ?: getString(if (it.isHighlighted == true) R.string.unhighlight_failed else R.string.highlight_failed), Toast.LENGTH_SHORT).show()
                            }
                        }

                        conversationForOptionsSheet = null
                    }

                    IconTextRowButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(if (it.isRead) R.string.unread else R.string.read),
                        drawableResource = if (it.isRead) R.drawable.ic_unread_rounded_24dp else R.drawable.ic_read_rounded_24dp
                    ) {
                        scope.launch {
                            try {
                                conversationViewModel.executeModConversationAction(
                                    it,
                                    if (it.isRead) ModConversationAction.UNREAD else ModConversationAction.READ,
                                    getRepository(page)
                                )

                                Toast.makeText(
                                    this@ModMailActivity,
                                    getString(if (it.isRead) R.string.unread else R.string.read),
                                    Toast.LENGTH_SHORT
                                ).show()

                                it.isRead = !it.isRead
                            } catch (e: Exception) {
                                if (e is CancellationException) {
                                    throw e
                                }

                                e.printStackTrace()

                                Toast.makeText(this@ModMailActivity, e.message?.let { message ->
                                    getString(if (it.isRead) R.string.unread_failed_with_reason else R.string.read_failed_with_reason, message)
                                } ?: getString(if (it.isRead) R.string.unread_failed else R.string.read_failed), Toast.LENGTH_SHORT).show()
                            }
                        }

                        conversationForOptionsSheet = null
                    }
                }
            }
        }
    }

    @Composable
    fun ConversationListView(
        conversations: List<Conversation>?,
        loadState: DataLoadState<Unit>,
        isManualRefreshing: Boolean,
        navigator: ThreePaneScaffoldNavigator<Conversation>,
        listState: LazyListState,
        onRefresh: () -> Unit,
        onRetryPagination: () -> Unit,
        onConversationLongClick: (Conversation) -> Unit
    ) {
        val state: PullToRefreshState = rememberPullToRefreshState()
        val scope = rememberCoroutineScope()

        PullToRefreshBox(
            isRefreshing = (loadState == DataLoadState.Loading && conversations == null) || isManualRefreshing,
            onRefresh = onRefresh,
            state = state,
            indicator = {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = (loadState == DataLoadState.Loading && conversations == null) || isManualRefreshing,
                    state = state,
                    containerColor = Color(LocalAppTheme.current.circularProgressBarBackground),
                    color = Color(LocalAppTheme.current.colorAccent),
                    elevation = 4.dp
                )
            }
        ) {
            conversations?.let {
                if (it.isNotEmpty()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
                        modifier = Modifier
                            .clipToBounds()
                            .fillMaxHeight()
                    ) {
                        items(
                            count = it.size,
                            contentType = { _ -> 1 },
                            key = { index -> it[index].id ?: index }
                        ) { index: Int ->
                            ConversationView(
                                it[index],
                                onItemClick = { conversation ->
                                    scope.launch {
                                        navigator.navigateTo(
                                            ListDetailPaneScaffoldRole.Detail,
                                            conversation
                                        )
                                    }
                                },
                                onItemLongClick = { conversation ->
                                    onConversationLongClick(conversation)
                                }
                            )
                        }

                        if (loadState is DataLoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CustomLoadingIndicator(
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        } else if (loadState is DataLoadState.Error) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SecondaryText(
                                        stringResource(R.string.could_not_load_mod_mail_with_reason, loadState.message),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(16.dp)
                                    )

                                    CustomFilledButton(
                                        modifier = Modifier.padding(16.dp),
                                        stringResId = R.string.retry
                                    ) {
                                        onRetryPagination()
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: run {
                if (loadState is DataLoadState.Error) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )

                        GlideImage(
                            model = R.drawable.error_image,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(top = 32.dp)
                                .padding(horizontal = 32.dp)
                        )

                        SecondaryText(
                            stringResource(R.string.could_not_load_mod_mail_with_reason, loadState.message),
                            modifier = Modifier
                                .padding(vertical = 16.dp, horizontal = 32.dp)
                        )

                        CustomFilledButton(
                            modifier = Modifier
                                .padding(bottom = 32.dp)
                                .padding(horizontal = 32.dp),
                            stringResId = R.string.retry
                        ) {
                            onRefresh()
                        }

                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                } else if (loadState is DataLoadState.Success) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )

                        GlideImage(
                            model = R.drawable.error_image,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(top = 32.dp)
                                .padding(horizontal = 32.dp)
                        )

                        SecondaryText(
                            stringResource(R.string.no_mod_mail),
                            modifier = Modifier
                                .padding(vertical = 16.dp, horizontal = 32.dp)
                        )

                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ConversationView(
        conversation: Conversation,
        onItemClick: (Conversation) -> Unit,
        onItemLongClick: (Conversation) -> Unit
    ) {
        val haptics = LocalHapticFeedback.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick(conversation) }
                .combinedClickable(
                    onClick = { onItemClick(conversation) },
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onItemLongClick(conversation)
                    }
                )
                .padding(top = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                conversation.owner?.displayName?.let {
                    SubredditText(
                        it,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                startActivity(
                                    Intent(
                                        this@ModMailActivity,
                                        ViewSubredditDetailActivity::class.java
                                    ).apply {
                                        putExtra(
                                            ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                            it
                                        )
                                    }
                                )
                            }
                            .padding(horizontal = 16.dp)
                    )
                } ?: run {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (conversation.lastUpdatedUTC > 0) {
                    SecondaryText(
                        Utils.getElapsedTime(this@ModMailActivity, conversation.lastUpdatedUTC),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.End
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (author in conversation.authors) {
                    author.name?.let {
                        UserText(
                            it,
                            modifier = Modifier.clickable {
                                if (it != "[deleted]") {
                                    startActivity(
                                        Intent(
                                            this@ModMailActivity,
                                            ViewUserDetailActivity::class.java
                                        ).apply {
                                            putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, it)
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            conversation.subject?.let {
                PrimaryText(
                    it,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = LocalTypography.current.fontSize.size16
                )
            }

            CustomHorizontalDivider(
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

    @Composable
    fun ConversationDetailsView(
        page: Int,
        conversation: Conversation,
        markwon: Markwon,
        refreshConversationMessagesTrigger: Boolean
    ) {
        val pullToRefreshState: PullToRefreshState = rememberPullToRefreshState()
        var refreshState by remember { mutableStateOf<DataLoadState<Unit>>(DataLoadState.Idle) }
        var newMessage by remember {
            mutableStateOf("")
        }
        var roleMenuExpanded by remember { mutableStateOf(false) }
        // Reply as subreddit if true
        var isAuthorHidden by remember { mutableStateOf(true) }
        // Reply as mods only if true
        var isInternal by remember { mutableStateOf(false) }
        var isSendingMessage by remember { mutableStateOf(false) }
        val lazyColumnScrollState = rememberLazyListState()

        var isInitial by remember { mutableStateOf(true) }

        val scope = rememberCoroutineScope()

        suspend fun refresh() {
            if (refreshState != DataLoadState.Loading) {
                refreshState = DataLoadState.Loading

                try {
                    conversationViewModel.refreshMessagesInConversation(conversation, getRepository(page))
                    refreshState = DataLoadState.Success(Unit)
                } catch (e: Exception) {
                    refreshState = DataLoadState.Error(e.message ?: getString(R.string.refresh_conversation_failed))
                }
            }
        }

        fun sendMessage() {
            if (!isSendingMessage) {
                isSendingMessage = true

                scope.launch {
                    try {
                        conversationViewModel.sendMessage(
                            conversation,
                            newMessage,
                            isAuthorHidden,
                            isInternal,
                            getRepository(page)
                        )
                    } catch (e: Exception) {
                        if (e !is CancellationException) {
                            Toast.makeText(this@ModMailActivity, e.message?.let {
                                getString(R.string.could_not_send_message_with_reason, it)
                            } ?: getString(R.string.could_not_send_message), Toast.LENGTH_SHORT).show()

                            e.printStackTrace()
                        } else {
                            throw e
                        }
                    }

                    newMessage = ""

                    isSendingMessage = false

                    refresh()
                }
            }
        }

        if (!conversation.isUpdated) {
            LaunchedEffect(conversation.id) {
                refresh()
            }
        }

        LaunchedEffect(conversation.modMailItems.size) {
            lazyColumnScrollState.animateScrollToItem(conversation.modMailItems.size - 1)
        }

        LaunchedEffect(refreshConversationMessagesTrigger) {
            if (isInitial) {
                isInitial = false
                return@LaunchedEffect
            }

            refresh()
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            PullToRefreshBox(
                modifier = Modifier.weight(1f),
                isRefreshing = refreshState == DataLoadState.Loading,
                onRefresh = {
                    scope.launch {
                        refresh()
                    }
                },
                state = pullToRefreshState,
                indicator = {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = refreshState == DataLoadState.Loading,
                        state = pullToRefreshState,
                        containerColor = Color(LocalAppTheme.current.circularProgressBarBackground),
                        color = Color(LocalAppTheme.current.colorAccent),
                        elevation = 4.dp
                    )
                }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clipToBounds(),
                    state = lazyColumnScrollState
                ) {
                    items(count = conversation.modMailItems.size, key = { index -> conversation.modMailItems[index].id ?: index }) { index: Int ->
                        when (conversation.modMailItems[index]) {
                            is ModMessage -> {
                                MessageView(
                                    conversation,
                                    conversation.modMailItems[index] as ModMessage,
                                    markwon
                                )
                            }
                            is ModAction -> {
                                ModActionView(
                                    conversation,
                                    conversation.modMailItems[index] as ModAction
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                CustomTextButton(
                    text = if (isInternal) {
                        stringResource(R.string.mods_only)
                    } else if (isAuthorHidden) {
                        conversation.owner?.displayName?.let {
                            stringResource(R.string.mod_mail_reply_as, "r/$it")
                        } ?: stringResource(R.string.mod_mail_reply_as_subreddit)
                    } else {
                        stringResource(
                            R.string.mod_mail_reply_as,
                            "u/$accountName"
                        )
                    },
                    textColor = Color(if (isInternal) {
                        LocalAppTheme.current.primaryTextColor
                    } else if (isAuthorHidden) {
                        LocalAppTheme.current.subreddit
                    } else {
                        LocalAppTheme.current.username
                    }),
                ) {
                    roleMenuExpanded = true
                }

                DropdownMenu(
                    expanded = roleMenuExpanded,
                    containerColor = Color(LocalAppTheme.current.backgroundColor),
                    onDismissRequest = { roleMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            SubredditText(
                                conversation.owner?.displayName?.let {
                                    stringResource(R.string.mod_mail_reply_as, "r/$it")
                                } ?: stringResource(R.string.mod_mail_reply_as_subreddit)
                            )
                        },
                        onClick = {
                            isAuthorHidden = true
                            isInternal = false

                            roleMenuExpanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            UserText(
                                stringResource(R.string.mod_mail_reply_as, "u/$accountName")
                            )
                        },
                        onClick = {
                            isAuthorHidden = false
                            isInternal = false

                            roleMenuExpanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            PrimaryText(
                                R.string.mods_only
                            )
                        },
                        onClick = {
                            isAuthorHidden = false
                            isInternal = true

                            roleMenuExpanded = false
                        }
                    )
                }
            }

            CustomTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 16.dp),
                value = newMessage,
                placeholder = stringResource(R.string.new_mod_mail_message_hint),
                maxLines = 3,
                trailingIcon = {
                    if (isSendingMessage) {
                        CustomCircularWavyProgressIndicator(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(28.dp)
                        )
                    } else {
                        SendMessageIconButton(
                            modifier = Modifier
                                .padding(end = 8.dp)
                        ) {
                            sendMessage()
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                if (!isSendingMessage) {
                    newMessage = it
                }
            }
        }
    }

    @Composable
    fun MessageView(conversation: Conversation, message: ModMessage, markwon: Markwon) {
        val markwonAdapter = remember {
            MarkdownUtils.createCustomTablesAdapter(this@ModMailActivity)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                message.author?.name?.let {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        UserText(
                            it,
                            modifier = Modifier
                                .clickable {
                                    if (it != "[deleted]") {
                                        startActivity(
                                            Intent(
                                                this@ModMailActivity,
                                                ViewUserDetailActivity::class.java
                                            ).apply {
                                                putExtra(
                                                    ViewUserDetailActivity.EXTRA_USER_NAME_KEY,
                                                    it
                                                )
                                            }
                                        )
                                    }
                                }
                                .padding(horizontal = 16.dp)
                        )

                        if (message.author?.isHidden == true) {
                            SubredditText(
                                "As r/${conversation.owner?.displayName}",
                                modifier = Modifier
                                    .clickable {
                                        startActivity(
                                            Intent(
                                                this@ModMailActivity,
                                                ViewSubredditDetailActivity::class.java
                                            ).apply {
                                                putExtra(
                                                    ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                                    conversation.owner?.displayName
                                                )
                                            }
                                        )
                                    }
                                    .padding(horizontal = 16.dp),
                                fontSize = LocalTypography.current.fontSize.size12
                            )
                        }
                    }
                } ?: run {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (message.isInternal == true) {
                    PrimaryText(
                        R.string.mods_only,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                if (message.dateUTC > 0) {
                    SecondaryText(
                        Utils.getElapsedTime(this@ModMailActivity, message.dateUTC),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.End
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            message.bodyMarkdown?.let {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    factory = { context ->
                        markwonAdapter.setMarkdown(markwon, it)
                        // Creates view
                        RecyclerView(context).apply {
                            isNestedScrollingEnabled = false
                            layoutManager = LinearLayoutManagerBugFixed(context)
                            adapter = markwonAdapter
                        }
                    },
                    update = { view ->
                        // View's been inflated or state read in this block has been updated
                        // Add logic here if necessary

                        // As selectedItem is read here, AndroidView will recompose
                        // whenever the state changes
                        // Example of Compose -> View communication
                    }
                )
            }

            CustomHorizontalDivider(modifier = Modifier.padding(top = 12.dp))
        }
    }

    @Composable
    fun ModActionView(conversation: Conversation, modAction: ModAction) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                modAction.author?.name?.let {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        UserText(
                            it,
                            modifier = Modifier
                                .clickable {
                                    if (it != "[deleted]") {
                                        startActivity(
                                            Intent(
                                                this@ModMailActivity,
                                                ViewUserDetailActivity::class.java
                                            ).apply {
                                                putExtra(
                                                    ViewUserDetailActivity.EXTRA_USER_NAME_KEY,
                                                    it
                                                )
                                            }
                                        )
                                    }
                                }
                                .padding(horizontal = 16.dp)
                        )

                        if (modAction.author.isHidden == true) {
                            SubredditText(
                                "As r/${conversation.owner?.displayName}",
                                modifier = Modifier
                                    .clickable {
                                        startActivity(
                                            Intent(
                                                this@ModMailActivity,
                                                ViewSubredditDetailActivity::class.java
                                            ).apply {
                                                putExtra(
                                                    ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                                    conversation.owner?.displayName
                                                )
                                            }
                                        )
                                    }
                                    .padding(horizontal = 16.dp),
                                fontSize = LocalTypography.current.fontSize.size12
                            )
                        }
                    }
                } ?: run {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (modAction.dateUTC > 0) {
                    SecondaryText(
                        Utils.getElapsedTime(this@ModMailActivity, modAction.dateUTC),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.End
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            PrimaryText(
                stringResource(when (modAction.actionType) {
                    ModActionType.HIGHLIGHT -> R.string.mod_action_highlighted_by
                    ModActionType.UN_HIGHLIGHT -> R.string.mod_action_unhighlighted_by
                    ModActionType.ARCHIVE -> R.string.mod_action_archived_by
                    ModActionType.UN_ARCHIVE -> R.string.mod_action_unarchived_by
                    ModActionType.REPORTED_TO_ADMINS -> R.string.mod_action_reported_to_admins_by
                    ModActionType.MUTE -> R.string.mod_action_muted_by
                    ModActionType.UN_MUTE -> R.string.mod_action_unmuted_by
                    ModActionType.BANNED -> R.string.mod_action_banned_by
                    ModActionType.UNBANNED -> R.string.mod_action_unbanned_by
                    ModActionType.APPROVED -> R.string.mod_action_approved_by
                    ModActionType.DISAPPROVED -> R.string.mod_action_disapproved_by
                    ModActionType.FILTERED -> R.string.mod_action_filtered_by
                    ModActionType.UNFILTERED -> R.string.mod_action_unfiltered_by
                    ModActionType.UNKNOWN -> R.string.mod_action_unknown_action_by
                }, modAction.author?.name?.let {"u/$it"} ?: stringResource(R.string.mod_action_unknown_user)),
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold
            )

            CustomHorizontalDivider(modifier = Modifier.padding(top = 12.dp))
        }
    }

    @Composable
    fun RecentConvoView(
        recentConvo: RecentConvo,
        onClick: () -> Unit
    ) {
        val dateElapsedString = remember { Utils.getElapsedTime(this@ModMailActivity, recentConvo.dateUTC) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(vertical = 4.dp)
                .background(
                    Color(LocalAppTheme.current.filledCardViewBackgroundColor),
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    onClick()
                }
                .padding(16.dp),
        ) {
            SecondaryText(
                stringResource(R.string.last_updated_with_time, dateElapsedString),
                modifier = Modifier
                    .padding(bottom = 4.dp),
                fontSize = LocalTypography.current.fontSize.size12
            )

            PrimaryText(
                recentConvo.subject,
                fontWeight = FontWeight.Bold
            )
        }
    }

    @Composable
    fun RecentCommentView(
        recentComment: ModMailRecentComment,
        markwon: Markwon,
        onClick: () -> Unit
    ) {
        val dateElapsedString = remember { Utils.getElapsedTime(this@ModMailActivity, recentComment.dateUTC) }
        val markwonAdapter = remember {
            MarkdownUtils.createCustomTablesAdapter(this@ModMailActivity)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(LocalAppTheme.current.filledCardViewBackgroundColor),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .padding(vertical = 16.dp),
            ) {
                SecondaryText(
                    dateElapsedString,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .padding(horizontal = 16.dp),
                    fontSize = LocalTypography.current.fontSize.size12
                )

                PrimaryText(
                    recentComment.title,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold
                )

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    factory = { context ->
                        markwonAdapter.setMarkdown(markwon, recentComment.comment)
                        // Creates view
                        RecyclerView(context).apply {
                            isNestedScrollingEnabled = false
                            layoutManager = LinearLayoutManagerBugFixed(context)
                            adapter = markwonAdapter
                        }
                    },
                    update = { view ->
                        // View's been inflated or state read in this block has been updated
                        // Add logic here if necessary

                        // As selectedItem is read here, AndroidView will recompose
                        // whenever the state changes
                        // Example of Compose -> View communication
                    }
                )
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        onClick()
                    }
            )
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