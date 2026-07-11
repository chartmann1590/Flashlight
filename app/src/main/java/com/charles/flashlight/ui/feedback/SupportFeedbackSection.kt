package com.charles.flashlight.ui.feedback

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.flashlight.data.feedback.BugReport
import com.charles.flashlight.data.feedback.BugReportRepo
import com.charles.flashlight.data.feedback.FeedbackService
import com.charles.flashlight.data.feedback.GithubClient
import com.charles.flashlight.data.feedback.GithubComment
import com.charles.flashlight.data.feedback.GithubIssue
import com.charles.flashlight.data.feedback.toBugReport
import kotlinx.coroutines.launch

@Composable
fun SupportFeedbackSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val repo = remember { BugReportRepo(appContext) }
    val service = remember { FeedbackService(appContext) }
    val reports by repo.bugReports.collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<BugReport?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Support & Feedback",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(vertical = 8.dp)) {
                ListItem(
                    headlineContent = { Text("Support & Feedback") },
                    supportingContent = {
                        Text("Report app problems, attach screenshots, and track submitted GitHub issues.")
                    },
                    leadingContent = { Icon(Icons.Outlined.BugReport, contentDescription = null) },
                    trailingContent = {
                        Button(onClick = { showReportDialog = true }) {
                            Text("Report a Problem")
                        }
                    }
                )
                if (message != null) {
                    Text(
                        text = message.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                if (reports.isEmpty()) {
                    Text(
                        text = "No submitted reports on this device yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else {
                    reports.forEachIndexed { index, report ->
                        if (index > 0) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        ReportRow(report = report, onClick = { selectedReport = report })
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        ReportProblemDialog(
            service = service,
            onDismiss = { showReportDialog = false },
            onSubmitted = { issue ->
                scope.launch { repo.saveBugReport(issue.toBugReport()) }
                message = "Submitted GitHub issue #${issue.number}."
                showReportDialog = false
            }
        )
    }

    selectedReport?.let { report ->
        IssueDetailsDialog(
            report = report,
            repo = repo,
            service = service,
            onDismiss = { selectedReport = null }
        )
    }
}

@Composable
private fun ReportRow(report: BugReport, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(report.title, maxLines = 2)
        },
        supportingContent = {
            Text("#${report.number} • ${report.createdAt.take(10)}")
        },
        trailingContent = {
            StatusChip(report.status)
        }
    )
}

@Composable
private fun StatusChip(status: String) {
    val closed = status.equals("closed", ignoreCase = true)
    val color = if (closed) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val textColor = if (closed) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.ifBlank { "open" },
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ReportProblemDialog(
    service: FeedbackService,
    onDismiss: () -> Unit,
    onSubmitted: (GithubIssue) -> Unit
) {
    val scope = rememberCoroutineScope()
    val configError = remember { service.configError() }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var includeDiagnostics by remember { mutableStateOf(true) }
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        attachmentUri = uri
    }

    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        title = { Text("Report a Problem") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WarningBox(
                    "Your report will be submitted to this app's GitHub issue tracker. Do not include passwords, private keys, medical information, financial information, or anything you do not want visible to repository maintainers. If this repository is public, your report may be publicly visible. Screenshots may contain private information."
                )
                if (configError != null) {
                    Text(configError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Subject") },
                    singleLine = true,
                    isError = title.isBlank() && error != null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 4,
                    isError = description.isBlank() && error != null,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeDiagnostics, onCheckedChange = { includeDiagnostics = it })
                    Text("Include phone/app diagnostics")
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                AttachmentPicker(
                    attachmentUri = attachmentUri,
                    onPick = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onClear = { attachmentUri = null }
                )
                attachmentUri?.let { ImagePreview(uri = it) }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(
                enabled = !submitting && configError == null,
                onClick = {
                    error = when {
                        title.isBlank() -> "Title is required."
                        description.isBlank() -> "Description is required."
                        else -> null
                    }
                    if (error != null) return@Button
                    submitting = true
                    scope.launch {
                        service.createIssue(
                            title = title,
                            description = description,
                            name = name,
                            email = email,
                            includeDiagnostics = includeDiagnostics,
                            attachmentUri = attachmentUri
                        ).onSuccess(onSubmitted).onFailure { e ->
                            error = e.message ?: "Unable to submit report."
                        }
                        submitting = false
                    }
                }
            ) {
                if (submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Submit")
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !submitting, onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IssueDetailsDialog(
    report: BugReport,
    repo: BugReportRepo,
    service: FeedbackService,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var issue by remember(report.number) { mutableStateOf<GithubIssue?>(null) }
    var comments by remember(report.number) { mutableStateOf<List<GithubComment>>(emptyList()) }
    var loading by remember(report.number) { mutableStateOf(true) }
    var posting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var reply by remember { mutableStateOf("") }
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        attachmentUri = uri
    }

    fun refresh() {
        loading = true
        error = null
        scope.launch {
            service.refreshIssue(report.number).onSuccess { latest ->
                issue = latest
                repo.saveBugReport(latest.toBugReport())
            }.onFailure { e ->
                error = e.message ?: "Unable to refresh issue."
            }
            service.comments(report.number).onSuccess { comments = it }.onFailure { e ->
                error = e.message ?: "Unable to load comments."
            }
            loading = false
        }
    }

    LaunchedEffect(report.number) { refresh() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(issue?.title ?: report.title, style = MaterialTheme.typography.titleLarge)
                        Text("#${report.number}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusChip(issue?.state ?: report.status)
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { uriHandler.openUri(issue?.htmlUrl ?: report.htmlUrl) }) {
                        Text("Open GitHub")
                    }
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
                if (loading) {
                    CircularProgressIndicator()
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("Comments", style = MaterialTheme.typography.titleMedium)
                    }
                    if (comments.isEmpty()) {
                        item {
                            Text(
                                "No comments yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    items(comments, key = { it.id }) { comment ->
                        ElevatedCard(
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "${comment.user.login} • ${comment.createdAt.take(10)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(comment.body, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = reply,
                    onValueChange = { reply = it },
                    label = { Text("Reply") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                AttachmentPicker(
                    attachmentUri = attachmentUri,
                    onPick = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onClear = { attachmentUri = null }
                )
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        enabled = !posting && reply.isNotBlank(),
                        onClick = {
                            posting = true
                            error = null
                            scope.launch {
                                service.postComment(report.number, reply, attachmentUri).onSuccess {
                                    reply = ""
                                    attachmentUri = null
                                    refresh()
                                }.onFailure { e ->
                                    error = e.message ?: "Unable to post reply."
                                }
                                posting = false
                            }
                        }
                    ) {
                        Icon(Icons.Outlined.AddComment, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (posting) "Posting..." else "Submit Reply")
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentPicker(
    attachmentUri: Uri?,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(onClick = onPick) {
            Icon(Icons.Outlined.AttachFile, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (attachmentUri == null) "Attach screenshot/image" else "Change attachment")
        }
        if (attachmentUri != null) {
            TextButton(onClick = onClear) { Text("Remove") }
        }
    }
}

@Composable
private fun WarningBox(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun ImagePreview(uri: Uri) {
    val context = LocalContext.current
    val bitmap = remember(uri) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Selected attachment preview",
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentScale = ContentScale.Crop
        )
    } else {
        AssistChip(onClick = {}, label = { Text("Attachment selected") })
    }
}
