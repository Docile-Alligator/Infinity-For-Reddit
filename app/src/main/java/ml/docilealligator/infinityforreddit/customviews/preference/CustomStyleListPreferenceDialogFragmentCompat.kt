package ml.docilealligator.infinityforreddit.customviews.preference

import android.app.Dialog
import android.os.Bundle
import androidx.preference.ListPreferenceDialogFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ml.docilealligator.infinityforreddit.R

class CustomStyleListPreferenceDialogFragmentCompat : ListPreferenceDialogFragmentCompat() {

    companion object {
        @JvmStatic
        fun newInstance(key: String?): CustomStyleListPreferenceDialogFragmentCompat {
            val fragment = CustomStyleListPreferenceDialogFragmentCompat()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialogTheme)
            .setTitle(preference.dialogTitle)
            .setIcon(preference.dialogIcon)
            .setPositiveButton(preference.positiveButtonText, this)
            .setNegativeButton(preference.negativeButtonText, this)

        val contentView = onCreateDialogView(requireContext())
        if (contentView != null) {
            onBindDialogView(contentView)
            builder.setView(contentView)
        } else {
            builder.setMessage(preference.dialogMessage)
        }

        onPrepareDialogBuilder(builder)

        return builder.create()
    }
}