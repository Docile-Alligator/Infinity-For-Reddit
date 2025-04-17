package ru.otus.pandina.screens.navigation

import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView
import ml.docilealligator.infinityforreddit.R

object NavigationViewLayout : Screen<NavigationViewLayout>() {

    val navBanner = KImageView{withId(R.id.banner_image_view_nav_header_main)}

    val accountNameTextView = KTextView { withId(R.id.name_text_view_nav_header_main) }

    val karmaTextView = KTextView { withId(R.id.karma_text_view_nav_header_main) }

    val accountSwitcher = KImageView { withId(R.id.account_switcher_image_view_nav_header_main) }

    val addAccountButton = KImageView { withId(R.id.image_view_item_nav_drawer_menu_item) }

    val addAccountTextView = KTextView { withId(R.id.text_view_item_nav_drawer_menu_item) }


}
