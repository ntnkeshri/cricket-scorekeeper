package com.androidApp.cricketscorekeeper

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
import com.androidApp.cricketscorekeeper.ui.screens.ScoreBoardScreen
import com.androidApp.cricketscorekeeper.viewmodel.ScoreViewModel
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

        // Stub external intents to prevent opening real apps during test
        intending(allOf(hasAction(Intent.ACTION_VIEW), hasType("application/pdf")))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        // Find "Download PDF Summary" and click
        composeTestRule.onNodeWithText("Download PDF Summary")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Download PDF Summary")
            .performClick()

        // Verify that an ACTION_VIEW intent with application/pdf MIME type was fired
        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasType("application/pdf"),
            ),
        )
    }
}
