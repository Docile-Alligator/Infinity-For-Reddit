package ml.docilealligator.infinityforreddit.activities

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper

class ReminderListingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ((application) as Infinity).appComponent.inject(this)

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isImmersiveInterfaceRespectForcedEdgeToEdge()) {
                enableEdgeToEdge()
            }
        }
    }

    override fun getDefaultSharedPreferences(): SharedPreferences? {
        TODO("Not yet implemented")
    }

    override fun getCurrentAccountSharedPreferences(): SharedPreferences? {
        TODO("Not yet implemented")
    }

    override fun getCustomThemeWrapper(): CustomThemeWrapper? {
        TODO("Not yet implemented")
    }

    override fun applyCustomTheme() {
        TODO("Not yet implemented")
    }
}