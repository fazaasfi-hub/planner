package com.example.network

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

interface JikanApi {
    @GET("anime")
    suspend fun searchAnime(@Query("q") query: String): JikanResponse
}

data class JikanResponse(val data: List<Anime>)
data class Anime(val images: Images)
data class Images(val jpg: ImageUrls?)
data class ImageUrls(val image_url: String)

object JikanClient {
    private const val BASE_URL = "https://api.jikan.moe/v4/"
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: JikanApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(JikanApi::class.java)
    }
}
