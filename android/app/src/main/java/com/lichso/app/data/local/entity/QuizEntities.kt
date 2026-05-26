package com.lichso.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_offline_sessions")
data class QuizOfflineSessionEntity(
    @PrimaryKey val clientSessionId: String,
    val sessionType: String,
    val category: String?,
    val questionIdsJson: String,
    val answersJson: String,
    val startedAtMs: Long,
    val finishedAtMs: Long,
    val createdAtMs: Long = System.currentTimeMillis(),
)
