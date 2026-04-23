package com.example.collegeeventmanagementsystem

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PutMapping
import retrofit2.http.Path
import retrofit2.http.DELETE

// The model matches the Java Backend Map<String, String>
data class ApiEvent(
    val id: String? = null,
    val title: String,
    val location: String,
    val description: String,
    val date: String? = null,
    val imageUrl: String? = null,
    val category: String? = null
)

data class ApiUser(
    val id: Long? = null,
    val name: String? = null,
    val email: String,
    val password: String? = null,
    val role: String? = null
)

interface EventApiService {
    @GET("api/events")
    suspend fun getEvents(): List<ApiEvent>

    @POST("api/events/add")
    suspend fun addEvent(@Body event: ApiEvent): String

    @POST("api/users/send-otp")
    suspend fun sendOtp(@Body user: ApiUser): String

    @POST("api/users/verify-otp")
    suspend fun verifyOtp(@Body request: Map<String, String>): ApiUser

    @Deprecated("Use sendOtp and verifyOtp instead")
    @POST("api/users/login")
    suspend fun login(@Body user: ApiUser): ApiUser

    @Deprecated("Use sendOtp (which auto-registers) instead")
    @POST("api/users/signup")
    suspend fun signup(@Body user: ApiUser): ApiUser

    @PutMapping("api/users/update")
    suspend fun updateProfile(@Body user: ApiUser): ApiUser

    @POST("api/users/forgot-password")
    suspend fun forgotPassword(@Body request: Map<String, String>): String

    @POST("api/users/reset-password")
    suspend fun resetPassword(@Body request: Map<String, String>): String

    @GET("api/memories")
    suspend fun getMemories(): List<ApiMemory>

    @POST("api/memories/add")
    suspend fun addMemory(@Body memory: ApiMemory): ApiMemory

    @GET("api/registrations/user/{userId}")
    suspend fun getUserRegistrations(@Path("userId") userId: Long): List<ApiRegistration>

    @POST("api/registrations/add")
    suspend fun addRegistration(@Body registration: ApiRegistration): ApiRegistration

    @DELETE("api/registrations/cancel/{userId}/{eventId}")
    suspend fun cancelRegistration(
        @Path("userId") userId: Long,
        @Path("eventId") eventId: Long
    ): Unit
}

data class ApiRegistration(
    val id: Long? = null,
    val userId: Long,
    val eventId: Long,
    val userName: String? = null,
    val userEmail: String? = null,
    val status: String? = null,
    val eventRole: String? = null,
    val certificateId: String? = null
)

data class ApiMemory(
    val id: Long? = null,
    val userName: String? = null,
    val eventTitle: String? = null,
    val image: String? = null,
    val thought: String? = null,
    val rating: Int = 0,
    val date: String? = null
)

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val instance: EventApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EventApiService::class.java)
    }
}
