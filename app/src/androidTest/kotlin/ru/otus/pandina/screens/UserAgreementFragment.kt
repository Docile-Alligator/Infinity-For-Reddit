package ru.otus.pandina.screens

import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import ml.docilealligator.infinityforreddit.R

object UserAgreementFragment : Screen<UserAgreementFragment>() {

    val alertTitle = KTextView { withId(R.id.alertTitle)}

    val dontAgreeButton = KButton { withText("Don't Agree")}

    val agreeButton = KButton { withText("Agree")}
}
