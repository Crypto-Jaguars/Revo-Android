package com.example.fideicomisoapproverring

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorPageActivityTest {

    @Test
    fun test404ErrorDisplay() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ErrorPageActivity::class.java).apply {
            putExtra(ErrorPageActivity.ERROR_TYPE, ErrorPageActivity.ERROR_404)
        }
        
        ActivityScenario.launch<ErrorPageActivity>(intent).use {
            onView(withId(R.id.errorTitle))
                .check(matches(withText(R.string.error_404_title)))
            
            onView(withId(R.id.errorMessage))
                .check(matches(withText(R.string.error_404_message)))
            
            onView(withId(R.id.primaryActionButton))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.button_go_home)))
        }
    }

    @Test
    fun testNetworkErrorDisplay() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ErrorPageActivity::class.java).apply {
            putExtra(ErrorPageActivity.ERROR_TYPE, ErrorPageActivity.ERROR_NETWORK)
        }
        
        ActivityScenario.launch<ErrorPageActivity>(intent).use {
            onView(withId(R.id.errorTitle))
                .check(matches(withText(R.string.error_network_title)))
            
            onView(withId(R.id.errorMessage))
                .check(matches(withText(R.string.error_network_message)))
            
            onView(withId(R.id.primaryActionButton))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.button_try_again)))
        }
    }
} 