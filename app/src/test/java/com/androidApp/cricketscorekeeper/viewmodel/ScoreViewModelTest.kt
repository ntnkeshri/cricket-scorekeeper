package com.androidApp.cricketscorekeeper.viewmodel

import com.androidApp.cricketscorekeeper.annotations.Requirement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@Requirement(
    id = "TEST-VM-01",
    description = "ScoreViewModel state flow unit test suite targeting 90%+ code coverage."
)
class ScoreViewModelTest {

    private lateinit var viewModel: ScoreViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ScoreViewModel()
    }

    @After
    fun tearDown() {
        viewModel.resetMatch()
        Dispatchers.resetMain()
    }

    @Test
    @Requirement(
        id = "ICC-18-ROTATION",
        description = "Strike rotation on 1s and 3s, maintaining ends on 2s."
    )
    fun testStrikeRotationOnSingleAndThree() {
        assertEquals("Batter 1", viewModel.state.value.striker.name)
        assertEquals("Batter 2", viewModel.state.value.nonStriker.name)

        viewModel.addValidDelivery(1)
        assertEquals("Batter 2", viewModel.state.value.striker.name)
        assertEquals("Batter 1", viewModel.state.value.nonStriker.name)

        viewModel.addValidDelivery(2)
        assertEquals("Batter 2", viewModel.state.value.striker.name)
        assertEquals("Batter 1", viewModel.state.value.nonStriker.name)

        viewModel.addValidDelivery(3)
        assertEquals("Batter 1", viewModel.state.value.striker.name)
        assertEquals("Batter 2", viewModel.state.value.nonStriker.name)
    }

    @Test
    @Requirement(
        id = "ICC-17-END-OVER",
        description = "Striker rotation at the completion of an over."
    )
    fun testStrikeRotationAtEndOfOver() {
        repeat(5) { viewModel.addValidDelivery(0) }
        assertEquals("Batter 1", viewModel.state.value.striker.name)

        viewModel.addValidDelivery(0)
        assertEquals("Batter 2", viewModel.state.value.striker.name)
        assertEquals("Batter 1", viewModel.state.value.nonStriker.name)
        assertEquals(1, viewModel.state.value.completedOvers)
    }

    @Test
    @Requirement(
        id = "ICC-LOCKOUT",
        description = "Innings complete lockout on 10 fallen wickets."
    )
    fun testWicketsAndInningsCompleteLockout() {
        repeat(10) { viewModel.addWicket() }

        val state = viewModel.state.value
        assertEquals(10, state.wickets)
        assertTrue(state.isInningsComplete)

        val scoreBeforeLockout = state.totalRuns

        viewModel.addValidDelivery(4)
        assertEquals(scoreBeforeLockout, viewModel.state.value.totalRuns)
        assertEquals(10, viewModel.state.value.wickets)
    }

    @Test
    @Requirement(
        id = "ICC-38",
        description = "Run out dismissal logic, incrementing team wickets without credited bowler wickets."
    )
    fun testRunOutIncrementsTeamWicketsButNotBowlerWickets() {
        viewModel.updateBowlerName("Starc")

        viewModel.addRunOut(runsCompleted = 1, isStrikerOut = true)

        val state = viewModel.state.value
        assertEquals(1, state.wickets)
        assertEquals(1, state.totalRuns)

        val bowlerStats = viewModel.getAggregatedBowlerStats()
        val starcStats = bowlerStats.first { it.name == "Starc" }
        assertEquals(0, starcStats.wickets)
    }

    @Test
    @Requirement(
        id = "ICC-DLS-01",
        description = "Duckworth-Lewis-Stern target calculation for rain-reduced overs matches."
    )
    fun testCalculateDlsTargetForReducedOversGame() {
        val target = viewModel.calculateDlsTarget(scoreTeam1 = 180, ballsTeam1 = 120, ballsTeam2 = 60)
        assertEquals(105, target)
    }

    @Test
    @Requirement(
        id = "ICC-21-19",
        description = "No ball penalty, runs off bat, and Free Hit status persistence."
    )
    fun testNoBallTriggersFreeHitAndConsumesOnLegalDelivery() {
        viewModel.addNoBall(runsOffBat = 0)
        assertTrue(viewModel.state.value.isFreeHit)
        assertEquals(1, viewModel.state.value.totalRuns)

        viewModel.addValidDelivery(2)
        assertFalse(viewModel.state.value.isFreeHit)
        assertEquals(3, viewModel.state.value.totalRuns)
    }

    @Test
    @Requirement(
        id = "ICC-22-FREEHIT",
        description = "Free Hit status persistence through wide deliveries."
    )
    fun testFreeHitPersistsThroughWide() {
        viewModel.addNoBall(runsOffBat = 0)
        assertTrue(viewModel.state.value.isFreeHit)

        viewModel.addWide(totalWideRuns = 1)
        assertTrue(viewModel.state.value.isFreeHit)
    }

    @Test
    @Requirement(
        id = "ICC-23-24",
        description = "Byes and Leg Byes extras tracking with 0 bowler runs conceded."
    )
    fun testByesAddsToTeamTotalAndExtrasButNotBowlerRunsConceded() {
        viewModel.updateBowlerName("Bumrah")

        viewModel.addByes(runs = 2, isLegBye = false)

        val state = viewModel.state.value
        assertEquals(2, state.totalRuns)
        assertEquals(2, state.totalExtras)
        assertEquals(1, state.validBalls)
        assertEquals(0, state.striker.runs)
        assertEquals(1, state.striker.balls)

        val bowlerStats = viewModel.getAggregatedBowlerStats()
        val bumrahStats = bowlerStats.first { it.name == "Bumrah" }
        assertEquals(0, bumrahStats.runsConceded)
    }

    @Test
    @Requirement(
        id = "ICC-25-4",
        description = "Retired Hurt vs Retired Out dismissal differentiation."
    )
    fun testRetireBatterHurtVsOut() {
        viewModel.retireBatter(isStriker = true, isHurt = true)
        assertEquals(1, viewModel.state.value.retiredHurtBatters.size)
        assertEquals(0, viewModel.state.value.wickets)

        viewModel.retireBatter(isStriker = false, isHurt = false)
        assertEquals(1, viewModel.state.value.wickets)
    }

    @Test
    @Requirement(
        id = "ICC-41-6",
        description = "Bouncer limit automation triggering No Ball penalty and Free Hit on 2nd bouncer."
    )
    fun testBouncerLimitTriggersNoBallPenaltyAndFreeHit() {
        viewModel.logBouncer()
        assertEquals(1, viewModel.state.value.bouncersBowledThisOver)
        assertEquals(0, viewModel.state.value.totalRuns)

        viewModel.logBouncer()
        assertEquals(2, viewModel.state.value.bouncersBowledThisOver)
        assertEquals(1, viewModel.state.value.totalRuns)
        assertTrue(viewModel.state.value.isFreeHit)
        assertTrue(viewModel.state.value.showBouncerPenaltyAlert)
    }

    @Test
    @Requirement(
        id = "ICC-MAIDEN",
        description = "Maiden over tracking on 6 legal deliveries with 0 bowler runs conceded."
    )
    fun testMaidenOverIncrementsBowlerMaidens() {
        viewModel.updateBowlerName("Cummins")

        repeat(6) { viewModel.addValidDelivery(0) }

        val bowlerStats = viewModel.getAggregatedBowlerStats()
        val cumminsStats = bowlerStats.first { it.name == "Cummins" }
        assertEquals(1, cumminsStats.maidens)
    }

    @Test
    @Requirement(
        id = "ICC-TEAM-SWAP",
        description = "Dynamic team naming and batting/bowling side swap across innings."
    )
    fun testTeamNamingAndInningsSwap() {
        viewModel.updateTeam1Name("India")
        viewModel.updateTeam2Name("Australia")

        assertEquals("India", viewModel.state.value.battingTeamName)
        assertEquals("Australia", viewModel.state.value.bowlingTeamName)

        viewModel.nextInnings()

        assertEquals(2, viewModel.state.value.innings)
        assertEquals("Australia", viewModel.state.value.battingTeamName)
        assertEquals("India", viewModel.state.value.bowlingTeamName)
    }
}