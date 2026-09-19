package com.ntnkeshri.cricketscorekeeper.repository

import com.ntnkeshri.cricketscorekeeper.models.MatchState

interface MatchRepository {
    suspend fun saveCurrentMatch(state: MatchState)
    suspend fun loadCurrentMatch(): MatchState?
}

class InMemoryMatchRepository : MatchRepository {
    private var currentMatchState: MatchState? = null

    override suspend fun saveCurrentMatch(state: MatchState) {
        currentMatchState = state
    }

    override suspend fun loadCurrentMatch(): MatchState? {
        return currentMatchState
    }
}

