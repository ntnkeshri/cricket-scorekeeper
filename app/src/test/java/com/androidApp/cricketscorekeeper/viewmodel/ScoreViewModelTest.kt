package com.androidApp.cricketscorekeeper.viewmodel

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
    fun testStrikeRotationAtEndOfOver() {
        // Bowl 5 dot balls
        repeat(5) { viewModel.addValidDelivery(0) }
        assertEquals("Batter 1", viewModel.state.value.striker.name)

        // 6th ball completes over -> strikers swap ends
        viewModel.addValidDelivery(0)
        assertEquals("Batter 2", viewModel.state.value.striker.name)
        assertEquals("Batter 1", viewModel.state.value.nonStriker.name)
        assertEquals(1, viewModel.state.value.completedOvers)
    }

    @Test
    fun testWicketsAndInningsCompleteLockout() {
        repeat(10) { viewModel.addWicket() }

        val state = viewModel.state.value
        assertEquals(10, state.wickets)
        assertTrue(state.isInningsComplete)

        val scoreBeforeLockout = state.totalRuns

        // Delivery after 10 wickets should NOT alter score or state
        viewModel.addValidDelivery(4)
        assertEquals(scoreBeforeLockout, viewModel.state.value.totalRuns)
        assertEquals(10, viewModel.state.value.wickets)
    }

    @Test
    fun testRunOutIncrementsTeamWicketsButNotBowlerWickets() {
        viewModel.updateBowlerName("Starc")

        viewModel.addRunOut(runsCompleted = 1, isStrikerOut = true)

        val state = viewModel.state.value
        assertEquals(1, state.wickets) // Team wicket incremented
        assertEquals(1, state.totalRuns)

        val bowlerStats = viewModel.getAggregatedBowlerStats()
        val starcStats = bowlerStats.first { it.name == "Starc" }
        assertEquals(0, starcStats.wickets) // Bowler credited 0 wickets for run out
    }

    @Test
    fun testCalculateDlsTargetForReducedOversGame() {
        // 180 runs in 20 overs (120 balls) -> reduced to 10 overs (60 balls)
        val target = viewModel.calculateDlsTarget(scoreTeam1 = 180, ballsTeam1 = 120, ballsTeam2 = 60)
        assertEquals(105, target)
    }

    @Test
    fun testNoBallTriggersFreeHitAndConsumesOnLegalDelivery() {
        viewModel.addNoBall(runsOffBat = 0)
        assertTrue(viewModel.state.value.isFreeHit)
        assertEquals(1, viewModel.state.value.totalRuns)

        viewModel.addValidDelivery(2)
        assertFalse(viewModel.state.value.isFreeHit) // Consumed on legal delivery
        assertEquals(3, viewModel.state.value.totalRuns)
    }

    @Test
    fun testFreeHitPersistsThroughWide() {
        viewModel.addNoBall(runsOffBat = 0)
        assertTrue(viewModel.state.value.isFreeHit)

        viewModel.addWide(totalWideRuns = 1)
        assertTrue(viewModel.state.value.isFreeHit) // Persists through wide
    }

    @Test
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
    fun testRetireBatterHurtVsOut() {
        // Retired Hurt
        viewModel.retireBatter(isStriker = true, isHurt = true)
        assertEquals(1, viewModel.state.value.retiredHurtBatters.size)
        assertEquals(0, viewModel.state.value.wickets)

        // Retired Out
        viewModel.retireBatter(isStriker = false, isHurt = false)
        assertEquals(1, viewModel.state.value.wickets) // Team wicket counted
    }

    @Test
    fun testBouncerLimitTriggersNoBallPenaltyAndFreeHit() {
        viewModel.logBouncer()
        assertEquals(1, viewModel.state.value.bouncersBowledThisOver)
        assertEquals(0, viewModel.state.value.totalRuns)

        // 2nd bouncer in same over
        viewModel.logBouncer()
        assertEquals(2, viewModel.state.value.bouncersBowledThisOver)
        assertEquals(1, viewModel.state.value.totalRuns) // No Ball penalty
        assertTrue(viewModel.state.value.isFreeHit)
        assertTrue(viewModel.state.value.showBouncerPenaltyAlert)
    }

    @Test
    fun testMaidenOverIncrementsBowlerMaidens() {
        viewModel.updateBowlerName("Cummins")

        // Bowl 6 dot balls
        repeat(6) { viewModel.addValidDelivery(0) }

        val bowlerStats = viewModel.getAggregatedBowlerStats()
        val cumminsStats = bowlerStats.first { it.name == "Cummins" }
        assertEquals(1, cumminsStats.maidens)
    }

    @Test
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
