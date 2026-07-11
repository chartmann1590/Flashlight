package com.charles.flashlight.data.feedback

import com.charles.flashlight.BuildConfig
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
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface GithubApi {
    @POST("repos/{owner}/{repo}/issues")
    suspend fun createIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body request: CreateIssueRequest
    ): Response<GithubIssue>

    @GET("repos/{owner}/{repo}/issues/{number}")
    suspend fun getIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: Int
    ): Response<GithubIssue>

    @GET("repos/{owner}/{repo}/issues/{number}/comments")
    suspend fun getComments(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: Int
    ): Response<List<GithubComment>>

    @POST("repos/{owner}/{repo}/issues/{number}/comments")
    suspend fun postComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: Int,
        @Body request: PostCommentRequest
    ): Response<GithubComment>

    @PUT("repos/{owner}/{repo}/contents/{assetDir}/{filename}")
    suspend fun uploadAsset(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("assetDir") assetDir: String,
        @Path("filename") filename: String,
        @Body request: UploadAssetRequest
    ): Response<UploadAssetResponse>
}

object GithubClient {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val config: GithubConfig
        get() = GithubConfig(
            token = BuildConfig.GITHUB_API_TOKEN.trim(),
            owner = BuildConfig.GITHUB_REPO_OWNER.trim(),
            repo = BuildConfig.GITHUB_REPO_NAME.trim(),
            assetDir = BuildConfig.FEEDBACK_ASSETS_DIR.trim().ifBlank { "feedback-assets" }
        )

    val api: GithubApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val cfg = config
                val requestBuilder = chain.request().newBuilder()
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "Flashlight-Android/0.1")
                if (cfg.token.isNotBlank()) {
                    requestBuilder.header("Authorization", "Bearer ${cfg.token}")
                }
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GithubApi::class.java)
    }
}

data class GithubConfig(
    val token: String,
    val owner: String,
    val repo: String,
    val assetDir: String
) {
    val isComplete: Boolean
        get() = token.isNotBlank() && owner.isNotBlank() && repo.isNotBlank()

    val missingMessage: String
        get() = buildList {
            if (token.isBlank()) add("GitHub API token")
            if (owner.isBlank()) add("GitHub repo owner")
            if (repo.isBlank()) add("GitHub repo name")
        }.joinToString(", ").ifBlank { "None" }
}
