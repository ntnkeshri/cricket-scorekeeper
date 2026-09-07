# Requirement Traceability Matrix (RTM)

Automated requirement traceability matrix mapping system requirements to source code implementations.

| Requirement ID | Description | File | Target Symbol |
|---|---|---|---|
| `ARCH-VM-01` | Central StateFlow viewmodel managing unidirectional match state mutations. | [`ScoreViewModel.kt`](app/src/main/java/com/androidApp/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `ScoreViewModel` |
| `ICC-DLS-01` | Duckworth-Lewis-Stern target score calculation for rain-affected or reduced overs matches. | [`ScoreViewModel.kt`](app/src/main/java/com/androidApp/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `calculateDlsTarget` |
| `ICC-DLS-02` | Enforce overs cap in second innings and trigger DLS recalculation dialog on overs reduction. | [`ScoreViewModel.kt`](app/src/main/java/com/androidApp/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `updateMaxOversRequest` |
| `ICC-LAW-17` | Over completion, striker end swap, bowler history logging, and Maiden Over detection. | [`ScoreViewModel.kt`](app/src/main/java/com/androidApp/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `handleEndOfOver` |
| `ICC-LAW-18` | Legal delivery run logging, strike rotation on odd runs, and consuming Free Hit status. | [`ScoreViewModel.kt`](app/src/main/java/com/androidApp/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `addValidDelivery` |
| `ICC-LAW-21-19` | No Ball penalty, runs off bat, and Free Hit status activation for next delivery. | [`ScoreViewModel.kt`](app/src/main/java/com/androidApp/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `addNoBall` |
| `ICC-LAW-22` | Wide ball penalty and additional runs off wide delivery. | [`ScoreViewModel.kt`](app/src/main/java/com/androidApp/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `addWide` |
| `ICC-LAW-23-24` | Byes and Leg Byes extras tracking, 0 bowler runs conceded, and strike rotation. | [`ScoreViewModel.kt`](app/src/main/java/com/androidApp/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `addByes` |
| `ICC-LAW-25-4` | Retired Hurt (no fallen wicket) vs Retired Out (team wicket, 0 bowler credit) logic. | [`ScoreViewModel.kt`](app/src/main/java/com/androidApp/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `retireBatter` |
| `ICC-LAW-33-39` | Standard wickets (Bowled, Caught, LBW, Stumped) credited to team and bowler. | [`ScoreViewModel.kt`](app/src/main/java/com/androidApp/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `addWicket` |
| `ICC-LAW-38` | Run Out dismissal logic, strike rotation, and 0 bowler credit. | [`ScoreViewModel.kt`](app/src/main/java/com/androidApp/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `addRunOut` |
| `ICC-LAW-41-6` | Limit short-pitched deliveries per over and enforce No Ball penalty + Free Hit on 2nd bouncer. | [`ScoreViewModel.kt`](app/src/main/java/com/androidApp/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `logBouncer` |

> Generated automatically via `./gradlew generateRtm`
