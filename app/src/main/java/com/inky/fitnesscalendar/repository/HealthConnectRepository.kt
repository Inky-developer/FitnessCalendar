package com.inky.fitnesscalendar.repository

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.inky.fitnesscalendar.db.entities.Steps
import com.inky.fitnesscalendar.util.result.Raises
import com.inky.fitnesscalendar.util.result.TypedResult
import com.inky.fitnesscalendar.util.result.asErr
import com.inky.fitnesscalendar.util.result.asOk
import com.inky.fitnesscalendar.util.result.tryScope
import com.inky.fitnesscalendar.util.result.unwrap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val db: DatabaseRepository
) {
    fun checkHealthConnectAvailability(): TypedResult<Unit, HealthConnectError> {
        val availabilityStatus = HealthConnectClient.getSdkStatus(context)
        return when (availabilityStatus) {
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectError.UpdateRequired.asErr()
            HealthConnectClient.SDK_UNAVAILABLE -> HealthConnectError.Unavailable.asErr()
            HealthConnectClient.SDK_AVAILABLE -> Unit.asOk()
            else -> HealthConnectError.Unknown.asErr()
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun rememberPermissions(onGranted: () -> Unit) =
        rememberPermissionState(
            "android.permission.health.READ_STEPS",
            onPermissionResult = { if (it) onGranted() }
        )

    suspend fun synchronize(): TypedResult<Unit, HealthConnectError> = tryScope {
        val stepData = aggregate()
        db.recordSteps(stepData)
    }

    context(_: Raises<HealthConnectError>)
    private suspend fun aggregate(): List<Steps> {
        val healthConnect = healthConnectClient()

        return healthConnect.aggregateGroupByDuration(
            AggregateGroupByDurationRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.after(
                    LocalDate.now().minusDays(30).atStartOfDay()
                ),
                timeRangeSlicer = Duration.ofSeconds(Steps.SECONDS_PER_FRAME.toLong())
            )
        ).mapNotNull { row ->
            val startTime = LocalDateTime.ofInstant(row.startTime, row.zoneOffset)
            val steps = row.result[StepsRecord.COUNT_TOTAL] ?: return@mapNotNull null
            Steps(dateTime = startTime, steps = steps.toInt())
        }
    }

    context(_: Raises<HealthConnectError>)
    private fun healthConnectClient(): HealthConnectClient {
        checkHealthConnectAvailability().unwrap()
        return HealthConnectClient.getOrCreate(context)
    }


    enum class HealthConnectError {
        Unavailable,
        UpdateRequired,
        Unknown
    }
}