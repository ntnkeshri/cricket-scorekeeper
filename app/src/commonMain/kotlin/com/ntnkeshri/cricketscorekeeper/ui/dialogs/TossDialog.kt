package com.ntnkeshri.cricketscorekeeper.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ntnkeshri.cricketscorekeeper.models.MatchState
import com.ntnkeshri.cricketscorekeeper.models.TossDecision

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TossDialog(
    state: MatchState,
    onConfirmToss: (winner: String, decision: TossDecision) -> Unit,
    onSkipToss: (battingTeam: String) -> Unit
) {
    var showManualOverride by remember { mutableStateOf(false) }
    var selectedWinner by remember { mutableStateOf<String?>(null) }
    var selectedDecision by remember { mutableStateOf<TossDecision?>(null) }

    Dialog(onDismissRequest = {}) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Match Toss", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                if (!showManualOverride) {
                    Text("Who won the toss?")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = selectedWinner == state.team1Name,
                            onClick = { selectedWinner = state.team1Name },
                            label = { Text(state.team1Name) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedWinner == state.team2Name,
                            onClick = { selectedWinner = state.team2Name },
                            label = { Text(state.team2Name) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedWinner != null) {
                        Text("Decision:")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = selectedDecision == TossDecision.BAT,
                                onClick = { selectedDecision = TossDecision.BAT },
                                label = { Text("Bat") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedDecision == TossDecision.BOWL,
                                onClick = { selectedDecision = TossDecision.BOWL },
                                label = { Text("Bowl") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showManualOverride = true }) {
                            Text("Skip Toss / Manual Override")
                        }
                        Button(
                            onClick = {
                                if (selectedWinner != null && selectedDecision != null) {
                                    onConfirmToss(selectedWinner!!, selectedDecision!!)
                                }
                            },
                            enabled = selectedWinner != null && selectedDecision != null
                        ) {
                            Text("Confirm Toss")
                        }
                    }
                } else {
                    Text("Select Batting Team")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onSkipToss(state.team1Name) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(state.team1Name)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onSkipToss(state.team2Name) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(state.team2Name)
                    }
                }
            }
        }
    }
}
