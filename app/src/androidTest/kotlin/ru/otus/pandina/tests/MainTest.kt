package ru.otus.pandina.tests

import androidx.test.espresso.action.GeneralLocation
import ml.docilealligator.infinityforreddit.R
import org.junit.Test
import ru.otus.pandina.screens.CustomizePostFilterScreen
import ru.otus.pandina.screens.FilteredPostsScreen
import ru.otus.pandina.screens.MainScreen


class MainTest : BaseTest() {

    @Test
    fun popularPostFilterTest() {
        run {
            step("Main screen popular tab") {
                MainScreen {
                    tabLayout {
                        isVisible()
                        click(GeneralLocation.CENTER)
                    }
                    button {
                        isVisible()
                        click()
                    }
                }
            }
            step("Popular Filtered Posts") {
                FilteredPostsScreen {
                    postFragmentList {
                        isVisible()

                        firstChild<FilteredPostsScreen.PostFragmentItem> {
                            isVisible()
                        }

                        lastChild<FilteredPostsScreen.PostFragmentItem> {
                            isVisible()
                            title.hasAnyText()
                            image.isVisible()
                        }

                        children<FilteredPostsScreen.PostFragmentItem> {
                            isVisible()
                        }
                    }
                    filterButton {
                        isVisible()
                        click()
                    }
                }
            }
            step("Check customize filter") {
                CustomizePostFilterScreen {
                    screenTitle.hasText(R.string.customize_post_filter_activity_label)
                    customizeFilterEditText {
                        isEnabled()
                        hasText("New Filter")
                    }
                    textFilterTextView {
                        isVisible()
                        hasText("Text")
                    }
                    textFilterCheckBox {
                        isVisible()
                        isChecked()
                        click()
                        isNotChecked()
                    }
                    linkFilterTextView {
                        isVisible()
                        hasText("Link")
                    }
                    linkFilterCheckBox {
                        isVisible()
                        isChecked()
                        click()
                        isNotChecked()
                    }
                    onlyNsfwTextView {
                        isVisible()
                        hasText("Only NSFW")
                    }
                    onlyNsfwSwitch {
                        isVisible()
                        isNotChecked()
                        click()
                        isChecked()
                    }
                    saveButton.click()
                }
            }
        }
    }
}