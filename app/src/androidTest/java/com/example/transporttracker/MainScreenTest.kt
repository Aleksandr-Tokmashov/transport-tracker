package com.example.transporttracker

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.transporttracker.ui.home.HomeScreen
import com.example.transporttracker.ui.home.HomeUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule
    val composeTestRule =
        createAndroidComposeRule<MainActivity>()

    @Test
    fun start_tracking_button_changes_state() {

        composeTestRule
            .onNodeWithTag("start_tracking_button")
            .assertExists()

        composeTestRule
            .onNodeWithTag("start_tracking_button")
            .performClick()
    }
}