package ru.otus.pandina.screens.navigation

import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.web.KWebView
import ml.docilealligator.infinityforreddit.R

object LoginScreen : Screen<LoginScreen>() {

    val userNameField = KEditText { withText("Username")}

    val loginPasswordField = KEditText { withResourceName("loginPassword")}

    val loginButton = KButton { withText("Log In")}

    val webView = KWebView { withId(R.id.webview_login_activity)}
}
