package com.inky.fitnesscalendar.repository

import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.runtime.Immutable
import com.inky.fitnesscalendar.data.gpx.GpxTrack
import com.inky.fitnesscalendar.data.gpx.TrackSvg
import com.inky.fitnesscalendar.data.gpx.TrackSvg.Companion.toTrackSvg
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.Activity.SerializedTrackPreview.Companion.serialize
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.db.entities.Track
import com.inky.fitnesscalendar.util.gpx.GpxReader
import kotlinx.coroutines.flow.first
import java.io.FileInputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ImportRepository"

@Immutable
@Singleton
class ImportRepository @Inject constructor(private val dbRepository: DatabaseRepository) {
    /**
     * Tries to import the given files without user interaction.
     * This only works if the activity type mapping is known.
     * Either imports all files or none, and returns true iff successful.
     */
    suspend fun tryImportFiles(files: List<ParcelFileDescriptor>): Boolean {
        val tracks = loadFiles(files)
        val activityTypeNames = dbRepository.getActivityTypeNames().first()

        val importData = tracks.map { importTrack ->
            val activityType = activityTypeNames[importTrack.track.type] ?: return false
            val richActivity = importTrack.toRichActivity(activityType) ?: return false
            richActivity to importTrack
        }

        for ((richActivity, track) in importData) {
            importActivity(richActivity, track)
        }

        return true
    }

    /**
     * Tries to import the track as activity and returns the activity id in case of success
     * or null if the track could not get imported
     */
    suspend fun importTrack(importTrack: ImportTrack, type: ActivityType): Int? {
        val activity = importTrack.toRichActivity(type) ?: return null
        return importActivity(activity, importTrack)
    }

    private suspend fun importActivity(richActivity: RichActivity, importTrack: ImportTrack): Int {
        val activityId = dbRepository.saveActivity(richActivity)
        val track = Track(activityId = activityId, points = importTrack.track.points)
        dbRepository.saveTrack(track)
        return activityId
    }

    /**
     * Recalculates statistics for all activities with a track, in case the calculation has changed
     * in an update, for example
     */
    suspend fun updateTrackActivities(): Int {
        var numChangedActivities = 0

        val tracks = dbRepository.loadTracks()
        for (track in tracks) {
            val richActivity = dbRepository.loadActivity(track.activityId)

            val updatedActivity = track.addStatsToActivity(richActivity.activity) ?: continue
            val cleanedActivity = updatedActivity.clean(richActivity.type)
            if (cleanedActivity == richActivity.activity) {
                continue
            }
            Log.d(TAG, "updateTrackActivities: From ${richActivity.activity}")
            Log.d(TAG, "updateTrackActivities:   To $cleanedActivity")
            numChangedActivities += 1
            dbRepository.saveActivity(richActivity.copy(activity = cleanedActivity))
        }

        return numChangedActivities
    }

    fun loadFiles(files: List<ParcelFileDescriptor>): List<ImportTrack> {
        return files.flatMap {
            it.use { descriptor ->
                val actualDescriptor = descriptor.fileDescriptor
                val stream = FileInputStream(actualDescriptor)
                loadFile(stream)
            }
        }
    }


    private fun loadFile(stream: InputStream): List<ImportTrack> {
        val gpx = GpxReader.read(stream)
        return gpx?.tracks?.map { ImportTrack(track = it) } ?: emptyList()
    }

    data class ImportTrack(
        val track: GpxTrack,
        val dbTrack: Track = Track(activityId = -1, points = track.points),
        val trackSvg: TrackSvg? = track.toTrackSvg()
    ) {
        fun toRichActivity(type: ActivityType): RichActivity? {
            val initialActivity = Activity(
                typeId = type.uid ?: return null,
                description = track.name,
                startTime = track.startTime ?: return null,
                endTime = track.endTime ?: return null,
                trackPreview = trackSvg?.serialize()
            )

            val activity = dbTrack.addStatsToActivity(initialActivity) ?: return null

            return RichActivity(
                activity = activity,
                type = type,
                place = null,
            )
        }
    }
}