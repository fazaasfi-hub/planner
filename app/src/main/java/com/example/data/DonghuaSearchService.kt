package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder

object DonghuaSearchService {
    private val client = OkHttpClient()

    /**
     * Mencari URL gambar sampul (cover) untuk judul Donghua tertentu.
     * Mencoba AniList GraphQL API terlebih dahulu, kemudian fallback ke Jikan API jika diperlukan.
     */
    suspend fun searchCoverImage(title: String): String? = withContext(Dispatchers.IO) {
        if (title.isBlank()) return@withContext null

        // 1. Coba AniList GraphQL API (Sangat lengkap untuk Donghua)
        val aniListUrl = searchAniList(title)
        if (aniListUrl != null) {
            return@withContext aniListUrl
        }

        // 2. Fallback ke Jikan API (MyAnimeList)
        val jikanUrl = searchJikan(title)
        if (jikanUrl != null) {
            return@withContext jikanUrl
        }

        return@withContext null
    }

    private fun searchAniList(title: String): String? {
        try {
            val url = "https://graphql.anilist.co"
            val query = """
                query (${'$'}search: String) {
                  Media (search: ${'$'}search, type: ANIME) {
                    coverImage {
                      extraLarge
                      large
                      medium
                    }
                  }
                }
            """.trimIndent()

            val variables = JSONObject().apply {
                put("search", title)
            }
            val jsonBody = JSONObject().apply {
                put("query", query)
                put("variables", variables)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonBody.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: return null
                    val json = JSONObject(responseBody)
                    val data = json.optJSONObject("data") ?: return null
                    val media = data.optJSONObject("Media") ?: return null
                    val coverImage = media.optJSONObject("coverImage") ?: return null
                    
                    val imageUrl = coverImage.optString("extraLarge").ifEmpty {
                        coverImage.optString("large").ifEmpty {
                            coverImage.optString("medium")
                        }
                    }
                    if (imageUrl.isNotEmpty()) {
                        return imageUrl
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun searchJikan(title: String): String? {
        try {
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            val url = "https://api.jikan.moe/v4/anime?q=$encodedTitle&limit=1"
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: return null
                    val json = JSONObject(responseBody)
                    val dataArray = json.optJSONArray("data") ?: return null
                    if (dataArray.length() > 0) {
                        val firstItem = dataArray.getJSONObject(0)
                        val images = firstItem.optJSONObject("images") ?: return null
                        val jpg = images.optJSONObject("jpg") ?: return null
                        val imageUrl = jpg.optString("large_image_url").ifEmpty {
                            jpg.optString("image_url")
                        }
                        if (imageUrl.isNotEmpty()) {
                            return imageUrl
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
