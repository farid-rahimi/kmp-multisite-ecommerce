package com.solutionium.shared.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Entity(tableName = "story_view")
@OptIn(ExperimentalTime::class)
data class StoryViewEntity(
    @PrimaryKey
    val storyId: Int,
    val viewedAt: Long = Clock.System.now().toEpochMilliseconds()
)
