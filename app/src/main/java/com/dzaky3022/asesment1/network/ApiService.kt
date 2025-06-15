package com.dzaky3022.asesment1.network

import com.dzaky3022.asesment1.ui.model.BaseResponse
import com.dzaky3022.asesment1.ui.model.WaterResultDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://wincal-one.vercel.app"

interface WaterResultApi {
    @Multipart
    @POST("/water-results/")
    suspend fun addWaterResult(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("roomTemp") roomTemp: RequestBody,
        @Part("tempUnit") tempUnit: RequestBody,
        @Part("weight") weight: RequestBody,
        @Part("weightUnit") weightUnit: RequestBody,
        @Part("activityLevel") activityLevel: RequestBody,
        @Part("drinkAmount") drinkAmount: RequestBody,
        @Part("waterUnit") waterUnit: RequestBody,
        @Part("resultValue") resultValue: RequestBody,
        @Part("percentage") percentage: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part image: MultipartBody.Part?
    ): BaseResponse<WaterResultDto>

    @GET("/water-results/")
    suspend fun getAllWaterResults(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 15,
        @Query("show_all") showAll: Boolean = true,
    ): BaseResponse<List<WaterResultDto>>

    @GET("/water-results/{result_id}")
    suspend fun getWaterResult(
        @Path("result_id") resultId: String
    ): BaseResponse<WaterResultDto>

    @Multipart
    @PUT("/water-results/{result_id}")
    suspend fun updateWaterResult(
        @Path("result_id") resultId: String,
        @Part("title") title: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("roomTemp") roomTemp: RequestBody?,
        @Part("tempUnit") tempUnit: RequestBody?,
        @Part("weight") weight: RequestBody?,
        @Part("weightUnit") weightUnit: RequestBody?,
        @Part("activityLevel") activityLevel: RequestBody?,
        @Part("drinkAmount") drinkAmount: RequestBody?,
        @Part("waterUnit") waterUnit: RequestBody?,
        @Part("resultValue") resultValue: RequestBody?,
        @Part("percentage") percentage: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("delete_image") deleteImage: RequestBody?,
        @Part image: MultipartBody.Part?
    ): BaseResponse<WaterResultDto>

    @DELETE("/water-results/{result_id}")
    suspend fun deleteWaterResult(
        @Path("result_id") resultId: String
    ): BaseResponse<Map<String, Any>>
}

object WaterApi {
    private val moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private var currentUid: String? = null
    private var _service: WaterResultApi? = null

    val service: WaterResultApi
        get() = _service ?: throw IllegalStateException("WaterApi not initialized. Call WaterApi.initialize(uid) first.")

    fun initialize(uid: String) {
        // Only recreate if uid changed or service doesn't exist
        if (currentUid != uid || _service == null) {
            currentUid = uid

            val client by lazy {
                OkHttpClient.Builder()
                    .addInterceptor(Interceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("uid", uid)
                            .build()
                        chain.proceed(request)
                    })
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()
            }

            val retrofit by lazy {
                Retrofit.Builder()
                    .client(client)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .baseUrl(BASE_URL)
                    .build()
            }

            _service = retrofit.create(WaterResultApi::class.java)
        }
    }

    fun clear() {
        _service = null
        currentUid = null
    }

    val isInitialized: Boolean
        get() = _service != null
}

enum class ApiStatus { IDLE, LOADING, SUCCESS, FAILED }