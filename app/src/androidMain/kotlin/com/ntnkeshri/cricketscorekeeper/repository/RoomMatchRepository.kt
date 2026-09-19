package com.ntnkeshri.cricketscorekeeper.repository

import com.google.gson.Gson
import com.ntnkeshri.cricketscorekeeper.db.ActiveMatchEntity
import com.ntnkeshri.cricketscorekeeper.db.MatchDao
import com.ntnkeshri.cricketscorekeeper.models.MatchState

class RoomMatchRepository(
    private val matchDao: MatchDao,
    private val gson: Gson = Gson()
) : MatchRepository {

    override suspend fun saveCurrentMatch(state: MatchState) {
        val json = gson.toJson(state)
        val entity = ActiveMatchEntity(id = 1, matchDataJson = json)
        matchDao.insertOrUpdateActiveMatch(entity)
    }

    override suspend fun loadCurrentMatch(): MatchState? {
        val entity = matchDao.getActiveMatch() ?: return null
        return try {
            gson.fromJson(entity.matchDataJson, MatchState::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
