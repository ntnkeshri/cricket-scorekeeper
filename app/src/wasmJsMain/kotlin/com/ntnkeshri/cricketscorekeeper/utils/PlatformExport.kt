package com.ntnkeshri.cricketscorekeeper.utils

import com.ntnkeshri.cricketscorekeeper.models.MatchState
import com.ntnkeshri.cricketscorekeeper.viewmodel.ScoreViewModel

actual fun exportMatchScorecard(state: MatchState, viewModel: ScoreViewModel) {
    // Stub for WebAssembly - PDF generation could be handled here via JS interop
    println("Exporting scorecard for match...")
}
