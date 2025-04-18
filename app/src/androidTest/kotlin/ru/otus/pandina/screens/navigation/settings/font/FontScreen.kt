package ru.otus.pandina.screens.navigation.settings.font

import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView
import ml.docilealligator.infinityforreddit.R

object FontScreen : Screen<FontScreen>() {

    val screenTitle = KTextView {
        withText("Font")
        withParent {
            withId(R.id.toolbar_settings_activity)
        }
    }

    val fontPreview = KTextView { withText("Font Preview") }

    val fontFamily = KTextView { withText("Font Family") }

    val summaryFontFamily = KTextView { withSibling { withText("Font Family") } }

    val fontSize = KTextView { withText("Font Size") }

    val titleFontFamily = KTextView { withText("Title Font Family") }

    val titleFontSize = KTextView { withText("Title Font Size") }

    val contentFontFamily = KTextView { withText("Content Font Family") }

    val contentFontSize = KTextView { withText("Content Font Size") }
}
