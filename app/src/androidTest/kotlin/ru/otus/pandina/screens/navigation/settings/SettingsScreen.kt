package ru.otus.pandina.screens.navigation.settings

import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView

object SettingsScreen : Screen<SettingsScreen>() {

    val screenTittle = KTextView { withText("Settings")}

    val notification = KTextView { withText("Notification")}

    val interfaceSetting = KTextView { withText("Interface")}

    val theme = KTextView { withText("Theme")}

    val gesturesAndButtons = KTextView {withText("Gestures & Buttons")}

    val video = KTextView {withText("Video")}

    val lazyModeInterval = KTextView{withText("Lazy Mode Interval")}

    val summary = KTextView{withText("2.5s")}

    val downloadLocation = KTextView{withText("Download Location")}

    val dataSavingMode = KTextView { withText("Data Saving Mode")}

    val nsfwAndSpoiler = KTextView {withText("NSFW & Spoiler")}

    val postHistory = KTextView { withText("Post History")}

    val postFilter = KTextView { withText("Post Filter")}

    val sortType = KTextView { withText("Sort Type")}

    val miscellaneous = KTextView { withText("Miscellaneous")}

    val advanced = KTextView { withText("Advanced")}

    val about = KTextView { withText("About")}

    val privacyPolicy = KTextView { withText("Privacy Policy")}

    val redditUserAgreement = KTextView { withText("Reddit User Agreement")}
}
