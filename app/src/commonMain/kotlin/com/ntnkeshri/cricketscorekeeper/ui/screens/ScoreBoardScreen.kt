package com.ntnkeshri.cricketscorekeeper.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ntnkeshri.cricketscorekeeper.models.MatchState
import com.ntnkeshri.cricketscorekeeper.models.TossDecision
import com.ntnkeshri.cricketscorekeeper.ui.dialogs.*
import com.ntnkeshri.cricketscorekeeper.viewmodel.ScoreViewModel

@Composable
fun ScoreBoardScreen(viewModel: ScoreViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    if (state.showOversCapError) {
        val maxO = state.firstInningsBallsPlayed?.let { "${it / 6}.${it % 6}" } ?: "20"
        AlertDialog(
            onDismissRequest = { viewModel.dismissOversCapError() },
            title = { Text("Overs Limit Exceeded") },
            text = { Text("Cannot increase overs beyond 1st innings total ($maxO).") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissOversCapError() }) {
                    Text("OK")
                }
            }
        )
    }

    if (state.showTossDialog) {
        TossDialog(
            state = state,
            onConfirmToss = { winner: String, decision: TossDecision -> viewModel.recordToss(winner, decision) },
            onSkipToss = { battingTeam: String -> viewModel.skipTossAndSetBattingTeam(battingTeam) }
        )
    }

    WicketDialog(
        state = state,
        onDismiss = { viewModel.toggleWicketDialog(false) },
        onConfirmStandardWicket = { viewModel.addWicket() },
        onConfirmRunOut = { runsCompleted, isStrikerOut ->
            viewModel.addRunOut(runsCompleted, isStrikerOut)
        }
    )

    ByesDialog(
        state = state,
        onDismiss = { viewModel.toggleByesDialog(false) },
        onConfirmByes = { runs, isLegBye -> viewModel.addByes(runs, isLegBye) }
    )

    NoBallDialog(
        state = state,
        onDismiss = { viewModel.toggleNoBallDialog(false) },
        onConfirmNoBall = { runsOffBat -> viewModel.addNoBall(runsOffBat) }
    )

    WideDialog(
        state = state,
        onDismiss = { viewModel.toggleWideDialog(false) },
        onConfirmWide = { totalWideRuns -> viewModel.addWide(totalWideRuns) }
    )

    PenaltyDialog(
        state = state,
        onDismiss = { viewModel.togglePenaltyDialog(false) },
        onConfirm = { runs -> viewModel.addCustomPenalty(runs) }
    )

    RetireBatterDialog(
        state = state,
        onDismiss = { viewModel.toggleRetireBatterDialog(false) },
        onConfirmRetire = { isStriker, isHurt -> viewModel.retireBatter(isStriker, isHurt) }
    )

    BouncerPenaltyAlertDialog(
        state = state,
        onDismiss = { viewModel.dismissBouncerPenaltyAlert() }
    )

    DlsDialog(
        state = state,
        viewModel = viewModel
    )

    NewGameSummaryDialog(
        state = state,
        viewModel = viewModel
    )

    InningsCompleteDialog(
        state = state,
        viewModel = viewModel
    )

    HistoryScorecardDialog(
        state = state,
        viewModel = viewModel
    )

    RenameBowlerDialog(
        knownBowlers = state.knownBowlers.toList(),
        onDismiss = { viewModel.toggleRenameBowlerDialog(false) },
        onRename = { oldName, newName -> viewModel.renameBowler(oldName, newName) }
    )

    RenameBatterDialog(
        dismissedBatters = state.dismissedBatters,
        onDismiss = { viewModel.toggleRenameBatterDialog(false) },
        onRename = { oldName, newName -> viewModel.renameBatter(oldName, newName) }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(state = state, viewModel = viewModel)
        Spacer(modifier = Modifier.height(16.dp))

        MatchSettingsCard(state = state, viewModel = viewModel)
        Spacer(modifier = Modifier.height(16.dp))

        ScoreDisplayCard(state = state)
        Spacer(modifier = Modifier.height(16.dp))

        ActivePlayersCard(state = state, viewModel = viewModel)
        Spacer(modifier = Modifier.height(16.dp))

        CurrentOverCard(state = state)
        Spacer(modifier = Modifier.height(16.dp))

        ScoringControlPanel(state = state, viewModel = viewModel)
    }
}

@Composable
fun HeaderSection(state: MatchState, viewModel: ScoreViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Cricket Scorekeeper",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.toggleHistoryDialog(true) }) {
                Text("Scorecard")
            }
            if (state.isInningsComplete && state.targetScore == null) {
                Button(onClick = { viewModel.nextInnings() }) {
                    Text("2nd Innings")
                }
            }
        }
    }
}

@Composable
fun MatchSettingsCard(state: MatchState, viewModel: ScoreViewModel) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Match Setup & Overs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle Settings"
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = state.team1Name,
                        onValueChange = { viewModel.updateTeam1Name(it) },
                        label = { Text(if (state.innings % 2 != 0) "Team 1 (Batting)" else "Team 1 (Bowling)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.team2Name,
                        onValueChange = { viewModel.updateTeam2Name(it) },
                        label = { Text(if (state.innings % 2 != 0) "Team 2 (Bowling)" else "Team 2 (Batting)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.maxOversInput,
                            onValueChange = { viewModel.updateMaxOversRequest(it) },
                            label = { Text("Overs Limit (e.g. 20 or 19.3)") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    if (state.isDlsApplied) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("DLS Target Applied: ${state.targetScore}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            OutlinedButton(onClick = { viewModel.undoDls() }) {
                                Text("Undo DLS")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreDisplayCard(state: MatchState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${state.battingTeamName} (${state.innings}${if (state.innings == 1) "st" else "nd"} Innings)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${state.totalRuns} / ${state.wickets}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Overs: ${state.displayOvers} / ${state.maxOversInput}",
                style = MaterialTheme.typography.titleSmall
            )

            if (state.targetScore != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val runsNeeded = (state.targetScore - state.totalRuns).coerceAtLeast(0)
                val remainingBalls = (state.maxValidBallsLimit - state.validBalls).coerceAtLeast(0)
                val remainingOversDisplay = "${remainingBalls / 6}.${remainingBalls % 6}"

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Target: ${state.targetScore}" + if (state.isDlsApplied) " (DLS)" else "",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Need $runsNeeded runs from $remainingBalls balls ($remainingOversDisplay overs)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val crr = (state.currentRunRate * 100).toInt() / 100.0
                Text("CRR: $crr", style = MaterialTheme.typography.bodyMedium)
                Text("Extras: ${state.totalExtras}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ActivePlayersCard(state: MatchState, viewModel: ScoreViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Batting", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("* ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = state.striker.name,
                    onValueChange = { viewModel.updateStrikerName(it) },
                    label = { Text("Striker") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                val sr = (state.strikerStrikeRate * 10).toInt() / 10.0
                Text(
                    text = "${state.striker.runs} (${state.striker.balls}) | SR: $sr",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = state.nonStriker.name,
                    onValueChange = { viewModel.updateNonStrikerName(it) },
                    label = { Text("Non-Striker") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${state.nonStriker.runs} (${state.nonStriker.balls})",
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.toggleRetireBatterDialog(true) }) {
                    Text("Retire Batter")
                }
                if (state.dismissedBatters.isNotEmpty()) {
                    TextButton(onClick = { viewModel.toggleRenameBatterDialog(true) }) {
                        Text("Edit Dismissed")
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text("Bowling", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.currentBowlerName,
                    onValueChange = { viewModel.updateBowlerName(it) },
                    label = { Text("Current Bowler") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { viewModel.changeBowlerMidOver() }) {
                    Text("Change", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (state.knownBowlers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Select Previous Bowler:", style = MaterialTheme.typography.bodySmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                    items(state.knownBowlers.toList()) { bowler ->
                        FilterChip(
                            selected = state.currentBowlerName == bowler,
                            onClick = { viewModel.updateBowlerName(bowler) },
                            label = { Text(bowler) }
                        )
                    }
                }
                TextButton(
                    onClick = { viewModel.toggleRenameBowlerDialog(true) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Edit Bowler Names")
                }
            }
        }
    }
}

@Composable
fun CurrentOverCard(state: MatchState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("This Over", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(state.currentOverHistory) { ball ->
                    BallBadge(ball = ball)
                }
                if (state.currentOverHistory.isEmpty()) {
                    item {
                        Text("No deliveries yet in this over.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun BallBadge(ball: String) {
    val bgColor = when {
        ball == "W" || ball.endsWith("W") -> MaterialTheme.colorScheme.error
        ball == "4" || ball == "6" -> MaterialTheme.colorScheme.primary
        ball.startsWith("Wd") || ball.startsWith("Nb") -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val textColor = when {
        ball == "W" || ball.endsWith("W") -> MaterialTheme.colorScheme.onError
        ball == "4" || ball == "6" -> MaterialTheme.colorScheme.onPrimary
        ball.startsWith("Wd") || ball.startsWith("Nb") -> MaterialTheme.colorScheme.onTertiary
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ball,
            color = textColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ScoringControlPanel(state: MatchState, viewModel: ScoreViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Scoring Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (state.bouncersBowledThisOver > 0) {
                Text(
                    "Bouncers bowled this over: ${state.bouncersBowledThisOver} / 1",
                    color = if (state.bouncersBowledThisOver >= 1) MaterialTheme.colorScheme.error else Color.Unspecified,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.logBouncer() }, modifier = Modifier.weight(1f)) {
                    Text("Bouncer")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 1, 2).forEach { runs ->
                    Button(onClick = { viewModel.addValidDelivery(runs) }, modifier = Modifier.weight(1f)) {
                        Text("$runs")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 4, 6).forEach { runs ->
                    Button(onClick = { viewModel.addValidDelivery(runs) }, modifier = Modifier.weight(1f)) {
                        Text("$runs")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.toggleWideDialog(true) }, modifier = Modifier.weight(1f)) {
                    Text("Wide")
                }
                Button(onClick = { viewModel.toggleNoBallDialog(true) }, modifier = Modifier.weight(1f)) {
                    Text("No Ball")
                }
                Button(onClick = { viewModel.toggleByesDialog(true) }, modifier = Modifier.weight(1f)) {
                    Text("Byes")
                }
                OutlinedButton(
                    onClick = { viewModel.addDeadBall() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dead Ball")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.toggleWicketDialog(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Wicket")
                }
                OutlinedButton(
                    onClick = { viewModel.togglePenaltyDialog(true) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Penalty")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(
                    onClick = { viewModel.undoLastBall() },
                    enabled = state.ballHistory.isNotEmpty()
                ) {
                    Text("Undo Last Ball")
                }

                Button(
                    onClick = { viewModel.toggleNewGameDialog(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("End / New Game")
                }
            }
        }
    }
}
