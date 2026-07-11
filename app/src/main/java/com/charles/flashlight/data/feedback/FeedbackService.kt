package com.charles.flashlight.data.feedback

import android.content.Context
import android.net.Uri
import retrofit2.Response

class FeedbackService(
    private val context: Context,
    private val api: GithubApi = GithubClient.api,
    private val config: GithubConfig = GithubClient.config
) {
    fun configError(): String? {
        return if (config.isComplete) null else "Feedback is not configured. Missing: ${config.missingMessage}."
    }

    suspend fun createIssue(
        title: String,
        description: String,
        name: String,
        email: String,
        includeDiagnostics: Boolean,
        attachmentUri: Uri?
    ): Result<GithubIssue> = runCatching {
        requireConfigured()
        val attachmentUrl = uploadAttachmentIfPresent(attachmentUri, "feedback attachment")
        val body = buildIssueBody(description, name, email, includeDiagnostics, attachmentUrl)
        api.createIssue(
            owner = config.owner,
            repo = config.repo,
            request = CreateIssueRequest(title = "[Feedback] $title", body = body)
        ).bodyOrThrow("create issue")
    }

    suspend fun refreshIssue(number: Int): Result<GithubIssue> = runCatching {
        requireConfigured()
        api.getIssue(config.owner, config.repo, number).bodyOrThrow("load issue")
    }

    suspend fun comments(number: Int): Result<List<GithubComment>> = runCatching {
        requireConfigured()
        api.getComments(config.owner, config.repo, number).bodyOrThrow("load comments")
    }

    suspend fun postComment(
        number: Int,
        reply: String,
        attachmentUri: Uri?
    ): Result<GithubComment> = runCatching {
        requireConfigured()
        val attachmentUrl = uploadAttachmentIfPresent(attachmentUri, "feedback comment attachment")
        val body = buildCommentBody(reply, attachmentUrl)
        api.postComment(config.owner, config.repo, number, PostCommentRequest(body)).bodyOrThrow("post comment")
    }

    private suspend fun uploadAttachmentIfPresent(uri: Uri?, message: String): String? {
        if (uri == null) return null
        val extension = extensionForUri(context, uri)
        val filename = uniqueFeedbackFilename(extension)
        val content = uriToBase64(context, uri)
        val response = api.uploadAsset(
            owner = config.owner,
            repo = config.repo,
            assetDir = config.assetDir,
            filename = filename,
            request = UploadAssetRequest(
                message = "$message $filename",
                content = content
            )
        ).bodyOrThrow("upload attachment")
        return response.content?.downloadUrl ?: response.content?.htmlUrl
            ?: throw IllegalStateException("GitHub did not return an attachment URL.")
    }

    private fun buildIssueBody(
        description: String,
        name: String,
        email: String,
        includeDiagnostics: Boolean,
        attachmentUrl: String?
    ): String = buildString {
        appendLine("## Description")
        appendLine()
        appendLine(description.trim())
        appendLine()
        appendLine("## Contact Info")
        appendLine()
        appendLine("- Name: ${name.ifBlank { "Not provided" }}")
        appendLine("- Email: ${email.ifBlank { "Not provided" }}")
        if (attachmentUrl != null) {
            appendLine()
            appendLine("## Attachment")
            appendLine()
            appendLine("![Screenshot]($attachmentUrl)")
        }
        if (includeDiagnostics) {
            appendLine()
            appendLine(DiagnosticsHelper.collectMarkdown(context))
        }
    }

    private fun buildCommentBody(reply: String, attachmentUrl: String?): String = buildString {
        appendLine("## Reply")
        appendLine()
        appendLine(reply.trim())
        if (attachmentUrl != null) {
            appendLine()
            appendLine("## Attachment")
            appendLine()
            appendLine("![Screenshot]($attachmentUrl)")
        }
    }

    private fun requireConfigured() {
        val error = configError()
        if (error != null) throw IllegalStateException(error)
    }

    private fun <T> Response<T>.bodyOrThrow(action: String): T {
        if (isSuccessful) {
            return body() ?: throw IllegalStateException("GitHub returned an empty response while trying to $action.")
        }
        val errorText = runCatching { errorBody()?.string() }.getOrNull().orEmpty()
        val shortError = errorText.take(400).ifBlank { message() }
        throw IllegalStateException("GitHub could not $action (${code()}): $shortError")
    }
}
