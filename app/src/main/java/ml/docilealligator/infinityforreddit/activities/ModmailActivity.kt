package ml.docilealligator.infinityforreddit.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.gson.Gson
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.mod.Conversation
import ml.docilealligator.infinityforreddit.mod.ModMailConversationViewModel
import ml.docilealligator.infinityforreddit.mod.ModMessage
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
class ModmailActivity : BaseActivity() {
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

        enableEdgeToEdge()

        conversationViewModel = ViewModelProvider.create(this, ModMailConversationViewModel.Factory(mOauthRetrofit, accessToken!!, mSharedPreferences))[ModMailConversationViewModel::class]

        setContent {
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = topAppBarColors(
                            containerColor = Color(mCustomThemeWrapper.colorPrimary),
                            titleContentColor = Color(mCustomThemeWrapper.toolbarPrimaryTextAndIconColor)
                        ),
                        title = {
                            Text(getString(R.string.modmail_activity_label))
                        }
                    )
                },
                containerColor = Color(mCustomThemeWrapper.backgroundColor),
            ) { innerPadding ->
                val listState: LazyListState = rememberLazyListState()
                val navigator = rememberListDetailPaneScaffoldNavigator<Conversation>()
                val pagingItems = conversationViewModel.flow.collectAsLazyPagingItems()

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
                    navigator.navigateBack()
                }

                ListDetailPaneScaffold(
                    modifier = Modifier.padding(
                        top = innerPadding.calculateTopPadding(),
                        start = 16.dp,
                        end = 16.dp
                    ),
                    directive = navigator.scaffoldDirective,
                    value = navigator.scaffoldValue,
                    listPane = {
                        AnimatedPane {
                            ConversationListView(pagingItems, navigator, listState)
                        }
                    },
                    detailPane = {
                        AnimatedPane {
                            navigator.currentDestination?.content?.let {
                                ConversationDetailsView(it, updateConversation)
                            }
                        }
                    }
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

    @Composable
    fun ConversationListView(pagingItems: LazyPagingItems<Conversation>, navigator: ThreePaneScaffoldNavigator<Conversation>,
                             listState: LazyListState) {
        val state: PullToRefreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            isRefreshing = pagingItems.loadState.refresh == LoadState.Loading,
            onRefresh = { pagingItems.refresh() },
            state = state,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = pagingItems.loadState.refresh == LoadState.Loading,
                    state = state,
                    containerColor = Color(mCustomThemeWrapper.circularProgressBarBackground),
                    color = Color(mCustomThemeWrapper.colorAccent)
                )
            }
        ) {
            if (pagingItems.itemCount > 0) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    state = listState,
                    contentPadding = WindowInsets(top = 16.dp).add(WindowInsets.navigationBars).asPaddingValues(),
                    modifier = Modifier.clipToBounds()
                ) {
                    items(
                        count = pagingItems.itemCount,
                        contentType = { _ -> 1 },
                        key = { index -> pagingItems[index]?.id ?: index }
                    ) { index: Int ->
                        pagingItems[index]?.let {
                            ConversationView(it) { conversation ->
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, conversation)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ConversationView(conversation: Conversation, onItemClick: (Conversation) -> Unit) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick(conversation) }
        ) {
            conversation.owner?.displayName?.let {
                Text(text = it, color = Color(mCustomThemeWrapper.subreddit))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (author in conversation.authors) {
                    author.name?.let {
                        Text(text = it, color = Color(mCustomThemeWrapper.username))
                    }
                }
            }
            conversation.subject?.let {
                Text(text = it)
            }
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
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = refreshState.value,
                    state = pullToRefreshState,
                    containerColor = Color(mCustomThemeWrapper.circularProgressBarBackground),
                    color = Color(mCustomThemeWrapper.colorAccent)
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
