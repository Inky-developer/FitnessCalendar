package com.inky.fitnesscalendar.util.gpx

import com.inky.fitnesscalendar.data.gpx.Coordinate
import java.util.PriorityQueue
import kotlin.math.absoluteValue

private const val TAG = "util/gpx/simplification"

/**
 * Simplifies the track by removing the most irrelevant points of the track until only a fixed number
 * of points is left.
 * A point is irrelevant if the triangle formed by its predecessor and ancestor has a low area.
 *
 * @param maxNumPoints Keep at most that many points
 */
fun simplify(points: List<Coordinate>, maxNumPoints: Int = 100): List<Coordinate> {
    val numPointsToRemove = points.size - maxNumPoints
    if (numPointsToRemove <= 0) {
        return points
    }

    val triangles = points.indices.map { index ->
        Triangle(
            area = calculateArea(
                points.getOrNull(index - 1),
                points[index],
                points.getOrNull(index + 1)
            ),
            coordinate = points[index],
            predecessorIndex = index - 1,
            index = index,
            successorIndex = index + 1,
        )
    }
    // TODO: This Priority Queue is pretty useless, since `remove` is O(n)
    val sortedTriangles = PriorityQueue(triangles)
    for (i in 0..<numPointsToRemove) {
        val toRemove = sortedTriangles.first()
        sortedTriangles.remove(toRemove)

        triangles.getOrNull(toRemove.predecessorIndex)?.apply {
            sortedTriangles.remove(this)
            successorIndex = toRemove.successorIndex
            updateArea(triangles, points)
            sortedTriangles.add(this)
        }
        triangles.getOrNull(toRemove.successorIndex)?.apply {
            sortedTriangles.remove(this)
            predecessorIndex = toRemove.predecessorIndex
            updateArea(triangles, points)
            sortedTriangles.add(this)
        }
    }

    val pointsToKeep = sortedTriangles.map { it.index }.toSet()
    val coordinates = points
        .asSequence()
        .filterIndexed { index, _ -> pointsToKeep.contains(index) }
        .toList()
    return coordinates
}

// Lets hope this is correct
private fun calculateArea(a: Coordinate?, b: Coordinate, c: Coordinate?): Float {
    // The start and end point should never get removed and thus will be assigned infinite area
    if (a == null || c == null) return Float.POSITIVE_INFINITY
    return 0.5f * (a.latitude * (b.longitude - c.longitude) + b.latitude * (c.longitude - a.longitude) + c.latitude * (a.longitude - b.longitude)).absoluteValue.toFloat()
}

private data class Triangle(
    var area: Float,
    val coordinate: Coordinate,
    var predecessorIndex: Int,
    val index: Int,
    var successorIndex: Int
) : Comparable<Triangle> {
    fun updateArea(triangles: List<Triangle>, points: List<Coordinate>) {
        val a = triangles.getOrNull(predecessorIndex)?.coordinate
        val c = triangles.getOrNull(successorIndex)?.coordinate
        area = calculateArea(a, points[index], c)
    }

    override fun compareTo(other: Triangle): Int {
        return area.compareTo(other.area)
    }
}