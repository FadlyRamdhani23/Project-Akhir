package com.tugasakhir.udmrputra

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.ui.logreg.LoginActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<LoginActivity> =
        ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun testInputEmailAndPassword() {
        onView(withId(R.id.email_textField))
            .perform(typeText("test@example.com"), closeSoftKeyboard())
        onView(withId(R.id.sandi_textField))
            .perform(typeText("password123"), closeSoftKeyboard())
    }

    @Test
    fun testClickLoginButton() {
        onView(withId(R.id.btnMasuk)).perform(click())
    }
}