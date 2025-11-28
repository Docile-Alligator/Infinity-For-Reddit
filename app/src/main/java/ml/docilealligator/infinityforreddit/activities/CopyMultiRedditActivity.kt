package ml.docilealligator.infinityforreddit.activities

import android.R.attr.checked
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customviews.compose.AppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.CustomTextField
import ml.docilealligator.infinityforreddit.customviews.compose.LocalAppTheme
import ml.docilealligator.infinityforreddit.customviews.compose.SwitchRow
import retrofit2.Retrofit
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

@OptIn(ExperimentalMaterial3Api::class)
class CopyMultiRedditActivity : BaseActivity() {
    @Inject
    @Named("oauth")
    lateinit var mOauthRetrofit: Retrofit

    @Inject
    lateinit var mRedditDataRoomDatabase: RedditDataRoomDatabase

    @Inject
    @Named("default")
    lateinit var mSharedPreferences: SharedPreferences

    @Inject
    @Named("current_account")
    lateinit var mCurrentAccountSharedPreferences: SharedPreferences

    @Inject
    lateinit var mCustomThemeWrapper: CustomThemeWrapper

    @Inject
    lateinit var mExecutor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        ((application) as Infinity).appComponent.inject(this)

        super.onCreate(savedInstanceState)

        setContent {
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            val name = rememberTextFieldState()
            val description = rememberTextFieldState()
            var isPrivate by remember { mutableStateOf(true) }

            AppTheme(customThemeWrapper.themeType) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(LocalAppTheme.current.colorPrimary),
                                titleContentColor = Color(LocalAppTheme.current.toolbarPrimaryTextAndIconColor),
                            ),
                            title = {
                                Text(stringResource(R.string.copy_multireddit_activity_label))
                            },
                            scrollBehavior = scrollBehavior
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        item {
                            CustomTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 16.dp, bottom = 8.dp),
                                state = name,
                                placeholder = stringResource(R.string.multi_reddit_name_hint)
                            )
                        }

                        item {
                            CustomTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 8.dp, bottom = 8.dp),
                                state = description,
                                placeholder = stringResource(R.string.multi_reddit_description_hint)
                            )
                        }

                        item {
                            SwitchRow(
                                checked = isPrivate,
                                title = stringResource(R.string.private_multi_reddit)
                            ) {
                                isPrivate = it
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getDefaultSharedPreferences(): SharedPreferences? {
        return mSharedPreferences
    }

    override fun getCurrentAccountSharedPreferences(): SharedPreferences? {
        return mCurrentAccountSharedPreferences
    }

    override fun getCustomThemeWrapper(): CustomThemeWrapper? {
        return mCustomThemeWrapper
    }

    override fun applyCustomTheme() {

    }
}