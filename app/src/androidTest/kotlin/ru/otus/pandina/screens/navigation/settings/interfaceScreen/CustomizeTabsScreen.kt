package ru.otus.pandina.screens.navigation.settings.interfaceScreen

import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView
import ml.docilealligator.infinityforreddit.R

object CustomizeTabsScreen : Screen<CustomizeTabsScreen>() {

    val screenTitle = KTextView { withText("Customize Tabs in Main Page") }

    val infoTextView = KTextView { withId(R.id.info_text_view_customize_main_page_tabs_fragment)}

    val tabCountTitle = KTextView { withId(R.id.tab_count_title_text_view_customize_main_page_tabs_fragment)}

    val tabCountSummary = KTextView { withId(R.id.tab_count_text_view_customize_main_page_tabs_fragment)}


}
