package com.ntnkeshri.cricketscorekeeper.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.ntnkeshri.cricketscorekeeper.models.BatterRecord
import com.ntnkeshri.cricketscorekeeper.models.MatchState
import com.ntnkeshri.cricketscorekeeper.utils.generateAndSavePdf
import com.ntnkeshri.cricketscorekeeper.viewmodel.ScoreViewModel

@Composable
fun RenameBowlerDialog(
    knownBowlers: List<String>,
    onDismiss: () -> Unit,
    onRename: (String, String) -> Unit
) {
    val editMap = remember { mutableStateMapOf<String, String>().apply { knownBowlers.forEach { put(it, it) } } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Bowler Names") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                items(knownBowlers) { oldName ->
                    OutlinedTextField(
                        value = editMap[oldName] ?: "",
                        onValueChange = { editMap[oldName] = it },
                        label = { Text("Rename $oldName") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                editMap.forEach { (oldName, newName) ->
                    if (newName.isNotBlank() && newName != oldName) {
                        onRename(oldName, newName)
                    }
                }
                onDismiss()
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RenameBatterDialog(
    dismissedBatters: List<BatterRecord>,
    onDismiss: () -> Unit,
    onRename: (String, String) -> Unit
) {
    val batterNames = dismissedBatters.map { it.name }.distinct().filter { it.isNotBlank() }
    val editMap = remember { mutableStateMapOf<String, String>().apply { batterNames.forEach { put(it, it) } } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Dismissed Batters") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                items(batterNames) { oldName ->
                    OutlinedTextField(
                        value = editMap[oldName] ?: "",
                        onValueChange = { editMap[oldName] = it },
                        label = { Text("Rename $oldName") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                editMap.forEach { (oldName, newName) ->
                    if (newName.isNotBlank() && newName != oldName) {
                        onRename(oldName, newName)
                    }
                }
                onDismiss()
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun WicketDialog(
    state: MatchState,
    onDismiss: () -> Unit,
    onConfirmStandardWicket: () -> Unit,
    onConfirmRunOut: (runsCompleted: Int, isStrikerOut: Boolean) -> Unit
) {
    if (!state.showWicketDialog) return

    var isRunOut by remember { mutableStateOf(state.isFreeHit) }
    var runsCompleted by remember { mutableIntStateOf(0) }
    var isStrikerOut by remember { mutableStateOf(true) }

    val strikerName = state.striker.name.ifBlank { "Striker" }
    val nonStrikerName = state.nonStriker.name.ifBlank { "Non-Striker" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Wicket") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (state.isFreeHit) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "FREE HIT ACTIVE: Standard dismissals (Bowled, Caught, LBW, Stumped) are disabled under ICC Rule 21.19. Only Run Out is active.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Text("Dismissal Type", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isRunOut,
                        onClick = { if (!state.isFreeHit) isRunOut = false },
                        enabled = !state.isFreeHit,
                        label = { Text("Standard (Bowled/Caught/LBW)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isRunOut,
                        onClick = { isRunOut = true },
                        label = { Text("Run Out") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isRunOut) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Runs Completed Before Dismissal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0, 1, 2, 3).forEach { runs ->
                            FilterChip(
                                selected = runsCompleted == runs,
                                onClick = { runsCompleted = runs },
                                label = { Text("$runs") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Who Was Dismissed?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = isStrikerOut,
                                    onClick = { isStrikerOut = true },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = isStrikerOut,
                                onClick = { isStrikerOut = true }
                            )
                            Text(
                                text = "$strikerName (Striker)",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = !isStrikerOut,
                                    onClick = { isStrikerOut = false },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = !isStrikerOut,
                                onClick = { isStrikerOut = false }
                            )
                            Text(
                                text = "$nonStrikerName (Non-Striker)",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isRunOut) {
                        onConfirmRunOut(runsCompleted, isStrikerOut)
                    } else {
                        onConfirmStandardWicket()
                    }
                }
            ) {
                Text(if (isRunOut) "Confirm Run Out" else "Confirm Wicket")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ByesDialog(
    state: MatchState,
    onDismiss: () -> Unit,
    onConfirmByes: (runs: Int, isLegBye: Boolean) -> Unit
) {
    if (!state.showByesDialog) return

    var runs by remember { mutableIntStateOf(1) }
    var isLegBye by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Byes / Leg Byes") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Type", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isLegBye,
                        onClick = { isLegBye = false },
                        label = { Text("Bye (B)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isLegBye,
                        onClick = { isLegBye = true },
                        label = { Text("Leg Bye (LB)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Runs Scored", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3, 4).forEach { r ->
                        FilterChip(
                            selected = runs == r,
                            onClick = { runs = r },
                            label = { Text("$r") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirmByes(runs, isLegBye) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun NoBallDialog(
    state: MatchState,
    onDismiss: () -> Unit,
    onConfirmNoBall: (runsOffBat: Int) -> Unit
) {
    if (!state.showNoBallDialog) return

    var runsOffBat by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record No Ball") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Runs off Bat (In addition to 1 No Ball penalty run):", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(0, 1, 2, 3, 4, 6).forEach { r ->
                        FilterChip(
                            selected = runsOffBat == r,
                            onClick = { runsOffBat = r },
                            label = { Text("$r") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirmNoBall(runsOffBat) }) {
                Text("Confirm No Ball")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun WideDialog(
    state: MatchState,
    onDismiss: () -> Unit,
    onConfirmWide: (totalWideRuns: Int) -> Unit
) {
    if (!state.showWideDialog) return

    var totalWideRuns by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Wide") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Total Wide Runs (1 penalty + extra runs/boundaries):", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 2, 3, 4, 5).forEach { r ->
                        FilterChip(
                            selected = totalWideRuns == r,
                            onClick = { totalWideRuns = r },
                            label = { Text("$r") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirmWide(totalWideRuns) }) {
                Text("Confirm Wide")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RetireBatterDialog(
    state: MatchState,
    onDismiss: () -> Unit,
    onConfirmRetire: (isStriker: Boolean, isHurt: Boolean) -> Unit
) {
    if (!state.showRetireBatterDialog) return

    var isStriker by remember { mutableStateOf(true) }
    var isHurt by remember { mutableStateOf(true) }

    val strikerName = state.striker.name.ifBlank { "Striker" }
    val nonStrikerName = state.nonStriker.name.ifBlank { "Non-Striker" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Retire Batter") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Select Batter", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isStriker,
                        onClick = { isStriker = true },
                        label = { Text(strikerName) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isStriker,
                        onClick = { isStriker = false },
                        label = { Text(nonStrikerName) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Reason for Retirement", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = isHurt, onClick = { isHurt = true }, role = Role.RadioButton)
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = isHurt, onClick = { isHurt = true })
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text("Retired Hurt / Injured", fontWeight = FontWeight.Medium)
                            Text("Can return later. Does NOT count as a fallen wicket.", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = !isHurt, onClick = { isHurt = false }, role = Role.RadioButton)
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = !isHurt, onClick = { isHurt = false })
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text("Retired Out", fontWeight = FontWeight.Medium)
                            Text("Unsanctioned retirement. Counts as team wicket (0 bowler credit).", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirmRetire(isStriker, isHurt) }) {
                Text("Confirm Retirement")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun BouncerPenaltyAlertDialog(
    state: MatchState,
    onDismiss: () -> Unit
) {
    if (!state.showBouncerPenaltyAlert) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bouncer Limit Penalty (ICC Rule 41.6)") },
        text = {
            Text("Second short-pitched delivery above shoulder height in this over! Called a No Ball penalty: 1 team run awarded, bowler credited with 1 run, and a FREE HIT is awarded on the next delivery.")
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Acknowledged")
            }
        }
    )
}

@Composable
fun DlsDialog(
    state: MatchState,
    viewModel: ScoreViewModel
) {
    if (!state.showDlsDialog || state.pendingNewMaxOversInput == null) return

    val pendingParts = state.pendingNewMaxOversInput.split(".")
    val pendingOvers = pendingParts.getOrNull(0)?.toIntOrNull() ?: 0
    val pendingBallsRemaining = pendingParts.getOrNull(1)?.toIntOrNull() ?: 0
    val totalPendingBalls = (pendingOvers * 6) + pendingBallsRemaining

    val proposedTarget = viewModel.calculateDlsTarget(
        state.firstInningsTotalRuns ?: 0,
        state.firstInningsBallsPlayed ?: 120,
        totalPendingBalls
    )

    AlertDialog(
        onDismissRequest = { viewModel.handleDlsDecision(false) },
        title = { Text("Overs Reduced") },
        text = {
            Column {
                Text("You are reducing the match to ${state.pendingNewMaxOversInput} overs. Would you like to recalculate the target using the Duckworth-Lewis-Stern method?")
                Spacer(modifier = Modifier.height(12.dp))
                Text("Original Target: ${state.originalTargetScore}", fontWeight = FontWeight.Bold)
                Text("Proposed DLS Target: $proposedTarget", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.handleDlsDecision(true) }) { Text("Apply DLS") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.handleDlsDecision(false) }) { Text("Change Overs Only") }
        }
    )
}

@Composable
fun NewGameSummaryDialog(
    state: MatchState,
    viewModel: ScoreViewModel
) {
    if (!state.showNewGameDialog) return

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            generateAndSavePdf(context, state, viewModel)
        }
    )

    Dialog(onDismissRequest = { viewModel.toggleNewGameDialog(false) }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Match Summary", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { viewModel.toggleNewGameDialog(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Text("Match: ${state.team1Name} vs ${state.team2Name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.previousInningsSummary.isNotEmpty()) {
                        Text("Previous Innings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(state.previousInningsSummary, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    Text("Current Innings (${state.innings} - ${state.battingTeamName})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Score: ${state.totalRuns}/${state.wickets} in ${state.displayOvers} overs")
                    if (state.targetScore != null) {
                        Text("Target: ${state.targetScore}" + if (state.isDlsApplied) " (DLS)" else "")
                    }
                    Text("Extras: ${state.totalExtras}")

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Batting", fontWeight = FontWeight.Bold)
                    val allBatters = state.dismissedBatters + state.retiredHurtBatters + state.striker + state.nonStriker
                    allBatters.forEach { b ->
                        if (b.name.isNotBlank()) {
                            val status = if (b.isOut) "" else if (state.retiredHurtBatters.contains(b)) " (Retired Hurt)" else " *"
                            Text("${b.name}$status: ${b.runs} (${b.balls}) | 4s: ${b.fours} | 6s: ${b.sixes}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Bowling", fontWeight = FontWeight.Bold)
                    viewModel.getAggregatedBowlerStats().forEach { b ->
                        Text("${b.name}: ${b.displayFigures}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                    generateAndSavePdf(context, state, viewModel)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                generateAndSavePdf(context, state, viewModel)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Download PDF Summary")
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.toggleNewGameDialog(false)
                            viewModel.resetMatch()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Confirm End & Start New Game")
                    }
                }
            }
        }
    }
}

@Composable
fun InningsCompleteDialog(
    state: MatchState,
    viewModel: ScoreViewModel
) {
    if (!state.showInningsCompleteDialog) return

    val targetScore = state.targetScore

    AlertDialog(
        onDismissRequest = { viewModel.dismissInningsCompleteDialog() },
        title = {
            Text(
                if (targetScore != null && state.totalRuns >= targetScore) "Target Reached!"
                else "Innings Complete"
            )
        },
        text = {
            if (targetScore != null && state.totalRuns >= targetScore) {
                Text("${state.battingTeamName} has chased down the target of $targetScore!")
            } else if (targetScore != null && state.wickets >= 10) {
                Text("All out! ${state.battingTeamName} fell short of the $targetScore target.")
            } else if (targetScore != null) {
                Text("Overs completed. ${state.battingTeamName} fell short of the $targetScore target.")
            } else {
                Text("First innings complete (${state.battingTeamName}: ${state.totalRuns}/${state.wickets}). Target for ${state.bowlingTeamName} will be set to ${state.totalRuns + 1}.")
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.dismissInningsCompleteDialog()
                if (targetScore == null) {
                    viewModel.nextInnings()
                } else {
                    viewModel.toggleNewGameDialog(true)
                }
            }) { Text(if (targetScore == null) "Start Next Innings" else "View Match Summary") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.dismissInningsCompleteDialog() }) { Text("Close") }
        }
    )
}

@Composable
fun HistoryScorecardDialog(
    state: MatchState,
    viewModel: ScoreViewModel
) {
    if (!state.showHistoryDialog) return

    AlertDialog(
        onDismissRequest = { viewModel.toggleHistoryDialog(false) },
        modifier = Modifier.fillMaxHeight(0.9f).fillMaxWidth(0.95f),
        title = { Text("Innings Scorecard") },
        text = {
            LazyColumn {
                item {
                    Text("Batting", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                }
                val allBatters = state.dismissedBatters + state.retiredHurtBatters + state.striker + state.nonStriker
                items(allBatters) { batter ->
                    if (batter.name.isNotBlank()) {
                        val status = if (batter.isOut) "" else if (state.retiredHurtBatters.contains(batter)) " (Retired Hurt)" else " *"
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(batter.name + status)
                                Text("${batter.runs} (${batter.balls})", fontWeight = FontWeight.Bold)
                            }
                            Text("4s: ${batter.fours} | 6s: ${batter.sixes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bowling", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                }
                val bowlerStats = viewModel.getAggregatedBowlerStats()
                items(bowlerStats) { bowler ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(bowler.name)
                        Text(bowler.displayFigures, fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Over-by-Over", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                }
                items(state.inningsHistory) { over ->
                    Column(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ov ${over.overNumber} - ${over.bowlerName}", fontWeight = FontWeight.Medium)
                            Text("${over.runsConceded} Runs, ${over.teamWickets} W", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(over.balls.joinToString(" "), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (state.inningsHistory.isEmpty()) {
                    item { Text("No completed overs yet.") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.toggleHistoryDialog(false) }) { Text("Close") }
        }
    )
}
