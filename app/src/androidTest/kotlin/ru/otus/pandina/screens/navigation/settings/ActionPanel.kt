package ru.otus.pandina.screens.navigation.settings

import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import ml.docilealligator.infinityforreddit.R

object ActionPanel : Screen<ActionPanel>() {

    val alertTitle = KTextView { withId(R.id.alertTitle)}

    val list = KView {
        withId(R.id.select_dialog_listview)
    }

    val cancelButton = KButton { withText("Cancel")}

    fun getItem(itm : String) = KTextView {
        withId(R.id.select_dialog_listview)
        withDescendant {
            withText(itm)
        }
    }.click()
}
