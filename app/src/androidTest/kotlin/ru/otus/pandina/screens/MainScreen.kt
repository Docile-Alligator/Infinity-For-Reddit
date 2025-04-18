package ru.otus.pandina.screens

import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.tabs.KTabLayout
import io.github.kakaocup.kakao.text.KButton
import ml.docilealligator.infinityforreddit.R

object MainScreen : Screen<MainScreen>() {

    val button = KButton { withId(R.id.fab_main_activity) }

    val tabLayout = KTabLayout { withId(R.id.tab_layout_main_activity) }

    val navButton = KButton { withContentDescription("Open navigation drawer") }

}