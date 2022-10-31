package com.example.android.planner5d.webservice

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://planner5d.com/api/"
private const val GALLERY_QUERY = "gallery"
private const val PROJECT_QUERY = "project/"

private val moshi = Moshi.Builder()
    .add(
        PolymorphicJsonAdapterFactory.of(ApiFloorItem::class.java, "className")
        .withSubtype(ApiFloorItem.ApiRoom::class.java, "Room")
        .withSubtype(ApiFloorItem.ApiDoor::class.java, "Door")
        .withSubtype(ApiFloorItem.ApiWindow::class.java, "Window")
        .withDefaultValue(ApiFloorItem.Unknown))
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
    fun getGallery(@Query("page") page: String, @Query("sort") sort: String = "editorschoice"): Deferred<ApiPlannerProjectResponsePaging>

    // запрос информации о проекте
    @GET(PROJECT_QUERY + "{id}")
    fun getProjectInfo(@Path("id") projectId: String): Deferred<ApiPlannerProjectsResponse>
}

object Planner5DApi {
    val planner5DApi : ApiService by lazy { retrofit.create(ApiService::class.java) }
}

fun getApiService(): ApiService = Planner5DApi.planner5DApi
