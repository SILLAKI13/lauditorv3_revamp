package com.digicoffer.lauditor.feature.dashboard.presentation.components

import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.digicoffer.lauditor.Dashboard.DahboardModels.CardDataModels
import com.digicoffer.lauditor.Dashboard.NewRevampViewModels.RoundedHorizontalBarChartRenderer
import com.digicoffer.lauditor.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

private val GillSansRegular = FontFamily(Font(R.font.gill_sans_regular))

@Composable
fun RevenueTrendCard(
    data: CardDataModels.TrendCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ranges = data.ranges ?: emptyMap()
    var selectedRangeKey by remember(data) {
        mutableStateOf(data.defaultRange ?: ranges.keys.firstOrNull() ?: "7days")
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .width(300.dp)
            .height(250.dp)
            .padding(6.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            // Header Row: Title + Range Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Revenue Trend",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                    Text(
                        text = "Based on appointments",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Range Tabs
                if (ranges.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8EEF5))
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (key in ranges.keys) {
                            val isSelected = key == selectedRangeKey
                            val label = when (key) {
                                "7days" -> "7D"
                                "30days" -> "30D"
                                "90days" -> "90D"
                                else -> key
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable { selectedRangeKey = key }
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        fontFamily = GillSansRegular,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF004D87) else Color(0xFF757575)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Line Chart
            AndroidView(
                factory = { ctx ->
                    LineChart(ctx).apply {
                        setTouchEnabled(false)
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    drawLineChart(chart, data, selectedRangeKey, context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

private fun drawLineChart(
    lineChart: LineChart,
    data: CardDataModels.TrendCardData,
    rangeKey: String,
    context: android.content.Context
) {
    val ranges = data.ranges ?: return
    val range = ranges[rangeKey] ?: return
    val datasets = range.datasets ?: return

    val colors = intArrayOf(
        ContextCompat.getColor(context, R.color.chart_green),
        ContextCompat.getColor(context, R.color.chart_blue),
        ContextCompat.getColor(context, R.color.chart_red)
    )

    val sets = ArrayList<ILineDataSet>()
    for (i in datasets.indices) {
        val ds = datasets[i]
        val entries = ArrayList<Entry>()
        val dsData = ds.data ?: emptyList()
        for (j in dsData.indices) {
            entries.add(Entry(j.toFloat(), dsData.toList()[j].toFloat()))
        }
        val lds = LineDataSet(entries, ds.name)
        val c = colors[Math.min(i, colors.size - 1)]
        lds.color = c
        lds.setCircleColor(c)
        lds.lineWidth = 2.5f
        lds.circleRadius = 3.5f
        lds.setDrawValues(false)
        lds.mode = LineDataSet.Mode.CUBIC_BEZIER
        lds.cubicIntensity = 0.15f
        lds.setDrawFilled(datasets.size == 1)
        lds.fillAlpha = 45
        lds.fillColor = c
        lds.setDrawCircles(true)
        lds.setDrawCircleHole(false)
        sets.add(lds)
    }

    val xAxis = lineChart.xAxis
    val xLabels = range.xLabels ?: emptyList()
    xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
    xAxis.granularity = 1f
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.setDrawGridLines(false)
    xAxis.textSize = 9f
    xAxis.axisMinimum = -0.3f
    xAxis.axisMaximum = if (xLabels.isNotEmpty()) xLabels.size - 0.7f else 6.3f
    xAxis.setAvoidFirstLastClipping(true)

    val left = lineChart.axisLeft
    var actualMax = 0f
    for (ds in datasets) {
        val dsData = ds.data ?: emptyList()
        for (value in dsData) {
            actualMax = Math.max(actualMax, value.toFloat())
        }
    }

    val yMaxValue = Math.max(range.yMax.toFloat(), actualMax * 1.1f)
    left.axisMinimum = 0f
    left.axisMaximum = if (yMaxValue > 0f) yMaxValue else 100f
    left.granularity = if (range.yStep > 0) range.yStep.toFloat() else 20f
    left.textSize = 9f
    left.setDrawGridLines(true)

    if ("currency" == range.yUnit && data.currencySymbol != null) {
        val sym = data.currencySymbol ?: ""
        left.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return sym + String.format("%,.0f", value)
            }
        }
    }

    lineChart.axisRight.isEnabled = false
    lineChart.data = LineData(sets)
    lineChart.description.isEnabled = false
    lineChart.legend.isEnabled = datasets.size > 1
    lineChart.animateXY(600, 600)
    lineChart.invalidate()
}

@Composable
fun AppointmentTrendCard(
    data: CardDataModels.TrendCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ranges = data.ranges ?: emptyMap()
    var selectedRangeKey by remember(data) {
        mutableStateOf(data.defaultRange ?: ranges.keys.firstOrNull() ?: "7days")
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .width(300.dp)
            .height(250.dp)
            .padding(6.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Appointment Trend",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                    Text(
                        text = "Weekly overview",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bar Chart
            AndroidView(
                factory = { ctx ->
                    BarChart(ctx).apply {
                        setTouchEnabled(false)
                        description.isEnabled = false
                        legend.isEnabled = false
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    drawAppointmentBars(chart, data, selectedRangeKey, context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // Legend Row
            val currentRange = ranges[selectedRangeKey]
            val datasets = currentRange?.datasets ?: emptyList()
            val colors = listOf(Color(0xFF2979FF), Color(0xFF43A047), Color(0xFFE53935))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in datasets.indices) {
                    val color = colors[i % colors.size]
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = datasets[i].name ?: "",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 10.sp,
                            color = Color(0xFF757575)
                        ),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            }
        }
    }
}

private fun drawAppointmentBars(
    barChart: BarChart,
    data: CardDataModels.TrendCardData,
    rangeKey: String,
    context: android.content.Context
) {
    val ranges = data.ranges ?: return
    val range = ranges[rangeKey] ?: return
    val datasets = range.datasets ?: return

    val colors = intArrayOf(
        ContextCompat.getColor(context, R.color.chart_blue),
        ContextCompat.getColor(context, R.color.green),
        ContextCompat.getColor(context, R.color.chart_red)
    )

    val dsCount = datasets.size
    val groupSpace = 0.12f
    val barSpace = 0.05f
    val barWidth = (1f - groupSpace - barSpace * dsCount) / dsCount

    val sets = ArrayList<IBarDataSet>()
    for (i in 0 until dsCount) {
        val ds = datasets[i]
        val entries = ArrayList<BarEntry>()
        val dsData = ds.data ?: emptyList()
        for (j in dsData.indices) {
            entries.add(BarEntry(j.toFloat(), dsData.toList()[j].toFloat()))
        }
        val bds = BarDataSet(entries, ds.name)
        bds.color = colors[Math.min(i, colors.size - 1)]
        bds.setDrawValues(false)
        sets.add(bds)
    }

    val barData = BarData(sets)
    barData.barWidth = barWidth

    val xAxis = barChart.xAxis
    val xLabels = range.xLabels ?: emptyList()
    xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
    xAxis.granularity = 1f
    xAxis.setCenterAxisLabels(dsCount > 1)
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.setDrawGridLines(false)
    xAxis.textSize = 9f
    xAxis.axisMinimum = 0f
    xAxis.axisMaximum = if (xLabels.isNotEmpty()) xLabels.size.toFloat() else 7f

    val left = barChart.axisLeft
    left.axisMinimum = 0f
    left.axisMaximum = if (range.yMax > 0) range.yMax.toFloat() else 100f
    left.granularity = if (range.yStep > 0) range.yStep.toFloat() else 20f
    left.textSize = 9f

    barChart.axisRight.isEnabled = false
    barChart.data = barData

    if (dsCount > 1) barChart.groupBars(0f, groupSpace, barSpace)
    barChart.setFitBars(true)
    barChart.animateXY(600, 600)
    barChart.invalidate()
}

@Composable
fun MatterStatusCard(
    data: CardDataModels.MatterCardData,
    onNavigateWithSubType: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .width(300.dp)
            .height(250.dp)
            .padding(6.dp)
            .clickable { onNavigateWithSubType(null) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header Row: Title + Grand Total Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Matter Status",
                    style = TextStyle(
                        fontFamily = GillSansRegular,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE4F2FF))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = data.grandTotal.toString(),
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004D87)
                        )
                    )
                }
            }

            // Horizontal Bar Chart
            AndroidView(
                factory = { ctx ->
                    HorizontalBarChart(ctx).apply {
                        setTouchEnabled(true)
                        description.isEnabled = false
                        legend.isEnabled = false
                        setDrawBorders(false)
                        axisRight.isEnabled = false
                        setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry, h: Highlight) {
                                val currentLabels = data.chartData?.yLabels
                                if (!currentLabels.isNullOrEmpty()) {
                                    val groupIndex = e.x.toInt()
                                    if (groupIndex in currentLabels.indices) {
                                        onNavigateWithSubType(currentLabels[groupIndex])
                                    }
                                }
                            }
                            override fun onNothingSelected() {}
                        })
                    }
                },
                update = { chart ->
                    drawMatterChart(chart, data, context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 6.dp)
            )

            // Legend Row
            val datasets = data.chartData?.datasets ?: emptyList()
            val counts = listOf(data.activeTotalCount, data.closedTotalCount)
            val colors = listOf(Color(0xFF2979FF), Color(0xFFFFA726))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in datasets.indices) {
                    val color = colors[i % colors.size]
                    val count = if (i < counts.size) counts[i] else 0
                    val name = datasets[i].name ?: ""

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onNavigateWithSubType(name) }
                            .padding(horizontal = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = count.toString(),
                            style = TextStyle(
                                fontFamily = GillSansRegular,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = name,
                            style = TextStyle(
                                fontFamily = GillSansRegular,
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun drawMatterChart(
    barChart: HorizontalBarChart,
    data: CardDataModels.MatterCardData,
    context: android.content.Context
) {
    val chart = data.chartData ?: return
    val datasets = chart.datasets ?: return

    val dsCount = datasets.size
    val resolvedColors = IntArray(dsCount)
    for (i in 0 until dsCount) {
        resolvedColors[i] = resolveColor(datasets[i].color)
    }

    val barWidth = 0.22f
    val barSpace = 0.04f
    val groupSpace = 0.50f

    val sets = ArrayList<IBarDataSet>()
    for (i in dsCount - 1 downTo 0) {
        val ds = datasets[i]
        val entries = ArrayList<BarEntry>()
        val dsData = ds.data ?: emptyList()
        val labelCount = dsData.size
        for (j in 0 until labelCount) {
            val reversedIndex = (labelCount - 1) - j
            entries.add(BarEntry(reversedIndex.toFloat(), dsData.toList()[j]))
        }
        val bds = BarDataSet(entries, ds.name)
        bds.color = resolvedColors[i]
        bds.setDrawValues(false)
        sets.add(bds)
    }

    val xAxis = barChart.xAxis
    val yLabels = chart.yLabels
    if (!yLabels.isNullOrEmpty()) {
        xAxis.valueFormatter = IndexAxisValueFormatter(yLabels)
    }
    xAxis.setCenterAxisLabels(true)
    xAxis.granularity = 1f
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.setDrawGridLines(false)
    xAxis.setDrawAxisLine(false)
    xAxis.textSize = 11f
    xAxis.textColor = AndroidColor.parseColor("#8A8A9A")
    xAxis.spaceMin = 0.6f
    xAxis.spaceMax = 0.6f

    val chartXAxis = chart.xAxis
    val yMin = chartXAxis?.min?.toFloat() ?: 0f
    val yMax = chartXAxis?.max?.toFloat() ?: 600f
    val yStep = chartXAxis?.step?.toFloat() ?: 135f

    val axisLeft = barChart.axisLeft
    axisLeft.axisMinimum = yMin
    axisLeft.axisMaximum = yMax
    axisLeft.granularity = yStep
    axisLeft.setDrawGridLines(true)
    axisLeft.gridColor = AndroidColor.parseColor("#F0F0F5")
    axisLeft.setDrawAxisLine(false)
    axisLeft.textSize = 9f
    axisLeft.textColor = AndroidColor.parseColor("#8A8A9A")

    barChart.axisRight.isEnabled = false
    barChart.renderer = RoundedHorizontalBarChartRenderer(
        barChart,
        barChart.animator,
        barChart.viewPortHandler,
        8f
    )

    barChart.data = BarData(sets).apply { this.barWidth = barWidth }

    if (dsCount > 1 && yLabels != null) {
        barChart.groupBars(0f, groupSpace, barSpace)
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = yLabels.size.toFloat()
    }

    barChart.animateY(600)
    barChart.invalidate()
}

private fun resolveColor(apiColor: String?): Int {
    if (apiColor == null) return AndroidColor.parseColor("#2979FF")
    return when (apiColor.lowercase()) {
        "blue" -> AndroidColor.parseColor("#2979FF")
        "purple", "orange" -> AndroidColor.parseColor("#FFA726")
        "green" -> AndroidColor.parseColor("#43A047")
        "red" -> AndroidColor.parseColor("#E53935")
        "teal" -> AndroidColor.parseColor("#00897B")
        else -> AndroidColor.parseColor("#2979FF")
    }
}

@Composable
fun DataStorageCard(
    data: CardDataModels.StorageCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .width(300.dp)
            .height(250.dp)
            .padding(6.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "Data Storage",
                style = TextStyle(
                    fontFamily = GillSansRegular,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Total Storage & Used Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total Storage
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Total Storage",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 11.sp,
                            color = Color(0xFF757575)
                        )
                    )
                    Text(
                        text = "${data.totalGB} GB",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }

                // Used Storage
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Used",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 11.sp,
                            color = Color(0xFF757575)
                        )
                    )
                    Text(
                        text = "${data.usedGB} GB",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004D87)
                        )
                    )
                }
            }

            // Donut Pie Chart + Available Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart
                AndroidView(
                    factory = { ctx ->
                        PieChart(ctx).apply {
                            setTouchEnabled(false)
                            description.isEnabled = false
                            isDrawHoleEnabled = true
                            holeRadius = 62f
                            transparentCircleRadius = 67f
                            setHoleColor(AndroidColor.WHITE)
                            setEntryLabelColor(AndroidColor.TRANSPARENT)
                            legend.isEnabled = false
                            setDrawCenterText(false)
                            rotationAngle = -90f
                        }
                    },
                    update = { chart ->
                        val entries = ArrayList<PieEntry>()
                        entries.add(PieEntry(data.usedGB.toFloat(), "Used"))
                        entries.add(PieEntry(data.availableGB.toFloat(), "Available"))

                        val ds = PieDataSet(entries, "")
                        ds.setColors(
                            ContextCompat.getColor(context, R.color.chart_blue),
                            ContextCompat.getColor(context, R.color.grey_badge_bg)
                        )
                        ds.setDrawValues(false)
                        ds.sliceSpace = 2f

                        chart.data = PieData(ds)
                        chart.invalidate()
                    },
                    modifier = Modifier.size(110.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Available Stats
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Available",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 11.sp,
                            color = Color(0xFF757575)
                        ),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = "${data.availableGB} GB",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF43A047)
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "${data.usagePercentage}% used",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                    )
                }
            }
        }
    }
}
