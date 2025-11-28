package ml.docilealligator.infinityforreddit.activities

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customviews.compose.AppTheme
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
            AppTheme(customThemeWrapper.themeType) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Text("Small Top App Bar")
                            }
                        )
                    },
                ) { innerPadding ->

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