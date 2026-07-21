package com.digicoffer.lauditor.Dashboard.NewRevampViewModels

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.Dashboard.DahboardModels.CardDataModels
import com.digicoffer.lauditor.Notifications.Models.Navigation
import com.digicoffer.lauditor.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
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
import java.text.DateFormatSymbols
import java.util.Calendar

class DashboardCardAdapter(
    private val context: Context,
    private val listener: CardActionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface CardActionListener {
        fun onNavigate(cardType: Int, navigation: Navigation?)
        fun onNavigate(cardType: Int, navigation: Navigation?, subType: String?)
        fun onPaySubscription(planLabel: String?, validityText: String?)
    }

    private var items: List<DashboardItem> = ArrayList()

    fun submitList(newItems: List<DashboardItem>?) {
        items = newItems ?: ArrayList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = items[position].type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return createHolder(viewType, LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        bindHolder(holder, item.type, item.data, item.navigation)
    }

    private fun createHolder(type: Int, inf: LayoutInflater, p: ViewGroup): RecyclerView.ViewHolder {
        return when (type) {
            DashboardItem.TYPE_MEETING, DashboardItem.TYPE_APPOINTMENT, DashboardItem.TYPE_NOTIFICATION ->
                ActivityHolder(inf.inflate(R.layout.card_activity_common, p, false), type)

            DashboardItem.TYPE_MESSAGES ->
                MessagesHolder(inf.inflate(R.layout.card_messages, p, false))

            DashboardItem.TYPE_REVENUE_TREND ->
                RevenueTrendHolder(inf.inflate(R.layout.card_trend, p, false))

            DashboardItem.TYPE_APPOINTMENT_TREND ->
                ApptTrendHolder(inf.inflate(R.layout.card_appointment, p, false))

            DashboardItem.TYPE_MATTER ->
                MatterHolder(inf.inflate(R.layout.card_matter_kpi, p, false))

            DashboardItem.TYPE_STORAGE ->
                StorageHolder(inf.inflate(R.layout.card_storage_ds, p, false))

            DashboardItem.TYPE_BILLABLE ->
                BillableHolder(inf.inflate(R.layout.card_billable, p, false))

            DashboardItem.TYPE_APPROX_REVENUE ->
                ApproxRevenueHolder(inf.inflate(R.layout.card_approx_revenue, p, false))

            DashboardItem.TYPE_SUBSCRIPTION ->
                SubscriptionHolder(inf.inflate(R.layout.card_subscription, p, false))

            DashboardItem.TYPE_HIRING ->
                HiringHolder(inf.inflate(R.layout.card_hiring_kpi, p, false))

            else ->
                ActivityHolder(inf.inflate(R.layout.card_activity_common, p, false), type)
        }
    }

    private fun bindHolder(h: RecyclerView.ViewHolder, type: Int, data: Any?, nav: Navigation?) {
        when (type) {
            DashboardItem.TYPE_MEETING -> {
                val holder = h as ActivityHolder
                holder.nav = nav
                holder.bindMeeting(data as CardDataModels.MeetingCardData)
            }
            DashboardItem.TYPE_APPOINTMENT -> {
                val holder = h as ActivityHolder
                holder.nav = nav
                holder.bindAppointment(data as CardDataModels.AppointmentCardData)
            }
            DashboardItem.TYPE_MESSAGES -> {
                val holder = h as MessagesHolder
                holder.nav = nav
                holder.bind(data as CardDataModels.MessagesCardData)
            }
            DashboardItem.TYPE_NOTIFICATION -> {
                val holder = h as ActivityHolder
                holder.nav = nav
                holder.bindNotification(data as CardDataModels.NotificationCardData)
            }
            DashboardItem.TYPE_REVENUE_TREND -> {
                val holder = h as RevenueTrendHolder
                holder.nav = nav
                holder.bind(data as CardDataModels.TrendCardData)
            }
            DashboardItem.TYPE_APPOINTMENT_TREND -> {
                val holder = h as ApptTrendHolder
                holder.bind(data as CardDataModels.TrendCardData)
            }
            DashboardItem.TYPE_MATTER -> {
                val holder = h as MatterHolder
                holder.nav = nav
                holder.bind(data as CardDataModels.MatterCardData)
            }
            DashboardItem.TYPE_STORAGE -> {
                val holder = h as StorageHolder
                holder.bind(data as CardDataModels.StorageCardData)
            }
            DashboardItem.TYPE_BILLABLE -> {
                val holder = h as BillableHolder
                holder.nav = nav
                holder.bind(data as CardDataModels.BillableCardData)
            }
            DashboardItem.TYPE_APPROX_REVENUE -> {
                val holder = h as ApproxRevenueHolder
                holder.nav = nav
                holder.bind(data as CardDataModels.ApproxRevenueCardData)
            }
            DashboardItem.TYPE_SUBSCRIPTION -> {
                val holder = h as SubscriptionHolder
                holder.bind(data as CardDataModels.SubscriptionCardData)
            }
            DashboardItem.TYPE_HIRING -> {
                val holder = h as HiringHolder
                holder.nav = nav
                holder.bind(data as CardDataModels.HiringCardData)
            }
        }
    }

    inner class ActivityHolder(v: View, private val cardType: Int) : RecyclerView.ViewHolder(v) {
        val ivIcon: ImageView = v.findViewById(R.id.iv_card_icon)
        val ivIconBg: ImageView = v.findViewById(R.id.iv_icon_bg)
        val tvLabel: TextView = v.findViewById(R.id.tv_card_label)
        val tvBadge: TextView = v.findViewById(R.id.tv_badge)
        val tvPrimary: TextView = v.findViewById(R.id.tv_primary_text)
        val tvSecondary: TextView = v.findViewById(R.id.tv_secondary_text)
        val tvEmpty: TextView = v.findViewById(R.id.tv_empty_text)
        val llContent: LinearLayout = v.findViewById(R.id.ll_content)
        var nav: Navigation? = null

        init {
            v.setOnClickListener {
                listener.onNavigate(cardType, nav)
            }
        }

        fun bindMeeting(d: CardDataModels.MeetingCardData) {
            ivIcon.setImageResource(R.drawable.ic_calendar_blue)
            ivIcon.clearColorFilter()
            ivIconBg.setImageResource(R.drawable.stat_box_blue)
            tvLabel.text = "Upcoming Meeting"
            applyBadge(
                tvBadge, d.badge,
                ContextCompat.getColor(context, R.color.blue),
                ContextCompat.getColor(context, R.color.blue_pale)
            )
            if (d.hasMeeting) {
                tvPrimary.text = d.subject
                tvSecondary.text = "${d.startTime} – ${d.endTime}"
                showContent(true)
            } else {
                tvEmpty.text = "No meetings scheduled for today"
                showContent(false)
            }
        }

        fun bindAppointment(d: CardDataModels.AppointmentCardData) {
            ivIcon.setImageResource(R.drawable.ic_clock)
            ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.chart_purple))
            ivIconBg.setImageResource(R.drawable.stat_box_purple)
            tvLabel.text = "Upcoming Appointment"
            applyBadge(
                tvBadge, d.badge,
                ContextCompat.getColor(context, R.color.chart_purple),
                ContextCompat.getColor(context, R.color.purple_pale)
            )
            if (d.hasAppointment) {
                tvPrimary.text = d.subject
                tvSecondary.text = "${d.startTime} – ${d.endTime}"
                showContent(true)
            } else {
                tvEmpty.text = "No appointments for today"
                showContent(false)
            }
        }

        fun bindMessages(d: CardDataModels.MessagesCardData) {
            ivIcon.setImageResource(R.drawable.ic_chat_green)
            ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.dark_shaded_blue))
            ivIconBg.setImageResource(R.drawable.stat_box_blue)
            tvLabel.text = "New Messages"
            applyBadge(
                tvBadge, d.badge,
                ContextCompat.getColor(context, R.color.dark_shaded_blue),
                ContextCompat.getColor(context, R.color.dark_shaded_blue_bg)
            )
            val total = (Constants.clientChat_count.toString().toIntOrNull() ?: 0) + (Constants.teamChat_count.toString().toIntOrNull() ?: 0)
            if (total > 0) {
                if (Constants.ROLE == "AAM") {
                    tvPrimary.text = "${Constants.teamChat_count} Team"
                    tvSecondary.text = "Unread messages"
                } else {
                    tvPrimary.text = "${Constants.clientChat_count} Client  |  ${Constants.teamChat_count} Team"
                    tvSecondary.text = "Unread messages"
                }
                tvBadge.text = total.toString()
                showContent(true)
            } else {
                tvEmpty.text = "No new messages"
                showContent(false)
            }
        }

        fun bindNotification(d: CardDataModels.NotificationCardData) {
            ivIcon.setImageResource(R.drawable.ic_bell_orange)
            ivIcon.clearColorFilter()
            ivIconBg.setImageResource(R.drawable.stat_box_orange)
            tvLabel.text = "Notifications"
            applyBadge(
                tvBadge, d.badge,
                ContextCompat.getColor(context, R.color.orange_color),
                ContextCompat.getColor(context, R.color.color_orange_icon_bg)
            )
            val notifs = d.notifications
            if (!notifs.isNullOrEmpty()) {
                val first = notifs[0]
                tvPrimary.text = if (first.message.isNullOrEmpty()) "New notification" else first.message
                tvSecondary.text = first.time
                showContent(true)
            } else {
                tvEmpty.text = "No new notifications"
                showContent(false)
            }
        }

        private fun showContent(hasData: Boolean) {
            llContent.visibility = if (hasData) View.VISIBLE else View.GONE
            tvEmpty.visibility = if (hasData) View.GONE else View.VISIBLE
        }
    }

    inner class MessagesHolder(v: View) : RecyclerView.ViewHolder(v) {
        val ivIcon: ImageView = v.findViewById(R.id.iv_card_icon)
        val ivIconBg: ImageView = v.findViewById(R.id.iv_icon_bg)
        val tvLabel: TextView = v.findViewById(R.id.tv_card_label)
        val tvBadge: TextView = v.findViewById(R.id.tv_badge)
        val tvEmpty: TextView = v.findViewById(R.id.tv_empty_text)
        val llContent: LinearLayout = v.findViewById(R.id.ll_content)
        val llClient: LinearLayout = v.findViewById(R.id.ll_client)
        val llTeam: LinearLayout = v.findViewById(R.id.ll_team)
        val tvClientCount: TextView
        val tvClientLabel: TextView
        val tvTeamCount: TextView
        val tvTeamLabel: TextView
        var nav: Navigation? = null

        init {
            val clientCountInclude = v.findViewById<View>(R.id.tv_client_count)
            val clientLabelInclude = v.findViewById<View>(R.id.tv_client)
            val teamCountInclude = v.findViewById<View>(R.id.tv_team_count)
            val teamLabelInclude = v.findViewById<View>(R.id.tv_team)

            tvClientCount = if (clientCountInclude is TextView) clientCountInclude else (clientCountInclude as ViewGroup).getChildAt(0) as TextView
            tvClientLabel = if (clientLabelInclude is TextView) clientLabelInclude else (clientLabelInclude as ViewGroup).getChildAt(0) as TextView
            tvTeamCount = if (teamCountInclude is TextView) teamCountInclude else (teamCountInclude as ViewGroup).getChildAt(0) as TextView
            tvTeamLabel = if (teamLabelInclude is TextView) teamLabelInclude else (teamLabelInclude as ViewGroup).getChildAt(0) as TextView

            val isAAM = Constants.ROLE == "AAM"
            if (isAAM) {
                Constants.isClient_chat = false
            }
            v.setOnClickListener { listener.onNavigate(DashboardItem.TYPE_MESSAGES, nav) }
        }

        fun bind(d: CardDataModels.MessagesCardData) {
            ivIcon.setImageResource(R.drawable.ic_chat_green)
            ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.dark_shaded_blue))
            ivIconBg.setImageResource(R.drawable.stat_box_blue)

            val clientCount = Constants.clientChat_count.toString().toIntOrNull() ?: 0
            val teamCount = Constants.teamChat_count.toString().toIntOrNull() ?: 0
            val total = clientCount + teamCount

            applyBadge(
                tvBadge, d.badge,
                ContextCompat.getColor(context, R.color.dark_shaded_blue),
                ContextCompat.getColor(context, R.color.dark_shaded_blue_bg)
            )
            tvBadge.text = total.toString()

            llContent.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE

            val isAAM = Constants.ROLE == "AAM"
            val isSolo = Constants.CATEGORY == "solo"

            llClient.visibility = if (isAAM) View.GONE else View.VISIBLE
            tvClientCount.text = clientCount.toString()
            tvClientLabel.text = "Client"

            llTeam.visibility = if (isSolo) View.GONE else View.VISIBLE
            tvTeamCount.text = teamCount.toString()
            tvTeamLabel.text = "Team"

            llClient.setOnClickListener {
                Constants.isClient_chat = true
                listener.onNavigate(DashboardItem.TYPE_MESSAGES, nav)
            }
            llTeam.setOnClickListener {
                Constants.isClient_chat = false
                listener.onNavigate(DashboardItem.TYPE_MESSAGES, nav)
            }
        }
    }

    inner class RevenueTrendHolder(v: View) : RecyclerView.ViewHolder(v) {
        val lineChart: LineChart = v.findViewById(R.id.line_chart_trend)
        val tvTitle: TextView = v.findViewById(R.id.tv_trend_title)
        val tvBadge: TextView? = v.findViewById(R.id.tv_trend_badge)
        val llRangeTabs: LinearLayout = v.findViewById(R.id.ll_range_tabs)
        var nav: Navigation? = null

        fun bind(d: CardDataModels.TrendCardData) {
            tvTitle.text = "Revenue Trend"
            buildRangeTabs(llRangeTabs, d)
            selectTabByKey(llRangeTabs, d.defaultRange)
            drawLine(d, d.defaultRange)
            lineChart.setTouchEnabled(false)
        }

        private fun drawLine(d: CardDataModels.TrendCardData, rangeKey: String?) {
            val ranges = d.ranges ?: return
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

                if (hasConsecutiveZeros(dsData)) {
                    lds.mode = LineDataSet.Mode.LINEAR
                } else {
                    lds.mode = LineDataSet.Mode.CUBIC_BEZIER
                    lds.cubicIntensity = 0.15f
                }

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
            xAxis.axisMaximum = xLabels.size - 0.7f
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
            left.axisMaximum = yMaxValue
            left.granularity = range.yStep.toFloat()
            left.textSize = 9f
            left.setDrawGridLines(true)

            if ("currency" == range.yUnit && d.currencySymbol != null) {
                val sym = d.currencySymbol
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
            lineChart.setDrawGridBackground(false)
            lineChart.setDrawBorders(false)
            lineChart.setTouchEnabled(false)
            lineChart.animateXY(800, 800)
            lineChart.invalidate()
        }

        private fun hasConsecutiveZeros(data: Collection<Double>): Boolean {
            var zeroCount = 0
            for (value in data) {
                if (value == 0.0) {
                    zeroCount++
                    if (zeroCount >= 2) return true
                } else {
                    zeroCount = 0
                }
            }
            return false
        }

        private fun buildRangeTabs(ll: LinearLayout, d: CardDataModels.TrendCardData) {
            ll.removeAllViews()
            val ranges = d.ranges ?: return
            for (key in ranges.keys) {
                val tab = TextView(context)
                tab.text = rangeLabel(key)
                tab.setPadding(20, 6, 20, 6)
                tab.textSize = 11f
                tab.tag = key
                tab.setOnClickListener { vv ->
                    selectTab(ll, vv as TextView)
                    drawLine(d, vv.tag as String)
                }
                ll.addView(tab)
            }
        }

        private fun selectTab(ll: LinearLayout, sel: TextView) {
            for (i in 0 until ll.childCount) {
                val child = ll.getChildAt(i)
                if (child is TextView) {
                    val active = child === sel
                    child.setBackgroundResource(if (active) R.drawable.tab_selected_bg else android.R.color.transparent)
                    child.setTextColor(
                        ContextCompat.getColor(
                            context,
                            if (active) R.color.blue else R.color.grey_text
                        )
                    )
                }
            }
        }

        private fun selectTabByKey(ll: LinearLayout, key: String?) {
            for (i in 0 until ll.childCount) {
                val child = ll.getChildAt(i)
                if (key == child.tag) {
                    selectTab(ll, child as TextView)
                    return
                }
            }
        }

        private fun rangeLabel(k: String): String {
            return when (k) {
                "7days" -> "7D"
                "30days" -> "30D"
                "90days" -> "90D"
                else -> k
            }
        }
    }

    inner class ApptTrendHolder(v: View) : RecyclerView.ViewHolder(v) {
        val barChart: BarChart = v.findViewById(R.id.bar_chart_trend)
        val tvTitle: TextView = v.findViewById(R.id.tv_trend_title)
        val tvBadge: TextView? = v.findViewById(R.id.tv_trend_badge)
        val llRangeTabs: LinearLayout = v.findViewById(R.id.ll_range_tabs)
        val llLegend: LinearLayout = v.findViewById(R.id.ll_legend)

        fun bind(d: CardDataModels.TrendCardData) {
            tvTitle.text = "Appointment Trend"
            buildRangeTabs(llRangeTabs, d)
            selectTabByKey(llRangeTabs, d.defaultRange)
            drawBars(d, d.defaultRange)
            barChart.setTouchEnabled(false)
        }

        private fun drawBars(d: CardDataModels.TrendCardData, rangeKey: String?) {
            val ranges = d.ranges ?: return
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
                bds.valueTextSize = 15f
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
            xAxis.axisMaximum = if (xLabels.isNotEmpty()) xLabels.size.toFloat() else ds(datasets)

            val left = barChart.axisLeft
            left.axisMinimum = 0f
            left.axisMaximum = range.yMax.toFloat()
            left.granularity = range.yStep.toFloat()
            left.textSize = 9f

            barChart.axisRight.isEnabled = false
            barChart.data = barData

            if (dsCount > 1) barChart.groupBars(0f, groupSpace, barSpace)

            barChart.description.isEnabled = false
            barChart.legend.isEnabled = false
            barChart.setFitBars(true)
            barChart.animateXY(800, 800)
            barChart.invalidate()

            buildLegend(datasets, colors)
        }

        private fun buildLegend(datasets: Collection<CardDataModels.TrendCardData.DataSet>, colors: IntArray) {
            llLegend.removeAllViews()
            for (i in datasets.indices) {
                val dot = View(context)
                val dotLp = LinearLayout.LayoutParams(DynamicUtils.twenty, DynamicUtils.twenty)
                dotLp.setMargins(0, 0, 6, 0)
                dot.layoutParams = dotLp
                val dotBg = GradientDrawable()
                dotBg.shape = GradientDrawable.OVAL
                dotBg.setColor(colors[Math.min(i, colors.size - 1)])
                dot.background = dotBg

                val label = TextView(context)
                label.text = datasets.elementAt(i).name
                label.textSize = 10f
                label.setTextColor(ContextCompat.getColor(context, R.color.grey_text))
                val lblLp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lblLp.setMargins(0, 0, DynamicUtils.twenty, 0)
                label.layoutParams = lblLp

                llLegend.addView(dot)
                llLegend.addView(label)
            }
        }

        private fun ds(datasets: Collection<CardDataModels.TrendCardData.DataSet>): Float {
            if (datasets.isEmpty()) return 7f
            val dsData = datasets.first().data ?: return 7f
            return dsData.size.toFloat()
        }

        private fun buildRangeTabs(ll: LinearLayout, d: CardDataModels.TrendCardData) {
            ll.removeAllViews()
            val ranges = d.ranges ?: return
            for (key in ranges.keys) {
                val tab = TextView(context)
                tab.text = rangeLabel(key)
                tab.setPadding(20, 6, 20, 6)
                tab.textSize = 11f
                tab.tag = key
                tab.setOnClickListener { vv ->
                    selectTab(ll, vv as TextView)
                    drawBars(d, vv.tag as String)
                }
                ll.addView(tab)
            }
        }

        private fun selectTab(ll: LinearLayout, sel: TextView) {
            for (i in 0 until ll.childCount) {
                val child = ll.getChildAt(i)
                if (child is TextView) {
                    val active = child === sel
                    child.setBackgroundResource(if (active) R.drawable.tab_selected_bg else android.R.color.transparent)
                    child.setTextColor(
                        ContextCompat.getColor(
                            context,
                            if (active) R.color.blue else R.color.grey_text
                        )
                    )
                }
            }
        }

        private fun selectTabByKey(ll: LinearLayout, key: String?) {
            for (i in 0 until ll.childCount) {
                val child = ll.getChildAt(i)
                if (key == child.tag) {
                    selectTab(ll, child as TextView)
                    return
                }
            }
        }

        private fun rangeLabel(k: String): String {
            return when (k) {
                "7days" -> "7D"
                "30days" -> "30D"
                "90days" -> "90D"
                else -> k
            }
        }
    }

    inner class StorageHolder(v: View) : RecyclerView.ViewHolder(v) {
        val pieChart: PieChart = v.findViewById(R.id.pie_chart_storage)
        val tvUsed: TextView = v.findViewById(R.id.tv_used_gb)
        val tvAvailable: TextView = v.findViewById(R.id.tv_available_gb)
        val tvTotal: TextView = v.findViewById(R.id.tv_total_gb)
        val tvPercent: TextView = v.findViewById(R.id.tv_usage_percent)
        val tvStatus: TextView = v.findViewById(R.id.tv_storage_status)

        fun bind(d: CardDataModels.StorageCardData) {
            tvTotal.text = "${d.totalGB} GB"
            tvUsed.text = "${d.usedGB} GB"
            tvAvailable.text = "${d.availableGB} GB"

            val pct = if (d.totalGB > 0) (d.usedGB / d.totalGB) * 100.0 else 0.0

            tvPercent.text = "${d.usagePercentage}%"

            buildDonut(d, pct.toFloat())
        }

        private fun buildDonut(d: CardDataModels.StorageCardData, usedPct: Float) {
            val entries = ArrayList<PieEntry>()
            entries.add(PieEntry(d.usedGB.toFloat(), "Used"))
            entries.add(PieEntry(d.availableGB.toFloat(), "Available"))

            val ds = PieDataSet(entries, "")
            ds.setColors(
                ContextCompat.getColor(context, R.color.chart_blue),
                ContextCompat.getColor(context, R.color.grey_badge_bg)
            )
            ds.setDrawValues(false)
            ds.sliceSpace = 2f

            pieChart.data = PieData(ds)
            pieChart.setUsePercentValues(false)
            pieChart.description.isEnabled = false
            pieChart.isDrawHoleEnabled = true
            pieChart.holeRadius = 62f
            pieChart.transparentCircleRadius = 67f
            pieChart.setHoleColor(Color.WHITE)
            pieChart.setEntryLabelColor(Color.TRANSPARENT)
            pieChart.legend.isEnabled = false
            pieChart.setDrawCenterText(false)
            pieChart.rotationAngle = -90f
            pieChart.setTouchEnabled(false)
            pieChart.animateXY(800, 800)
            pieChart.invalidate()
        }
    }

    inner class BillableHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvHours: TextView = v.findViewById(R.id.tv_billing_hours)
        val tvPercent: TextView = v.findViewById(R.id.tv_bh_percentage)
        val tvDateRange: TextView = v.findViewById(R.id.tv_date_range)
        var nav: Navigation? = null

        fun bind(d: CardDataModels.BillableCardData) {
            tvHours.text = d.billableHours
            val badge = d.badge
            tvPercent.text = badge?.label
            if (badge?.color != null) {
                applyBadgeStyle(tvPercent, badge.color)
            } else {
                tvPercent.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (d.billablePercentage >= 0) R.color.chart_green else R.color.chart_red
                    )
                )
            }
            tvDateRange.text = d.dateRangeLabel
        }
    }

    inner class ApproxRevenueHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvRevenue: TextView = v.findViewById(R.id.tv_revenue_value)
        val tvPercent: TextView = v.findViewById(R.id.tv_revenue_percentage)
        val tvDateRange: TextView = v.findViewById(R.id.tv_revenue_date_range)
        var nav: Navigation? = null

        fun bind(d: CardDataModels.ApproxRevenueCardData) {
            val sym = d.currencySymbol ?: ""
            tvRevenue.text = sym + String.format("%,.2f", d.approxRevenue)

            val badge = d.badge
            if (badge?.label != null) {
                tvPercent.text = badge.label
                if (badge.color != null) {
                    applyBadgeStyle(tvPercent, badge.color)
                }
            } else {
                tvPercent.text = "0%"
                tvPercent.setTextColor(ContextCompat.getColor(context, R.color.chart_green))
            }
            tvDateRange.text = d.dateRangeLabel
        }
    }

    inner class SubscriptionHolder(v: View) : RecyclerView.ViewHolder(v) {
        val subscriptionCard: LinearLayout = v.findViewById(R.id.subscription_card)
        val rightLayout: LinearLayout = v.findViewById(R.id.rightLayout)
        val tvPlan: TextView = v.findViewById(R.id.tv_product_subscription)
        val tvValidity: TextView = v.findViewById(R.id.tv_ps_year)
        val tvPendingTxt: TextView = v.findViewById(R.id.tv_pending_txt)
        val tvValidityDate: TextView? = v.findViewById(R.id.tv_validity_date)
        val tvStatusChip: TextView = v.findViewById(R.id.tv_sub_status_chip)
        val tvMessage: TextView = v.findViewById(R.id.tv_message)
        val btnPayNow: Button = v.findViewById(R.id.btn_pay_now)

        fun bind(d: CardDataModels.SubscriptionCardData) {
            tvPlan.text = "Product Subscription"
            tvValidity.text = if (!d.planLabel.isNullOrEmpty()) d.planLabel else "—"

            tvStatusChip.visibility = View.VISIBLE
            val status = d.status
            if (status?.label != null) {
                tvStatusChip.text = status.label
                applyBadgeStyle(tvStatusChip, status.color)
            } else {
                tvStatusChip.text = if (d.isActive) "Active" else "Inactive"
                applyBadgeStyle(tvStatusChip, if (d.isActive) "green" else "grey")
            }

            tvPendingTxt.visibility = View.VISIBLE
            tvPendingTxt.text = if (d.isExpired) "Expired On" else "Valid Until"
            tvPendingTxt.setTextColor(ContextCompat.getColor(context, R.color.grey_text))

            if (tvValidityDate != null) {
                tvValidityDate.visibility = View.VISIBLE
                val dateVal = if (!d.validTill.isNullOrEmpty()) d.validTill!! else "N/A"
                tvValidityDate.text = dateVal

                val hasRealDate = !dateVal.equals("N/A", ignoreCase = true) &&
                        dateVal != "—" && dateVal.isNotEmpty()

                tvValidityDate.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (hasRealDate) R.color.dark_text else R.color.grey_text
                    )
                )
            }

            if (d.canPayNow && d.isExpired) {
                btnPayNow.visibility = View.VISIBLE
                btnPayNow.text = "Pay Now"
                btnPayNow.setOnClickListener {
                    listener.onPaySubscription(d.planLabel, d.validTill)
                }
            } else if (d.canUpgrade) {
                btnPayNow.visibility = View.VISIBLE
                btnPayNow.text = "Upgrade Plan"
                btnPayNow.setOnClickListener {
                    listener.onNavigate(DashboardItem.TYPE_SUBSCRIPTION, null)
                }
            } else {
                btnPayNow.visibility = View.GONE
            }
            btnPayNow.visibility = View.GONE

            rightLayout.visibility = View.GONE
            tvMessage.visibility = View.GONE
        }
    }

    inner class MatterHolder(v: View) : RecyclerView.ViewHolder(v) {
        val barChart: HorizontalBarChart = v.findViewById(R.id.bar_chart_matter)
        val tvGrandTotal: TextView = v.findViewById(R.id.tv_grand_total)
        val tvActive: TextView = v.findViewById(R.id.tv_active_count)
        val tvClosed: TextView = v.findViewById(R.id.tv_closed_count)
        val tvBadge: TextView? = v.findViewById(R.id.tv_badge)
        val llLegend: LinearLayout = v.findViewById(R.id.ll_matter_legend)
        var nav: Navigation? = null

        init {
            v.setOnClickListener {
                listener.onNavigate(DashboardItem.TYPE_MATTER, nav)
            }
        }

        fun bind(d: CardDataModels.MatterCardData) {
            if (tvBadge != null) {
                tvBadge.visibility = View.VISIBLE
                tvBadge.text = d.grandTotal.toString()
            }
            buildBarChart(d)
        }

        private fun buildBarChart(d: CardDataModels.MatterCardData) {
            val chart = d.chartData ?: return
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

            val barData = BarData(sets)
            barData.barWidth = barWidth

            xAxis.setCenterAxisLabels(true)
            xAxis.granularity = 1f
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
            xAxis.textSize = 11f
            xAxis.textColor = Color.parseColor("#8A8A9A")
            xAxis.yOffset = 4f
            xAxis.spaceMin = 0.6f
            xAxis.spaceMax = 0.6f

            val chartXAxis = chart.xAxis
            val yMin = if (chartXAxis != null) chartXAxis.min.toFloat() else 0f
            val yMax = if (chartXAxis != null) chartXAxis.max.toFloat() else 600f
            val yStep = if (chartXAxis != null) chartXAxis.step.toFloat() else 135f

            val axisLeft = barChart.axisLeft
            axisLeft.axisMinimum = yMin
            axisLeft.axisMaximum = yMax
            axisLeft.granularity = yStep
            axisLeft.setLabelCount(((yMax - yMin) / yStep).toInt() + 1, true)
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = Color.parseColor("#F0F0F5")
            axisLeft.gridLineWidth = 1f
            axisLeft.setDrawAxisLine(false)
            axisLeft.textSize = 9f
            axisLeft.textColor = Color.parseColor("#8A8A9A")
            axisLeft.xOffset = 4f

            barChart.axisRight.isEnabled = false

            barChart.renderer = RoundedHorizontalBarChartRenderer(
                barChart,
                barChart.animator,
                barChart.viewPortHandler,
                8f
            )

            barChart.data = barData

            if (dsCount > 1 && yLabels != null) {
                barChart.groupBars(0f, groupSpace, barSpace)
                xAxis.axisMinimum = 0f
                xAxis.axisMaximum = yLabels.size.toFloat()
            }

            barChart.description.isEnabled = false
            barChart.legend.isEnabled = false
            barChart.setFitBars(true)
            barChart.setDrawBorders(false)

            barChart.setViewPortOffsets(
                dpToPx(70).toFloat(),
                dpToPx(10).toFloat(),
                dpToPx(10).toFloat(),
                dpToPx(20).toFloat()
            )

            barChart.setTouchEnabled(true)
            barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry, h: Highlight) {
                    if (e !is BarEntry) return
                    val currentLabels = chart.yLabels
                    if (currentLabels.isNullOrEmpty()) return

                    val groupIndex = e.x.toInt()
                    if (groupIndex < 0 || groupIndex >= currentLabels.size) return

                    val clickedLabel = currentLabels[groupIndex]
                    listener.onNavigate(DashboardItem.TYPE_MATTER, nav, clickedLabel)
                }

                override fun onNothingSelected() {}
            })

            barChart.animateY(600)
            barChart.invalidate()

            buildLegend(d, resolvedColors)
        }

        private fun buildLegend(d: CardDataModels.MatterCardData, colors: IntArray) {
            llLegend.removeAllViews()

            val counts = intArrayOf(d.activeTotalCount, d.closedTotalCount)
            val datasets = d.chartData?.datasets ?: return

            for (i in 0 until datasets.size) {
                val ds = datasets[i]
                val color = colors[Math.min(i, colors.size - 1)]
                val count = if (i < counts.size) counts[i] else 0

                val dot = View(context)
                val dotPx = dpToPx(8)
                val dotLp = LinearLayout.LayoutParams(dotPx, dotPx)
                dotLp.setMargins(0, 0, dpToPx(4), 0)
                dotLp.gravity = Gravity.CENTER_VERTICAL
                dot.layoutParams = dotLp
                val dotBg = GradientDrawable()
                dotBg.shape = GradientDrawable.OVAL
                dotBg.setColor(color)
                dot.background = dotBg

                val tvCount = TextView(context)
                tvCount.text = count.toString()
                tvCount.textSize = DynamicUtils.fifteen.toFloat()
                tvCount.setTextColor(ContextCompat.getColor(context, R.color.black))
                tvCount.setTypeface(null, Typeface.BOLD)
                val countLp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                countLp.setMargins(0, 0, dpToPx(3), 0)
                tvCount.layoutParams = countLp

                val tvLabel = TextView(context)
                tvLabel.text = ds.name
                tvLabel.textSize = DynamicUtils.twelve.toFloat()
                tvLabel.setTextColor(ContextCompat.getColor(context, R.color.black))
                val lblLp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lblLp.setMargins(
                    0, 0,
                    if (i < datasets.size - 1) dpToPx(20) else 0, 0
                )
                tvLabel.layoutParams = lblLp

                val labelName = ds.name
                dot.setOnClickListener {
                    listener.onNavigate(DashboardItem.TYPE_MATTER, nav, labelName)
                }
                tvCount.setOnClickListener {
                    listener.onNavigate(DashboardItem.TYPE_MATTER, nav, labelName)
                }
                tvLabel.setOnClickListener {
                    listener.onNavigate(DashboardItem.TYPE_MATTER, nav, labelName)
                }

                llLegend.addView(dot)
                llLegend.addView(tvCount)
                llLegend.addView(tvLabel)
            }
        }

        private fun resolveColor(apiColor: String?): Int {
            if (apiColor == null) return Color.parseColor("#2979FF")
            return when (apiColor.lowercase()) {
                "blue" -> Color.parseColor("#2979FF")
                "purple", "orange" -> Color.parseColor("#FFA726")
                "green" -> Color.parseColor("#43A047")
                "red" -> Color.parseColor("#E53935")
                "teal" -> Color.parseColor("#00897B")
                else -> Color.parseColor("#2979FF")
            }
        }

        private fun dpToPx(dp: Int): Int {
            return Math.round(dp * context.resources.displayMetrics.density)
        }
    }

    inner class HiringHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvBadge: TextView = v.findViewById(R.id.tv_badge)
        val tvClientCount: TextView = v.findViewById(R.id.tv_client_count)
        val tvTeamCount: TextView = v.findViewById(R.id.tv_team_count)
        val tvClientLabel: TextView = v.findViewById(R.id.tv_client_label)
        val tvTeamLabel: TextView = v.findViewById(R.id.tv_team_label)
        var nav: Navigation? = null

        init {
            v.setOnClickListener { listener.onNavigate(DashboardItem.TYPE_HIRING, nav) }
        }

        fun bind(d: CardDataModels.HiringCardData) {
            applyBadge(
                tvBadge, d.badge,
                ContextCompat.getColor(context, R.color.blue),
                ContextCompat.getColor(context, R.color.blue_pale)
            )
            val groups = d.groups
            if (!groups.isNullOrEmpty()) {
                if (groups.isNotEmpty()) {
                    tvClientCount.text = groups[0].memberCount.toString()
                    tvClientLabel.text = groups[0].name
                }
                if (groups.size > 1) {
                    tvTeamCount.text = groups[1].memberCount.toString()
                    tvTeamLabel.text = groups[1].name
                }
            }
        }
    }

    private fun applyBadge(tv: TextView?, badge: CardDataModels.BadgeData?, textColor: Int, bgTint: Int) {
        if (tv == null) return
        tv.visibility = View.VISIBLE
        val label = if (badge == null) {
            "0"
        } else if (!badge.label.isNullOrEmpty()) {
            badge.label
        } else {
            badge.count.toInt().toString()
        }
        tv.text = label
        tv.setTextColor(textColor)
        tv.setBackgroundResource(R.drawable.badge_bg)
        tv.background.setTint(bgTint)
    }

    private fun applyBadgeStyle(chip: TextView, color: String?) {
        val finalColor = color ?: "grey"

        val textColorRes: Int
        val bgColorRes: Int
        val bgDrawable: Int

        when (finalColor) {
            "green" -> {
                textColorRes = R.color.chart_green
                bgColorRes = R.color.completed_bg
                bgDrawable = R.drawable.completed_badge
            }
            "red" -> {
                textColorRes = R.color.chart_red
                bgColorRes = R.color.cancelled_bg
                bgDrawable = R.drawable.cancelled_badge
            }
            "orange" -> {
                textColorRes = R.color.pending_text
                bgColorRes = R.color.pending_bg
                bgDrawable = R.drawable.pending_badge
            }
            else -> {
                textColorRes = R.color.grey_text
                bgColorRes = R.color.grey_badge_bg
                bgDrawable = R.drawable.grey_badge
            }
        }

        chip.setTextColor(ContextCompat.getColor(context, textColorRes))
        chip.setBackgroundResource(bgDrawable)
        chip.background.setTint(ContextCompat.getColor(context, bgColorRes))
    }
}
