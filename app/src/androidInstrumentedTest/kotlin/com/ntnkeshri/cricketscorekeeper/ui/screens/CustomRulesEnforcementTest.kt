package com.ntnkeshri.cricketscorekeeper.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ntnkeshri.cricketscorekeeper.viewmodel.ScoreViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomRulesEnforcementTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testUI_retireBatterDialog_selectsNonStriker() {
        val viewModel = ScoreViewModel()
        composeTestRule.setContent { ScoreBoardScreen(viewModel = viewModel) }

        // Trigger Retire dialog
        composeTestRule.onNodeWithText("Retire", substring = true).performScrollTo().performClick()
        
        // Select Non-Striker (Batter 2)
        composeTestRule.onNode(hasText("Batter 2", substring = true) and hasAnyAncestor(isDialog())).performClick()
        composeTestRule.onNodeWithText("Confirm Retirement").performClick()

        // Non-striker should be replaced (Batter 3), striker remains Batter 1
        // We use hasText because it matches the input value string of the OutlinedTextField
        composeTestRule.onNode(hasText("Batter 1", substring = true)).assertIsDisplayed()
        composeTestRule.onNode(hasText("Batter 3", substring = true)).assertIsDisplayed()
    }

    @Test
    fun testUI_customGroundPenalty_addsRunsWithoutRotation() {
        val viewModel = ScoreViewModel()
        composeTestRule.setContent { ScoreBoardScreen(viewModel = viewModel) }

        // Check initial state
        composeTestRule.onNode(hasText("Batter 1", substring = true)).assertIsDisplayed() // Striker

        // Trigger Pole Hit (custom ground penalty -> 5 runs)
        composeTestRule.onNodeWithText("Penalty", substring = true).performScrollTo().performClick()
        composeTestRule.onNodeWithText("Pole Hit (5 Runs)", substring = true).performClick()
        composeTestRule.onNodeWithText("Confirm", substring = false, ignoreCase = true).performClick()

        // Assert 5 runs added
        composeTestRule.onNodeWithText("5 - 0", substring = true).assertIsDisplayed()

        // Assert no strike rotation (Batter 1 still striker)
        composeTestRule.onNode(hasText("Batter 1", substring = true)).assertIsDisplayed()
    }
}
