package ru.otus.pandina.tests

import androidx.test.espresso.web.webdriver.Locator
import org.junit.Test
import ru.otus.pandina.screens.MainScreen
import ru.otus.pandina.screens.UserAgreementFragment
import ru.otus.pandina.screens.navigation.LoginScreen
import ru.otus.pandina.screens.navigation.NavigationViewLayout


class LoginTest : BaseTest() {

    @Test
    fun loginTest() {
        run {
            step("Open navigation") {
                MainScreen {
                    navButton {
                        isVisible()
                        click()
                    }
                }
                NavigationViewLayout {
                    navBanner.isVisible()
                }
            }
            step("Go to login form") {
                NavigationViewLayout {
                    accountNameTextView {
                        isVisible()
                        hasText("Anonymous")
                    }
                    karmaTextView {
                        isVisible()
                        hasText("Press here to login")
                    }
                    accountSwitcher {
                        isVisible()
                        click()
                    }
                    addAccountTextView {
                        isVisible()
                        hasText("Add an account")
                    }
                    addAccountButton {
                        isVisible()
                        click()
                    }
                }
            }
            step("Agree user agreement") {
                UserAgreementFragment {
                    alertTitle {
                        isVisible()
                        hasText("User Agreement")
                    }
                    dontAgreeButton.isVisible()
                    agreeButton {
                        isVisible()
                        click()
                    }
                }
            }
            step("Enter Login and password") {
                LoginScreen {
                    webView {
                        withElement(
                            Locator.XPATH,
                            "//h1[text()='Log In']"
                        ) {
                            containsText("Log In")
                        }
                        withElement(
                            Locator.XPATH,
                            "//*[@id='loginUsername']"
                        ) {
                            containsText("Username")
                            keys("*****")
                        }
                    }
                }
            }
        }
    }
}