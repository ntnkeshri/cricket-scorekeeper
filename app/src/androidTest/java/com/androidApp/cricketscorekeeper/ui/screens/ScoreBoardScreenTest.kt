package com.androidApp.cricketscorekeeper.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidApp.cricketscorekeeper.viewmodel.ScoreViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScoreBoardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testInputFieldBinding() {
        val viewModel = ScoreViewModel()

        composeTestRule.setContent {
            ScoreBoardScreen(viewModel = viewModel)
        }

        // Find the text field labeled "Striker *", input "Sachin", and assert it displays "Sachin"
        composeTestRule.onNodeWithText("Striker *")
            .performTextClearance()

        composeTestRule.onNodeWithText("Striker *")
            .performTextInput("Sachin")

        composeTestRule.onNodeWithText("Sachin")
            .assertIsDisplayed()
    }

    @Test
    fun testDialogExpansionAndDismissal() {
        val viewModel = ScoreViewModel()

        composeTestRule.setContent {
            ScoreBoardScreen(viewModel = viewModel)
        }

        // Find "Scorecard" button, click, and assert "Innings Scorecard" dialog is displayed
        composeTestRule.onNodeWithText("Scorecard")
            .performClick()

        composeTestRule.onNodeWithText("Innings Scorecard")
            .assertIsDisplayed()

        // Click "Close" button and assert dialog disappears
        composeTestRule.onNodeWithText("Close")
            .performClick()

        composeTestRule.onNodeWithText("Innings Scorecard")
            .assertDoesNotExist()
    }

    @Test
    fun testDynamicElementRenderingWithKnownBowlers() {
        val viewModel = ScoreViewModel()

        // Mock state where knownBowlers contains two names by logging overs
        viewModel.updateBowlerName("Bumrah")
        repeat(6) { viewModel.addValidDelivery(0) }

        viewModel.updateBowlerName("Shami")
        repeat(6) { viewModel.addValidDelivery(0) }

        composeTestRule.setContent {
            ScoreBoardScreen(viewModel = viewModel)
        }

        // Assert that two AssistChip composables exist in the UI hierarchy with those exact names
        composeTestRule.onNodeWithText("Bumrah")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Shami")
            .assertIsDisplayed()
    }
}
