package com.ntnkeshri.cricketscorekeeper.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ntnkeshri.cricketscorekeeper.models.MatchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenaltyDialog(
    state: MatchState,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    if (!state.showPenaltyDialog) return

    var penaltyRuns by remember { mutableIntStateOf(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Ground Penalty") },
        text = {
            Column {
                Text("Select Penalty Runs:")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = penaltyRuns == 4,
                        onClick = { penaltyRuns = 4 },
                        label = { Text("4 Runs") }
                    )
                    FilterChip(
                        selected = penaltyRuns == 5,
                        onClick = { penaltyRuns = 5 },
                        label = { Text("Pole Hit (5 Runs)") }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(penaltyRuns) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
