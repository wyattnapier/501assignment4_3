package com.example.assign4_3

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.assign4_3.ui.theme.Assign4_3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = MainViewModel()
        enableEdgeToEdge()
        setContent {
            Assign4_3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        vm
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(vm: MainViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ManageDataGeneration(vm)
        Spacer(modifier = Modifier.padding(6.dp))
        ShowStats(vm)
        Spacer(modifier = Modifier.padding(6.dp))
        TempChartWrapper(vm)
        Spacer(modifier = Modifier.padding(6.dp))
        TempReadingList(vm)
    }
}

@Composable
fun ShowStats(vm: MainViewModel) {
    Column {
        Text("Temperature Stats",
            style = MaterialTheme.typography.headlineMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(fraction = 0.8f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Minimum")
                Text(vm.minTemp.collectAsState().value.toString())
            }
            Column {
                Text("Maximum")
                Text(vm.maxTemp.collectAsState().value.toString())
            }
            Column {
                Text("Average")
                Text(vm.avgTemp.collectAsState().value.toString())
            }
        }
    }
}

@Composable
fun ManageDataGeneration(vm: MainViewModel) {
    val pauseDataGeneration by vm.autoGenerate.collectAsState()
    Column {
        Text(
            "Manage Data Generation",
            style = MaterialTheme.typography.headlineMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(fraction = 0.8f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Auto generate temperature data")
            Switch(checked = pauseDataGeneration, onCheckedChange = { vm.setAutoGenerate(it) })
        }
    }
}

@Composable
fun TempChart(vm: MainViewModel) {
    val tempList by vm.tempEntries.collectAsState()

    Canvas(
        contentDescription = "Temperature Chart",
        modifier = Modifier.fillMaxWidth().height(200.dp)
    ) {
        // early return if no data
        if (tempList.isEmpty()) {
            return@Canvas
        }

        val lineAndPointColor = Color.Blue
        val chartPadding = 20.dp.toPx()
        val yMin = 60f
        val yMax = 90f

        val yAxisPadding = 25.dp.toPx()
        val xAxisPadding = 10.dp.toPx()
        val yAxisLineStart = Offset(x = yAxisPadding, y = 0f)
        val yAxisLineEnd = Offset(x = yAxisPadding, y = size.height - xAxisPadding)

        drawLine(
            color = Color.Black,
            start = yAxisLineStart,
            end = yAxisLineEnd,
            strokeWidth = 2f
        )

        val textPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 12.sp.toPx()
            color = Color.Black.toArgb()
            textAlign = android.graphics.Paint.Align.RIGHT // Align text to the right of the point
        }

        // y axis labels
        val yLabelCount = 4
        for (i in 0 until yLabelCount) {
            val value = yMin + (i * (yMax - yMin) / (yLabelCount - 1))
            val normalizedY = 1 - ((value - yMin) / (yMax - yMin))
            val y = normalizedY * (size.height - xAxisPadding)

            // Draw the label text
            drawContext.canvas.nativeCanvas.drawText(
                "%.0f°".format(value),
                yAxisPadding - 8.dp.toPx(), // Position text to the left of the axis line
                y,
                textPaint
            )

            // Draw a small tick mark on the axis
            drawLine(
                color = Color.Black,
                start = Offset(yAxisPadding - 4.dp.toPx(), y),
                end = Offset(yAxisPadding, y),
                strokeWidth = 2f
            )
        }

        // x axis labels
        val xAxisLineStart = Offset(x = yAxisPadding, y = size.height - xAxisPadding)
        val xAxisLineEnd = Offset(x = size.width, y = size.height - xAxisPadding)
        drawLine(
            color = Color.Black,
            start = xAxisLineStart,
            end = xAxisLineEnd,
            strokeWidth = 2f
        )

        val points = tempList.mapIndexed { index, entry ->
            val x =
                yAxisPadding + index * (size.width - yAxisPadding) / (tempList.size - 1).coerceAtLeast(
                    1
                )
            val normalizedY = 1 - ((entry.temp - yMin) / (yMax - yMin)).coerceIn(0f, 1f)
            val y = normalizedY * (size.height - xAxisPadding)

            Offset(x, y)
        }

        // --- 3. Draw the Line ---
        val linePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
            }
        }

        drawPath(
            path = linePath,
            color = lineAndPointColor,
            style = Stroke(width = 4f)
        )

        // --- 4. Draw the Points ---
        points.forEach { point ->
            drawCircle(
                color = lineAndPointColor,
                radius = 8f,
                center = point
            )
        }
    }
}

@Composable
fun TempChartWrapper(vm: MainViewModel) {
    Column(modifier = Modifier.fillMaxWidth(fraction = 0.8f)) {
        Text(
            "Temperature Chart",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        TempChart(vm)
    }
}

@Composable
fun TempReadingList(vm: MainViewModel) {
    val tempList by vm.tempEntries.collectAsState()
    val listState = rememberLazyListState()

    Column (modifier = Modifier.height(200.dp).fillMaxWidth(fraction = 0.8f)) {
        Text("Temperature Readings",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        LazyColumn(state = listState) {
            items(count = tempList.size) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(tempList[index].timestamp + " - " + tempList[index].temp.toString())
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Assign4_3Theme {
        MainScreen(MainViewModel())
    }
}