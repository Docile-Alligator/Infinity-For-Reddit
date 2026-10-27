package ml.docilealligator.infinityforreddit.activities

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spanned
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularWavyProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.gson.Gson
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.core.MarkwonTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed
import ml.docilealligator.infinityforreddit.customviews.compose.AppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.CustomFilledButton
import ml.docilealligator.infinityforreddit.customviews.compose.CustomHorizontalDivider
import ml.docilealligator.infinityforreddit.customviews.compose.CustomLoadingIndicator
import ml.docilealligator.infinityforreddit.customviews.compose.CustomTextButton
import ml.docilealligator.infinityforreddit.customviews.compose.CustomTextField
import ml.docilealligator.infinityforreddit.customviews.compose.LocalAppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.LocalTypography
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryText
import ml.docilealligator.infinityforreddit.customviews.compose.SecondaryText
import ml.docilealligator.infinityforreddit.customviews.compose.SendMessageIconButton
import ml.docilealligator.infinityforreddit.customviews.compose.SubredditText
import ml.docilealligator.infinityforreddit.customviews.compose.ThemedTopAppBar
import ml.docilealligator.infinityforreddit.customviews.compose.ToolbarIcon
import ml.docilealligator.infinityforreddit.customviews.compose.UserText
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils
import ml.docilealligator.infinityforreddit.moderation.modmail.Conversation
import ml.docilealligator.infinityforreddit.moderation.modmail.ModMessage
import ml.docilealligator.infinityforreddit.repositories.ModMailConversationRepository
import ml.docilealligator.infinityforreddit.state.DataLoadState
import ml.docilealligator.infinityforreddit.utils.Utils
import ml.docilealligator.infinityforreddit.viewmodels.ModMailConversationViewModel
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
                var isToolbarMenuOpen by remember { mutableStateOf(false) }
                val navigators = listOf(
                    rememberListDetailPaneScaffoldNavigator<Conversation>(),
                    rememberListDetailPaneScaffoldNavigator<Conversation>(),
                    rememberListDetailPaneScaffoldNavigator<Conversation>(),
                    rememberListDetailPaneScaffoldNavigator<Conversation>(),
                )

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
                                        if (navigators[pagerState.currentPage].scaffoldValue[ListDetailPaneScaffoldRole.List] != PaneAdaptedValue.Hidden) {
                                            DropdownMenuItem(
                                                text = {
                                                    PrimaryText(
                                                        R.string.action_refresh_mod_mail_conversations
                                                    )
                                                },
                                                onClick = {
                                                    isToolbarMenuOpen = false
                                                }
                                            )
                                        }

                                        if (navigators[pagerState.currentPage].currentDestination?.contentKey != null) {
                                            DropdownMenuItem(
                                                text = {
                                                    PrimaryText(
                                                        R.string.action_refresh_mod_mail_messages_in_conversation
                                                    )
                                                },
                                                onClick = {
                                                    isToolbarMenuOpen = false
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
                                        end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
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
                                        text = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
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
                            ConversationListDetailPaneView(page, navigators[page])
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
            else -> conversationViewModel.recentModMailConversationRepository
        }
    }

    @Composable
    fun ConversationListDetailPaneView(page: Int, navigator: ThreePaneScaffoldNavigator<Conversation>) {
        val listState: LazyListState = rememberLazyListState()


        val conversations by getRepository(page).conversations.collectAsStateWithLifecycle()
        val loadState by getRepository(page).loadState.collectAsStateWithLifecycle()
        val isRefreshingConversations by getRepository(page).isRefreshingConversations.collectAsStateWithLifecycle()

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
                        isRefreshingConversations,
                        navigator,
                        listState,
                        onRefresh = {
                            conversationViewModel.refreshConversations(
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
                        ConversationDetailsView(page, it, updateConversation)
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
                }
            }
        }
    }

    @Composable
    fun ConversationView(conversation: Conversation, onItemClick: (Conversation) -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick(conversation) }
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
                                if (it != "[deleted") {
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
    fun ConversationDetailsView(page: Int, conversation: Conversation, updateConversation: (Conversation, Conversation) -> Unit) {
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

        LaunchedEffect(conversation.messages.size) {
            lazyColumnScrollState.animateScrollToItem(conversation.messages.size - 1)
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
                    items(count = conversation.messages.size, key = { index -> conversation.messages[index].id ?: index }) { index: Int ->
                        MessageView(conversation, conversation.messages[index], markwon)
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
                        CircularWavyProgressIndicator(
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