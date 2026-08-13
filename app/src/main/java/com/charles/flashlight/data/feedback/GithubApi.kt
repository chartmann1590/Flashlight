package com.charles.flashlight.data.feedback

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface GithubApi {
    @POST("issue")
    suspend fun createIssue(
        @Body request: CreateIssueRequest
    ): Response<GithubIssue>

    @GET("issue/{number}")
    suspend fun getIssue(
        @Path("number") number: Int
    ): Response<GithubIssue>

    @GET("issue/{number}/comments")
    suspend fun getComments(
        @Path("number") number: Int
    ): Response<List<GithubComment>>

    @POST("issue/{number}/comments")
    suspend fun postComment(
        @Path("number") number: Int,
        @Body request: PostCommentRequest
    ): Response<GithubComment>

    @POST("upload-image")
    suspend fun uploadAsset(
        @Body request: UploadAssetRequest
    ): Response<UploadAssetResponse>
}

/**
 * Talks to the cloudflare-worker/ feedback relay, not api.github.com directly. See
 * cloudflare-worker/src/index.ts, which holds the GitHub token server-side as a Worker
 * secret. Previously this embedded BuildConfig.GITHUB_API_TOKEN client-side as a Bearer
 * header, which shipped a real repo-write PAT in every release build (extractable from
 * the APK).
 */
object GithubClient {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val api: GithubApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://flashlight-github-feedback.charles-h-hartmann1.workers.dev/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GithubApi::class.java)
    }
}
