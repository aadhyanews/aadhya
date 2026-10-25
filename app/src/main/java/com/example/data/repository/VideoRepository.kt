package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.VideoNews
import com.example.data.util.DefaultNewsData
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class VideoRepository(val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    fun observeVideos(): Flow<List<VideoNews>> = callbackFlow {
        val query = db.collection("videos").whereEqualTo("isPublished", true)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "videos")
                trySend(DefaultNewsData.sampleVideos)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                trySend(snapshot.toObjects(VideoNews::class.java))
            } else {
                trySend(DefaultNewsData.sampleVideos)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun getVideo(videoId: String): VideoNews? {
        val docRef = db.collection("videos").document(videoId)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(VideoNews::class.java)
            } else {
                DefaultNewsData.sampleVideos.find { it.id == videoId }
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            DefaultNewsData.sampleVideos.find { it.id == videoId }
        }
    }

    suspend fun createVideo(video: VideoNews): Result<String> {
        val colRef = db.collection("videos")
        val docRef = colRef.document()
        val data = mapOf(
            "titleTe" to video.titleTe,
            "thumbnailUrl" to video.thumbnailUrl,
            "videoUrl" to video.videoUrl,
            "durationSeconds" to video.durationSeconds,
            "isPublished" to video.isPublished
        )
        return try {
            docRef.set(data).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteVideo(videoId: String): Result<Unit> {
        val docRef = db.collection("videos").document(videoId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
