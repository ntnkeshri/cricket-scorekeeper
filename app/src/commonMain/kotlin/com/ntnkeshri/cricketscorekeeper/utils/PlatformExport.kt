package com.ntnkeshri.cricketscorekeeper.utils

import com.ntnkeshri.cricketscorekeeper.models.MatchState
import com.ntnkeshri.cricketscorekeeper.viewmodel.ScoreViewModel

expect fun exportMatchScorecard(state: MatchState, viewModel: ScoreViewModel)
