package ml.docilealligator.infinityforreddit.settings

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.PostDetailCommentsCacheManager
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.customviews.preference.CustomFontPreferenceFragmentCompat
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils
import javax.inject.Inject

class PostDetailsPreferenceFragment : CustomFontPreferenceFragmentCompat() {
    @Inject
    lateinit var mPostDetailCommentsCacheManager: PostDetailCommentsCacheManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val preferenceManager = getPreferenceManager()
        preferenceManager.setSharedPreferencesName(SharedPreferencesUtils.POST_DETAILS_SHARED_PREFERENCES_FILE)
        setPreferencesFromResource(R.xml.post_details_preferences, rootKey)

        (mActivity.application as Infinity).appComponent.inject(this)

        val commentThreadContinuityCapacityEditTextPreference =
            findPreference<EditTextPreference?>(SharedPreferencesUtils.COMMENT_THREAD_CONTINUITY_CAPACITY)

        if (commentThreadContinuityCapacityEditTextPreference != null) {
            commentThreadContinuityCapacityEditTextPreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    try {
                        val value = (newValue as String?)?.toInt() ?: -1
                        if (value < 0) {
                            false
                        } else {
                            mPostDetailCommentsCacheManager.setCapacity(value)
                            true
                        }
                    } catch (_: NumberFormatException) {
                        false
                    }
                }
            commentThreadContinuityCapacityEditTextPreference.setSummaryProvider(
                SummaryProvider { _: Preference? ->
                    getString(
                        R.string.settings_comment_thread_continuity_capacity_summary,
                        commentThreadContinuityCapacityEditTextPreference.text
                    )
                }
            )
        }
    }
}