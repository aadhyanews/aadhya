package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Article
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class NewsRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createArticle_byAdmin_succeeds() = runBlocking {
        signInTestUser("srisahasramedicals@gmail.com")
        val repo = NewsRepository(firestore)

        val article = Article(
            id = "test_art_" + UUID.randomUUID().toString().take(8),
            titleTe = "పాలమూరు తాజా వార్త",
            contentTe = "పాలమూరు రంగారెడ్డి ప్రాజెక్టు పనులు శరవేగంగా సాగుతున్నాయి.",
            categoryId = "mahabubnagar",
            authorName = "ఆధ్య అడ్మిన్",
            status = "published",
            isBreaking = true,
            isFeatured = true
        )

        val result = withTimeout(5000L) {
            repo.createArticle(article)
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun unauthenticatedUser_cannotWriteArticle() = runBlocking {
        auth.signOut()
        val repo = NewsRepository(firestore)

        val article = Article(
            id = "unauth_art",
            titleTe = "అనధికార వార్త",
            contentTe = "ఈ వార్తను రాయడానికి అనుమతి లేదు.",
            categoryId = "latest",
            authorName = "అజ్ఞాత",
            status = "published"
        )

        val result = repo.createArticle(article)
        assertTrue(result.isFailure)
    }

    @Test
    fun getArticle_published_returnsArticle() = runBlocking {
        signInTestUser("srisahasramedicals@gmail.com")
        val repo = NewsRepository(firestore)

        val artId = "test_pub_" + UUID.randomUUID().toString().take(8)
        val article = Article(
            id = artId,
            titleTe = "ప్రచురించిన వార్త",
            contentTe = "ముఖ్య సమాచారం మరియు వివరాలు ఇక్కడ ఉన్నాయి.",
            categoryId = "telangana",
            authorName = "ప్రతినిధి",
            status = "published"
        )
        val result = repo.createArticle(article)
        assertTrue(result.isSuccess)
        val createdId = result.getOrThrow()

        // Switch to unauthenticated caller
        auth.signOut()
        val fetched = withTimeout(5000L) {
            repo.getArticle(createdId)
        }
        assertNotNull(fetched)
        assertEquals("ప్రచురించిన వార్త", fetched?.titleTe)
    }
}
