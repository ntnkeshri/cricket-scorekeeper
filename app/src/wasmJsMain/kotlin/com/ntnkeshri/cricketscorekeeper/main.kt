package com.ntnkeshri.cricketscorekeeper

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.ntnkeshri.cricketscorekeeper.ui.screens.ScoreBoardScreen
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        ScoreBoardScreen()
    }
}
