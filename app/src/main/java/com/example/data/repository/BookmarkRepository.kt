package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import com.example.data.model.Article
import com.example.data.model.UserBookmark
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class BookmarkRepository(
    private val context: Context,
    val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )
) {
    private val auth = Firebase.auth
    private val prefs: SharedPreferences =
        context.getSharedPreferences("aadhyanews_bookmarks", Context.MODE_PRIVATE)

    // Observe bookmarks: if user is logged in, sync with Firestore; if guest, use local storage
    fun observeBookmarks(): Flow<List<UserBookmark>> = callbackFlow {
        val user = auth.currentUser
        if (user != null) {
            val query = db.collection("users").document(user.uid).collection("bookmarks")
            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, query.path)
                    trySend(getLocalBookmarks())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookmarks = snapshot.toObjects(UserBookmark::class.java)
                    trySend(bookmarks)
                } else {
                    trySend(getLocalBookmarks())
                }
            }
            awaitClose { registration.remove() }
        } else {
            trySend(getLocalBookmarks())
            // Listen for local changes or emit initial
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "saved_bookmarks") {
                    trySend(getLocalBookmarks())
                }
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }
    }

    suspend fun isBookmarked(articleId: String): Boolean {
        val user = auth.currentUser
        if (user != null) {
            val docRef = db.collection("users").document(user.uid).collection("bookmarks").document(articleId)
            return try {
                docRef.get().await().exists()
            } catch (e: Exception) {
                getLocalBookmarks().any { it.articleId == articleId }
            }
        } else {
            return getLocalBookmarks().any { it.articleId == articleId }
        }
    }

    suspend fun toggleBookmark(article: Article): Boolean {
        val currentlyBookmarked = isBookmarked(article.id)
        if (currentlyBookmarked) {
            removeBookmark(article.id)
            return false
        } else {
            addBookmark(article)
            return true
        }
    }

    suspend fun addBookmark(article: Article) {
        val bookmark = UserBookmark(
            id = article.id,
            articleId = article.id,
            titleTe = article.titleTe,
            imageUrl = article.imageUrl,
            categoryNameTe = article.categoryId,
            savedAtTimestamp = System.currentTimeMillis()
        )

        // Always save to local cache
        val local = getLocalBookmarks().toMutableList()
        local.removeAll { it.articleId == article.id }
        local.add(0, bookmark)
        saveLocalBookmarks(local)

        // If signed in, sync to Firestore
        val user = auth.currentUser
        if (user != null) {
            val docRef = db.collection("users").document(user.uid).collection("bookmarks").document(article.id)
            try {
                val data = mapOf(
                    "articleId" to bookmark.articleId,
                    "titleTe" to bookmark.titleTe,
                    "imageUrl" to bookmark.imageUrl,
                    "categoryNameTe" to bookmark.categoryNameTe,
                    "savedAtTimestamp" to bookmark.savedAtTimestamp
                )
                docRef.set(data).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.WRITE, docRef.path)
            }
        }
    }

    suspend fun removeBookmark(articleId: String) {
        val local = getLocalBookmarks().toMutableList()
        local.removeAll { it.articleId == articleId }
        saveLocalBookmarks(local)

        val user = auth.currentUser
        if (user != null) {
            val docRef = db.collection("users").document(user.uid).collection("bookmarks").document(articleId)
            try {
                docRef.delete().await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.DELETE, docRef.path)
            }
        }
    }

    private fun getLocalBookmarks(): List<UserBookmark> {
        val jsonStr = prefs.getString("saved_bookmarks", null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<UserBookmark>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    UserBookmark(
                        id = obj.optString("articleId"),
                        articleId = obj.optString("articleId"),
                        titleTe = obj.optString("titleTe"),
                        imageUrl = obj.optString("imageUrl"),
                        categoryNameTe = obj.optString("categoryNameTe"),
                        savedAtTimestamp = obj.optLong("savedAtTimestamp")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveLocalBookmarks(list: List<UserBookmark>) {
        val array = JSONArray()
        list.forEach { b ->
            val obj = JSONObject().apply {
                put("articleId", b.articleId)
                put("titleTe", b.titleTe)
                put("imageUrl", b.imageUrl)
                put("categoryNameTe", b.categoryNameTe)
                put("savedAtTimestamp", b.savedAtTimestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString("saved_bookmarks", array.toString()).apply()
    }
}
