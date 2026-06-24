package ml.docilealligator.infinityforreddit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customviews.compose.AppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.LocalAppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.LocalTypography
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryText
import ml.docilealligator.infinityforreddit.customviews.compose.SecondaryText
import ml.docilealligator.infinityforreddit.customviews.compose.ThemedTopAppBar
import ml.docilealligator.infinityforreddit.font.FontStyle
import ml.docilealligator.infinityforreddit.reminder.Reminder
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils
import ml.docilealligator.infinityforreddit.utils.Utils
import ml.docilealligator.infinityforreddit.viewmodels.RemindersViewModel
import ml.docilealligator.infinityforreddit.viewmodels.RemindersViewModel.Companion.provideFactory
import retrofit2.Retrofit
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

class ReminderListingActivity : BaseActivity() {
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
    @Inject
    lateinit var mExecutor: Executor

    lateinit var mViewModel: RemindersViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        ((application) as Infinity).appComponent.inject(this)

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isImmersiveInterfaceRespectForcedEdgeToEdge()) {
                enableEdgeToEdge()
            }
        }

        mViewModel = ViewModelProvider.create(
            this,
            provideFactory(mRetrofit, mOauthRetrofit, mRedditDataRoomDatabase, mCurrentAccountSharedPreferences)
        )[RemindersViewModel::class.java]

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = customThemeWrapper.isLightStatusBar

        setContent {
            AppTheme(customThemeWrapper.themeType, mSharedPreferences) {
                val context = LocalContext.current
                val scrollBehavior = enterAlwaysScrollBehavior()
                val reminders by mViewModel.reminders.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    /*mRedditDataRoomDatabase.reminderDao().insert(
                        Reminder(
                            accountName, "post id", "comment id", "content", System.currentTimeMillis(),
                            System.currentTimeMillis() + Utils.DAY_MILLIS
                        )
                    )*/

                    mViewModel.initializeReminders()
                }

                Scaffold(
                    topBar = {
                        ThemedTopAppBar(
                            titleStringResId = R.string.reminders,
                            isImmersiveInterfaceEnabled = isImmersiveInterfaceEnabled,
                            scrollBehavior = scrollBehavior,
                            windowInsetsController = windowInsetsController
                        ) {
                            finish()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .imePadding(),
                    contentWindowInsets = if (isImmersiveInterfaceEnabled) WindowInsets.safeDrawing else WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                ) { innerPadding ->
                    reminders?.let {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(LocalAppTheme.current.backgroundColor)),
                            contentPadding = innerPadding
                        ) {
                            item {
                                Spacer(Modifier.height(16.dp))
                            }
                            items(it) { reminder ->
                                if (reminder.commentId.isEmpty()) {
                                    PostReminder(
                                        Modifier
                                            .padding(horizontal = 16.dp)
                                            .padding(bottom = 16.dp),
                                        reminder
                                    ) {
                                        startActivity(
                                            Intent(context, ViewPostDetailActivity::class.java).apply {
                                                putExtra(ViewPostDetailActivity.EXTRA_POST_ID, reminder.postId)
                                            }
                                        )
                                    }
                                } else {
                                    CommentReminder(
                                        Modifier
                                            .padding(horizontal = 16.dp)
                                            .padding(bottom = 16.dp),
                                        reminder
                                    ) {
                                        startActivity(
                                            Intent(context, ViewPostDetailActivity::class.java).apply {
                                                putExtra(ViewPostDetailActivity.EXTRA_POST_ID, reminder.postId)
                                                putExtra(ViewPostDetailActivity.EXTRA_SINGLE_COMMENT_ID, reminder.commentId)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PostReminder(modifier: Modifier, reminder: Reminder, onClick: () -> Unit) {
        val context = LocalContext.current
        val remainingText by remember {
            mutableStateOf(getRemainingTimeText(context, reminder.reminderTime))
        }

        Column(
            modifier = modifier
                .fillMaxSize(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    onClick()
                }
                .background(Color(LocalAppTheme.current.filledCardViewBackgroundColor))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(1f)
                    .padding(bottom = 8.dp)
            ) {
                PrimaryText(R.string.post)

                Spacer(modifier = Modifier.weight(1f))

                PrimaryText(remainingText)
            }

            SecondaryText(reminder.content, fontSize = LocalTypography.current.titleFontSize.default)
        }
    }

    @Composable
    private fun CommentReminder(modifier: Modifier, reminder: Reminder, onClick: () -> Unit) {
        val context = LocalContext.current
        val remainingText by remember {
            mutableStateOf(getRemainingTimeText(context, reminder.reminderTime))
        }

        Column(
            modifier = modifier
                .fillMaxSize(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    onClick()
                }
                .background(Color(LocalAppTheme.current.filledCardViewBackgroundColor))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(1f)
                    .padding(bottom = 8.dp)
            ) {
                PrimaryText(R.string.comment)

                Spacer(modifier = Modifier.weight(1f))

                PrimaryText(remainingText)
            }

            SecondaryText(reminder.content, fontSize = LocalTypography.current.titleFontSize.default)
        }
    }

    fun getRemainingTimeText(context: Context, time: Long): String {
        val diff = System.currentTimeMillis() - time

        if (diff <= 0) return "Expired"

        val minutes = diff / Utils.MINUTE_MILLIS
        val hours = diff / Utils.HOUR_MILLIS
        val days = diff / Utils.DAY_MILLIS
        val months = diff / Utils.MONTH_MILLIS
        val years = diff / Utils.YEAR_MILLIS

        return when {
            years > 1 -> context.getString(R.string.remaining_time_in_years, years)
            years == 1L -> context.getString(R.string.remaining_time_1_year)
            months > 1 -> context.getString(R.string.remaining_time_in_months, months)
            months == 1L -> context.getString(R.string.remaining_time_1_month)
            days > 1 -> context.getString(R.string.remaining_time_in_days, days)
            days == 1L -> context.getString(R.string.remaining_time_1_day)
            hours > 1 -> context.getString(R.string.remaining_time_in_hours, hours)
            hours == 1L -> context.getString(R.string.remaining_time_1_hour)
            minutes > 1 -> context.getString(R.string.remaining_time_in_minutes, minutes)
            minutes == 1L -> context.getString(R.string.remaining_time_1_minute)
            else -> context.getString(R.string.remaining_time_less_than_1_minute)
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