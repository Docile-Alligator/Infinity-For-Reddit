package ml.docilealligator.infinityforreddit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ml.docilealligator.infinityforreddit.AppResult
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customviews.compose.AppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.CustomFilledButton
import ml.docilealligator.infinityforreddit.customviews.compose.CustomNeutralTextButton
import ml.docilealligator.infinityforreddit.customviews.compose.CustomPositiveTextButton
import ml.docilealligator.infinityforreddit.customviews.compose.LocalAppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.LocalTypography
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryText
import ml.docilealligator.infinityforreddit.customviews.compose.SecondaryText
import ml.docilealligator.infinityforreddit.customviews.compose.ThemedTopAppBar
import ml.docilealligator.infinityforreddit.post.Post
import ml.docilealligator.infinityforreddit.reminder.ReminderManager
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils
import ml.docilealligator.infinityforreddit.viewmodels.SetReminderViewModel
import ml.docilealligator.infinityforreddit.viewmodels.SetReminderViewModel.Companion.provideFactory
import retrofit2.Retrofit
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class SetReminderActivity: BaseActivity() {
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
    lateinit var mReminderManager: ReminderManager

    lateinit var mViewModel: SetReminderViewModel

    companion object {
        private val EXTRA_POST = "EP"
        private val EXTRA_POST_ID = "EPI"
        private val EXTRA_COMMENT = "EC"

        fun startReminderActivity(context: Context, post: Post, comment: Comment?) {
            context.startActivity(Intent(context, SetReminderActivity::class.java).apply {
                putExtra(EXTRA_POST, post)
                putExtra(EXTRA_COMMENT, comment)
            })
        }

        fun startReminderActivity(context: Context, postId: String, comment: Comment) {
            context.startActivity(Intent(context, SetReminderActivity::class.java).apply {
                putExtra(EXTRA_POST_ID, postId)
                putExtra(EXTRA_COMMENT, comment)
            })
        }
    }

    private class ReminderPredefinedTime(
        val textOnButton: String,
        val timeInMillis: Long
    )

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
            provideFactory(
                accountName,
                intent.getParcelableExtra(EXTRA_POST),
                intent.getStringExtra(EXTRA_POST_ID),
                intent.getParcelableExtra(EXTRA_COMMENT),
                mReminderManager
            )
        )[SetReminderViewModel::class.java]

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = customThemeWrapper.isLightStatusBar

        val calendar = Calendar.getInstance()
        val formatter = DateTimeFormatter.ofPattern(
            mSharedPreferences.getString(
                SharedPreferencesUtils.TIME_FORMAT_KEY,
                SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE
            ), Locale.getDefault()
        )

        val reminderPresetTimes = listOf(
            ReminderPredefinedTime(
                getString(R.string.in_6_hours),
                6 * 60 * 60 * 1000
            ),
            ReminderPredefinedTime(
                getString(R.string.in_1_day),
                24 * 60 * 60 * 1000
            ),
            ReminderPredefinedTime(
                getString(R.string.in_2_days),
                48 * 60 * 60 * 1000
            ),
            ReminderPredefinedTime(
                getString(R.string.in_3_days),
                72 * 60 * 60 * 1000
            ),
            ReminderPredefinedTime(
                getString(R.string.in_1_week),
                7 * 24 * 60 * 60 * 1000
            ),
            ReminderPredefinedTime(
                getString(R.string.in_2_weeks),
                14 * 24 * 60 * 60 * 1000
            )
        )

        setContent {
            AppTheme(customThemeWrapper.themeType, mSharedPreferences) {
                val context = LocalContext.current

                val content = remember {
                    mViewModel.content
                }

                var reminderTimeMillis: Long by remember {
                    mutableLongStateOf(System.currentTimeMillis() + 60 * 60 * 24 * 1000)
                }
                var reminderTimeString: String by remember {
                    mutableStateOf("")
                }

                var showDatePicker by remember { mutableStateOf(false) }
                var showTimePicker by remember { mutableStateOf(false) }
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = reminderTimeMillis + ZonedDateTime.now().offset.totalSeconds * 1000
                )
                val timePickerState = rememberTimePickerState(
                    initialHour = calendar.get(Calendar.HOUR_OF_DAY),
                    initialMinute = calendar.get(Calendar.MINUTE),
                    is24Hour = true,
                )

                val setReminderResult by mViewModel.setReminderResult.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }
                var snackbarMessage: String? by remember { mutableStateOf(null) }

                LaunchedEffect(timePickerState.hour, timePickerState.minute, datePickerState.selectedDateMillis) {
                    datePickerState.selectedDateMillis?.let {
                        reminderTimeMillis = getDateAndTimeMillis(it, timePickerState.hour, timePickerState.minute)
                        val instant = Instant.ofEpochMilli(reminderTimeMillis)
                        reminderTimeString = formatter.withZone(ZoneId.systemDefault()).format(instant)
                    } ?: run {
                        reminderTimeMillis = 0
                        reminderTimeString = ""
                    }
                }

                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let {
                        snackbarHostState.showSnackbar(it)
                    }
                }

                LaunchedEffect(setReminderResult) {
                    setReminderResult?.let {
                        when (it) {
                            is AppResult.Success<*> -> {
                                Toast.makeText(context, R.string.reminder_set, Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            is AppResult.Error<*> -> {
                                snackbarMessage = getString(it.error as Int)
                            }
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        ThemedTopAppBar(
                            titleStringResId = R.string.set_reminder,
                            isImmersiveInterfaceEnabled = isImmersiveInterfaceEnabled,
                            windowInsetsController = windowInsetsController,
                            actions = {
                                IconButton(onClick = {
                                    if (reminderTimeMillis == 0L || reminderTimeString.isEmpty()) {
                                        snackbarMessage = getString(R.string.please_set_reminder_time)
                                        return@IconButton
                                    }
                                    if (reminderTimeMillis < System.currentTimeMillis()) {
                                        snackbarMessage = getString(R.string.reminder_time_must_be_in_future)
                                        return@IconButton
                                    }
                                    mViewModel.setReminder(reminderTimeMillis)
                                }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_check_circle_toolbar_24dp),
                                        tint = Color(LocalAppTheme.current.toolbarPrimaryTextAndIconColor),
                                        contentDescription = "Localized description"
                                    )
                                }
                            }
                        ) {
                            finish()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                    contentWindowInsets = if (isImmersiveInterfaceEnabled) WindowInsets.safeDrawing else WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(LocalAppTheme.current.backgroundColor))
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        if (content.isNotEmpty()) {
                            PrimaryText(
                                content,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        HorizontalDivider(
                            color = Color(LocalAppTheme.current.dividerColor)
                        )

                        PrimaryText(
                            R.string.choose_a_predefined_reminder_time,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp, bottom = 8.dp),
                            fontSize = LocalTypography.current.fontSize.size16
                        )

                        FlowRow(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            reminderPresetTimes.forEachIndexed { _, reminderPresetTime ->
                                CustomFilledButton(text = reminderPresetTime.textOnButton) {
                                    val reminderTime = reminderPresetTime.timeInMillis + System.currentTimeMillis()
                                    datePickerState.selectedDateMillis = reminderTime + ZonedDateTime.now().offset.totalSeconds * 1000
                                    calendar.timeInMillis = reminderTime
                                    timePickerState.hour = calendar.get(Calendar.HOUR_OF_DAY)
                                    timePickerState.minute = calendar.get(Calendar.MINUTE)
                                }
                            }
                        }

                        HorizontalDivider(
                            color = Color(LocalAppTheme.current.dividerColor)
                        )

                        PrimaryText(
                            R.string.or_set_a_custom_reminder_time,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp),
                            fontSize = LocalTypography.current.fontSize.size16
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CustomPositiveTextButton(
                                stringResId = R.string.set_date
                            ) {
                                showDatePicker = true
                            }

                            CustomPositiveTextButton(
                                stringResId = R.string.set_time
                            ) {
                                showTimePicker = true
                            }
                        }

                        if (!reminderTimeString.isEmpty()) {
                            HorizontalDivider(
                                color = Color(LocalAppTheme.current.dividerColor)
                            )

                            PrimaryText(
                                R.string.you_will_receive_reminder_notification,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 16.dp, bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(1f)
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 8.dp, bottom = 16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                PrimaryText(
                                    reminderTimeString,
                                    fontSize = LocalTypography.current.fontSize.size20,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        PrimaryText(
                            R.string.note,
                            modifier = Modifier
                                .padding(horizontal = 16.dp),
                            fontSize = LocalTypography.current.fontSize.size16,
                            fontWeight = FontWeight.Bold
                        )

                        SecondaryText(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = Color(LocalAppTheme.current.primaryTextColor),
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(stringResource(R.string.reminder_reliability_notice_app_update_force_stop_title))
                                }

                                append(stringResource(R.string.reminder_reliability_notice_app_update_force_stop_description))
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp),
                            textAlign = TextAlign.Justify
                        )
                        SecondaryText(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = Color(LocalAppTheme.current.primaryTextColor),
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(stringResource(R.string.reminder_reliability_notice_battery_title))
                                }

                                append(stringResource(R.string.reminder_reliability_notice_battery_description))
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp),
                            textAlign = TextAlign.Justify
                        )
                        SecondaryText(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = Color(LocalAppTheme.current.primaryTextColor),
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(stringResource(R.string.reminder_reliability_notice_device_settings_title))
                                }

                                append(stringResource(R.string.reminder_reliability_notice_device_settings_description))
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Justify
                        )

                        if (showDatePicker) {
                            DatePickerDialog(
                                onDismissRequest = {
                                    showDatePicker = false
                                },
                                confirmButton = {
                                    CustomPositiveTextButton(stringResId = R.string.ok) {
                                        showDatePicker = false
                                    }
                                },
                                dismissButton = {
                                    CustomNeutralTextButton(stringResId = R.string.cancel) {
                                        showDatePicker = false
                                    }
                                }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }

                        if (showTimePicker) {
                            AlertDialog(
                                onDismissRequest = {
                                    showTimePicker = false
                                },
                                dismissButton = {
                                    CustomNeutralTextButton(stringResId = R.string.cancel) {
                                        showTimePicker = false
                                    }
                                },
                                confirmButton = {
                                    CustomPositiveTextButton(stringResId = R.string.ok) {
                                        showTimePicker = false
                                    }
                                },
                                text = {
                                    TimePicker(
                                        state = timePickerState,
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    fun getDateAndTimeMillis(dateMillis: Long, hour: Int, minute: Int): Long {
        return dateMillis + hour * 60 * 60 * 1000 + minute * 60 * 1000 - ZonedDateTime.now().offset.totalSeconds * 1000
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