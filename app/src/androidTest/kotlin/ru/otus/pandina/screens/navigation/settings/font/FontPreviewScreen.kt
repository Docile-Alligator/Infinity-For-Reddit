package ru.otus.pandina.screens.navigation.settings.font

import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView

object FontPreviewScreen : Screen<FontPreviewScreen>() {

    val screenTitle = KTextView { withText("Font Preview") }

    val someFont = KTextView { withAnyText() }

    fun selectFont(font : String) {
        someFont {
            hasText(font)
            click()
        }
    }
}
