package com.example.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Category(
    @DocumentId val id: String = "",
    val nameTe: String = "",
    val slug: String = "",
    val isActive: Boolean = true,
    val sortOrder: Long = 0L
)

@IgnoreExtraProperties
data class District(
    @DocumentId val id: String = "",
    val nameTe: String = "",
    val isActive: Boolean = true,
    val sortOrder: Long = 0L
)

@IgnoreExtraProperties
data class AppSettings(
    @DocumentId val id: String = "public",
    val appName: String = "AADHYA NEWS",
    val taglineTe: String = "ప్రజల గొంతుక... ప్రజల వార్త...",
    val liveStreamUrl: String = "",
    val liveStreamEnabled: Boolean = false,
    val breakingNewsArticleIds: List<String> = emptyList(),
    val maintenanceMode: Boolean = false
)

@IgnoreExtraProperties
data class VideoNews(
    @DocumentId val id: String = "",
    val titleTe: String = "",
    val thumbnailUrl: String = "",
    val videoUrl: String = "",
    val durationSeconds: Long = 0L,
    val isPublished: Boolean = true
)

@IgnoreExtraProperties
data class UserBookmark(
    @DocumentId val id: String = "",
    val articleId: String = "",
    val titleTe: String = "",
    val imageUrl: String = "",
    val categoryNameTe: String = "",
    val savedAtTimestamp: Long = System.currentTimeMillis()
)
