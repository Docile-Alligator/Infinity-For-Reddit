package ru.otus.pandina.screens.navigation.settings.notification

import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.switch.KSwitch
import io.github.kakaocup.kakao.text.KTextView
import ml.docilealligator.infinityforreddit.R

object NotificationScreen : Screen<NotificationScreen>() {

    val screenTitle = KTextView { withText("Notification") }

    val enableNotifications = KTextView { withText("Enable Notifications") }

    val notificationSwitch = KSwitch { withId(R.id.material_switch_switch_preference) }

    val notificationInterval = KTextView { withText("Check Notifications Interval")}
}
