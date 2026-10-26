package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import com.example.navigation.NavRoutes

object WebsiteDomainManager {
    const val DEFAULT_DOMAIN = "aadhyanews.com"
    const val DEFAULT_URL = "https://aadhyanews.com"

    private const val PREFS_NAME = "aadhya_domain_prefs"
    private const val KEY_CUSTOM_DOMAIN = "custom_domain"
    private const val KEY_CUSTOM_URL = "custom_url"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getDomain(context: Context): String {
        val prefs = getPrefs(context)
        return prefs.getString(KEY_CUSTOM_DOMAIN, DEFAULT_DOMAIN) ?: DEFAULT_DOMAIN
    }

    fun getWebsiteUrl(context: Context): String {
        val prefs = getPrefs(context)
        val url = prefs.getString(KEY_CUSTOM_URL, DEFAULT_URL) ?: DEFAULT_URL
        return if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://$url"
        }
    }

    fun getArticleUrl(context: Context, articleId: String): String {
        val baseUrl = getWebsiteUrl(context).trimEnd('/')
        return "$baseUrl/article/$articleId"
    }

    fun saveDomainSettings(context: Context, domain: String, url: String) {
        val cleanDomain = domain.trim().removePrefix("https://").removePrefix("http://").trimEnd('/')
        val cleanUrl = if (url.isNotBlank()) {
            if (url.startsWith("http://") || url.startsWith("https://")) url.trim() else "https://${url.trim()}"
        } else {
            "https://$cleanDomain"
        }
        getPrefs(context).edit()
            .putString(KEY_CUSTOM_DOMAIN, cleanDomain.ifBlank { DEFAULT_DOMAIN })
            .putString(KEY_CUSTOM_URL, cleanUrl.ifBlank { DEFAULT_URL })
            .apply()
    }

    fun openWebsite(context: Context, targetUrl: String? = null) {
        val destination = targetUrl?.ifBlank { null } ?: getWebsiteUrl(context)
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(destination)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "బ్రౌజర్ తెరవడం సాధ్యపడలేదు", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "లోపం సంభవించింది: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareWebsite(context: Context) {
        val url = getWebsiteUrl(context)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "ఆధ్య న్యూస్ అధికారిక వెబ్‌సైట్")
            putExtra(
                Intent.EXTRA_TEXT,
                "ఆధ్య న్యూస్ అధికారిక వెబ్‌సైట్ మరియు తాజా తెలుగు వార్తల కోసం ఇక్కడ క్లిక్ చేయండి: $url"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "వెబ్‌సైట్ లింక్ షేర్ చేయండి"))
    }

    fun parseDeepLinkToRoute(uri: Uri?): String? {
        if (uri == null) return null

        val scheme = uri.scheme?.lowercase() ?: return null
        val host = uri.host?.lowercase() ?: ""
        val path = uri.path ?: ""

        // Custom scheme: aadhyanews://article/{id} or aadhyanews://article?id={id}
        if (scheme == "aadhyanews") {
            if (host == "article" || path.startsWith("/article")) {
                val idFromPath = uri.lastPathSegment
                val idFromQuery = uri.getQueryParameter("id")
                val articleId = idFromQuery ?: (if (idFromPath != "article") idFromPath else null)
                if (!articleId.isNullOrBlank()) {
                    return NavRoutes.articleDetail(articleId)
                }
            } else if (host == "live" || host == "livetv") {
                return NavRoutes.LIVE_TV
            } else if (host == "reporter") {
                return NavRoutes.REPORTER_DASHBOARD
            } else if (host == "admin") {
                return NavRoutes.ADMIN_DASHBOARD
            }
            return NavRoutes.HOME
        }

        // Web scheme: http or https
        if (scheme == "http" || scheme == "https") {
            val supportedHosts = setOf(
                "aadhyanews.com",
                "www.aadhyanews.com",
                "aadhyanews.in",
                "www.aadhyanews.in",
                DEFAULT_DOMAIN
            )

            // Match if host is recognized or user's custom domain
            val segments = uri.pathSegments
            if (segments.isNotEmpty()) {
                val firstSegment = segments[0].lowercase()
                when (firstSegment) {
                    "article", "news" -> {
                        val articleId = if (segments.size > 1) segments[1] else uri.getQueryParameter("id")
                        if (!articleId.isNullOrBlank()) {
                            return NavRoutes.articleDetail(articleId)
                        }
                    }
                    "live", "livetv" -> return NavRoutes.LIVE_TV
                    "reporter" -> return NavRoutes.REPORTER_DASHBOARD
                    "admin" -> return NavRoutes.ADMIN_DASHBOARD
                    "categories" -> return NavRoutes.CATEGORIES
                    "videos" -> return NavRoutes.VIDEOS
                }
            }

            // Check query parameter id
            val idParam = uri.getQueryParameter("id")
            if (!idParam.isNullOrBlank()) {
                return NavRoutes.articleDetail(idParam)
            }

            // Root domain or other web path -> Home
            return NavRoutes.HOME
        }

        return null
    }

    fun getAssetLinksJsonSnippet(packageName: String = "com.aadhyanews.telugu"): String {
        return """
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "$packageName",
      "sha256_cert_fingerprints": [
        "14:6D:E9:7F:02:FB:7C:E7:7E:6D:A6:12:F1:F1:C7:A7:F9:6E:9B:C8:47:89:BE:86:1B:3E:9F:51:7E:5C:74:96"
      ]
    }
  }
]
""".trimIndent()
    }
}
