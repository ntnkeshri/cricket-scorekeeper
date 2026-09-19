package com.ntnkeshri.cricketscorekeeper.utils

import com.ntnkeshri.cricketscorekeeper.MainActivity
import com.ntnkeshri.cricketscorekeeper.models.MatchState
import com.ntnkeshri.cricketscorekeeper.viewmodel.ScoreViewModel

actual fun exportMatchScorecard(state: MatchState, viewModel: ScoreViewModel) {
    MainActivity.appContext?.let { context ->
        generateAndSavePdf(context, state, viewModel)
    }
}
