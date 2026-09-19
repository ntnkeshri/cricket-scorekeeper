package com.ntnkeshri.cricketscorekeeper.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ntnkeshri.cricketscorekeeper.db.MatchDatabase
import com.ntnkeshri.cricketscorekeeper.models.BallEvent
import com.ntnkeshri.cricketscorekeeper.models.MatchState
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomMatchRepositoryTest {

    private lateinit var database: MatchDatabase
    private lateinit var repository: RoomMatchRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            MatchDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RoomMatchRepository(database.matchDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testSaveAndLoadMatchInRoom() = runTest {
        val dummyState = MatchState(
            team1Name = "India",
            team2Name = "Australia",
            innings = 1,
            ballHistory = listOf(
                BallEvent(runsOffBat = 4, isValidBall = true, strikerName = "Rohit"),
                BallEvent(runsOffBat = 6, isValidBall = true, strikerName = "Rohit")
            ),
            maxOversInput = "20"
        )

        repository.saveCurrentMatch(dummyState)
        val loadedState = repository.loadCurrentMatch()

        assertNotNull("Loaded MatchState should not be null", loadedState)
        assertEquals("Loaded MatchState should match original state", dummyState, loadedState)
    }
}
