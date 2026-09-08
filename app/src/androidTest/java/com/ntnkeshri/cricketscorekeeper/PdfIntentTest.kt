package com.ntnkeshri.cricketscorekeeper

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ntnkeshri.cricketscorekeeper.ui.screens.ScoreBoardScreen
import com.ntnkeshri.cricketscorekeeper.viewmodel.ScoreViewModel
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfIntentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testDownloadPdfFiresActionViewIntentWithPdfMimeType() {
        val viewModel = ScoreViewModel()
        viewModel.toggleNewGameDialog(show = true)

        composeTestRule.setContent {
            ScoreBoardScreen(viewModel = viewModel)
        }

        intending(allOf(hasAction(Intent.ACTION_VIEW), hasType("application/pdf")))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        composeTestRule.onNodeWithText("Download PDF Summary")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Download PDF Summary")
            .performClick()

        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasType("application/pdf"),
            ),
        )
    }
}
