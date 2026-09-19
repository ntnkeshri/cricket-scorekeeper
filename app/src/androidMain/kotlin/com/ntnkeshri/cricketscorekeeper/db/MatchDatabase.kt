package com.ntnkeshri.cricketscorekeeper.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ActiveMatchEntity::class], version = 1, exportSchema = false)
abstract class MatchDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
}
