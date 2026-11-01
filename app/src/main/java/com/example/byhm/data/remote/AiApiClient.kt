package com.example.byhm.data.remote

import com.example.byhm.data.model.AlertItem
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class ClassificationRequest(
    val description: String
)

data class ClassificationResponse(
    val predicted_category: String,
    val confidence_score: Float,
    val factual_summary: String,
    val recommended_actions: List<String>,
    val disclaimer: String
)

data class ChatMsgDto(
    val role: String,
    val content: String
)

data class ConversationalAiRequestDto(
    val message: String,
    val conversationHistory: List<ChatMsgDto> = emptyList(),
    val language: String = "en"
)

data class ConversationalAiResponseDto(
    val response: String,
    val suggestedActions: List<String> = emptyList(),
    val disclaimer: String
)

data class SosTriggerRequestDto(
    val locationName: String,
    val latitude: Double? = 28.6139,
    val longitude: Double? = 77.2090,
    val emergencyProfileText: String
)

data class SosTriggerResponseDto(
    val status: String,
    val sosId: String,
    val message: String
)

interface AiApiService {
    @POST("/predict-category")
    suspend fun predictCategory(@Body request: ClassificationRequest): ClassificationResponse

    @POST("/api/ai/chat")
    suspend fun sendConversationalChat(@Body request: ConversationalAiRequestDto): ConversationalAiResponseDto

    @POST("/api/sos/trigger")
    suspend fun triggerSosCallout(@Body request: SosTriggerRequestDto): SosTriggerResponseDto

    @GET("/api/alerts")
    suspend fun getPublicAlerts(
        @Query("state") state: String? = null,
        @Query("category") category: String? = null,
        @Query("severity") severity: String? = null
    ): List<AlertItem>

    @GET("/api/alerts/history")
    suspend fun getAlertsHistory(
        @Query("state") state: String? = null,
        @Query("category") category: String? = null
    ): List<AlertItem>
}

object AiApiClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: AiApiService = retrofit.create(AiApiService::class.java)
}
