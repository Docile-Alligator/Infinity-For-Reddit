package ml.docilealligator.infinityforreddit.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.util.Linkify
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.auth.AuthTabIntent
import androidx.browser.customtabs.CustomTabsClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customviews.compose.AppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.CustomAlert
import ml.docilealligator.infinityforreddit.customviews.compose.CustomFilledButton
import ml.docilealligator.infinityforreddit.customviews.compose.LocalAppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.PrimaryText
import ml.docilealligator.infinityforreddit.customviews.compose.ThemedTopAppBar
import ml.docilealligator.infinityforreddit.events.NewUserLoggedInEvent
import ml.docilealligator.infinityforreddit.extensions.linkify
import ml.docilealligator.infinityforreddit.utils.APIUtils
import ml.docilealligator.infinityforreddit.utils.getChromeCustomTabPackageName
import ml.docilealligator.infinityforreddit.viewmodels.AppAuthLoginViewModel
import ml.docilealligator.infinityforreddit.viewmodels.AppAuthLoginViewModel.Companion.provideFactory
import org.greenrobot.eventbus.EventBus
import retrofit2.Retrofit
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

class AppAuthLoginActivity : BaseActivity() {
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

    lateinit var mViewModel: AppAuthLoginViewModel

    private val mLauncher: ActivityResultLauncher<Intent?> =
        AuthTabIntent.registerActivityResultLauncher(this, this::handleAuthResult)

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
        )[AppAuthLoginViewModel::class.java]

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = customThemeWrapper.isLightStatusBar

        setContent {
            AppTheme(customThemeWrapper.themeType) {
                val context = LocalContext.current
                val scrollBehavior = enterAlwaysScrollBehavior()
                val accountFetched by mViewModel.accountFetched.collectAsStateWithLifecycle()
                val errorMessageId by mViewModel.errorMessageId.collectAsStateWithLifecycle()
                var isAgreeToUserAgreement by rememberSaveable { mutableStateOf(false) }

                LaunchedEffect(accountFetched) {
                    if (accountFetched) {
                        EventBus.getDefault().post(NewUserLoggedInEvent())
                        finish()
                    }
                }

                Scaffold(
                    topBar = {
                        ThemedTopAppBar(
                            titleStringResId = R.string.login_activity_label,
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(LocalAppTheme.current.backgroundColor))
                            .padding(innerPadding)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!isAgreeToUserAgreement) {
                            CustomAlert(
                                title = stringResource(R.string.user_agreement_dialog_title),
                                text = getString(
                                    R.string.user_agreement_message,
                                    "https://www.redditinc.com/policies/user-agreement",
                                    "https://docile-alligator.github.io"
                                ).linkify(Linkify.WEB_URLS, SpanStyle(Color(LocalAppTheme.current.linkColor))) {
                                    startActivity(Intent(this@AppAuthLoginActivity, LinkResolverActivity::class.java).apply {
                                        data = it
                                    })
                                },
                                confirmText = getString(R.string.agree),
                                dismissText = getString(R.string.do_not_agree),
                                cancelable = false,
                                onConfirm = {
                                    isAgreeToUserAgreement = true
                                    launchAuthTab()
                                },
                                onDismiss = {
                                    finish()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        CustomFilledButton(
                            modifier = Modifier.padding(bottom = 16.dp),
                            stringResId = R.string.login
                        ) {
                            mViewModel.clearError()
                            launchAuthTab()
                        }

                        errorMessageId?.let {
                            PrimaryText(
                                stringResource(R.string.login_failed_error),
                                textAlign = TextAlign.Center
                            )

                            PrimaryText(
                                it,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        PrimaryText(
                            stringResource(R.string.login_using_different_method)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            CustomFilledButton(stringResId = R.string.login_using_chrome_custom_tab) {
                                if (getChromeCustomTabPackageName(context) == null) {
                                    Toast.makeText(context, R.string.login_chrome_required, Toast.LENGTH_SHORT).show()
                                } else {
                                    startActivity(Intent(context, LoginChromeCustomTabActivity::class.java))
                                    finish()
                                }
                            }

                            CustomFilledButton(stringResId = R.string.login_using_webview) {
                                startActivity(Intent(context, LoginActivity::class.java))
                                finish()
                            }
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

    private fun launchAuthTab() {
        getChromeCustomTabPackageName(this)?.let {
            if (!CustomTabsClient.isAuthTabSupported(this, it)) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return
            }

            val baseUri = APIUtils.OAUTH_URL.toUri()
            val uriBuilder = baseUri.buildUpon()
            uriBuilder.appendQueryParameter(APIUtils.CLIENT_ID_KEY, APIUtils.CLIENT_ID)
            uriBuilder.appendQueryParameter(APIUtils.RESPONSE_TYPE_KEY, APIUtils.RESPONSE_TYPE)
            uriBuilder.appendQueryParameter(APIUtils.STATE_KEY, APIUtils.STATE)
            uriBuilder.appendQueryParameter(APIUtils.REDIRECT_URI_KEY, APIUtils.REDIRECT_URI)
            uriBuilder.appendQueryParameter(APIUtils.DURATION_KEY, APIUtils.DURATION)
            uriBuilder.appendQueryParameter(APIUtils.SCOPE_KEY, APIUtils.SCOPE)

            val authTabIntent = AuthTabIntent.Builder().setEphemeralBrowsingEnabled(true).build()
            authTabIntent.launch(mLauncher, uriBuilder.build(), "infinity")
        }
    }

    private fun handleAuthResult(result: AuthTabIntent.AuthResult) {
        result.resultCode
        when (result.resultCode) {
            AuthTabIntent.RESULT_OK -> result.resultUri?.let {
                mViewModel.setUpAccount(it)
            }
            AuthTabIntent.RESULT_CANCELED,
            AuthTabIntent.RESULT_VERIFICATION_FAILED -> mViewModel.setError(R.string.login_failed_auth_result_verification_failed)
            AuthTabIntent.RESULT_VERIFICATION_TIMED_OUT -> mViewModel.setError(R.string.login_failed_auth_result_verification_timed_out)
            else -> mViewModel.setError(R.string.login_failed_auth_result_unknown_error)
        }
    }
}