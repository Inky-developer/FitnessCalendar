package com.inky.fitnesscalendar.view_model.import

import com.inky.fitnesscalendar.data.gpx.GpxTrack
import com.inky.fitnesscalendar.data.gpx.TrackSvg
import com.inky.fitnesscalendar.data.gpx.TrackSvg.Companion.toTrackSvg
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.Activity.SerializedTrackPreview.Companion.serialize
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.db.entities.Track

data class ImportTrack(
    val track: GpxTrack,
    val dbTrack: Track = Track(activityId = -1, points = track.points),
    val trackSvg: TrackSvg? = track.toTrackSvg()
) {
    fun toRichActivity(type: ActivityType): RichActivity? {
        val stats = track.computeStatistics()
        return RichActivity(
            activity = Activity(
                typeId = type.uid ?: return null,
                description = track.name,
                startTime = track.startTime ?: return null,
                endTime = track.endTime ?: return null,
                distance = stats?.totalDistance,
                movingDuration = stats?.movingDuration,
                trackPreview = trackSvg?.serialize()
            ),
            type = type,
            place = null,
        )
    }
}