package ru.otus.pandina.screens.navigation.settings.interfaceScreen

import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.switch.KSwitch
import io.github.kakaocup.kakao.text.KTextView
import ml.docilealligator.infinityforreddit.R

object InterfaceScreen : Screen<InterfaceScreen>() {

    val screenTitle = KTextView {withText("Interface")}

    val font = KTextView {withText("Font")}

    val immersiveInterface = KTextView {withText("Immersive Interface")}

    val navigationDrawer = KTextView {withText("Navigation Drawer")}

    val customizeTabs = KTextView {withText("Customize Tabs in Main Page")}

    val customizeBottom = KTextView{withText("Customize Bottom Navigation Bar")}

    val hideFab = KTextView{withText("Hide FAB in Post Feed")}

    val hideFabSwitch = KSwitch {withId(R.id.material_switch_switch_preference)}

    val enableBottomNav = KTextView{withText("Enable Bottom Navigation")}


}
