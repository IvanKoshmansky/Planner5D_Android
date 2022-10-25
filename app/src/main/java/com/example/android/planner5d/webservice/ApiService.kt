package com.example.android.planner5d.webservice

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://planner5d.com/api/"

private const val GALLERY_QUERY = "gallery"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface ApiService {
    // запрос одной страницы галереи
    @GET(GALLERY_QUERY)
    fun getGallery(@Query("page") page: String): ApiPlannerProjectResponsePaging
}

object Planner5DApi {
    val planner5DApi : ApiService by lazy { retrofit.create(ApiService::class.java) }
}

fun getApiService(): ApiService = Planner5DApi.planner5DApi
