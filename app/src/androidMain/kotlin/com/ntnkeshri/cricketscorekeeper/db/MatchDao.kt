package com.ntnkeshri.cricketscorekeeper.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateActiveMatch(entity: ActiveMatchEntity)

    @Query("SELECT * FROM active_match WHERE id = 1 LIMIT 1")
    suspend fun getActiveMatch(): ActiveMatchEntity?

    @Query("DELETE FROM active_match")
    suspend fun clearActiveMatch()
}
