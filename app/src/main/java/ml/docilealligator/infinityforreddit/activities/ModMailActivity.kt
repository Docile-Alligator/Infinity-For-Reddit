package ml.docilealligator.infinityforreddit.activities

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.window.core.layout.WindowHeightSizeClass
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.gson.Gson
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.state.DataLoadState
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customviews.compose.AppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.CustomFilledButton
import ml.docilealligator.infinityforreddit.customviews.compose.CustomLoadingIndicator
import ml.docilealligator.infinityforreddit.customviews.compose.LocalAppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.LocalTypography
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryText
import ml.docilealligator.infinityforreddit.customviews.compose.SecondaryText
import ml.docilealligator.infinityforreddit.customviews.compose.SubredditText
import ml.docilealligator.infinityforreddit.customviews.compose.ThemedTopAppBar
import ml.docilealligator.infinityforreddit.customviews.compose.UserText
import ml.docilealligator.infinityforreddit.moderation.modmail.Conversation
import ml.docilealligator.infinityforreddit.viewmodels.ModMailConversationViewModel
import ml.docilealligator.infinityforreddit.moderation.modmail.ModMessage
import ml.docilealligator.infinityforreddit.repositories.ModMailConversationRepository
import ml.docilealligator.infinityforreddit.utils.deriveContrastingColor
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalGlideComposeApi::class
)
class ModMailActivity : BaseActivity() {
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

        conversationViewModel = ViewModelProvider.create(this, ModMailConversationViewModel.Factory(mOauthRetrofit, accessToken!!, mSharedPreferences))[ModMailConversationViewModel::class]

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = customThemeWrapper.isLightStatusBar

        setContent {
            AppTheme(customThemeWrapper.themeType, mSharedPreferences) {
                val scrollBehavior = enterAlwaysScrollBehavior()

                val scope = rememberCoroutineScope()
                val pagerState = rememberPagerState { 4 }
                val selectedTabIndex by remember { derivedStateOf { pagerState.currentPage } }

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
                                /*IconButton(onClick = {
                                    if (multiRedditState is DataLoadState.Success) {
                                        copyMultiRedditActivityViewModel.copyMultiRedditInfo()
                                    }
                                }) {
                                    ToolbarIcon(
                                        drawableId = R.drawable.ic_check_circle_toolbar_24dp,
                                        contentDescription = stringResource(R.string.action_copy_multi_reddit)
                                    )
                                }*/
                            }
                        ) {
                            finish()
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
                                        text = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                    )
                                }
                            }
                        }

                        HorizontalPager(
                            state = pagerState
                        ) { page ->
                            ConversationListDetailPaneView(page)
                        }
                    }
                }
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

    private suspend fun refreshConversation(conversation: Conversation, refreshState: MutableState<Boolean>,
                                            updateConversation: (Conversation, Conversation) -> Unit) {
        refreshState.value = true

        conversation.id?.let { id ->
            val updatedConversation = Conversation.fetchConversation(
                mOauthRetrofit, accessToken!!, id, Gson()
            )

            updatedConversation?.let {
                updateConversation(conversation, it)
            } ?: Toast.makeText(this, R.string.refresh_conversation_failed, Toast.LENGTH_SHORT).show()
        }

        refreshState.value = false
    }

    fun getRepository(page: Int): ModMailConversationRepository {
        return when(page) {
            0 -> conversationViewModel.unreadModMailConversationRepository
            1 -> conversationViewModel.recentModMailConversationRepository
            2 -> conversationViewModel.modModMailConversationRepository
            3 -> conversationViewModel.userModMailConversationRepository
            else -> conversationViewModel.recentModMailConversationRepository
        }
    }

    @Composable
    fun ConversationListDetailPaneView(page: Int) {
        val listState: LazyListState = rememberLazyListState()
        val navigator = rememberListDetailPaneScaffoldNavigator<Conversation>()

        val conversations by getRepository(page).conversations.collectAsStateWithLifecycle()
        val loadState by getRepository(page).loadState.collectAsStateWithLifecycle()
        val isManualRefreshing by getRepository(page).isRefreshing.collectAsStateWithLifecycle()

        val needPagination by remember {
            derivedStateOf {
                conversations?.let {
                    (it.isNotEmpty() && listState.firstVisibleItemIndex >= it.size - 5) || !listState.canScrollForward
                } ?: false
            }
        }

        val scope = rememberCoroutineScope()

        LaunchedEffect(needPagination) {
            if (needPagination) {
                conversationViewModel.loadNextPage(getRepository(page))
            }
        }

        val updateConversation: (Conversation, Conversation) -> Unit = { conversation, updatedConversation ->
            conversation.apply {
                if (updatedConversation.id == id) {
                    isAuto = updatedConversation.isAuto
                    participant = updatedConversation.participant
                    objIds = updatedConversation.objIds
                    isRepliable = updatedConversation.isRepliable
                    lastUserUpdate = updatedConversation.lastUserUpdate
                    isInternal = updatedConversation.isInternal
                    lastModUpdate = updatedConversation.lastModUpdate
                    authors = updatedConversation.authors
                    lastUpdated = updatedConversation.lastUpdated
                    legacyFirstMessageId = updatedConversation.legacyFirstMessageId
                    this.state = updatedConversation.state
                    conversationType = updatedConversation.conversationType
                    lastUnread = updatedConversation.lastUnread
                    owner = updatedConversation.owner
                    subject = updatedConversation.subject
                    isHighlighted = updatedConversation.isHighlighted
                    numMessages = updatedConversation.numMessages
                    messages = updatedConversation.messages
                    isUpdated = true
                }
            }
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
                        isManualRefreshing,
                        navigator,
                        listState,
                        onRefresh = {
                            conversationViewModel.refresh(
                                when (page) {
                                    0 -> conversationViewModel.unreadModMailConversationRepository
                                    1 -> conversationViewModel.recentModMailConversationRepository
                                    2 -> conversationViewModel.modModMailConversationRepository
                                    3 -> conversationViewModel.userModMailConversationRepository
                                    else -> conversationViewModel.recentModMailConversationRepository
                                }
                            )
                        },
                        onRetryPagination = {
                            conversationViewModel.loadNextPage(getRepository(page))
                        }
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    navigator.currentDestination?.contentKey?.let {
                        ConversationDetailsView(it, updateConversation)
                    }
                }
            }
        )
    }

    @Composable
    fun ConversationListView(
        conversations: List<Conversation>?,
        loadState: DataLoadState<Unit>,
        isManualRefreshing: Boolean,
        navigator: ThreePaneScaffoldNavigator<Conversation>,
        listState: LazyListState,
        onRefresh: () -> Unit,
        onRetryPagination: () -> Unit
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
                if (conversations.isNotEmpty()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
                        modifier = Modifier.clipToBounds()
                    ) {
                        items(
                            count = conversations.size,
                            contentType = { _ -> 1 },
                            key = { index -> conversations[index].id ?: index }
                        ) { index: Int ->
                            ConversationView(conversations[index]) { conversation ->
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, conversation)
                                }
                            }
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
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
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
                }
            }
        }
    }

    @Composable
    fun ConversationView(conversation: Conversation, onItemClick: (Conversation) -> Unit) {
        Column(
            //verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick(conversation) }
                .padding(top = 12.dp)
        ) {
            conversation.owner?.displayName?.let {
                SubredditText(
                    it,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        //.padding(bottom = 4.dp)
                )
            }
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (author in conversation.authors) {
                    author.name?.let {
                        UserText(it)
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

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp),
                color = Color(LocalAppTheme.current.dividerColor)
            )
        }
    }

    @Composable
    fun ConversationDetailsView(conversation: Conversation, updateConversation: (Conversation, Conversation) -> Unit) {
        val pullToRefreshState: PullToRefreshState = rememberPullToRefreshState()
        val refreshState = remember { mutableStateOf(false) }

        if (!conversation.isUpdated) {
            LaunchedEffect(conversation.id) {
                refreshConversation(conversation, refreshState, updateConversation)
            }
        }

        PullToRefreshBox(
            isRefreshing = refreshState.value,
            onRefresh = {
                lifecycleScope.launch {
                    refreshConversation(conversation, refreshState, updateConversation)
                }
            },
            state = pullToRefreshState,
            indicator = {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = refreshState.value,
                    state = pullToRefreshState,
                    containerColor = Color(LocalAppTheme.current.circularProgressBarBackground),
                    color = Color(LocalAppTheme.current.colorAccent),
                    elevation = 4.dp
                )
            }
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = WindowInsets(top = 16.dp).add(WindowInsets.navigationBars).asPaddingValues(),
                modifier = Modifier.clipToBounds()
            ) {
                items(count = conversation.messages.size, key = { index -> conversation.messages[index].id ?: index }) { index: Int ->
                    MessageView(conversation.messages[index])
                }
            }
        }
    }

    @Composable
    fun MessageView(message: ModMessage) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                message.author?.name?.let {
                    Text(text = it)
                }
                message.date?.let {
                    Text(text = it)
                }
            }

            message.bodyMarkdown?.let {
                Text(text = it)
            }
        }
    }
}