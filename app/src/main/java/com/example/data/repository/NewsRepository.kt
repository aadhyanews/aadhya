package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AppSettings
import com.example.data.model.Article
import com.example.data.model.Category
import com.example.data.model.District
import com.example.data.model.ReporterUser
import com.example.data.util.DefaultNewsData
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NewsRepository(val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun isUserAdmin(): Boolean {
        val email = auth.currentUser?.email
        return email == "srisahasramedicals@gmail.com"
    }

    // Observe approved & published articles for public feed, optionally filtered by category and/or district
    fun observeArticles(categoryId: String? = null, districtId: String? = null): Flow<List<Article>> = callbackFlow {
        var query: Query = db.collection("articles")
            .whereIn("status", listOf("published", "approved"))

        if (!categoryId.isNullOrBlank() && categoryId != "latest" && categoryId != "breaking") {
            query = query.whereEqualTo("categoryId", categoryId)
        }
        if (!districtId.isNullOrBlank()) {
            query = query.whereEqualTo("districtId", districtId)
        }

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "articles")
                // Fallback to default sample articles if database query fails or empty
                var fallback = DefaultNewsData.sampleArticles
                if (!categoryId.isNullOrBlank() && categoryId != "latest" && categoryId != "breaking") {
                    fallback = fallback.filter { it.categoryId == categoryId }
                }
                if (!districtId.isNullOrBlank()) {
                    fallback = fallback.filter { it.districtId == districtId }
                }
                trySend(fallback)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val articles = snapshot.toObjects(Article::class.java)
                trySend(articles)
            } else {
                // If cloud collection is currently empty, serve rich default Telugu news
                var fallback = DefaultNewsData.sampleArticles
                if (!categoryId.isNullOrBlank() && categoryId != "latest" && categoryId != "breaking") {
                    fallback = fallback.filter { it.categoryId == categoryId }
                }
                if (!districtId.isNullOrBlank()) {
                    fallback = fallback.filter { it.districtId == districtId }
                }
                trySend(fallback)
            }
        }

        awaitClose { registration.remove() }
    }

    // Observe breaking news articles
    fun observeBreakingNews(): Flow<List<Article>> = callbackFlow {
        val query = db.collection("articles")
            .whereEqualTo("status", "published")
            .whereEqualTo("isBreaking", true)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "articles")
                trySend(DefaultNewsData.sampleArticles.filter { it.isBreaking })
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                trySend(snapshot.toObjects(Article::class.java))
            } else {
                trySend(DefaultNewsData.sampleArticles.filter { it.isBreaking })
            }
        }
        awaitClose { registration.remove() }
    }

    // Observe featured carousel articles
    fun observeFeaturedNews(): Flow<List<Article>> = callbackFlow {
        val query = db.collection("articles")
            .whereEqualTo("status", "published")
            .whereEqualTo("isFeatured", true)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "articles")
                trySend(DefaultNewsData.sampleArticles.filter { it.isFeatured })
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                trySend(snapshot.toObjects(Article::class.java))
            } else {
                trySend(DefaultNewsData.sampleArticles.filter { it.isFeatured })
            }
        }
        awaitClose { registration.remove() }
    }

    // Fetch single article by ID
    suspend fun getArticle(articleId: String): Article? {
        val docRef = db.collection("articles").document(articleId)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(Article::class.java)
            } else {
                DefaultNewsData.sampleArticles.find { it.id == articleId }
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            DefaultNewsData.sampleArticles.find { it.id == articleId }
        }
    }

    // Increment view count for trending analysis
    suspend fun incrementViewCount(articleId: String) {
        val docRef = db.collection("articles").document(articleId)
        try {
            docRef.update("viewCount", FieldValue.increment(1)).await()
        } catch (e: Exception) {
            // Non-fatal for analytics
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
        }
    }

    // Observe categories with fallback
    fun observeCategories(): Flow<List<Category>> = callbackFlow {
        val registration = db.collection("categories").addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "categories")
                trySend(DefaultNewsData.categories)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val list = snapshot.toObjects(Category::class.java).sortedBy { it.sortOrder }
                trySend(list)
            } else {
                trySend(DefaultNewsData.categories)
            }
        }
        awaitClose { registration.remove() }
    }

    // Observe districts with fallback
    fun observeDistricts(): Flow<List<District>> = callbackFlow {
        val registration = db.collection("districts").addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "districts")
                trySend(DefaultNewsData.districts)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val list = snapshot.toObjects(District::class.java).sortedBy { it.sortOrder }
                trySend(list)
            } else {
                trySend(DefaultNewsData.districts)
            }
        }
        awaitClose { registration.remove() }
    }

    // Observe public app settings
    fun observeAppSettings(): Flow<AppSettings> = callbackFlow {
        val docRef = db.collection("appSettings").document("public")
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                trySend(AppSettings())
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toObject(AppSettings::class.java) ?: AppSettings())
            } else {
                trySend(AppSettings())
            }
        }
        awaitClose { registration.remove() }
    }

    // Admin: create article
    suspend fun createArticle(article: Article): Result<String> {
        val colRef = db.collection("articles")
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("User not authenticated"))
        val docRef = if (article.id.isNotBlank()) colRef.document(article.id) else colRef.document()

        val payload = article.copy(
            id = docRef.id,
            authorId = user.uid,
            authorName = article.authorName.ifBlank { user.displayName ?: "ఆధ్య అడ్మిన్" }
        ).toMap() + mapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "publishedAt" to if (article.status == "published") FieldValue.serverTimestamp() else null
        )

        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            Result.failure(e)
        }
    }

    // Admin: update article
    suspend fun updateArticle(article: Article): Result<Unit> {
        require(article.id.isNotBlank()) { "Article ID must not be blank" }
        val docRef = db.collection("articles").document(article.id)

        val payload = article.toMap() + mapOf(
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // Admin: delete article
    suspend fun deleteArticle(articleId: String): Result<Unit> {
        val docRef = db.collection("articles").document(articleId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // Admin: update app settings
    suspend fun updateAppSettings(settings: AppSettings): Result<Unit> {
        val docRef = db.collection("appSettings").document("public")
        val data = mapOf(
            "appName" to settings.appName,
            "taglineTe" to settings.taglineTe,
            "liveStreamUrl" to settings.liveStreamUrl,
            "liveStreamEnabled" to settings.liveStreamEnabled,
            "breakingNewsArticleIds" to settings.breakingNewsArticleIds,
            "maintenanceMode" to settings.maintenanceMode
        )
        return try {
            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // Admin: Observe pending articles submitted by reporters
    fun observePendingArticles(): Flow<List<Article>> = callbackFlow {
        val query = db.collection("articles")
            .whereEqualTo("status", "pending")

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "articles/pending")
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.toObjects(Article::class.java)
                trySend(list)
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { registration.remove() }
    }

    // Reporter: Observe articles submitted by a specific phone number
    fun observeReporterArticles(reporterPhone: String): Flow<List<Article>> = callbackFlow {
        if (reporterPhone.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val query = db.collection("articles")
            .whereEqualTo("reporterPhone", reporterPhone)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "articles/reporter")
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.toObjects(Article::class.java)
                trySend(list)
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { registration.remove() }
    }

    // Reporter: Submit news for admin approval
    suspend fun submitReporterArticle(article: Article): Result<String> {
        val colRef = db.collection("articles")
        val docRef = if (article.id.isNotBlank()) colRef.document(article.id) else colRef.document()

        val finalArticle = article.copy(
            id = docRef.id,
            status = "pending",
            authorId = auth.currentUser?.uid ?: ("phone_" + article.reporterPhone.replace("+", ""))
        )

        val payload = finalArticle.toMap() + mapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            Result.failure(e)
        }
    }

    // Admin: Approve news post and publish to live news stream
    suspend fun approveArticle(articleId: String): Result<Unit> {
        val docRef = db.collection("articles").document(articleId)
        return try {
            docRef.update(
                mapOf(
                    "status" to "approved",
                    "publishedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "rejectionReason" to ""
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // Admin: Reject news post with reason
    suspend fun rejectArticle(articleId: String, reason: String): Result<Unit> {
        val docRef = db.collection("articles").document(articleId)
        return try {
            docRef.update(
                mapOf(
                    "status" to "rejected",
                    "rejectionReason" to reason.ifBlank { "నియమాలకు అనుగుణంగా లేదు" },
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // Observe all registered reporters for admin management
    fun observeReporters(): Flow<List<ReporterUser>> = callbackFlow {
        val query = db.collection("reporters")
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "reporters")
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                trySend(snapshot.toObjects(ReporterUser::class.java))
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { registration.remove() }
    }

    // Save/update reporter profile
    suspend fun saveReporterProfile(reporter: ReporterUser): Result<Unit> {
        val docId = reporter.phone.replace("+", "").ifBlank { reporter.id.ifBlank { db.collection("reporters").document().id } }
        val docRef = db.collection("reporters").document(docId)
        val data = mapOf(
            "id" to docId,
            "name" to reporter.name,
            "phone" to reporter.phone,
            "role" to reporter.role,
            "status" to reporter.status,
            "assignedDistrict" to reporter.assignedDistrict,
            "assignedState" to reporter.assignedState,
            "totalSubmitted" to reporter.totalSubmitted,
            "totalApproved" to reporter.totalApproved,
            "registeredDate" to reporter.registeredDate
        )
        return try {
            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // Admin: toggle reporter active/disabled status
    suspend fun toggleReporterStatus(reporterId: String, active: Boolean): Result<Unit> {
        val docRef = db.collection("reporters").document(reporterId)
        return try {
            docRef.update("status", if (active) "active" else "disabled").await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // Admin: seed initial default categories and districts to Firestore
    suspend fun seedInitialDataIfEmpty(): Result<Unit> {
        return try {
            val catSnap = db.collection("categories").limit(1).get().await()
            if (catSnap.isEmpty) {
                val batch = db.batch()
                DefaultNewsData.categories.forEach { cat ->
                    val doc = db.collection("categories").document(cat.id)
                    batch.set(doc, cat)
                }
                DefaultNewsData.districts.forEach { dist ->
                    val doc = db.collection("districts").document(dist.id)
                    batch.set(doc, dist)
                }
                val settingsDoc = db.collection("appSettings").document("public")
                batch.set(settingsDoc, AppSettings())
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "seed")
            Result.failure(e)
        }
    }
}
