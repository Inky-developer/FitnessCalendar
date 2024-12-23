package com.inky.fitnesscalendar.db.entities

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.gpx.GpxTrackPoint
import com.inky.fitnesscalendar.data.gpx.GpxTrackStats
import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.data.measure.Duration
import com.inky.fitnesscalendar.data.measure.Duration.Companion.until
import com.inky.fitnesscalendar.data.measure.Elevation
import com.inky.fitnesscalendar.data.measure.HeartFrequency
import com.inky.fitnesscalendar.data.measure.Speed
import com.inky.fitnesscalendar.data.measure.Temperature
import com.inky.fitnesscalendar.repository.ImportRepository.ImportError
import com.inky.fitnesscalendar.util.result.TypedResult
import com.inky.fitnesscalendar.util.result.tryScope
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt

private const val TAG = "Track"

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = ["uid"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("activity_id")]
)
data class Track(
    @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = "activity_id") val activityId: Int,
    @ColumnInfo(name = "points") val points: List<GpxTrackPoint>
) {
    val startTime get() = points.firstOrNull()?.time
    val endTime get() = points.lastOrNull()?.time

    private val totalDuration get() = endTime?.let { startTime?.until(it) }

    /**
     * Adds all relevant data of this track to the activity
     */
    fun addStatsToActivity(activity: Activity): TypedResult<Activity, ImportError> = tryScope {
        val stats = computeStatistics().unwrap()
        activity.copy(
            startTime = startTime ?: raise(ImportError.NoStartAndEndTime),
            endTime = endTime ?: raise(ImportError.NoStartAndEndTime),
            distance = stats.totalDistance,
            movingDuration = stats.movingDuration,
            temperature = stats.averageTemperature,
            averageHeartRate = stats.averageHeartFrequency,
            maximalHeartRate = stats.maxHeartFrequency,
            totalAscent = stats.totalAscent,
            totalDescent = stats.totalDescent
        )
    }

    fun computedPoints(): List<ComputedTrackPoint> {
        val computedPoints = computeTrackPoints(points)

        // Unfortunately gpx files have some outliers, so here we remove all points with an unlikely acceleration
        // FIXME: the distance data can still be messed up here
        val filteredPoints = filterOutliers(computedPoints)
        if (filteredPoints.size != computedPoints.size) {
            Log.i(TAG, "Removed ${computedPoints.size - filteredPoints.size} outlier(s)")
        }
        return filteredPoints
    }

    fun computeStatistics(): TypedResult<GpxTrackStats, ImportError> = tryScope {
        val duration = totalDuration ?: raise(ImportError.NoStartAndEndTime)

        val trackPointComputedData = computedPoints()

        val totalDistance =
            Distance(meters = trackPointComputedData.sumOf { it.distanceMeters }.roundToLong())
        val movingDuration = Duration(
            elapsedMs = trackPointComputedData
                .filter { it.speed.kmh > 3 }
                .sumOf { it.duration.elapsedMs }
        )

        val maxSpeed = trackPointComputedData.maxByOrNull { it.speed.metersPerSecond }?.speed
            ?: Speed(metersPerSecond = 0.0)

        val averageHeartRate = points.mapNotNull { it.heartFrequency?.bpm }.let {
            if (it.isEmpty()) {
                null
            } else {
                HeartFrequency(bpm = it.sum() / it.size)
            }
        }
        val maxHeartRate = points.mapNotNull { it.heartFrequency }.maxByOrNull { it }

        val averageTemperature = points
            .mapNotNull { it.temperature?.celsius }
            .let {
                if (it.isEmpty()) {
                    null
                } else {
                    Temperature(celsius = it.sum() / it.size)
                }
            }
        val minTemperature = points.mapNotNull { it.temperature }.minByOrNull { it }
        val maxTemperature = points.mapNotNull { it.temperature }.maxByOrNull { it }

        val minHeight = points.mapNotNull { it.elevation }.minByOrNull { it }
        val maxHeight = points.mapNotNull { it.elevation }.maxByOrNull { it }

        var totalAscent: Distance? = null
        var totalDescent: Distance? = null
        if (minHeight != null) {
            var ascent = 0.0
            var descent = 0.0
            for ((prev, curr) in trackPointComputedData.windowed(2)) {
                val prevEle = prev.computedElevation
                val currEle = curr.computedElevation
                if (prevEle == null || currEle == null) continue

                if (prevEle > currEle) {
                    descent += prevEle.meters - currEle.meters
                } else {
                    ascent += currEle.meters - prevEle.meters
                }
            }
            totalAscent = Distance(meters = ascent.roundToLong())
            totalDescent = Distance(meters = descent.roundToLong())
        }

        GpxTrackStats(
            totalDistance = totalDistance,
            totalDuration = duration,
            movingDuration = movingDuration,
            maxSpeed = maxSpeed,
            averageHeartFrequency = averageHeartRate,
            maxHeartFrequency = maxHeartRate,
            averageTemperature = averageTemperature,
            minTemperature = minTemperature,
            maxTemperature = maxTemperature,
            minHeight = minHeight,
            maxHeight = maxHeight,
            totalAscent = totalAscent,
            totalDescent = totalDescent
        )
    }

    data class ComputedTrackPoint(
        val distanceMeters: Double,
        val cumDistance: Distance,
        val duration: Duration,
        val point: GpxTrackPoint,
        var computedElevation: Elevation? = null,
        var acceleration: Double = 0.0,
    ) {
        val speed
            get() = if (duration.elapsedMs > 0)
                Speed(metersPerSecond = distanceMeters / duration.elapsedSeconds)
            else
                Speed(metersPerSecond = 0.0)
    }

    companion object {
        private fun computeTrackPoints(points: List<GpxTrackPoint>): List<ComputedTrackPoint> {
            if (points.isEmpty()) {
                return emptyList()
            }

            val cachedDistanceResultsArray = FloatArray(3)
            var cumDistanceMeters = 0.0
            val firstComputedTrackPoint = ComputedTrackPoint(
                distanceMeters = 0.0,
                cumDistance = Distance(meters = cumDistanceMeters.roundToLong()),
                duration = Duration(elapsedMs = 0L),
                point = points[0]
            )

            val computedPoints = mutableListOf(firstComputedTrackPoint)
            points
                .windowed(2)
                .mapTo(computedPoints) { (a, b) ->
                    val distance =
                        a.coordinate.distanceMeters(b.coordinate, cachedDistanceResultsArray)
                    cumDistanceMeters += distance
                    val duration = a.time.until(b.time)
                    ComputedTrackPoint(
                        distanceMeters = distance,
                        cumDistance = Distance(meters = cumDistanceMeters.roundToLong()),
                        duration = duration,
                        point = b,
                    )
                }

            for ((p1, p2) in computedPoints.subList(1, computedPoints.size).windowed(2)) {
                p1.acceleration = (p1.speed - p2.speed).metersPerSecond / p1.duration.elapsedSeconds
            }

            smoothElevations(computedPoints)

            return computedPoints
        }

        /**
         * Implements a moving average to smooth elevations out a bit
         */
        private fun smoothElevations(data: List<ComputedTrackPoint>) {
            // This is quite arbitrary, but yields somewhat decent results
            // Still, the computed totals ascent and descent can differ wildly from source data :(
            val windowSize = 6
            var runningSum = 0f

            if (data.size <= windowSize) {
                for (point in data) {
                    point.computedElevation = point.point.elevation
                }
                return
            }

            for (point in data.subList(0, windowSize)) {
                runningSum += point.point.elevation?.meters ?: 0f
                point.computedElevation = point.point.elevation
            }

            for ((index, point) in data.subList(windowSize / 2, data.size - windowSize / 2)
                .withIndex()) {
                point.computedElevation = Elevation(meters = runningSum / windowSize)
                runningSum -= data[index].point.elevation?.meters ?: 0f
                runningSum += data[index + windowSize].point.elevation?.meters ?: 0f
            }

            for (point in data.subList(data.size - windowSize / 2, data.size)) {
                point.computedElevation = Elevation(meters = runningSum / windowSize)
            }
        }

        private fun filterOutliers(points: List<ComputedTrackPoint>): List<ComputedTrackPoint> {
            val accelerations = points.map { it.acceleration }
            val accAvg = accelerations.average()
            val accStd = sqrt(accelerations.map { (it - accAvg).pow(2) }.average())

            val (accMin, accMax) = normalDistributionQuantile(accAvg, accStd)
            return points.filter { it.acceleration > accMin && it.acceleration < accMax }
        }

        /// Returns quantiles with 90% probability [0.05 to 0.95] for the given normal distribution
        private fun normalDistributionQuantile(
            mean: Double,
            standardDeviation: Double
        ): Pair<Double, Double> {
            // See https://en.wikipedia.org/wiki/Normal_distribution#Quantile_function
            val z = 1.959963984540
            val quantile0995 = mean + standardDeviation * z
            return -quantile0995 to quantile0995
        }
    }
}
