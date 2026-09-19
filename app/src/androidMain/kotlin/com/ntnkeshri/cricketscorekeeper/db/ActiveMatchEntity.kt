package com.ntnkeshri.cricketscorekeeper.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_match")
data class ActiveMatchEntity(
    @PrimaryKey
    val id: Int = 1,
    val matchDataJson: String
)
