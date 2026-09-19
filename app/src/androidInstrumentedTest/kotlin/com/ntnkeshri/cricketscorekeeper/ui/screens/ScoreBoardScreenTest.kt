package com.ntnkeshri.cricketscorekeeper.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ntnkeshri.cricketscorekeeper.viewmodel.ScoreViewModel
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

        composeTestRule.onNodeWithText("Scorecard")
            .performClick()

        composeTestRule.onNodeWithText("Innings Scorecard")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Close")
            .performClick()

        composeTestRule.onNodeWithText("Innings Scorecard")
            .assertDoesNotExist()
    }

    @Test
    fun testDynamicElementRenderingWithKnownBowlers() {
        val viewModel = ScoreViewModel()

        viewModel.updateBowlerName("Bumrah")
        repeat(6) { viewModel.addValidDelivery(0) }

        viewModel.updateBowlerName("Shami")
        repeat(6) { viewModel.addValidDelivery(0) }

        composeTestRule.setContent {
            ScoreBoardScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Bumrah")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Shami")
            .assertIsDisplayed()
    }

    @Test
    fun testE2E_fullOneOverMatchProgression() {
        val viewModel = ScoreViewModel()
        composeTestRule.setContent { ScoreBoardScreen(viewModel = viewModel) }

        // Bypass Toss
        composeTestRule.onNodeWithText("Skip Toss / Manual Override", substring = true).performClick()
        composeTestRule.onNode(hasText("Team 1", substring = true) and hasAnyAncestor(isDialog())).performClick()
        composeTestRule.waitForIdle()

        // Setup 1 over match
        composeTestRule.onNode(hasText("Overs Limit", substring = true) and hasSetTextAction()).performTextClearance()
        composeTestRule.onNode(hasText("Overs Limit", substring = true) and hasSetTextAction()).performTextInput("1")

        // Bowl 6 dots
        repeat(6) {
            composeTestRule.onAllNodes(hasText("0") and hasClickAction()).onFirst().performScrollTo().performClick()
        }

        // Dialog should show
        composeTestRule.onNodeWithText("Innings Complete", substring = true).assertIsDisplayed()
        
        // Start next innings
        composeTestRule.onNodeWithText("Start Next Innings").performClick()
        
        // Next innings should have target 1
        composeTestRule.onNodeWithText("Target: 1", substring = true).assertIsDisplayed()
    }

    @Test
    fun testE2E_targetReached_showsInningsCompleteDialog() {
        val viewModel = ScoreViewModel()
        composeTestRule.setContent { ScoreBoardScreen(viewModel = viewModel) }

        // Bypass Toss
        composeTestRule.onNodeWithText("Skip Toss / Manual Override", substring = true).performClick()
        composeTestRule.onNode(hasText("Team 1", substring = true) and hasAnyAncestor(isDialog())).performClick()
        composeTestRule.waitForIdle()

        // Finish first innings at 0 runs
        composeTestRule.onNode(hasText("Overs Limit", substring = true) and hasSetTextAction()).performTextClearance()
        composeTestRule.onNode(hasText("Overs Limit", substring = true) and hasSetTextAction()).performTextInput("1")
        repeat(6) {
            composeTestRule.onAllNodes(hasText("0") and hasClickAction()).onFirst().performScrollTo().performClick()
        }
        composeTestRule.onNodeWithText("Start Next Innings").performClick()
        composeTestRule.waitForIdle()

        // Fill in active bowler for the second innings
        composeTestRule.onNode(hasText("Active Bowler", substring = true) and hasSetTextAction())
            .performScrollTo()
            .performTextInput("Bowler 2")
        composeTestRule.onNode(hasText("Active Bowler", substring = true) and hasSetTextAction())
            .performImeAction()

        // Target is 1 run. Score 1 run to reach target.
        composeTestRule.onAllNodes(hasText("1") and hasClickAction() and !hasSetTextAction()).onFirst().performScrollTo().performClick()

        composeTestRule.waitForIdle()

        // Match complete dialog should appear
        composeTestRule.onNodeWithText("Target Reached!", substring = true).assertIsDisplayed()
    }

    @Test
    fun testUI_tossDialog_assignsTeams() {
        val viewModel = ScoreViewModel()
        composeTestRule.setContent { ScoreBoardScreen(viewModel = viewModel) }

        // Give teams names
        composeTestRule.onNode(hasText("Team 1", substring = true) and hasSetTextAction()).performTextClearance()
        composeTestRule.onNode(hasText("Team 1", substring = true) and hasSetTextAction()).performTextInput("India")

        composeTestRule.onNode(hasText("Team 2", substring = true) and hasSetTextAction()).performTextClearance()
        composeTestRule.onNode(hasText("Team 2", substring = true) and hasSetTextAction()).performTextInput("Australia")

        // Wait for inputs
        composeTestRule.waitForIdle()

        // We expect the Toss Dialog to be open by default
        composeTestRule.onNodeWithText("Match Toss", substring = true).assertIsDisplayed()

        // Select India
        composeTestRule.onNode(hasText("India", substring = true) and hasAnyAncestor(isDialog())).performClick()

        // Select BAT
        composeTestRule.onNode(hasText("Bat", substring = false, ignoreCase = false) and hasClickAction()).performClick()

        // Confirm Toss
        composeTestRule.onNodeWithText("Confirm Toss", substring = true).performClick()

        // Wait for UI to settle and dialog to dismiss
        composeTestRule.waitForIdle()

        // Assert India is now the batting team
        // The Batting section has "Innings 1 - India"
        composeTestRule.onNodeWithText("Innings 1 - India").assertIsDisplayed()
    }

    @Test
    fun testUI_tossDialog_skipTossManualSelection() {
        val viewModel = ScoreViewModel()
        composeTestRule.setContent { ScoreBoardScreen(viewModel = viewModel) }

        // Give teams names
        composeTestRule.onNode(hasText("Team 1", substring = true) and hasSetTextAction()).performTextClearance()
        composeTestRule.onNode(hasText("Team 1", substring = true) and hasSetTextAction()).performTextInput("England")

        composeTestRule.onNode(hasText("Team 2", substring = true) and hasSetTextAction()).performTextClearance()
        composeTestRule.onNode(hasText("Team 2", substring = true) and hasSetTextAction()).performTextInput("NZ")

        composeTestRule.waitForIdle()

        // Dialog should be displayed
        composeTestRule.onNodeWithText("Match Toss", substring = true).assertIsDisplayed()

        // Click Skip Toss (this may be an outlined button)
        composeTestRule.onNodeWithText("Skip Toss / Manual Override", substring = true).performClick()
        
        // Manual override should present choices for who bats first
        composeTestRule.onNodeWithText("Select Batting Team", substring = true).assertIsDisplayed()
        
        // Select NZ
        composeTestRule.onNode(hasText("NZ", substring = true) and hasAnyAncestor(isDialog())).performClick()
        
        composeTestRule.waitForIdle()

        // Toss Dialog should dismiss. Assert NZ is batting team
        composeTestRule.onNodeWithText("Innings 1 - NZ").assertIsDisplayed()
    }
}
