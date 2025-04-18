package ru.otus.pandina.screens.navigation

import android.view.View
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView
import ml.docilealligator.infinityforreddit.R
import org.hamcrest.Matcher

object NavigationViewLayout : Screen<NavigationViewLayout>() {

    val navBanner = KImageView{withId(R.id.banner_image_view_nav_header_main)}

    val accountNameTextView = KTextView { withId(R.id.name_text_view_nav_header_main) }

    val karmaTextView = KTextView { withId(R.id.karma_text_view_nav_header_main) }

    val accountSwitcher = KImageView { withId(R.id.account_switcher_image_view_nav_header_main) }

    val addAccountButton = KImageView { withId(R.id.image_view_item_nav_drawer_menu_item) }

    val addAccountTextView = KTextView { withId(R.id.text_view_item_nav_drawer_menu_item) }

    val subscriptions = KTextView { withText("Subscriptions") }

    val multireddit = KTextView { withText("Multireddit") }

    val historyIcon = KImageView { withId(R.id.image_view_item_nav_drawer_menu_item) }

    val trending = KTextView { withText("Trending") }

    val darkThemeIcon = KImageView { withDrawable(R.drawable.ic_dark_theme_24dp) }

    val darkTheme = KTextView { withText("Dark Theme") }

    val lightThemeIcon = KImageView { withDrawable(R.drawable.ic_light_theme_24dp) }

    val lightTheme = KTextView { withText("Light Theme") }

    val settings = KTextView { withText("Settings") }


    val nawDrawerRecyclerView = KRecyclerView(
        builder = {withId(R.id.nav_drawer_recycler_view_main_activity)},
        itemTypeBuilder = {itemType(NavigationViewLayout::NawDrawerRecyclerItem)}
    )

    class NawDrawerRecyclerItem(parent : Matcher<View>) : KRecyclerItem<NawDrawerRecyclerItem>(parent) {

    }
}
