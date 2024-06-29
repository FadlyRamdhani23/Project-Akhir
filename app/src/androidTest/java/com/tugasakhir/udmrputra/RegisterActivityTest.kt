package com.tugasakhir.udmrputra

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tugasakhir.udmrputra.ui.logreg.RegisterActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterActivityTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<RegisterActivity> =
        ActivityScenarioRule(RegisterActivity::class.java)

    @Test
    fun testInputEmailAndPassword() {
        onView(withId(R.id.nama_textField)).perform(typeText("Test User"), closeSoftKeyboard())
        onView(withId(R.id.email_textField)).perform(typeText("testuser@example.com"), closeSoftKeyboard())
        onView(withId(R.id.sandi_textField)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.konfirmasi_textField)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.nomor_textField)).perform(typeText("08123456789"), closeSoftKeyboard())
    }

    @Test
    fun testClickLoginButton() {
        onView(withId(R.id.nama_textField)).perform(typeText("Test User"), closeSoftKeyboard())
        onView(withId(R.id.email_textField)).perform(typeText("testuser@example.com"), closeSoftKeyboard())
        onView(withId(R.id.sandi_textField)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.konfirmasi_textField)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.nomor_textField)).perform(typeText("08123456789"), closeSoftKeyboard())
        onView(withId(R.id.btn_register)).perform(click())

    }
}
