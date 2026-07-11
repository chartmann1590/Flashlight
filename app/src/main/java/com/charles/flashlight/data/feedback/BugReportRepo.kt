package com.charles.flashlight.data.feedback

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.feedbackBugReportsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "feedback_bug_reports"
)

class BugReportRepo(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val bugReports: Flow<List<BugReport>> = context.feedbackBugReportsDataStore.data.map { prefs ->
        decodeReports(prefs[KEY_REPORTS])
    }

    suspend fun saveBugReport(report: BugReport) {
        val current = getBugReportsList().filterNot { it.number == report.number }
        updateBugReports((listOf(report) + current).sorted())
    }

    suspend fun updateBugReports(reports: List<BugReport>) {
        context.feedbackBugReportsDataStore.edit { prefs ->
            prefs[KEY_REPORTS] = json.encodeToString(reports.sorted())
        }
    }

    suspend fun getBugReportsList(): List<BugReport> {
        return decodeReports(context.feedbackBugReportsDataStore.data.first()[KEY_REPORTS])
    }

    private fun decodeReports(raw: String?): List<BugReport> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<BugReport>>(raw).sorted() }.getOrDefault(emptyList())
    }

    private fun List<BugReport>.sorted(): List<BugReport> =
        sortedWith(compareByDescending<BugReport> { it.createdAt }.thenByDescending { it.number })

    companion object {
        private val KEY_REPORTS = stringPreferencesKey("bug_reports_list")
    }
}
