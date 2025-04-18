package ru.otus.pandina.tests

import android.graphics.Color
import androidx.test.core.app.ActivityScenario
import ml.docilealligator.infinityforreddit.activities.MainActivity
import org.junit.Test
import ru.otus.pandina.screens.navigation.settings.interfaceScreen.CustomizeTabsScreen
import ru.otus.pandina.screens.MainScreen
import ru.otus.pandina.screens.navigation.NavigationViewLayout
import ru.otus.pandina.screens.navigation.settings.ActionPanel
import ru.otus.pandina.screens.navigation.settings.SettingsScreen
import ru.otus.pandina.screens.navigation.settings.ThemeScreen
import ru.otus.pandina.screens.navigation.settings.font.FontPreviewScreen
import ru.otus.pandina.screens.navigation.settings.font.FontScreen
import ru.otus.pandina.screens.navigation.settings.interfaceScreen.InterfaceScreen
import ru.otus.pandina.screens.navigation.settings.notification.NotificationScreen

class SettingsTest : BaseTest() {

    fun openSettings() {
        run {
            step("Open navigation drawer") {
                MainScreen {
                    navButton {
                        isVisible()
                        click()
                    }
                }
                NavigationViewLayout {
                    navBanner.isVisible()
                    settings.isVisible()
                    nawDrawerRecyclerView {
                        scrollToEnd()
                    }
                }
            }
            step("Open settings") {
                NavigationViewLayout.settings.click()
                SettingsScreen {
                    screenTittle {
                        isVisible()
                        hasText("Settings")
                    }
                }
            }
        }
    }

    @Test
    fun disableNotificationTest() {
        before {
            openSettings()
        }.after {

        }.run {
            step("Open notifications screen and disable notifications") {
                SettingsScreen.notification.click()
                NotificationScreen {
                    screenTitle {
                        isVisible()
                        hasText("Notification")
                    }
                    enableNotifications.isVisible()
                    notificationInterval.isVisible()
                    notificationSwitch.click()
                    notificationInterval.doesNotExist()
                    notificationSwitch.click()
                    notificationInterval.isVisible()
                }
            }
        }
    }

    @Test
    fun setFontTest() {
        before {
            openSettings()
        }.after {

        }.run {
            step("Open interface screen") {
                SettingsScreen.interfaceSetting.click()
                InterfaceScreen {
                    screenTitle {
                        isVisible()
                        hasText("Interface")
                    }
                    font.isVisible()
                }
            }
            step("Open font screen and set font") {
                InterfaceScreen.font.click()
                FontScreen {
                    screenTitle {
                        isVisible()
                        hasText("Font")
                    }
                    fontPreview {
                        isVisible()
                        click()
                    }
                }
                FontPreviewScreen {
                    screenTitle {
                        isVisible()
                        hasText("Font Preview")
                    }
                }
            }
        }
    }

    @Test
    fun customizeTabsInMainPage() {
        before {
            openSettings()
        }.after {
        }.run {
            step("Open interface screen") {
                SettingsScreen.interfaceSetting.click()
                InterfaceScreen {
                    screenTitle {
                        isVisible()
                        hasText("Interface")
                    }
                    customizeTabs.isVisible()
                }
            }
            step("Open and Customize Tabs in Main Page") {
                InterfaceScreen.customizeTabs.click()
                CustomizeTabsScreen {
                    screenTitle {
                        isVisible()
                        hasText("Customize Tabs in Main Page")
                    }
                    infoTextView {
                        isVisible()
                        containsText("Restart the app to see the changes")
                    }
                    tabCountTitle {
                        isVisible()
                        hasText("Tab Count")
                        click()
                    }
                }
                ActionPanel {
                    alertTitle {
                        isVisible()
                        hasText("Tab Count")
                    }
                    list {
                        isVisible()
                        hasDescendant {
                            this.withText("2")
                        }
                    }
                    getItem("2")
                }
                CustomizeTabsScreen {
                    tabCountSummary.hasText("2")
                }
            }
            step("Restart app") {
                activityRule.scenario.close()
                ActivityScenario.launch(MainActivity::class.java, null)
            }
            step("Check tabs") {
                MainScreen {
                    tabLayout {
                        hasDescendant {
                            withText("Home")
                        }
                        hasDescendant {
                            withText("Popular")
                        }
                        hasNotDescendant {
                            withText("All")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun setDarkThemeTest() {
        before {
            openSettings()
        }.after {
        }.run {
            step("Open Theme screen and set dark theme") {
                SettingsScreen.theme.click()
                ThemeScreen {
                    themeRecycler {
                        firstChild<ThemeScreen.ThemeRecyclerItem> {
                            title.click()
                        }
                    }
                }
                ActionPanel {
                    getItem("Dark Theme")
                }
                ThemeScreen {
                    themeRecycler {
                        firstChild<ThemeScreen.ThemeRecyclerItem> {
                            summary.hasText("Dark Theme")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun setLightThemeTest() {
        before {
            openSettings()
        }.after {
        }.run {
            step("Open Theme screen and set light theme") {
                SettingsScreen.theme.click()
                ThemeScreen {
                    themeRecycler {
                        firstChild<ThemeScreen.ThemeRecyclerItem> {
                            title.click()
                        }
                    }
                }
                ActionPanel {
                    getItem("Light Theme")
                }
                ThemeScreen {
                    themeRecycler {
                        firstChild<ThemeScreen.ThemeRecyclerItem> {
                            summary.hasText("Light Theme")
                        }
                    }
                    frame.hasBackgroundColor(Color.WHITE)
                }
            }
        }
    }
}