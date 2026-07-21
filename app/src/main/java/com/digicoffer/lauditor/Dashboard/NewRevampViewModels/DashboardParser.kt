package com.digicoffer.lauditor.Dashboard.NewRevampViewModels

import com.digicoffer.lauditor.Dashboard.DahboardModels.CardDataModels
import com.digicoffer.lauditor.Notifications.Models.Navigation
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList
import java.util.LinkedHashMap

class DashboardParser {
    companion object {
        @JvmStatic
        @Throws(Exception::class)
        fun parse(root: JSONObject): List<DashboardItem> {
            val items = ArrayList<DashboardItem>()
            val data = root.optJSONObject("data") ?: return items

            parseMeeting(data, items)
            parseAppointment(data, items)
            parseMessages(data, items)
            parseNotification(data, items)
            parseTrend(data, items, "appointmentTrend", DashboardItem.TYPE_APPOINTMENT_TREND)
            parseTrend(data, items, "revenueTrend", DashboardItem.TYPE_REVENUE_TREND)
            parseAppointmentRevenueTrend(data, items)
            parseMatter(data, items)
            parseStorage(data, items)
            parseHours(data, items)
            parseApproximateRevenue(data, items)
            parseSubscription(data, items)
            parseHiring(data, items)

            return items
        }

        @JvmStatic
        private fun parseNavigation(card: JSONObject): Navigation? {
            val nav = card.optJSONObject("navigation_data") ?: return null

            val n = Navigation()
            n.route_name = nav.optString("route_name", "")
            n.params = nav.optJSONObject("params")
            return n
        }

        @JvmStatic
        private fun parseMeeting(data: JSONObject, out: MutableList<DashboardItem>) {
            val card = data.optJSONObject("meeting")
            if (card == null || !card.optBoolean("visible", true)) return

            val m = CardDataModels.MeetingCardData()
            m.badge = parseBadge(card)

            val d = card.optJSONObject("data")
            if (d != null) {
                m.hasMeeting = d.optBoolean("hasMeeting")
                m.meetingsCount = d.optInt("meetingsCount")

                val next = d.optJSONObject("nextMeeting")
                if (next != null) {
                    m.subject = next.optString("subject")
                    m.date = next.optString("date")
                    m.startTime = next.optString("startTime")
                    m.endTime = next.optString("endTime")
                    m.meetingType = next.optString("type")
                }
            }

            val item = DashboardItem(DashboardItem.TYPE_MEETING, m)
            item.navigation = parseNavigation(card)
            out.add(item)
        }

        @JvmStatic
        private fun parseAppointment(data: JSONObject, out: MutableList<DashboardItem>) {
            val card = data.optJSONObject("upcoming_appointment")
            if (card == null || !card.optBoolean("visible", true)) return

            val a = CardDataModels.AppointmentCardData()
            a.badge = parseBadge(card)

            val d = card.optJSONObject("data")
            if (d != null) {
                a.hasAppointment = d.optBoolean("hasAppointment")
                val next = d.optJSONObject("nextAppointment")
                if (next != null) {
                    a.subject = next.optString("client_name")
                    a.date = next.optString("date")
                    a.startTime = next.optString("startTime")
                    a.endTime = next.optString("endTime")
                }
            }

            val item = DashboardItem(DashboardItem.TYPE_APPOINTMENT, a)
            item.navigation = parseNavigation(card)
            out.add(item)
        }

        @JvmStatic
        private fun parseMessages(data: JSONObject, out: MutableList<DashboardItem>) {
            val card = data.optJSONObject("messages")
            if (card == null || !card.optBoolean("visible", true)) return

            val m = CardDataModels.MessagesCardData()
            m.badge = parseBadge(card)

            val d = card.optJSONObject("data")
            if (d != null) {
                m.hasMessages = d.optBoolean("hasMessages")
                m.clientMessages = d.optInt("clientMessages")
                m.teamMessages = d.optInt("teamMessages")
            }

            val item = DashboardItem(DashboardItem.TYPE_MESSAGES, m)
            item.navigation = parseNavigation(card)
            out.add(item)
        }

        @JvmStatic
        private fun parseNotification(data: JSONObject, out: MutableList<DashboardItem>) {
            val card = data.optJSONObject("notification")
            if (card == null || !card.optBoolean("visible", true)) return

            val n = CardDataModels.NotificationCardData()
            n.badge = parseBadge(card)

            val d = card.optJSONObject("data")
            if (d != null) {
                n.hasNotifications = d.optBoolean("hasNotifications")
                n.unreadCount = d.optInt("unreadCount")

                val arr = d.optJSONArray("notifications")
                if (arr != null) {
                    val notifs = ArrayList<CardDataModels.NotificationCardData.NotificationItem>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val ni = CardDataModels.NotificationCardData.NotificationItem()
                        ni.id = obj.optString("id")
                        ni.message = obj.optString("message")
                        ni.time = obj.optString("time")
                        ni.date = obj.optString("date")
                        ni.isRead = obj.optBoolean("isRead")
                        notifs.add(ni)
                    }
                    n.notifications = notifs
                }
            }

            val item = DashboardItem(DashboardItem.TYPE_NOTIFICATION, n)
            item.navigation = parseNavigation(card)
            out.add(item)
        }

        @JvmStatic
        private fun parseAppointmentRevenueTrend(data: JSONObject, out: MutableList<DashboardItem>) {
            val card = data.optJSONObject("appointmentRevenueTrend")
            if (card == null || !card.optBoolean("visible", true)) {
                buildRevenueFromAppointmentTrend(data, out)
                return
            }

            val t = CardDataModels.TrendCardData()
            t.badge = parseBadge(card)
            t.cardKey = "appointmentRevenueTrend"

            val d = card.optJSONObject("data")
            if (d != null) {
                t.defaultRange = d.optString("defaultRange", "7days")

                val currency = d.optJSONObject("currency")
                if (currency != null) t.currencySymbol = currency.optString("symbol")

                val ranges = d.optJSONObject("ranges")
                if (ranges != null) {
                    val map = LinkedHashMap<String, CardDataModels.TrendCardData.TrendRange>()
                    val names = ranges.names()
                    if (names != null) {
                        for (i in 0 until names.length()) {
                            val rKey = names.optString(i)
                            val rObj = ranges.optJSONObject(rKey) ?: continue

                            val range = CardDataModels.TrendCardData.TrendRange()

                            val xAxis = rObj.optJSONObject("x_axis")
                            if (xAxis != null) {
                                range.xLabels = jsonArrayToStringList(xAxis.optJSONArray("labels"))
                                range.xKey = xAxis.optString("key")
                            }

                            val yAxis = rObj.optJSONObject("y_axis")
                            if (yAxis != null) {
                                range.yMin = yAxis.optDouble("min", 0.0)
                                range.yMax = yAxis.optDouble("max", 10000.0)
                                range.yStep = yAxis.optDouble("step", 2000.0)
                                range.yUnit = yAxis.optString("unit", "currency")
                                range.yLabel = yAxis.optString("label", "Revenue")
                            }

                            val dsArr = rObj.optJSONArray("datasets")
                            if (dsArr != null) {
                                val datasets = ArrayList<CardDataModels.TrendCardData.DataSet>()
                                for (j in 0 until dsArr.length()) {
                                    val dsObj = dsArr.optJSONObject(j) ?: continue
                                    val ds = CardDataModels.TrendCardData.DataSet()
                                    ds.name = dsObj.optString("name")
                                    ds.color = dsObj.optString("color")
                                    ds.data = jsonArrayToDoubleList(dsObj.optJSONArray("data"))
                                    datasets.add(ds)
                                }
                                range.datasets = datasets
                            }
                            map[rKey] = range
                        }
                    }
                    t.ranges = map
                }
            }

            val item = DashboardItem(DashboardItem.TYPE_APPOINTMENT_REVENUE_TREND, t)
            item.navigation = parseNavigation(card)
            out.add(item)
        }

        @JvmStatic
        private fun buildRevenueFromAppointmentTrend(data: JSONObject, out: MutableList<DashboardItem>) {
            val appointmentTrend = data.optJSONObject("appointmentTrend") ?: return
            val trendData = appointmentTrend.optJSONObject("data") ?: return

            val revenueTrend = CardDataModels.TrendCardData()
            revenueTrend.cardKey = "appointmentRevenueTrend"
            revenueTrend.defaultRange = trendData.optString("defaultRange", "7days")

            val ranges = trendData.optJSONObject("ranges")
            if (ranges != null) {
                val map = LinkedHashMap<String, CardDataModels.TrendCardData.TrendRange>()
                val rangeNames = ranges.names()

                if (rangeNames != null) {
                    for (i in 0 until rangeNames.length()) {
                        val rangeKey = rangeNames.optString(i)
                        val rangeObj = ranges.optJSONObject(rangeKey) ?: continue

                        val tooltipDaily = rangeObj.optJSONObject("tooltip_daily") ?: continue

                        val revenueRange = CardDataModels.TrendCardData.TrendRange()

                        val xAxis = rangeObj.optJSONObject("x_axis")
                        if (xAxis != null) {
                            revenueRange.xLabels = jsonArrayToStringList(xAxis.optJSONArray("labels"))
                            revenueRange.xKey = xAxis.optString("key")
                        }

                        revenueRange.yMin = 0.0
                        revenueRange.yMax = 10000.0
                        revenueRange.yStep = 2000.0
                        revenueRange.yUnit = "currency"
                        revenueRange.yLabel = "Revenue (INR)"

                        val revenueData = ArrayList<Double>()
                        val xLabels = xAxis?.optJSONArray("labels")

                        if (xLabels != null) {
                            for (j in 0 until xLabels.length()) {
                                val label = xLabels.optString(j)
                                var dailyRevenue = 0.0

                                val dayData = tooltipDaily.optJSONObject(label)
                                if (dayData != null) {
                                    if (dayData.has("amount")) {
                                        dailyRevenue = dayData.optDouble("amount")
                                    } else if (dayData.has("upcoming") && dayData.has("completed")) {
                                        dailyRevenue = calculateRevenueFromAppointments(dayData)
                                    }
                                }
                                revenueData.add(dailyRevenue)
                            }
                        }

                        val ds = CardDataModels.TrendCardData.DataSet()
                        ds.name = "Revenue"
                        ds.color = "green"
                        ds.data = revenueData

                        val datasets = ArrayList<CardDataModels.TrendCardData.DataSet>()
                        datasets.add(ds)
                        revenueRange.datasets = datasets
                        map[rangeKey] = revenueRange
                    }
                }
                revenueTrend.ranges = map
            }

            val item = DashboardItem(DashboardItem.TYPE_APPOINTMENT_REVENUE_TREND, revenueTrend)
            out.add(item)
        }

        @JvmStatic
        private fun calculateRevenueFromAppointments(dayData: JSONObject): Double {
            var totalRevenue = 0.0

            val upcoming = dayData.optJSONArray("upcoming_appointments")
            if (upcoming != null) {
                for (i in 0 until upcoming.length()) {
                    val apt = upcoming.optJSONObject(i)
                    if (apt != null) {
                        totalRevenue += apt.optDouble("amount", 0.0)
                    }
                }
            }

            val completed = dayData.optJSONArray("completed_appointments")
            if (completed != null) {
                for (i in 0 until completed.length()) {
                    val apt = completed.optJSONObject(i)
                    if (apt != null) {
                        totalRevenue += apt.optDouble("amount", 0.0)
                    }
                }
            }

            return totalRevenue
        }

        @JvmStatic
        private fun parseTrend(data: JSONObject, out: MutableList<DashboardItem>, key: String, type: Int) {
            val card = data.optJSONObject(key)
            if (card == null || !card.optBoolean("visible", true)) return

            val t = CardDataModels.TrendCardData()
            t.badge = parseBadge(card)
            t.cardKey = key

            val d = card.optJSONObject("data")
            if (d != null) {
                t.defaultRange = d.optString("defaultRange", "7days")

                val currency = d.optJSONObject("currency")
                if (currency != null) t.currencySymbol = currency.optString("symbol")

                val ranges = d.optJSONObject("ranges")
                if (ranges != null) {
                    val map = LinkedHashMap<String, CardDataModels.TrendCardData.TrendRange>()
                    val names = ranges.names()
                    if (names != null) {
                        for (i in 0 until names.length()) {
                            val rKey = names.optString(i)
                            val rObj = ranges.optJSONObject(rKey) ?: continue

                            val range = CardDataModels.TrendCardData.TrendRange()

                            val xAxis = rObj.optJSONObject("x_axis")
                            if (xAxis != null) {
                                range.xLabels = jsonArrayToStringList(xAxis.optJSONArray("labels"))
                                range.xKey = xAxis.optString("key")
                            }

                            val yAxis = rObj.optJSONObject("y_axis")
                            if (yAxis != null) {
                                range.yMin = yAxis.optDouble("min", 0.0)
                                range.yMax = yAxis.optDouble("max", 100.0)
                                range.yStep = yAxis.optDouble("step", 10.0)
                                range.yUnit = yAxis.optString("unit")
                                range.yLabel = yAxis.optString("label")
                            }

                            val dsArr = rObj.optJSONArray("datasets")
                            if (dsArr != null) {
                                val datasets = ArrayList<CardDataModels.TrendCardData.DataSet>()
                                for (j in 0 until dsArr.length()) {
                                    val dsObj = dsArr.optJSONObject(j) ?: continue
                                    val ds = CardDataModels.TrendCardData.DataSet()
                                    ds.name = dsObj.optString("name")
                                    ds.color = dsObj.optString("color")
                                    ds.data = jsonArrayToDoubleList(dsObj.optJSONArray("data"))
                                    datasets.add(ds)
                                }
                                range.datasets = datasets
                            }
                            map[rKey] = range
                        }
                    }
                    t.ranges = map
                }
            }

            val item = DashboardItem(type, t)
            item.navigation = parseNavigation(card)
            out.add(item)
        }

        @JvmStatic
        private fun parseMatter(data: JSONObject, out: MutableList<DashboardItem>) {
            val card = data.optJSONObject("matter")
            if (card == null || !card.optBoolean("visible", true)) return

            val m = CardDataModels.MatterCardData()
            m.badge = parseBadge(card)

            val d = card.optJSONObject("data")
            if (d != null) {
                m.hasMatters = d.optBoolean("hasMatters")
                m.grandTotal = d.optInt("grandTotal")
                m.activeTotalCount = d.optInt("activeTotalCount")
                m.closedTotalCount = d.optInt("closedTotalCount")

                val cd = d.optJSONObject("chartData")
                if (cd != null) {
                    val chartData = CardDataModels.MatterCardData.ChartData()
                    m.chartData = chartData

                    val xAxis = cd.optJSONObject("x_axis")
                    if (xAxis != null) {
                        val xAxisConfig = CardDataModels.MatterCardData.XAxisConfig()
                        xAxisConfig.min = xAxis.optDouble("min", 0.0)
                        xAxisConfig.max = xAxis.optDouble("max", 600.0)
                        xAxisConfig.step = xAxis.optDouble("step", 100.0)
                        chartData.xAxis = xAxisConfig
                    }

                    val yAxis = cd.optJSONObject("y_axis")
                    if (yAxis != null) {
                        chartData.yLabels = jsonArrayToStringList(yAxis.optJSONArray("labels"))
                    }

                    val dsArr = cd.optJSONArray("datasets")
                    if (dsArr != null) {
                        val datasets = ArrayList<CardDataModels.MatterCardData.DataSet>()
                        for (i in 0 until dsArr.length()) {
                            val dsObj = dsArr.optJSONObject(i) ?: continue
                            val ds = CardDataModels.MatterCardData.DataSet()
                            ds.name = dsObj.optString("name")
                            ds.color = dsObj.optString("color")
                            ds.data = jsonArrayToFloatList(dsObj.optJSONArray("data"))
                            datasets.add(ds)
                        }
                        chartData.datasets = datasets
                    }
                }
            }

            val item = DashboardItem(DashboardItem.TYPE_MATTER, m)
            item.navigation = parseNavigation(card)
            out.add(item)
        }

        @JvmStatic
        private fun parseStorage(data: JSONObject, out: MutableList<DashboardItem>) {
            val card = data.optJSONObject("storage")
            if (card == null || !card.optBoolean("visible", true)) return

            val s = CardDataModels.StorageCardData()
            s.badge = parseBadge(card)

            val d = card.optJSONObject("data")
            if (d != null) {
                s.usedGB = d.optDouble("usedGB")
                s.availableGB = d.optDouble("availableGB")
                s.totalGB = d.optDouble("totalGB")
                s.usagePercentage = d.optDouble("usagePercentage")
                s.status = d.optString("status")
                s.displayText = d.optString("displayText")
            }

            val item = DashboardItem(DashboardItem.TYPE_STORAGE, s)
            item.navigation = parseNavigation(card)
            out.add(item)
        }

        @JvmStatic
        private fun parseHours(data: JSONObject, out: MutableList<DashboardItem>) {
            val card = data.optJSONObject("hours")
            if (card == null || !card.optBoolean("visible", true)) return

            val d = card.optJSONObject("data") ?: return
            val nav = parseNavigation(card)

            val b = CardDataModels.BillableCardData()
            b.badge = parseBadge(card)
            b.billableHours = d.optString("billableHours")
            b.billablePercentage = d.optDouble("billablePercentage")
            b.nonBillableHours = d.optString("nonBillableHours")
            b.nonBillablePercentage = d.optDouble("nonBillablePercentage")
            b.dateRangeLabel = d.optString("dateRangeLabel")

            val billableItem = DashboardItem(DashboardItem.TYPE_BILLABLE, b)
            billableItem.navigation = nav
            out.add(billableItem)
        }

        @JvmStatic
        private fun parseApproximateRevenue(data: JSONObject, out: MutableList<DashboardItem>) {
            val card = data.optJSONObject("approximate_revenue")
            if (card == null || !card.optBoolean("visible", true)) return

            val r = CardDataModels.ApproxRevenueCardData()
            r.badge = parseBadge(card)

            val d = card.optJSONObject("data")
            if (d != null) {
                r.approxRevenue = d.optDouble("amount", 0.0)
                r.appointmentCount = d.optInt("appointmentCount", 0)
                r.dateRangeLabel = d.optString("dateRangeLabel", "")
                r.description = d.optString("description", "")

                val currency = d.optJSONObject("currency")
                if (currency != null) {
                    r.currencyCode = currency.optString("code", "")
                    r.currencySymbol = currency.optString("symbol", "₹")
                }

                r.averageBillingRate = 0.0
            }

            val revenueItem = DashboardItem(DashboardItem.TYPE_APPROX_REVENUE, r)
            revenueItem.navigation = parseNavigation(card)
            out.add(revenueItem)
        }

        @JvmStatic
        private fun parseSubscription(data: JSONObject, out: MutableList<DashboardItem>) {
            val card = data.optJSONObject("subscription")
            if (card == null || !card.optBoolean("visible", true)) return

            val s = CardDataModels.SubscriptionCardData()

            val d = card.optJSONObject("data")
            if (d != null) {
                s.isPaid = d.optBoolean("isPaid")
                s.isActive = d.optBoolean("isActive")
                s.isExpired = d.optBoolean("isExpired")
                s.canUpgrade = d.optBoolean("canUpgrade")
                s.canPayNow = d.optBoolean("canPayNow")
                s.model = d.optString("model")
                s.planLabel = d.optString("planLabel")
                s.validTill = d.optString("validTill")
                s.validityText = d.optString("validityText")

                val statusBadge = d.optJSONObject("statusBadge")
                if (statusBadge != null) {
                    val sb = CardDataModels.SubscriptionCardData.StatusBadge()
                    sb.label = statusBadge.optString("label")
                    sb.color = statusBadge.optString("color")
                    s.status = sb
                }

                val cta = d.optJSONObject("cta")
                if (cta != null) {
                    val ctaData = CardDataModels.SubscriptionCardData.CtaData()
                    ctaData.label = cta.optString("label")
                    ctaData.style = cta.optString("style")
                    ctaData.action = cta.optString("action")
                    s.cta = ctaData
                }
            }

            val item = DashboardItem(DashboardItem.TYPE_SUBSCRIPTION, s)
            item.navigation = parseNavigation(card)
            out.add(item)
        }

        @JvmStatic
        private fun parseHiring(data: JSONObject, out: MutableList<DashboardItem>) {
            val card = data.optJSONObject("hiring")
            if (card == null || !card.optBoolean("visible", true)) return

            val h = CardDataModels.HiringCardData()
            h.badge = parseBadge(card)

            val d = card.optJSONObject("data")
            if (d != null) {
                h.totalMembers = d.optInt("totalMembers")

                val arr = d.optJSONArray("groups")
                if (arr != null) {
                    val groups = ArrayList<CardDataModels.HiringCardData.GroupItem>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val g = CardDataModels.HiringCardData.GroupItem()
                        g.id = obj.optString("id")
                        g.name = obj.optString("name")
                        g.memberCount = obj.optInt("memberCount")
                        groups.add(g)
                    }
                    h.groups = groups
                }
            }

            val item = DashboardItem(DashboardItem.TYPE_HIRING, h)
            item.navigation = parseNavigation(card)
            out.add(item)
        }

        @JvmStatic
        fun parseBadge(card: JSONObject): CardDataModels.BadgeData? {
            val b = card.optJSONObject("badge") ?: return null
            return CardDataModels.BadgeData(
                b.optDouble("count"),
                b.optString("label"),
                b.optString("color", "blue")
            )
        }

        @JvmStatic
        private fun jsonArrayToStringList(arr: JSONArray?): List<String> {
            val list = ArrayList<String>()
            if (arr == null) return list
            for (i in 0 until arr.length()) {
                list.add(arr.optString(i))
            }
            return list
        }

        @JvmStatic
        private fun jsonArrayToDoubleList(arr: JSONArray?): List<Double> {
            val list = ArrayList<Double>()
            if (arr == null) return list
            for (i in 0 until arr.length()) {
                list.add(arr.optDouble(i))
            }
            return list
        }

        @JvmStatic
        private fun jsonArrayToFloatList(arr: JSONArray?): List<Float> {
            val list = ArrayList<Float>()
            if (arr == null) return list
            for (i in 0 until arr.length()) {
                list.add(arr.optDouble(i).toFloat())
            }
            return list
        }
    }
}
