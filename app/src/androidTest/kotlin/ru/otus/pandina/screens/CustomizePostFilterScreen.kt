package ru.otus.pandina.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.switch.KSwitch
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import io.github.kakaocup.kakao.toolbar.KToolbar
import ml.docilealligator.infinityforreddit.R

object CustomizePostFilterScreen : KScreen<CustomizePostFilterScreen>() {

    val toolBar = KToolbar{withId(R.id.toolbar_customize_post_filter_activity)}

    val customizeFilterEditText = KEditText { withId(R.id.name_text_input_edit_text_customize_post_filter_activity)}

    val textFilterTextView = KTextView { withId(R.id.post_type_text_text_view_customize_post_filter_activity)}

    val textFilterCheckBox = KSwitch { withId(R.id.post_type_text_switch_customize_post_filter_activity)}

    val linkFilterTextView = KTextView { withId(R.id.post_type_link_text_view_customize_post_filter_activity)}

    val linkFilterCheckBox = KSwitch { withId(R.id.post_type_link_switch_customize_post_filter_activity)}

    val onlyNsfwTextView = KTextView { withId(R.id.only_nsfw_text_view_customize_post_filter_activity)}

    val saveButton = KButton { withId(R.id.action_save_customize_post_filter_activity)}
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null
}
