# Requirement Traceability Matrix (RTM)

Automated requirement traceability matrix mapping system requirements to source code and test implementations.

| Requirement ID | Description | File | Target Symbol |
|---|---|---|---|
| `ARCH-VM-01` | Central StateFlow viewmodel managing unidirectional match state mutations. | [`ScoreViewModel.kt`](app/src/main/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `ScoreViewModel` |
| `ICC-17-END-OVER` | Striker rotation at the completion of an over. | [`ScoreViewModelTest.kt`](app/src/test/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModelTest.kt) | `testStrikeRotationAtEndOfOver` |
| `ICC-18-ROTATION` | Strike rotation on 1s and 3s, maintaining ends on 2s. | [`ScoreViewModelTest.kt`](app/src/test/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModelTest.kt) | `testStrikeRotationOnSingleAndThree` |
| `ICC-21-19` | No ball penalty, runs off bat, and Free Hit status persistence. | [`ScoreViewModelTest.kt`](app/src/test/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModelTest.kt) | `testNoBallTriggersFreeHitAndConsumesOnLegalDelivery` |
| `ICC-22-FREEHIT` | Free Hit status persistence through wide deliveries. | [`ScoreViewModelTest.kt`](app/src/test/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModelTest.kt) | `testFreeHitPersistsThroughWide` |
| `ICC-23-24` | Byes and Leg Byes extras tracking with 0 bowler runs conceded. | [`ScoreViewModelTest.kt`](app/src/test/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModelTest.kt) | `testByesAddsToTeamTotalAndExtrasButNotBowlerRunsConceded` |
| `ICC-25-4` | Retired Hurt vs Retired Out dismissal differentiation. | [`ScoreViewModelTest.kt`](app/src/test/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModelTest.kt) | `testRetireBatterHurtVsOut` |
| `ICC-38` | Run out dismissal logic, incrementing team wickets without credited bowler wickets. | [`ScoreViewModelTest.kt`](app/src/test/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModelTest.kt) | `testRunOutIncrementsTeamWicketsButNotBowlerWickets` |
| `ICC-41-6` | Bouncer limit automation triggering No Ball penalty and Free Hit on 2nd bouncer. | [`ScoreViewModelTest.kt`](app/src/test/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModelTest.kt) | `testBouncerLimitTriggersNoBallPenaltyAndFreeHit` |
| `ICC-DLS-01` | Duckworth-Lewis-Stern target calculation for rain-reduced overs matches. | [`ScoreViewModelTest.kt`](app/src/test/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModelTest.kt) | `testCalculateDlsTargetForReducedOversGame` |
| `ICC-DLS-01` | Duckworth-Lewis-Stern target score calculation for rain-affected or reduced overs matches. | [`ScoreViewModel.kt`](app/src/main/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `calculateDlsTarget` |
| `ICC-DLS-02` | Enforce overs cap in second innings and trigger DLS recalculation dialog on overs reduction. | [`ScoreViewModel.kt`](app/src/main/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `updateMaxOversRequest` |
| `ICC-LAW-17` | Over completion, striker end swap, bowler history logging, and Maiden Over detection. | [`ScoreViewModel.kt`](app/src/main/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `handleEndOfOver` |
| `ICC-LAW-18` | Legal delivery run logging, strike rotation on odd runs, and consuming Free Hit status. | [`ScoreViewModel.kt`](app/src/main/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `addValidDelivery` |
| `ICC-LAW-21-19` | No Ball penalty, runs off bat, and Free Hit status activation for next delivery. | [`ScoreViewModel.kt`](app/src/main/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `addNoBall` |
| `ICC-LAW-22` | Wide ball penalty and additional runs off wide delivery. | [`ScoreViewModel.kt`](app/src/main/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `addWide` |
| `ICC-LAW-23-24` | Byes and Leg Byes extras tracking, 0 bowler runs conceded, and strike rotation. | [`ScoreViewModel.kt`](app/src/main/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `addByes` |
| `ICC-LAW-25-4` | Retired Hurt (no fallen wicket) vs Retired Out (team wicket, 0 bowler credit) logic. | [`ScoreViewModel.kt`](app/src/main/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `retireBatter` |
| `ICC-LAW-33-39` | Standard wickets (Bowled, Caught, LBW, Stumped) credited to team and bowler. | [`ScoreViewModel.kt`](app/src/main/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `addWicket` |
| `ICC-LAW-38` | Run Out dismissal logic, strike rotation, and 0 bowler credit. | [`ScoreViewModel.kt`](app/src/main/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `addRunOut` |
| `ICC-LAW-41-6` | Limit short-pitched deliveries per over and enforce No Ball penalty + Free Hit on 2nd bouncer. | [`ScoreViewModel.kt`](app/src/main/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt) | `logBouncer` |
| `ICC-LOCKOUT` | Innings complete lockout on 10 fallen wickets. | [`ScoreViewModelTest.kt`](app/src/test/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModelTest.kt) | `testWicketsAndInningsCompleteLockout` |
| `ICC-MAIDEN` | Maiden over tracking on 6 legal deliveries with 0 bowler runs conceded. | [`ScoreViewModelTest.kt`](app/src/test/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModelTest.kt) | `testMaidenOverIncrementsBowlerMaidens` |
| `ICC-TEAM-SWAP` | Dynamic team naming and batting/bowling side swap across innings. | [`ScoreViewModelTest.kt`](app/src/test/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModelTest.kt) | `testTeamNamingAndInningsSwap` |
| `TEST-VM-01` | ScoreViewModel state flow unit test suite targeting 90%+ code coverage. | [`ScoreViewModelTest.kt`](app/src/test/java/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModelTest.kt) | `ScoreViewModelTest` |

> Generated automatically via `./gradlew generateRtm`
