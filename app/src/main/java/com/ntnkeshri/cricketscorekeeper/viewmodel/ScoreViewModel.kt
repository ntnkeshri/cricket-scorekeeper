package com.ntnkeshri.cricketscorekeeper.viewmodel

import androidx.lifecycle.ViewModel
import com.ntnkeshri.cricketscorekeeper.annotations.Requirement
import com.ntnkeshri.cricketscorekeeper.models.BatterRecord
import com.ntnkeshri.cricketscorekeeper.models.BowlerRecord
import com.ntnkeshri.cricketscorekeeper.models.MatchState
import com.ntnkeshri.cricketscorekeeper.models.OverRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Requirement(
    id = "ARCH-VM-01",
    description = "Central StateFlow viewmodel managing unidirectional match state mutations."
)
class ScoreViewModel : ViewModel() {
    private val _state = MutableStateFlow(MatchState())
    val state: StateFlow<MatchState> = _state.asStateFlow()

    private val dlsResources = mapOf(
        20 to 100.0, 19 to 96.1, 18 to 92.2, 17 to 88.2, 16 to 84.2,
        15 to 80.1, 14 to 75.9, 13 to 71.6, 12 to 67.3, 11 to 62.8,
        10 to 58.3, 9 to 53.6, 8 to 48.9, 7 to 44.1, 6 to 39.1,
        5 to 34.1, 4 to 28.9, 3 to 23.4, 2 to 17.5, 1 to 10.6,
        0 to 0.0
    )

    fun updateTeam1Name(name: String) {
        _state.update { it.copy(team1Name = name) }
    }

    fun updateTeam2Name(name: String) {
        _state.update { it.copy(team2Name = name) }
    }

    private fun getDlsResource(balls: Int): Double {
        val overs = balls / 6
        val rem = balls % 6
        val rOver = dlsResources[overs.coerceIn(0, 20)] ?: (overs * 5.0)
        val rNext = dlsResources[(overs + 1).coerceIn(0, 20)] ?: ((overs + 1) * 5.0)
        return rOver + ((rNext - rOver) * (rem / 6.0))
    }

    @Requirement(
        id = "ICC-DLS-01",
        description = "Duckworth-Lewis-Stern target score calculation for rain-affected or reduced overs matches."
    )
    fun calculateDlsTarget(scoreTeam1: Int, ballsTeam1: Int, ballsTeam2: Int): Int {
        val r1 = getDlsResource(ballsTeam1)
        val r2 = getDlsResource(ballsTeam2)
        if (r1 <= 0.0) return scoreTeam1 + 1
        return (scoreTeam1 * (r2 / r1)).toInt() + 1
    }

    @Requirement(
        id = "ICC-DLS-02",
        description = "Enforce overs cap in second innings and trigger DLS recalculation dialog on overs reduction."
    )
    fun updateMaxOversRequest(oversStr: String) {
        val s = _state.value

        if (oversStr.isNotEmpty() && !oversStr.matches(Regex("^\\d*(\\.[0-5]?)?$"))) return

        if (oversStr.isEmpty()) {
            _state.update { it.copy(maxOversInput = "") }
            return
        }

        val parts = oversStr.split(".")
        val o = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val b = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val newMaxBalls = (o * 6) + b

        if (s.innings == 2) {
            val maxAllowedBalls = s.firstInningsBallsPlayed ?: s.maxValidBallsLimit

            if (newMaxBalls > maxAllowedBalls) {
                _state.update { it.copy(showOversCapError = true) }
                return
            }

            if (newMaxBalls < s.maxValidBallsLimit) {
                _state.update { it.copy(pendingNewMaxOversInput = oversStr, showDlsDialog = true) }
            } else {
                _state.update { it.copy(maxOversInput = oversStr) }
            }
        } else {
            _state.update { it.copy(maxOversInput = oversStr) }
        }
    }

    fun dismissOversCapError() {
        _state.update { it.copy(showOversCapError = false) }
    }

    fun handleDlsDecision(applyDls: Boolean) {
        _state.update { s ->
            val newInput = s.pendingNewMaxOversInput ?: s.maxOversInput
            val parts = newInput.split(".")
            val o = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val b = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val newBalls = (o * 6) + b

            if (applyDls && s.firstInningsTotalRuns != null && s.firstInningsBallsPlayed != null) {
                val newTarget = calculateDlsTarget(s.firstInningsTotalRuns, s.firstInningsBallsPlayed, newBalls)
                s.copy(
                    maxOversInput = newInput,
                    targetScore = newTarget,
                    isDlsApplied = true,
                    showDlsDialog = false,
                    pendingNewMaxOversInput = null
                )
            } else {
                s.copy(
                    maxOversInput = newInput,
                    showDlsDialog = false,
                    pendingNewMaxOversInput = null
                )
            }
        }
    }

    fun undoDls() {
        _state.update { s ->
            s.copy(
                maxOversInput = s.originalMaxOversInput ?: s.maxOversInput,
                targetScore = s.originalTargetScore ?: s.targetScore,
                isDlsApplied = false
            )
        }
    }

    private fun swapStrikers(currentState: MatchState): MatchState {
        return currentState.copy(
            striker = currentState.nonStriker,
            nonStriker = currentState.striker
        )
    }

    private fun checkInningsComplete(currentState: MatchState): MatchState {
        if (currentState.isInningsComplete) {
            return currentState.copy(showInningsCompleteDialog = true)
        }
        return currentState
    }

    @Requirement(
        id = "ICC-LAW-17",
        description = "Over completion, striker end swap, bowler history logging, and Maiden Over detection."
    )
    private fun handleEndOfOver(currentState: MatchState): MatchState {
        return if (currentState.validBalls % 6 == 0 && currentState.validBalls > 0) {
            var bowlerName = currentState.currentBowlerName.trim()
            var newUnknownCount = currentState.unknownBowlerCount

            if (bowlerName.isEmpty()) {
                newUnknownCount += 1
                bowlerName = "Unknown Bowler $newUnknownCount"
            }

            val validCount = currentState.currentOverHistory.count { !it.startsWith("Wd") && !it.startsWith("Nb") && it != "DB" }

            val finishedOver = OverRecord(
                overNumber = (currentState.validBalls - 1) / 6 + 1,
                bowlerName = bowlerName,
                balls = currentState.currentOverHistory,
                runsConceded = currentState.currentOverRuns,
                teamWickets = currentState.currentOverTeamWickets,
                bowlerWickets = currentState.currentOverBowlerWickets,
                validBallsBowled = validCount
            )

            swapStrikers(currentState).copy(
                inningsHistory = currentState.inningsHistory + finishedOver,
                currentOverHistory = emptyList(),
                currentOverRuns = 0,
                currentOverTeamWickets = 0,
                currentOverBowlerWickets = 0,
                bouncersBowledThisOver = 0,
                knownBowlers = currentState.knownBowlers + bowlerName,
                unknownBowlerCount = newUnknownCount,
                currentBowlerName = ""
            )
        } else {
            currentState
        }
    }

    fun changeBowlerMidOver() {
        if (_state.value.isInningsComplete) return
        _state.update { s ->
            if (s.currentOverHistory.isEmpty()) return@update s

            var bowlerName = s.currentBowlerName.trim()
            var newUnknownCount = s.unknownBowlerCount

            if (bowlerName.isEmpty()) {
                newUnknownCount += 1
                bowlerName = "Unknown Bowler $newUnknownCount"
            }

            val validCount = s.currentOverHistory.count { !it.startsWith("Wd") && !it.startsWith("Nb") && it != "DB" }

            val partialOver = OverRecord(
                overNumber = (s.validBalls - 1) / 6 + 1,
                bowlerName = bowlerName,
                balls = s.currentOverHistory,
                runsConceded = s.currentOverRuns,
                teamWickets = s.currentOverTeamWickets,
                bowlerWickets = s.currentOverBowlerWickets,
                validBallsBowled = validCount
            )

            s.copy(
                inningsHistory = s.inningsHistory + partialOver,
                currentOverHistory = emptyList(),
                currentOverRuns = 0,
                currentOverTeamWickets = 0,
                currentOverBowlerWickets = 0,
                bouncersBowledThisOver = 0,
                knownBowlers = s.knownBowlers + bowlerName,
                unknownBowlerCount = newUnknownCount,
                currentBowlerName = ""
            )
        }
    }

    @Requirement(
        id = "ICC-LAW-18",
        description = "Legal delivery run logging, strike rotation on odd runs, and consuming Free Hit status."
    )
    fun addValidDelivery(runs: Int) {
        if (_state.value.isInningsComplete) return
        _state.update { s ->
            val isFour = runs == 4
            val isSix = runs == 6

            var next = s.copy(
                totalRuns = s.totalRuns + runs,
                validBalls = s.validBalls + 1,
                currentOverHistory = s.currentOverHistory + runs.toString(),
                currentOverRuns = s.currentOverRuns + runs,
                striker = s.striker.copy(
                    runs = s.striker.runs + runs,
                    balls = s.striker.balls + 1,
                    fours = s.striker.fours + if (isFour) 1 else 0,
                    sixes = s.striker.sixes + if (isSix) 1 else 0
                ),
                isFreeHit = false
            )

            if (runs % 2 != 0) {
                next = swapStrikers(next)
            }

            checkInningsComplete(handleEndOfOver(next))
        }
    }

    @Requirement(
        id = "ICC-LAW-23-24",
        description = "Byes and Leg Byes extras tracking, 0 bowler runs conceded, and strike rotation."
    )
    fun addByes(runs: Int, isLegBye: Boolean) {
        if (_state.value.isInningsComplete) return
        _state.update { s ->
            val notation = if (isLegBye) "${runs}LB" else "${runs}B"

            var next = s.copy(
                totalRuns = s.totalRuns + runs,
                totalExtras = s.totalExtras + runs,
                validBalls = s.validBalls + 1,
                currentOverHistory = s.currentOverHistory + notation,
                striker = s.striker.copy(
                    balls = s.striker.balls + 1
                ),
                isFreeHit = false,
                showByesDialog = false
            )

            if (runs % 2 != 0) {
                next = swapStrikers(next)
            }

            checkInningsComplete(handleEndOfOver(next))
        }
    }

    @Requirement(
        id = "ICC-LAW-21-19",
        description = "No Ball penalty, runs off bat, and Free Hit status activation for next delivery."
    )
    fun addNoBall(runsOffBat: Int) {
        if (_state.value.isInningsComplete) return
        _state.update { s ->
            val totalRunsThisBall = 1 + runsOffBat
            val isFour = runsOffBat == 4
            val isSix = runsOffBat == 6
            val notation = if (runsOffBat > 0) "Nb+$runsOffBat" else "Nb"

            var next = s.copy(
                totalRuns = s.totalRuns + totalRunsThisBall,
                totalExtras = s.totalExtras + 1,
                currentOverHistory = s.currentOverHistory + notation,
                currentOverRuns = s.currentOverRuns + totalRunsThisBall,
                striker = s.striker.copy(
                    runs = s.striker.runs + runsOffBat,
                    balls = s.striker.balls + 1,
                    fours = s.striker.fours + if (isFour) 1 else 0,
                    sixes = s.striker.sixes + if (isSix) 1 else 0
                ),
                isFreeHit = true,
                showNoBallDialog = false
            )

            if (runsOffBat % 2 != 0) {
                next = swapStrikers(next)
            }

            checkInningsComplete(next)
        }
    }

    @Requirement(
        id = "ICC-LAW-22",
        description = "Wide ball penalty and additional runs off wide delivery."
    )
    fun addWide(totalWideRuns: Int) {
        if (_state.value.isInningsComplete) return
        _state.update { s ->
            val additionalWideRuns = (totalWideRuns - 1).coerceAtLeast(0)
            val notation = if (additionalWideRuns > 0) "Wd+$additionalWideRuns" else "Wd"

            var next = s.copy(
                totalRuns = s.totalRuns + totalWideRuns,
                totalExtras = s.totalExtras + totalWideRuns,
                currentOverHistory = s.currentOverHistory + notation,
                currentOverRuns = s.currentOverRuns + totalWideRuns,
                showWideDialog = false
            )

            if (totalWideRuns % 2 != 0) {
                next = swapStrikers(next)
            }

            checkInningsComplete(next)
        }
    }

    @Requirement(
        id = "ICC-LAW-33-39",
        description = "Standard wickets (Bowled, Caught, LBW, Stumped) credited to team and bowler."
    )
    fun addWicket() {
        if (_state.value.isInningsComplete) return
        _state.update { s ->
            val dismissed = s.striker.copy(balls = s.striker.balls + 1, isOut = true)
            val isAllOut = s.wickets + 1 >= 10

            val next = s.copy(
                wickets = s.wickets + 1,
                validBalls = s.validBalls + 1,
                currentOverHistory = s.currentOverHistory + "W",
                currentOverTeamWickets = s.currentOverTeamWickets + 1,
                currentOverBowlerWickets = s.currentOverBowlerWickets + 1,
                dismissedBatters = s.dismissedBatters + dismissed,
                striker = if (isAllOut) BatterRecord("") else BatterRecord("Batter ${s.wickets + s.retiredHurtBatters.size + 3}"),
                isFreeHit = false,
                showWicketDialog = false
            )
            checkInningsComplete(handleEndOfOver(next))
        }
    }

    @Requirement(
        id = "ICC-LAW-38",
        description = "Run Out dismissal logic, strike rotation, and 0 bowler credit."
    )
    fun addRunOut(runsCompleted: Int, isStrikerOut: Boolean) {
        if (_state.value.isInningsComplete) return
        _state.update { s ->
            val isAllOut = s.wickets + 1 >= 10
            val ballNotation = if (runsCompleted > 0) "${runsCompleted}W" else "W"

            val dismissedBatter: BatterRecord
            val survivingBatter: BatterRecord

            if (isStrikerOut) {
                dismissedBatter = s.striker.copy(
                    runs = s.striker.runs + runsCompleted,
                    balls = s.striker.balls + 1,
                    isOut = true
                )
                survivingBatter = s.nonStriker
            } else {
                dismissedBatter = s.nonStriker.copy(isOut = true)
                survivingBatter = s.striker.copy(
                    runs = s.striker.runs + runsCompleted,
                    balls = s.striker.balls + 1
                )
            }

            val incomingBatter = if (isAllOut) BatterRecord("") else BatterRecord("Batter ${s.wickets + s.retiredHurtBatters.size + 3}")

            val newStriker: BatterRecord
            val newNonStriker: BatterRecord

            if (isStrikerOut) {
                if (runsCompleted % 2 == 0) {
                    newStriker = incomingBatter
                    newNonStriker = survivingBatter
                } else {
                    newStriker = survivingBatter
                    newNonStriker = incomingBatter
                }
            } else {
                if (runsCompleted % 2 == 0) {
                    newStriker = survivingBatter
                    newNonStriker = incomingBatter
                } else {
                    newStriker = incomingBatter
                    newNonStriker = survivingBatter
                }
            }

            val next = s.copy(
                totalRuns = s.totalRuns + runsCompleted,
                wickets = s.wickets + 1,
                validBalls = s.validBalls + 1,
                currentOverHistory = s.currentOverHistory + ballNotation,
                currentOverRuns = s.currentOverRuns + runsCompleted,
                currentOverTeamWickets = s.currentOverTeamWickets + 1,
                dismissedBatters = s.dismissedBatters + dismissedBatter,
                striker = newStriker,
                nonStriker = newNonStriker,
                isFreeHit = false,
                showWicketDialog = false
            )

            checkInningsComplete(handleEndOfOver(next))
        }
    }

    @Requirement(
        id = "ICC-LAW-25-4",
        description = "Retired Hurt (no fallen wicket) vs Retired Out (team wicket, 0 bowler credit) logic."
    )
    fun retireBatter(isStriker: Boolean, isHurt: Boolean) {
        _state.update { s ->
            val targetBatter = if (isStriker) s.striker else s.nonStriker
            if (targetBatter.name.isBlank()) return@update s

            if (isHurt) {
                val updatedRetired = s.retiredHurtBatters + targetBatter.copy(isOut = false)
                val incoming = BatterRecord("Batter ${s.wickets + updatedRetired.size + 2}")

                s.copy(
                    retiredHurtBatters = updatedRetired,
                    striker = if (isStriker) incoming else s.striker,
                    nonStriker = if (isStriker) s.nonStriker else incoming,
                    showRetireBatterDialog = false
                )
            } else {
                val dismissed = targetBatter.copy(isOut = true)
                val isAllOut = s.wickets + 1 >= 10
                val incoming = if (isAllOut) BatterRecord("") else BatterRecord("Batter ${s.wickets + s.retiredHurtBatters.size + 3}")

                val next = s.copy(
                    wickets = s.wickets + 1,
                    currentOverTeamWickets = s.currentOverTeamWickets + 1,
                    dismissedBatters = s.dismissedBatters + dismissed,
                    striker = if (isStriker) incoming else s.striker,
                    nonStriker = if (isStriker) s.nonStriker else incoming,
                    showRetireBatterDialog = false
                )

                checkInningsComplete(next)
            }
        }
    }

    @Requirement(
        id = "ICC-LAW-41-6",
        description = "Limit short-pitched deliveries per over and enforce No Ball penalty + Free Hit on 2nd bouncer."
    )
    fun logBouncer() {
        if (_state.value.isInningsComplete) return
        _state.update { s ->
            if (s.bouncersBowledThisOver == 0) {
                s.copy(bouncersBowledThisOver = 1)
            } else {
                val notation = "Nb(Bouncer)"
                s.copy(
                    bouncersBowledThisOver = s.bouncersBowledThisOver + 1,
                    totalRuns = s.totalRuns + 1,
                    totalExtras = s.totalExtras + 1,
                    currentOverHistory = s.currentOverHistory + notation,
                    currentOverRuns = s.currentOverRuns + 1,
                    isFreeHit = true,
                    showBouncerPenaltyAlert = true
                )
            }
        }
    }

    fun dismissBouncerPenaltyAlert() {
        _state.update { it.copy(showBouncerPenaltyAlert = false) }
    }

    fun addDeadBall() {
        if (_state.value.isInningsComplete) return
        _state.update { s ->
            s.copy(
                currentOverHistory = s.currentOverHistory + "DB"
            )
        }
    }

    fun renameBowler(oldName: String, newName: String) {
        _state.update { s ->
            val cleanNewName = newName.trim()
            val updatedHistory = s.inningsHistory.map {
                if (it.bowlerName == oldName) it.copy(bowlerName = cleanNewName) else it
            }
            val updatedKnown = s.knownBowlers.map {
                if (it == oldName) cleanNewName else it
            }.toSet()

            val updatedCurrent = if (s.currentBowlerName == oldName) cleanNewName else s.currentBowlerName

            s.copy(
                inningsHistory = updatedHistory,
                knownBowlers = updatedKnown,
                currentBowlerName = updatedCurrent
            )
        }
    }

    fun renameBatter(oldName: String, newName: String) {
        _state.update { s ->
            val cleanNewName = newName.trim()
            if (cleanNewName.isEmpty() || cleanNewName == oldName) return@update s

            val updatedDismissed = s.dismissedBatters.map {
                if (it.name == oldName) it.copy(name = cleanNewName) else it
            }
            val updatedRetired = s.retiredHurtBatters.map {
                if (it.name == oldName) it.copy(name = cleanNewName) else it
            }
            val updatedStriker = if (s.striker.name == oldName) s.striker.copy(name = cleanNewName) else s.striker
            val updatedNonStriker = if (s.nonStriker.name == oldName) s.nonStriker.copy(name = cleanNewName) else s.nonStriker

            s.copy(
                dismissedBatters = updatedDismissed,
                retiredHurtBatters = updatedRetired,
                striker = updatedStriker,
                nonStriker = updatedNonStriker
            )
        }
    }

    fun getAggregatedBowlerStats(s: MatchState = _state.value): List<BowlerRecord> {
        val statsMap = mutableMapOf<String, BowlerRecord>()

        s.inningsHistory.forEach { over ->
            val bName = over.bowlerName
            val current = statsMap[bName] ?: BowlerRecord(bName)
            val isMaiden = over.validBallsBowled == 6 && over.runsConceded == 0
            statsMap[bName] = current.copy(
                validBalls = current.validBalls + over.validBallsBowled,
                runsConceded = current.runsConceded + over.runsConceded,
                wickets = current.wickets + over.bowlerWickets,
                maidens = current.maidens + if (isMaiden) 1 else 0
            )
        }

        if (s.currentOverHistory.isNotEmpty()) {
            val bName = s.currentBowlerName.ifBlank { "Unknown Bowler (Active)" }
            val current = statsMap[bName] ?: BowlerRecord(bName)
            val activeValidBalls = s.currentOverHistory.count { !it.startsWith("Wd") && !it.startsWith("Nb") && it != "DB" }
            statsMap[bName] = current.copy(
                validBalls = current.validBalls + activeValidBalls,
                runsConceded = current.runsConceded + s.currentOverRuns,
                wickets = current.wickets + s.currentOverBowlerWickets
            )
        }
        return statsMap.values.toList()
    }

    fun updateBowlerName(name: String) { _state.update { it.copy(currentBowlerName = name) } }
    fun updateStrikerName(name: String) { _state.update { it.copy(striker = it.striker.copy(name = name)) } }
    fun updateNonStrikerName(name: String) { _state.update { it.copy(nonStriker = it.nonStriker.copy(name = name)) } }
    fun toggleHistoryDialog(show: Boolean) { _state.update { it.copy(showHistoryDialog = show) } }
    fun dismissInningsCompleteDialog() { _state.update { it.copy(showInningsCompleteDialog = false) } }
    fun toggleNewGameDialog(show: Boolean) { _state.update { it.copy(showNewGameDialog = show) } }
    fun toggleRenameBowlerDialog(show: Boolean) { _state.update { it.copy(showRenameBowlerDialog = show) } }
    fun toggleRenameBatterDialog(show: Boolean) { _state.update { it.copy(showRenameBatterDialog = show) } }
    fun toggleWicketDialog(show: Boolean) { _state.update { it.copy(showWicketDialog = show) } }
    fun toggleByesDialog(show: Boolean) { _state.update { it.copy(showByesDialog = show) } }
    fun toggleNoBallDialog(show: Boolean) { _state.update { it.copy(showNoBallDialog = show) } }
    fun toggleWideDialog(show: Boolean) { _state.update { it.copy(showWideDialog = show) } }
    fun toggleRetireBatterDialog(show: Boolean) { _state.update { it.copy(showRetireBatterDialog = show) } }

    fun nextInnings() {
        _state.update {
            val allBatters = it.dismissedBatters + it.retiredHurtBatters + it.striker + it.nonStriker
            val batterStatsStr = allBatters.filter { b -> b.name.isNotBlank() }
                .joinToString("\n") { b ->
                    val status = if (b.isOut) "" else if (it.retiredHurtBatters.contains(b)) " (Retired Hurt)" else " *"
                    "${b.name}$status: ${b.runs} (${b.balls}) | 4s: ${b.fours} | 6s: ${b.sixes}"
                }

            val bowlerStatsStr = getAggregatedBowlerStats(it).joinToString("\n") { b ->
                "${b.name}: ${b.displayFigures}"
            }

            val summary = """
                Innings ${it.innings} (${it.battingTeamName}): ${it.totalRuns}/${it.wickets} in ${it.displayOvers} overs
                Extras: ${it.totalExtras}
                
                Batting:
                $batterStatsStr
                
                Bowling:
                $bowlerStatsStr
            """.trimIndent()

            val ballsPlayed = it.validBalls
            val formattedOvers = "${ballsPlayed / 6}" + if (ballsPlayed % 6 > 0) ".${ballsPlayed % 6}" else ""

            MatchState(
                team1Name = it.team1Name,
                team2Name = it.team2Name,
                innings = it.innings + 1,
                maxOversInput = formattedOvers,
                firstInningsBallsPlayed = ballsPlayed,
                firstInningsTotalRuns = it.totalRuns,
                targetScore = it.totalRuns + 1,
                originalTargetScore = it.totalRuns + 1,
                originalMaxOversInput = formattedOvers,
                previousInningsSummary = summary,
                unknownBowlerCount = 0
            )
        }
    }

    fun resetMatch() {
        _state.value = MatchState()
    }
}
