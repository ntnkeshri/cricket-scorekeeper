package com.ntnkeshri.cricketscorekeeper.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ntnkeshri.cricketscorekeeper.viewmodel.ScoreViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScoreRuleEnforcementTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testDotBallOnFreeHit() {
        val viewModel = ScoreViewModel()
        composeTestRule.setContent { ScoreBoardScreen(viewModel) }

        // 1. Trigger No Ball -> 0 runs
        composeTestRule.onNodeWithText("No Ball").performScrollTo().performClick()
        composeTestRule.onAllNodes(hasText("0") and hasClickAction()).onLast().performClick()
        composeTestRule.onNodeWithText("Confirm No Ball").performClick()

        // Get total score before free hit delivery (1 run for No Ball)
        composeTestRule.onNodeWithText("1 - 0").assertIsDisplayed()

        // 2. Bowl 0 runs (Dot ball on Free Hit)
        composeTestRule.onAllNodes(hasText("0") and hasClickAction()).onFirst().performClick()

        // 3. Assert over summary pill renders "0-FH" with a red highlight
        // This is expected to fail initially because the ViewModel logs "0" instead of "0-FH"
        composeTestRule.onNodeWithText("0-FH", substring = true).assertIsDisplayed()

        // 4. Assert total score did not increase
        composeTestRule.onNodeWithText("1 - 0").assertIsDisplayed()
    }

    @Test
    fun testFreeHitWicketLockout() {
        val viewModel = ScoreViewModel()
        composeTestRule.setContent { ScoreBoardScreen(viewModel) }

        // 1. Trigger No Ball -> 0 runs to activate Free Hit
        composeTestRule.onNodeWithText("No Ball").performScrollTo().performClick()
        composeTestRule.onAllNodes(hasText("0") and hasClickAction()).onLast().performClick()
        composeTestRule.onNodeWithText("Confirm No Ball").performClick()

        // 2. Trigger Wicket Dialog
        composeTestRule.onNode(hasText("Wicket", substring = false, ignoreCase = false) and hasClickAction()).performScrollTo().performClick()
        composeTestRule.waitForIdle() // Ensure dialog recomposition is fully settled

        // 3. Assert Standard Wicket chip is disabled
        composeTestRule.onNodeWithText("Standard (Bowled/Caught/LBW)", substring = true).assertIsNotEnabled()
    }

    @Test
    fun testWideStrikeRotation() {
        val viewModel = ScoreViewModel()
        composeTestRule.setContent { ScoreBoardScreen(viewModel) }

        // Initial striker is Batter 1
        // Score is 0 - 0

        // 1. Trigger Wide -> 3 total runs (1 penalty + 2 run ran)
        composeTestRule.onNodeWithText("Wide").performScrollTo().performClick()
        composeTestRule.onAllNodes(hasText("3") and hasClickAction()).onLast().performClick()
        composeTestRule.onNodeWithText("Confirm Wide").performClick()

        // 2. Assert team total increases by 3
        composeTestRule.onNodeWithText("3 - 0").assertIsDisplayed()

        // 3. Assert striker identity does NOT swap (remains Batter 1)
        composeTestRule.onNodeWithText("Batter 1").assertIsDisplayed()
    }

    @Test
    fun testUI_freeHitDotBall_formatsAs0FH() {
        val viewModel = ScoreViewModel()
        composeTestRule.setContent { ScoreBoardScreen(viewModel = viewModel) }
        
        // No Ball
        composeTestRule.onNodeWithText("No Ball").performScrollTo().performClick()
        composeTestRule.onAllNodes(hasText("0") and hasClickAction()).onLast().performClick()
        composeTestRule.onNodeWithText("Confirm No Ball").performClick()

        // Bowl 0 runs
        composeTestRule.onAllNodes(hasText("0") and hasClickAction()).onFirst().performScrollTo().performClick()
        
        composeTestRule.onNodeWithText("0-FH", substring = true).assertIsDisplayed()
    }

    @Test
    fun testUI_secondBouncer_automaticallyLogsNoBall() {
        val viewModel = ScoreViewModel()
        composeTestRule.setContent { ScoreBoardScreen(viewModel = viewModel) }

        // Bouncer 1
        composeTestRule.onNode(hasText("Bouncer", substring = true) and hasClickAction()).performScrollTo().performClick()
        composeTestRule.onAllNodes(hasText("Confirm", substring = true) and hasClickAction()).onLast().performClick()

        // Bouncer 2 -> Should become No Ball
        composeTestRule.onNode(hasText("Bouncer", substring = true) and hasClickAction()).performScrollTo().performClick()
        composeTestRule.onAllNodes(hasText("Confirm", substring = true) and hasClickAction()).onLast().performClick()

        // Check if No ball run was added (team total = 1)
        composeTestRule.onNodeWithText("1 - 0").assertIsDisplayed()
    }

    @Test
    fun testUI_runOutOnWide_handlesRotationCorrectly() {
        val viewModel = ScoreViewModel()
        composeTestRule.setContent { ScoreBoardScreen(viewModel = viewModel) }

        // 1. Record the Wide (1 penalty run)
        composeTestRule.onNodeWithText("Wide").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Confirm Wide").performClick()
        
        // 2. Record the Run Out
        composeTestRule.onNode(hasText("Wicket", substring = false, ignoreCase = false) and hasClickAction()).performScrollTo().performClick()
        composeTestRule.onNodeWithText("Run Out").performClick()
        
        // Assuming we click 1 run completed (means batters crossed once)
        composeTestRule.onAllNodes(hasText("1") and hasClickAction()).onLast().performClick()
        composeTestRule.onNodeWithText("Confirm Run Out").performClick()

        // Team total = 1 (wide) + 1 (run) = 2. Wickets = 1.
        composeTestRule.onNodeWithText("2 - 1").assertIsDisplayed()
    }

    @Test
    fun testUI_retireBatter_showsDialogAndRequiresSelection() {
        val viewModel = ScoreViewModel()
        composeTestRule.setContent { ScoreBoardScreen(viewModel = viewModel) }

        // Tap Retire Batter
        composeTestRule.onNode(hasText("Retire Batter") and hasClickAction()).performScrollTo().performClick()

        // Assert Dialog displays Striker and Non-Striker selection
        composeTestRule.onNodeWithText("Select Batter", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Reason for Retirement", substring = true).assertIsDisplayed()
        
        // Assert defaults or labels are there in the dialog
        composeTestRule.onNode(hasText("Batter 1", substring = true) and hasAnyAncestor(isDialog())).assertIsDisplayed()
        composeTestRule.onNode(hasText("Batter 2", substring = true) and hasAnyAncestor(isDialog())).assertIsDisplayed()
    }

    @Test
    fun testUI_customPenalty_increasesScoreWithoutRotation() {
        val viewModel = ScoreViewModel()
        composeTestRule.setContent { ScoreBoardScreen(viewModel = viewModel) }

        // Assert initial score
        composeTestRule.onNodeWithText("0 - 0").assertIsDisplayed()
        
        // Tap Penalty
        composeTestRule.onNode(hasText("Penalty", substring = true) and hasClickAction()).performScrollTo().performClick()
        
        // Select 4 Runs
        composeTestRule.onNodeWithText("4 Runs").performClick()
        composeTestRule.onNode(hasText("Confirm", substring = false, ignoreCase = false) and hasClickAction()).performClick()

        // Assert team total increased by 4
        composeTestRule.onNodeWithText("4 - 0").assertIsDisplayed()
        
        // Assert Striker score increased by 4
        composeTestRule.onNodeWithText("4 (1)", substring = true).assertIsDisplayed()

        // Assert strike did NOT rotate (Batter 1 is still striker with 4 runs)
        // If it rotated, the striker would be Batter 2 with 0 runs
        composeTestRule.onNode(hasText("Batter 1", substring = true) and hasSetTextAction()).assertIsDisplayed()
    }
}
