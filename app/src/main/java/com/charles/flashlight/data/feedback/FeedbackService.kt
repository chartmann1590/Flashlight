package com.charles.flashlight.data.feedback

import android.content.Context
import android.net.Uri
import retrofit2.Response

class FeedbackService(
    private val context: Context,
    private val api: GithubApi = GithubClient.api
) {
    fun configError(): String? = null

    suspend fun createIssue(
        title: String,
        description: String,
        name: String,
        email: String,
        includeDiagnostics: Boolean,
        attachmentUri: Uri?
    ): Result<GithubIssue> = runCatching {
        val attachmentUrl = uploadAttachmentIfPresent(attachmentUri)
        val body = buildIssueBody(description, name, email, includeDiagnostics, attachmentUrl)
        api.createIssue(
            request = CreateIssueRequest(title = "[Feedback] $title", body = body)
        ).bodyOrThrow("create issue")
    }

    suspend fun refreshIssue(number: Int): Result<GithubIssue> = runCatching {
        api.getIssue(number).bodyOrThrow("load issue")
    }

    suspend fun comments(number: Int): Result<List<GithubComment>> = runCatching {
        api.getComments(number).bodyOrThrow("load comments")
    }

    suspend fun postComment(
        number: Int,
        reply: String,
        attachmentUri: Uri?
    ): Result<GithubComment> = runCatching {
        val attachmentUrl = uploadAttachmentIfPresent(attachmentUri)
        val body = buildCommentBody(reply, attachmentUrl)
        api.postComment(number, PostCommentRequest(body)).bodyOrThrow("post comment")
    }

    private suspend fun uploadAttachmentIfPresent(uri: Uri?): String? {
        if (uri == null) return null
        val extension = extensionForUri(context, uri)
        val filename = uniqueFeedbackFilename(extension)
        val content = uriToBase64(context, uri)
        val response = api.uploadAsset(
            request = UploadAssetRequest(
                filename = filename,
                contentBase64 = content
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

    private fun <T> Response<T>.bodyOrThrow(action: String): T {
        if (isSuccessful) {
            return body() ?: throw IllegalStateException("GitHub returned an empty response while trying to $action.")
        }
        val errorText = runCatching { errorBody()?.string() }.getOrNull().orEmpty()
        val shortError = errorText.take(400).ifBlank { message() }
        throw IllegalStateException("GitHub could not $action (${code()}): $shortError")
    }
}
