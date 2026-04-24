package com.torresagro.app.data.firebase

import android.content.Context
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsTracker {
    private fun analytics(context: Context): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context.applicationContext)

    fun setUserId(context: Context, userId: String?) {
        analytics(context).setUserId(userId)
    }

    fun clearUserId(context: Context) {
        analytics(context).setUserId(null)
    }

    fun logScreenView(context: Context, route: String) {
        analytics(context).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundleOf(
            FirebaseAnalytics.Param.SCREEN_NAME to route,
            FirebaseAnalytics.Param.SCREEN_CLASS to "TorresAgroApp"
        ))
    }

    fun logLoginAttempt(context: Context, provider: String) {
        analytics(context).logEvent("login_attempt", bundleOf(
            "provider" to provider
        ))
    }

    fun logLoginResult(context: Context, provider: String, result: String) {
        analytics(context).logEvent("login_result", bundleOf(
            "provider" to provider,
            "result" to result
        ))
    }

    fun logSignOut(context: Context) {
        analytics(context).logEvent("sign_out", null)
    }

    fun logParcelSaved(
        context: Context,
        cropType: String,
        hasBoundary: Boolean,
        hasCoordinates: Boolean,
        isEdit: Boolean
    ) {
        analytics(context).logEvent("parcel_saved", bundleOf(
            "crop_type" to cropType,
            "has_boundary" to hasBoundary,
            "has_coordinates" to hasCoordinates,
            "mode" to if (isEdit) "edit" else "create"
        ))
    }

    fun logTaskSaved(
        context: Context,
        taskType: String,
        priority: String,
        reminderEnabled: Boolean,
        isEdit: Boolean
    ) {
        analytics(context).logEvent("task_saved", bundleOf(
            "task_type" to taskType,
            "priority" to priority,
            "reminder_enabled" to reminderEnabled,
            "mode" to if (isEdit) "edit" else "create"
        ))
    }

    fun logInventorySaved(
        context: Context,
        category: String,
        lowStock: Boolean,
        isEdit: Boolean
    ) {
        analytics(context).logEvent("inventory_saved", bundleOf(
            "category" to category,
            "low_stock" to lowStock,
            "mode" to if (isEdit) "edit" else "create"
        ))
    }

    fun logActivitySaved(
        context: Context,
        activityType: String,
        hasPhoto: Boolean,
        isEdit: Boolean
    ) {
        analytics(context).logEvent("activity_saved", bundleOf(
            "activity_type" to activityType,
            "has_photo" to hasPhoto,
            "mode" to if (isEdit) "edit" else "create"
        ))
    }

    fun logObservationSaved(
        context: Context,
        status: String,
        symptomCount: Int,
        hasPhoto: Boolean,
        isEdit: Boolean
    ) {
        analytics(context).logEvent("observation_saved", bundleOf(
            "status" to status,
            "symptom_count" to symptomCount.toLong(),
            "has_photo" to hasPhoto,
            "mode" to if (isEdit) "edit" else "create"
        ))
    }

    fun logReportGenerated(
        context: Context,
        cropType: String,
        hasAgriData: Boolean,
        result: String
    ) {
        analytics(context).logEvent("parcel_report", bundleOf(
            "crop_type" to cropType,
            "has_agri_data" to hasAgriData,
            "result" to result
        ))
    }

    fun logManualSync(context: Context, source: String) {
        analytics(context).logEvent("manual_sync", bundleOf(
            "source" to source
        ))
    }

    fun logSmartScreenSummary(
        context: Context,
        suggestionCount: Int,
        libraryCount: Int,
        parcelAnalysisCount: Int
    ) {
        analytics(context).logEvent("smart_screen_view", bundleOf(
            "suggestion_count" to suggestionCount.toLong(),
            "library_count" to libraryCount.toLong(),
            "parcel_analysis_count" to parcelAnalysisCount.toLong()
        ))
    }

    fun logSmartSuggestionOpen(
        context: Context,
        source: String,
        priority: String,
        confidencePercent: Int
    ) {
        analytics(context).logEvent("smart_suggestion_open", bundleOf(
            "source" to source,
            "priority" to priority,
            "confidence_pct" to confidencePercent.toLong()
        ))
    }

    fun logTechnicalSheetOpen(context: Context, cropType: String) {
        analytics(context).logEvent("technical_sheet_open", bundleOf(
            "crop_type" to cropType
        ))
    }

    fun logAlertsCenterSummary(
        context: Context,
        alertCount: Int,
        recommendationCount: Int
    ) {
        analytics(context).logEvent("alerts_center_view", bundleOf(
            "alert_count" to alertCount.toLong(),
            "recommendation_count" to recommendationCount.toLong()
        ))
    }

    fun logAlertOpen(context: Context, severity: String) {
        analytics(context).logEvent("alert_open", bundleOf(
            "severity" to severity
        ))
    }

    fun logRecommendationOpen(context: Context, recommendationType: String) {
        analytics(context).logEvent("recommendation_open", bundleOf(
            "recommendation_type" to recommendationType
        ))
    }

    fun logMapLocationRequest(
        context: Context,
        trigger: String,
        result: String,
        pointCount: Int
    ) {
        analytics(context).logEvent("map_location_req", bundleOf(
            "trigger" to trigger,
            "result" to result,
            "point_count" to pointCount.toLong()
        ))
    }
}
