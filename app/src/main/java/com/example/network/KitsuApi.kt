package com.example.network

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

import okhttp3.OkHttpClient

interface KitsuApi {
    @GET("anime")
    suspend fun searchAnime(@Query("filter[text]") query: String): KitsuResponse
}

data class KitsuResponse(val data: List<KitsuData>)
data class KitsuData(val attributes: KitsuAttributes)
data class KitsuAttributes(val posterImage: KitsuPosterImage?)
data class KitsuPosterImage(val large: String?)

object KitsuClient {
    private const val BASE_URL = "https://kitsu.io/api/edge/"
    
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: KitsuApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(KitsuApi::class.java)
    }
}
