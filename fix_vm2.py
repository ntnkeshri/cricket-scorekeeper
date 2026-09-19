import re

with open("app/src/commonMain/kotlin/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt", "r") as f:
    code = f.read()

methods = """
    fun recordToss(winner: String, decision: TossDecision) {
        _state.update { s ->
            val battingTeam = if (decision == TossDecision.BAT) winner else {
                if (winner == s.team1Name) s.team2Name else s.team1Name
            }
            val newTeam1 = battingTeam
            val newTeam2 = if (battingTeam == s.team1Name) s.team2Name else s.team1Name

            s.copy(
                tossWinner = winner,
                tossDecision = decision,
                showTossDialog = false,
                team1Name = newTeam1,
                team2Name = newTeam2
            )
        }
    }

    fun skipTossAndSetBattingTeam(battingTeamName: String) {
        _state.update { s ->
            val newTeam1 = battingTeamName
            val newTeam2 = if (battingTeamName == s.team1Name) s.team2Name else s.team1Name
            
            s.copy(
                tossWinner = null,
                tossDecision = null,
                showTossDialog = false,
                team1Name = newTeam1,
                team2Name = newTeam2
            )
        }
    }

    fun togglePenaltyDialog(show: Boolean) {
        _state.update { it.copy(showPenaltyDialog = show) }
    }

    fun addCustomPenalty(runs: Int) {
        if (_state.value.isInningsComplete) return
        _state.update { s ->
            val event = BallEvent(runsOffBat = runs, isValidBall = true, strikerName = s.striker.name)
            val notation = "P$runs"
            val isFour = runs == 4
            val isSix = runs == 6
            
            val next = s.copy(
                ballHistory = s.ballHistory + event,
                currentOverHistory = s.currentOverHistory + notation,
                currentOverRuns = s.currentOverRuns + runs,
                striker = s.striker.copy(
                    runs = s.striker.runs + runs,
                    balls = s.striker.balls + 1,
                    fours = s.striker.fours + if (isFour) 1 else 0,
                    sixes = s.striker.sixes + if (isSix) 1 else 0
                ),
                showPenaltyDialog = false,
                isFreeHit = false // Consumes a free hit
            )
            
            checkInningsComplete(handleEndOfOver(next))
        }
    }
}
"""

code = code.rsplit("}", 1)[0] + methods

with open("app/src/commonMain/kotlin/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt", "w") as f:
    f.write(code)


methods += """
    fun undoLastBall() {
        _state.update { currentState ->
            if (currentState.ballHistory.isEmpty()) return@update currentState
            
            val lastEvent = currentState.ballHistory.last()
            
            val newOverHistory = if (currentState.currentOverHistory.isNotEmpty()) {
                currentState.currentOverHistory.dropLast(1)
            } else {
                currentState.currentOverHistory
            }
            
            val newStriker = if (lastEvent.strikerName == currentState.striker.name) {
                currentState.striker.copy(
                    runs = currentState.striker.runs - lastEvent.runsOffBat,
                    balls = currentState.striker.balls - if (lastEvent.isValidBall) 1 else 0
                )
            } else {
                currentState.striker
            }
            
            currentState.copy(
                ballHistory = currentState.ballHistory.dropLast(1),
                currentOverHistory = newOverHistory,
                striker = newStriker
            )
        }
    }
"""

with open("app/src/commonMain/kotlin/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt", "r") as f:
    code = f.read()

code = code.rsplit("}", 1)[0] + methods

with open("app/src/commonMain/kotlin/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt", "w") as f:
    f.write(code)

