package com.androidApp.cricketscorekeeper.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.androidApp.cricketscorekeeper.models.MatchState
import com.androidApp.cricketscorekeeper.ui.dialogs.*
import com.androidApp.cricketscorekeeper.viewmodel.ScoreViewModel

@Composable
fun ScoreBoardScreen(viewModel: ScoreViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    if (state.showOversCapError) {
        val maxO = state.firstInningsBallsPlayed?.let { "${it / 6}.${it % 6}" } ?: "20"
        Toast.makeText(context, "Cannot increase overs beyond 1st innings total ($maxO).", Toast.LENGTH_SHORT).show()
        viewModel.dismissOversCapError()
    }

    // Dialogs
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
    RetireBatterDialog(
        state = state,
        onDismiss = { viewModel.toggleRetireBatterDialog(false) },
        onConfirmRetire = { isStriker, isHurt -> viewModel.retireBatter(isStriker, isHurt) }
    )
    BouncerPenaltyAlertDialog(
        state = state,
        onDismiss = { viewModel.dismissBouncerPenaltyAlert() }
    )
    DlsDialog(state = state, viewModel = viewModel)
    NewGameSummaryDialog(state = state, viewModel = viewModel)
    InningsCompleteDialog(state = state, viewModel = viewModel)
    HistoryScorecardDialog(state = state, viewModel = viewModel)

    if (state.showRenameBowlerDialog) {
        RenameBowlerDialog(
            knownBowlers = state.knownBowlers.toList(),
            onDismiss = { viewModel.toggleRenameBowlerDialog(false) },
            onRename = { oldName, newName -> viewModel.renameBowler(oldName, newName) }
        )
    }

    if (state.showRenameBatterDialog) {
        RenameBatterDialog(
            dismissedBatters = state.dismissedBatters,
            onDismiss = { viewModel.toggleRenameBatterDialog(false) },
            onRename = { oldName, newName -> viewModel.renameBatter(oldName, newName) }
        )
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TeamHeader(state = state, viewModel = viewModel)

        Spacer(modifier = Modifier.height(12.dp))

        InningsHeader(state = state, viewModel = viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        ScoreHeader(state = state, viewModel = viewModel)

        Spacer(modifier = Modifier.height(8.dp))

        if (state.previousInningsSummary.isNotBlank()) {
            PreviousInningsCard(summary = state.previousInningsSummary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        BattingCard(state = state, viewModel = viewModel)

        Spacer(modifier = Modifier.height(8.dp))

        BowlingCard(state = state, viewModel = viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        ControlPanel(viewModel = viewModel)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TeamHeader(state: MatchState, viewModel: ScoreViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = state.team1Name,
            onValueChange = { viewModel.updateTeam1Name(it) },
            label = {
                Text(if (state.innings % 2 != 0) "Team 1 (Batting)" else "Team 1 (Bowling)")
            },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        OutlinedTextField(
            value = state.team2Name,
            onValueChange = { viewModel.updateTeam2Name(it) },
            label = {
                Text(if (state.innings % 2 != 0) "Team 2 (Bowling)" else "Team 2 (Batting)")
            },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }
}

@Composable
private fun InningsHeader(state: MatchState, viewModel: ScoreViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Innings ${state.innings} - ${state.battingTeamName}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = state.maxOversInput,
                onValueChange = { viewModel.updateMaxOversRequest(it) },
                label = { Text("Overs Limit") },
                modifier = Modifier.width(130.dp).padding(top = 8.dp),
                singleLine = true
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Button(onClick = { viewModel.toggleHistoryDialog(true) }) {
                Text("Scorecard")
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (state.targetScore == null) {
                Button(onClick = { viewModel.nextInnings() }) {
                    Text("Next Innings")
                }
            }
        }
    }
}

@Composable
private fun ScoreHeader(state: MatchState, viewModel: ScoreViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${state.totalRuns} - ${state.wickets}",
                            style = MaterialTheme.typography.displayLarge
                        )
                        if (state.isFreeHit) {
                            Surface(
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                Text(
                                    text = "FREE HIT",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Overs: ${state.displayOvers} / ${state.maxOversInput}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                if (state.isDlsApplied) {
                    OutlinedButton(onClick = { viewModel.undoDls() }) {
                        Text("Undo DLS", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            val targetScore = state.targetScore
            if (targetScore != null) {
                val runsNeeded = targetScore - state.totalRuns
                val ballsRemaining = state.maxValidBallsLimit - state.validBalls
                val dlsTag = if (state.isDlsApplied) " (DLS)" else ""

                Text(
                    text = "Target: $targetScore$dlsTag | Need $runsNeeded runs in $ballsRemaining balls",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PreviousInningsCard(summary: String) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("1st Innings Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle Summary"
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Text(summary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun BattingCard(state: MatchState, viewModel: ScoreViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = state.striker.name,
                        onValueChange = { viewModel.updateStrikerName(it) },
                        label = { Text("Striker *") },
                        modifier = Modifier.fillMaxWidth().padding(end = 4.dp),
                        singleLine = true
                    )
                    Text(
                        text = "${state.striker.runs} (${state.striker.balls})",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    OutlinedTextField(
                        value = state.nonStriker.name,
                        onValueChange = { viewModel.updateNonStrikerName(it) },
                        label = { Text("Non-Striker") },
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                        singleLine = true
                    )
                    Text(
                        text = "${state.nonStriker.runs} (${state.nonStriker.balls})",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { viewModel.toggleRetireBatterDialog(true) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Retire Batter", style = MaterialTheme.typography.bodySmall)
                }

                if (state.dismissedBatters.isNotEmpty() || state.retiredHurtBatters.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.toggleRenameBatterDialog(true) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Rename Past Batters", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BowlingCard(state: MatchState, viewModel: ScoreViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.currentBowlerName,
                    onValueChange = { viewModel.updateBowlerName(it) },
                    label = { Text("Active Bowler") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                TextButton(
                    onClick = { viewModel.changeBowlerMidOver() },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Injured / Switch")
                }
            }

            if (state.knownBowlers.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Previous Bowlers", style = MaterialTheme.typography.bodySmall)
                    TextButton(
                        onClick = { viewModel.toggleRenameBowlerDialog(true) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Rename", style = MaterialTheme.typography.bodySmall)
                    }
                }
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.knownBowlers.toList()) { bowler ->
                        AssistChip(
                            onClick = { viewModel.updateBowlerName(bowler) },
                            label = { Text(bowler) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Over History", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${state.currentOverRuns} Runs - ${state.currentOverTeamWickets} Wickets",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    state.currentOverHistory.forEach { ball ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (ball.startsWith("Nb") || ball.startsWith("Wd"))
                                        MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.secondaryContainer
                                )
                        ) {
                            Text(
                                text = ball,
                                color = if (ball.startsWith("Nb") || ball.startsWith("Wd"))
                                    MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Bouncers this over: ${state.bouncersBowledThisOver} / 1 limit",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                OutlinedButton(
                    onClick = { viewModel.logBouncer() },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Log Bouncer", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ControlPanel(viewModel: ScoreViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(0, 1, 2).forEach { runs ->
                Button(onClick = { viewModel.addValidDelivery(runs) }, modifier = Modifier.weight(1f)) {
                    Text("$runs")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(3, 4, 6).forEach { runs ->
                Button(onClick = { viewModel.addValidDelivery(runs) }, modifier = Modifier.weight(1f)) {
                    Text("$runs")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.toggleWideDialog(true) }, modifier = Modifier.weight(1f)) {
                Text("Wide")
            }
            Button(onClick = { viewModel.toggleNoBallDialog(true) }, modifier = Modifier.weight(1f)) {
                Text("No Ball")
            }
            Button(onClick = { viewModel.toggleByesDialog(true) }, modifier = Modifier.weight(1f)) {
                Text("Byes/LB")
            }
            Button(
                onClick = { viewModel.addDeadBall() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Dead")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.toggleWicketDialog(true) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Wicket")
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { viewModel.toggleNewGameDialog(true) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("End Match / New Game")
        }
    }
}
