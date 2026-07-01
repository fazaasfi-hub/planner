package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder

data class CharacterResult(
    val name: String,
    val nativeName: String = "",
    val imageUrl: String,
    val sourceName: String = "",
    val description: String = ""
)

class CharacterSearchService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun searchCharacter(query: String): CharacterResult? = withContext(Dispatchers.IO) {
        var result: CharacterResult? = null

        // Try AniList first (best for Donghua and Anime)
        try {
            result = searchAniList(query)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Try Jikan (MyAnimeList) as fallback
        if (result == null) {
            try {
                result = searchJikan(query)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // If found, translate the English description into Indonesian using Gemini AI
        if (result != null) {
            val translatedDesc = translateWithGemini(result.description)
            return@withContext result.copy(description = translatedDesc)
        }

        // If not found in traditional APIs, use Gemini AI to generate details of this character in Indonesian
        try {
            val geminiResult = generateWithGemini(query)
            if (geminiResult != null) {
                // Try searching AniList/Jikan one more time with the generated clean name to find the official image
                var imageResult: CharacterResult? = null
                try {
                    imageResult = searchAniList(geminiResult.name)
                } catch (e: Exception) { /* ignore */ }

                if (imageResult == null) {
                    try {
                        imageResult = searchJikan(geminiResult.name)
                    } catch (e: Exception) { /* ignore */ }
                }

                // Beautiful high-quality anime illustration placeholders from Unsplash as fallback
                val fallbackImages = listOf(
                    "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=400&auto=format&fit=crop&q=60",
                    "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=400&auto=format&fit=crop&q=60"
                )
                val imageUrl = if (imageResult?.imageUrl != null && imageResult.imageUrl.isNotEmpty()) {
                    imageResult.imageUrl
                } else {
                    fallbackImages.random()
                }

                return@withContext geminiResult.copy(imageUrl = imageUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        null
    }

    private fun translateWithGemini(description: String): String {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") return description

        val prompt = "Terjemahkan biografi/deskripsi karakter anime/donghua berikut ke dalam Bahasa Indonesia yang sangat seru, kasual, penuh rasa sayang (cocok untuk waifu/bini), dan menarik. Jangan gunakan format markdown yang tebal berlebihan, buat agar rapi, ringkas, dan enak dibaca:\n\n$description"
        val response = callGeminiApi(prompt)
        return response ?: description
    }

    private fun generateWithGemini(query: String): CharacterResult? {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") return null

        val prompt = """
            Berikan informasi lengkap mengenai karakter wanita anime atau donghua bernama '$query' dalam format JSON. Jawab HANYA dengan JSON valid, tanpa backtick ```json atau penjelasan lainnya.
            Format JSON harus persis seperti ini:
            {
              "name": "Nama Karakter",
              "nativeName": "Nama Asli (Kanji/Hanzi)",
              "sourceName": "Asal Anime/Donghua",
              "description": "Biografi/deskripsi lengkap dalam Bahasa Indonesia yang seru, kasual, penuh rasa sayang, dan menarik."
            }
        """.trimIndent()

        val response = callGeminiApi(prompt) ?: return null
        try {
            val cleanJson = response.trim().removeSurrounding("```json", "```").trim()
            val json = JSONObject(cleanJson)
            val name = json.getString("name")
            val nativeName = json.optString("nativeName", "")
            val sourceName = json.optString("sourceName", "Anime / Donghua")
            val description = json.optString("description", "")
            return CharacterResult(
                name = name,
                nativeName = nativeName,
                imageUrl = "",
                sourceName = sourceName,
                description = description
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun callGeminiApi(prompt: String): String? {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") return null

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val jsonPayload = JSONObject().apply {
            val contentsArray = org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            }
            put("contents", contentsArray)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonPayload.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val bodyString = response.body?.string() ?: return null
                val json = JSONObject(bodyString)
                val candidates = json.optJSONArray("candidates") ?: return null
                if (candidates.length() == 0) return null
                val firstCandidate = candidates.getJSONObject(0)
                val contentObj = firstCandidate.optJSONObject("content") ?: return null
                val parts = contentObj.optJSONArray("parts") ?: return null
                if (parts.length() == 0) return null
                return parts.getJSONObject(0).optString("text")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun searchAniList(query: String): CharacterResult? {
        val url = "https://graphql.anilist.co"
        val graphQuery = """
            query (${'$'}search: String) {
              Character (search: ${'$'}search) {
                name {
                  full
                  native
                }
                image {
                  large
                }
                description
                media(perPage: 1) {
                  nodes {
                    title {
                      userPreferred
                      romaji
                      english
                    }
                  }
                }
              }
            }
        """.trimIndent()

        val variables = JSONObject().put("search", query)
        val jsonPayload = JSONObject().apply {
            put("query", graphQuery)
            put("variables", variables)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonPayload.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val bodyString = response.body?.string() ?: return null
            val json = JSONObject(bodyString)
            val dataObj = json.optJSONObject("data") ?: return null
            val charObj = dataObj.optJSONObject("Character") ?: return null

            val nameObj = charObj.optJSONObject("name")
            val fullName = nameObj?.optString("full", query) ?: query
            val nativeName = nameObj?.optString("native", "") ?: ""

            val imageObj = charObj.optJSONObject("image")
            val imageUrl = imageObj?.optString("large", "") ?: ""

            val rawDesc = charObj.optString("description", "")
            val description = cleanDescription(rawDesc)

            var sourceName = "Donghua / Anime"
            val mediaObj = charObj.optJSONObject("media")
            val nodesArr = mediaObj?.optJSONArray("nodes")
            if (nodesArr != null && nodesArr.length() > 0) {
                val mediaNode = nodesArr.getJSONObject(0)
                val titleObj = mediaNode.optJSONObject("title")
                if (titleObj != null) {
                    sourceName = titleObj.optString("userPreferred")
                        .ifEmpty { titleObj.optString("romaji") }
                        .ifEmpty { titleObj.optString("english") }
                        .ifEmpty { "Donghua / Anime" }
                }
            }

            if (imageUrl.isNotEmpty()) {
                return CharacterResult(
                    name = fullName,
                    nativeName = nativeName,
                    imageUrl = imageUrl,
                    sourceName = sourceName,
                    description = description
                )
            }
        }
        return null
    }

    private fun searchJikan(query: String): CharacterResult? {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://api.jikan.moe/v4/characters?q=$encodedQuery&limit=1"
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val bodyString = response.body?.string() ?: return null
            val json = JSONObject(bodyString)
            val dataArr = json.optJSONArray("data") ?: return null
            if (dataArr.length() == 0) return null

            val charObj = dataArr.getJSONObject(0)
            val name = charObj.optString("name", query)
            val nativeName = charObj.optString("name_kanji", "")

            val imagesObj = charObj.optJSONObject("images")
            val jpgObj = imagesObj?.optJSONObject("jpg")
            val imageUrl = jpgObj?.optString("image_url", "") ?: ""

            val rawAbout = charObj.optString("about", "")
            val description = cleanDescription(rawAbout)

            if (imageUrl.isNotEmpty()) {
                return CharacterResult(
                    name = name,
                    nativeName = nativeName,
                    imageUrl = imageUrl,
                    sourceName = "Anime / Donghua",
                    description = description
                )
            }
        }
        return null
    }

    private fun cleanDescription(raw: String?): String {
        if (raw == null) return ""
        return raw.replace(Regex("<[^>]*>"), "") // Strip HTML
            .replace(Regex("__"), "") // Strip markdown bold
            .replace(Regex("\\*\\*"), "")
            .replace(Regex("~"), "") // Strip tags
            .replace(Regex("\\[\\s*Written by[^\\]]*\\]"), "") // Strip MAL copyright note
            .trim()
    }
}
