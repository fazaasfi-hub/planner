package com.example.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

interface AniListApi {
    @POST("/")
    suspend fun searchAnime(@Body body: AniListRequest): AniListResponse
}

data class AniListRequest(val query: String, val variables: Map<String, String>)
data class AniListResponse(val data: AniListData)
data class AniListData(val Media: AniListMedia?)
data class AniListMedia(
    val title: AniListTitle?,
    val coverImage: AniListCoverImage?
)
data class AniListTitle(val romaji: String?, val english: String?, val native: String?)
data class AniListCoverImage(val large: String?)

object AniListClient {
    private const val BASE_URL = "https://graphql.anilist.co/"
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: AniListApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AniListApi::class.java)
    }
}
