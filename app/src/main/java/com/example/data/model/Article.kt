package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Article(
    @DocumentId val id: String = "",
    val titleTe: String = "",
    val contentTe: String = "",
    val imageUrl: String = "",
    val imageCaption: String = "",
    val categoryId: String = "latest",
    val districtId: String = "",
    val state: String = "తెలంగాణ",
    val mandal: String = "",
    val village: String = "",
    val gpsCoordinates: String = "",
    val mediaUrls: List<String> = emptyList(),
    val authorName: String = "ఆధ్య ప్రతినిధి",
    val authorId: String = "",
    val reporterPhone: String = "",
    val status: String = "published", // pending, approved, rejected, published, draft
    val rejectionReason: String = "",
    val isBreaking: Boolean = false,
    val isFeatured: Boolean = false,
    val videoUrl: String = "",
    val viewCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    val publishedAt: Timestamp? = null,
    val formattedDateTime: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "titleTe" to titleTe,
            "contentTe" to contentTe,
            "imageUrl" to imageUrl,
            "imageCaption" to imageCaption,
            "categoryId" to categoryId,
            "districtId" to districtId,
            "state" to state,
            "mandal" to mandal,
            "village" to village,
            "gpsCoordinates" to gpsCoordinates,
            "mediaUrls" to mediaUrls,
            "authorName" to authorName,
            "authorId" to authorId,
            "reporterPhone" to reporterPhone,
            "status" to status,
            "rejectionReason" to rejectionReason,
            "isBreaking" to isBreaking,
            "isFeatured" to isFeatured,
            "videoUrl" to videoUrl,
            "viewCount" to viewCount,
            "formattedDateTime" to formattedDateTime
        ).filterValues { it != null }
    }
}

@IgnoreExtraProperties
data class ReporterUser(
    @DocumentId val id: String = "",
    val name: String = "",
    val phone: String = "",
    val role: String = "reporter", // "admin" or "reporter"
    val status: String = "active", // "active", "disabled", "pending"
    val assignedDistrict: String = "",
    val assignedState: String = "తెలంగాణ",
    val totalSubmitted: Long = 0L,
    val totalApproved: Long = 0L,
    val registeredDate: String = ""
)
