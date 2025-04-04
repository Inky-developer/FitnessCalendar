package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.data.ActivityStatistics
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.ceil

@Immutable
data class MosaicChartState<T>(
    val tiles: List<MosaicEntry<T>>,
    val height: Int,
    val xLabels: Map<Int, String>,
    val yLabels: List<String>,
    val colors: List<Color>
) {
    init {
        assert(colors.isNotEmpty()) { "Mosaic needs at least one color" }
    }

    private val maxCount = (tiles.maxOfOrNull { it.count } ?: 0).toFloat()
    internal val rows = tiles.chunked(height).withIndex().toList()

    fun getColor(value: Int): Color {
        val percent = value / maxCount
        val index = ceil((percent * (colors.size - 1))).toInt()
        return colors[index]
    }
}

data class MosaicEntry<T>(val count: Int, val data: T)

fun ActivityStatistics.calculateMosaicState(colors: List<Color>): MosaicChartState<LocalDate> {
    val dayOfWeekField = WeekFields.of(Locale.getDefault()).dayOfWeek()
    val lastDay = LocalDate.now()
    val firstDay = lastDay.minusYears(1).with(dayOfWeekField, 1)
    val locale = Locale.getDefault()

    val activityData = activitiesByDay

    val days = mutableListOf<MosaicEntry<LocalDate>>()
    val xLabels = mutableMapOf<Int, String>()

    var day = firstDay
    var index = 0
    while (!day.isAfter(lastDay)) {
        days.add(MosaicEntry(count = activityData[day]?.size ?: 0, data = day))
        val nextDay = day.plusDays(1)
        if (day.month != nextDay.month) {
            xLabels[(index + 1) / 7] = nextDay.month.getDisplayName(TextStyle.SHORT, locale)
        }
        day = nextDay
        index += 1
    }

    val yLabels = DayOfWeek.entries.map { it.getDisplayName(TextStyle.SHORT, locale) }

    return MosaicChartState(
        tiles = days,
        height = 7,
        xLabels = xLabels,
        yLabels = yLabels,
        colors = colors
    )
}

const val TILE_SIZE_DP = 12

@Composable
fun <T> MosaicChart(
    state: MosaicChartState<T>,
    hoverText: @Composable (MosaicEntry<T>) -> String,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surface) {
        Row {
            Column(modifier = Modifier.padding(end = 4.dp)) {
                Spacer(modifier = Modifier.height(TILE_SIZE_DP.dp))
                for (label in state.yLabels) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.height(TILE_SIZE_DP.dp)
                    )
                }
            }

            val textMeasurer = rememberTextMeasurer()
            LazyRow(state = rememberLazyListState(initialFirstVisibleItemIndex = state.rows.size - 1)) {
                items(state.rows) { (index, row) ->
                    Column {
                        val xLabel = state.xLabels[index]
                        if (xLabel != null) {

                            val textColor = LocalContentColor.current
                            val typography = MaterialTheme.typography
                            Canvas(modifier = Modifier.size(TILE_SIZE_DP.dp)) {
                                drawText(
                                    textMeasurer.measure(
                                        xLabel,
                                        style = typography.labelSmall
                                    ),
                                    color = textColor,
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(TILE_SIZE_DP.dp))
                        }
                        for (entry in row) {
                            MosaicTile(state.getColor(entry.count), hoverText(entry))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MosaicTile(color: Color, message: String) {
    val state = rememberTooltipState()
    val positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider()
    TooltipBox(
        state = state,
        positionProvider = positionProvider,
        tooltip = {
            Text(
                message,
                modifier = Modifier
                    .shadow(1.dp, MaterialTheme.shapes.small)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(horizontal = 2.dp)
            )
        },
    ) {
        Box(
            modifier = Modifier
                .requiredSize(TILE_SIZE_DP.dp)
                .padding(1.dp)
                .clip(RoundedCornerShape(percent = 25))
                .border(
                    width = 0.5.dp,
                    color = Color(0.2f, 0.2f, 0.2f, 0.3f),
                    shape = RoundedCornerShape(percent = 25)
                )
                .background(color)
        )
    }
}

//@Preview
//@Composable
//private fun PreviewMosaicChart() {
//    var data by remember { mutableStateOf<MosaicChartState<LocalDate>?>(null) }
//    val context = LocalContext.current
//    LaunchedEffect(Unit) {
//        val random = Random(1)
//
//        val days = mutableListOf<MosaicEntry<LocalDate>>()
//        val xLabels = mutableMapOf<Int, String>()
//
//        val dayOfWeekField = WeekFields.of(Locale.getDefault()).dayOfWeek()
//        val today = LocalDate.now()
//        val start = today.minusYears(1).with(dayOfWeekField, 1)
//        var currentDay = start
//        var index = 0
//        while (!currentDay.isAfter(today)) {
//            days.add(MosaicEntry(random.nextInt(5), currentDay))
//            val nextDay = currentDay.plusDays(1)
//            if (nextDay.month != currentDay.month) {
//                xLabels[index / 7] = nextDay.month.getDisplayName(
//                    TextStyle.SHORT,
//                    Locale.getDefault()
//                )
//            }
//            currentDay = nextDay
//            index += 1
//        }
//
//        val yLabels =
//            DayOfWeek.entries.map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
//        val colors = listOf(
//            Color(context.getColor(R.color.mosaic_0)),
//            Color(context.getColor(R.color.mosaic_1)),
//            Color(context.getColor(R.color.mosaic_2)),
//            Color(context.getColor(R.color.mosaic_3)),
//        )
//
//        data = MosaicChartState(days, 7, xLabels, yLabels, colors)
//    }
//
//
//    data?.let {
//        MosaicChart(
//            it,
//            hoverText = { entry ->
//                val date = entry.data.format(LocalizationRepository.localDateFormatter)
//                "$date: ${entry.count}"
//            },
//            modifier = Modifier.fillMaxWidth()
//        )
//    }
//}
