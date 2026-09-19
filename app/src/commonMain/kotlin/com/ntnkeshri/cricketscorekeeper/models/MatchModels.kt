package com.ntnkeshri.cricketscorekeeper.models

import kotlinx.serialization.Serializable

@Serializable
data class OverRecord(
    val overNumber: Int,
    val bowlerName: String,
    val balls: List<String>,
    val runsConceded: Int,
    val teamWickets: Int,
    val bowlerWickets: Int,
    val validBallsBowled: Int
)

@Serializable
data class BatterRecord(
    val name: String,
    val runs: Int = 0,
    val balls: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val isOut: Boolean = false
) {
    val strikeRate: Double get() = if (balls == 0) 0.0 else (runs.toDouble() / balls) * 100
}

@Serializable
data class BowlerRecord(
    val name: String,
    val validBalls: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0,
    val maidens: Int = 0
) {
    val completedOvers: Int get() = validBalls / 6
    val extraBalls: Int get() = validBalls % 6
    val displayOvers: String get() = "$completedOvers.$extraBalls"
    val displayFigures: String get() = "$displayOvers Ov | $maidens M | $runsConceded R | $wickets W"
}

@Serializable
data class BallEvent(
    val runsOffBat: Int = 0,
    val extras: Int = 0,
    val isWicket: Boolean = false,
    val isValidBall: Boolean = true,
    val strikerName: String = ""
)

enum class TossDecision { BAT, BOWL }

@Serializable
data class MatchState(
    val team1Name: String = "Team 1",
    val team2Name: String = "Team 2",
    val innings: Int = 1,
    val ballHistory: List<BallEvent> = emptyList(),

    // Toss
    val tossWinner: String? = null,
    val tossDecision: TossDecision? = null,
    val showTossDialog: Boolean = true,

    val maxOversInput: String = "20",
    val targetScore: Int? = null,
    val previousInningsSummary: String = "",

    // Free Hit & ICC Rules
    val isFreeHit: Boolean = false,
    val bouncersBowledThisOver: Int = 0,
    val retiredHurtBatters: List<BatterRecord> = emptyList(),

    // DLS & Second Innings Tracking
    val firstInningsBallsPlayed: Int? = null,
    val firstInningsTotalRuns: Int? = null,
    val originalTargetScore: Int? = null,
    val originalMaxOversInput: String? = null,
    val pendingNewMaxOversInput: String? = null,
    val isDlsApplied: Boolean = false,
    val showDlsDialog: Boolean = false,
    val showOversCapError: Boolean = false,

    val striker: BatterRecord = BatterRecord("Batter 1"),
    val nonStriker: BatterRecord = BatterRecord("Batter 2"),
    val dismissedBatters: List<BatterRecord> = emptyList(),

    val currentBowlerName: String = "",
    val knownBowlers: Set<String> = setOf(),
    val unknownBowlerCount: Int = 0,
    val currentOverHistory: List<String> = emptyList(),
    val currentOverRuns: Int = 0,
    val currentOverTeamWickets: Int = 0,
    val currentOverBowlerWickets: Int = 0,
    val inningsHistory: List<OverRecord> = emptyList(),

    val showHistoryDialog: Boolean = false,
    val showInningsCompleteDialog: Boolean = false,
    val showNewGameDialog: Boolean = false,
    val showRenameBowlerDialog: Boolean = false,
    val showRenameBatterDialog: Boolean = false,
    val showWicketDialog: Boolean = false,
    val showByesDialog: Boolean = false,
    val showNoBallDialog: Boolean = false,
    val showWideDialog: Boolean = false,
    val showRetireBatterDialog: Boolean = false,
    val showBouncerPenaltyAlert: Boolean = false,
    val showPenaltyDialog: Boolean = false
) {
    val totalRuns: Int get() = ballHistory.sumOf { it.runsOffBat + it.extras }
    val wickets: Int get() = ballHistory.count { it.isWicket }
    val validBalls: Int get() = ballHistory.count { it.isValidBall }
    val totalExtras: Int get() = ballHistory.sumOf { it.extras }

    val currentRunRate: Double
        get() {
            val legalBalls = validBalls
            if (legalBalls == 0) return 0.0
            val overs = (legalBalls / 6) + ((legalBalls % 6) / 6.0)
            return totalRuns / overs
        }

    val strikerStrikeRate: Double
        get() {
            val strikerBalls = ballHistory.filter { it.strikerName == striker.name && it.isValidBall }
            if (strikerBalls.isEmpty()) return 0.0
            val runs = strikerBalls.sumOf { it.runsOffBat }
            return (runs.toDouble() / strikerBalls.size) * 100
        }

    val battingTeamName: String
        get() = if (innings % 2 != 0) team1Name else team2Name

    val bowlingTeamName: String
        get() = if (innings % 2 != 0) team2Name else team1Name

    val maxValidBallsLimit: Int
        get() {
            val parts = maxOversInput.split(".")
            val overs = parts.getOrNull(0)?.toIntOrNull() ?: 20
            val balls = parts.getOrNull(1)?.toIntOrNull() ?: 0
            return (overs * 6) + balls
        }

    val completedOvers: Int get() = validBalls / 6
    val currentOverBalls: Int get() = validBalls % 6
    val displayOvers: String get() = "$completedOvers.$currentOverBalls"

    val isInningsComplete: Boolean
        get() = wickets >= 10 ||
                validBalls >= maxValidBallsLimit ||
                (targetScore != null && totalRuns >= targetScore)
}
