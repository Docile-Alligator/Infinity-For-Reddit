package ru.otus.pandina.screens.navigation.settings

import android.view.View
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.switch.KSwitch
import io.github.kakaocup.kakao.text.KTextView
import ml.docilealligator.infinityforreddit.R
import org.hamcrest.Matcher

object ThemeScreen : Screen<ThemeScreen>() {

    val screeTitle = KTextView {
        withParent { withId(R.id.toolbar_settings_activity) }
        withText("Theme")
    }

    val frame = KView { withId(R.id.frame_layout_settings_activity) }

    val themeRecycler = KRecyclerView(
        builder = { withId(R.id.recycler_view) },
        itemTypeBuilder = { itemType(ThemeScreen::ThemeRecyclerItem) }
    )


    class ThemeRecyclerItem(parent: Matcher<View>) : KRecyclerItem<ThemeRecyclerItem>(parent) {

        val icon = KImageView(parent) { withResourceName("icon_frame") }

        val title = KTextView(parent) { withResourceName("title") }

        val summary = KTextView(parent) { withResourceName("summary") }

        val switcher = KSwitch(parent) { withResourceName("material_switch_switch_preference") }
    }


}
