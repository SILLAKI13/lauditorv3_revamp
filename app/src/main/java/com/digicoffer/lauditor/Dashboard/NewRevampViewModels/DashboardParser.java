package com.digicoffer.lauditor.Dashboard.NewRevampViewModels;

import com.digicoffer.lauditor.Dashboard.DahboardModels.CardDataModels;
import com.digicoffer.lauditor.Notifications.Models.Navigation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DashboardParser {

    public static List<DashboardItem> parse(JSONObject root) throws Exception {
        List<DashboardItem> items = new ArrayList<>();
        JSONObject data = root.optJSONObject("data");
        if (data == null) return items;

        parseMeeting(data, items);
        parseAppointment(data, items);
        parseMessages(data, items);
        parseNotification(data, items);
        parseTrend(data, items, "appointmentTrend", DashboardItem.TYPE_APPOINTMENT_TREND);
        parseTrend(data, items, "revenueTrend",     DashboardItem.TYPE_REVENUE_TREND);
        parseAppointmentRevenueTrend(data, items);  // ← NEW
        parseMatter(data, items);
        parseStorage(data, items);
        parseHours(data, items);                    // ← Billable card only
        parseApproximateRevenue(data, items);
        parseSubscription(data, items);
        parseHiring(data, items);

        return items;
    }

    // =========================================================================
    // Navigation helper
    // =========================================================================

    /** Reads navigation_data block and returns a Navigation object (may be null) */
    private static Navigation parseNavigation(JSONObject card) {
        JSONObject nav = card.optJSONObject("navigation_data");
        if (nav == null) return null;

        Navigation n = new Navigation();
        n.setRoute_name(nav.optString("route_name", ""));
        n.setParams(nav.optJSONObject("params"));   // may be null — that's fine
        return n;
    }

    // =========================================================================
    // Card parsers
    // =========================================================================

    private static void parseMeeting(JSONObject data, List<DashboardItem> out) {
        JSONObject card = data.optJSONObject("meeting");
        if (card == null || !card.optBoolean("visible", true)) return;

        CardDataModels.MeetingCardData m = new CardDataModels.MeetingCardData();
        m.badge = parseBadge(card);

        JSONObject d = card.optJSONObject("data");
        if (d != null) {
            m.hasMeeting    = d.optBoolean("hasMeeting");
            m.meetingsCount = d.optInt("meetingsCount");

            JSONObject next = d.optJSONObject("nextMeeting");
            if (next != null) {
                m.subject   = next.optString("subject");
                m.date      = next.optString("date");
                m.startTime = next.optString("startTime");
                m.endTime   = next.optString("endTime");
                m.meetingType = next.optString("type");
            }
        }

        DashboardItem item = new DashboardItem(DashboardItem.TYPE_MEETING, m);
        item.setNavigation(parseNavigation(card));
        out.add(item);
    }

    private static void parseAppointment(JSONObject data, List<DashboardItem> out) {
        JSONObject card = data.optJSONObject("upcoming_appointment");
        if (card == null || !card.optBoolean("visible", true)) return;

        CardDataModels.AppointmentCardData a = new CardDataModels.AppointmentCardData();
        a.badge = parseBadge(card);

        JSONObject d = card.optJSONObject("data");
        if (d != null) {
            a.hasAppointment = d.optBoolean("hasAppointment");
            JSONObject next = d.optJSONObject("nextAppointment");
            if (next != null) {
                a.subject   = next.optString("client_name");
                a.date      = next.optString("date");
                a.startTime = next.optString("startTime");
                a.endTime   = next.optString("endTime");
            }
        }

        DashboardItem item = new DashboardItem(DashboardItem.TYPE_APPOINTMENT, a);
        item.setNavigation(parseNavigation(card));
        out.add(item);
    }

    private static void parseMessages(JSONObject data, List<DashboardItem> out) {
        JSONObject card = data.optJSONObject("messages");
        if (card == null || !card.optBoolean("visible", true)) return;

        CardDataModels.MessagesCardData m = new CardDataModels.MessagesCardData();
        m.badge = parseBadge(card);

        JSONObject d = card.optJSONObject("data");
        if (d != null) {
            m.hasMessages    = d.optBoolean("hasMessages");
            m.clientMessages = d.optInt("clientMessages");
            m.teamMessages   = d.optInt("teamMessages");
        }

        DashboardItem item = new DashboardItem(DashboardItem.TYPE_MESSAGES, m);
        item.setNavigation(parseNavigation(card));
        out.add(item);
    }

    private static void parseNotification(JSONObject data, List<DashboardItem> out) {
        JSONObject card = data.optJSONObject("notification");
        if (card == null || !card.optBoolean("visible", true)) return;

        CardDataModels.NotificationCardData n = new CardDataModels.NotificationCardData();
        n.badge = parseBadge(card);

        JSONObject d = card.optJSONObject("data");
        if (d != null) {
            n.hasNotifications = d.optBoolean("hasNotifications");
            n.unreadCount      = d.optInt("unreadCount");

            JSONArray arr = d.optJSONArray("notifications");
            if (arr != null) {
                n.notifications = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.optJSONObject(i);
                    if (obj == null) continue;
                    CardDataModels.NotificationCardData.NotificationItem ni =
                            new CardDataModels.NotificationCardData.NotificationItem();
                    ni.id      = obj.optString("id");
                    ni.message = obj.optString("message");
                    ni.time    = obj.optString("time");
                    ni.date    = obj.optString("date");
                    ni.isRead  = obj.optBoolean("isRead");
                    n.notifications.add(ni);
                }
            }
        }

        DashboardItem item = new DashboardItem(DashboardItem.TYPE_NOTIFICATION, n);
        item.setNavigation(parseNavigation(card));
        out.add(item);
    }
    private static void parseAppointmentRevenueTrend(JSONObject data, List<DashboardItem> out) {
        JSONObject card = data.optJSONObject("appointmentRevenueTrend");
        if (card == null || !card.optBoolean("visible", true)) {
            // If no separate card, build from appointmentTrend data
            buildRevenueFromAppointmentTrend(data, out);
            return;
        }

        CardDataModels.TrendCardData t = new CardDataModels.TrendCardData();
        t.badge = parseBadge(card);
        t.cardKey = "appointmentRevenueTrend";

        JSONObject d = card.optJSONObject("data");
        if (d != null) {
            t.defaultRange = d.optString("defaultRange", "7days");

            JSONObject currency = d.optJSONObject("currency");
            if (currency != null) t.currencySymbol = currency.optString("symbol");

            JSONObject ranges = d.optJSONObject("ranges");
            if (ranges != null) {
                t.ranges = new java.util.LinkedHashMap<>();
                JSONArray names = ranges.names();
                if (names != null) {
                    for (int i = 0; i < names.length(); i++) {
                        String rKey = names.optString(i);
                        JSONObject rObj = ranges.optJSONObject(rKey);
                        if (rObj == null) continue;

                        CardDataModels.TrendCardData.TrendRange range =
                                new CardDataModels.TrendCardData.TrendRange();

                        // x_axis → labels / key
                        JSONObject xAxis = rObj.optJSONObject("x_axis");
                        if (xAxis != null) {
                            range.xLabels = jsonArrayToStringList(xAxis.optJSONArray("labels"));
                            range.xKey = xAxis.optString("key");
                        }

                        // y_axis for revenue
                        JSONObject yAxis = rObj.optJSONObject("y_axis");
                        if (yAxis != null) {
                            range.yMin = yAxis.optDouble("min", 0);
                            range.yMax = yAxis.optDouble("max", 10000);
                            range.yStep = yAxis.optDouble("step", 2000);
                            range.yUnit = yAxis.optString("unit", "currency");
                            range.yLabel = yAxis.optString("label", "Revenue");
                        }

                        // datasets with revenue amounts
                        JSONArray dsArr = rObj.optJSONArray("datasets");
                        if (dsArr != null) {
                            range.datasets = new ArrayList<>();
                            for (int j = 0; j < dsArr.length(); j++) {
                                JSONObject dsObj = dsArr.optJSONObject(j);
                                if (dsObj == null) continue;
                                CardDataModels.TrendCardData.DataSet ds =
                                        new CardDataModels.TrendCardData.DataSet();
                                ds.name = dsObj.optString("name");
                                ds.color = dsObj.optString("color");
                                ds.data = jsonArrayToDoubleList(dsObj.optJSONArray("data"));
                                range.datasets.add(ds);
                            }
                        }
                        t.ranges.put(rKey, range);
                    }
                }
            }
        }

        DashboardItem item = new DashboardItem(DashboardItem.TYPE_APPOINTMENT_REVENUE_TREND, t);
        item.setNavigation(parseNavigation(card));
        out.add(item);
    }
    private static void buildRevenueFromAppointmentTrend(JSONObject data, List<DashboardItem> out) {
        JSONObject appointmentTrend = data.optJSONObject("appointmentTrend");
        if (appointmentTrend == null) return;

        JSONObject trendData = appointmentTrend.optJSONObject("data");
        if (trendData == null) return;

        CardDataModels.TrendCardData revenueTrend = new CardDataModels.TrendCardData();
        revenueTrend.cardKey = "appointmentRevenueTrend";
        revenueTrend.defaultRange = trendData.optString("defaultRange", "7days");

        JSONObject ranges = trendData.optJSONObject("ranges");
        if (ranges != null) {
            revenueTrend.ranges = new java.util.LinkedHashMap<>();
            JSONArray rangeNames = ranges.names();

            if (rangeNames != null) {
                for (int i = 0; i < rangeNames.length(); i++) {
                    String rangeKey = rangeNames.optString(i);
                    JSONObject rangeObj = ranges.optJSONObject(rangeKey);
                    if (rangeObj == null) continue;

                    // Get tooltip_daily which contains revenue amounts per appointment
                    JSONObject tooltipDaily = rangeObj.optJSONObject("tooltip_daily");
                    if (tooltipDaily == null) continue;

                    CardDataModels.TrendCardData.TrendRange revenueRange =
                            new CardDataModels.TrendCardData.TrendRange();

                    // Copy x_axis
                    JSONObject xAxis = rangeObj.optJSONObject("x_axis");
                    if (xAxis != null) {
                        revenueRange.xLabels = jsonArrayToStringList(xAxis.optJSONArray("labels"));
                        revenueRange.xKey = xAxis.optString("key");
                    }

                    // Set y_axis for revenue
                    revenueRange.yMin = 0;
                    revenueRange.yMax = 10000;
                    revenueRange.yStep = 2000;
                    revenueRange.yUnit = "currency";
                    revenueRange.yLabel = "Revenue (INR)";

                    // Build revenue dataset from tooltip_daily amounts
                    List<Double> revenueData = new ArrayList<>();
                    JSONArray xLabels = xAxis.optJSONArray("labels");

                    if (xLabels != null) {
                        for (int j = 0; j < xLabels.length(); j++) {
                            String label = xLabels.optString(j);
                            double dailyRevenue = 0.0;

                            JSONObject dayData = tooltipDaily.optJSONObject(label);
                            if (dayData != null) {
                                // Check if amount exists (for revenue trend from appointments)
                                if (dayData.has("amount")) {
                                    dailyRevenue = dayData.optDouble("amount");
                                }
                                // Or calculate from appointment amounts if structure has upcoming/completed with amounts
                                else if (dayData.has("upcoming") && dayData.has("completed")) {
                                    // If your tooltip contains appointment objects with amounts
                                    dailyRevenue = calculateRevenueFromAppointments(dayData);
                                }
                            }
                            revenueData.add(dailyRevenue);
                        }
                    }

                    // Create dataset
                    CardDataModels.TrendCardData.DataSet ds =
                            new CardDataModels.TrendCardData.DataSet();
                    ds.name = "Revenue";
                    ds.color = "green";
                    ds.data = revenueData;

                    revenueRange.datasets = new ArrayList<>();
                    revenueRange.datasets.add(ds);
                    revenueTrend.ranges.put(rangeKey, revenueRange);
                }
            }
        }

        DashboardItem item = new DashboardItem(DashboardItem.TYPE_APPOINTMENT_REVENUE_TREND, revenueTrend);
        out.add(item);
    }
    private static double calculateRevenueFromAppointments(JSONObject dayData) {
        double totalRevenue = 0.0;

        // Check if there are upcoming appointments with amounts
        JSONArray upcoming = dayData.optJSONArray("upcoming_appointments");
        if (upcoming != null) {
            for (int i = 0; i < upcoming.length(); i++) {
                JSONObject apt = upcoming.optJSONObject(i);
                if (apt != null) {
                    totalRevenue += apt.optDouble("amount", 0);
                }
            }
        }

        // Check if there are completed appointments with amounts
        JSONArray completed = dayData.optJSONArray("completed_appointments");
        if (completed != null) {
            for (int i = 0; i < completed.length(); i++) {
                JSONObject apt = completed.optJSONObject(i);
                if (apt != null) {
                    totalRevenue += apt.optDouble("amount", 0);
                }
            }
        }

        return totalRevenue;
    }
    private static void parseTrend(JSONObject data,
                                   List<DashboardItem> out,
                                   String key,
                                   int type) {
        JSONObject card = data.optJSONObject(key);
        if (card == null || !card.optBoolean("visible", true)) return;

        CardDataModels.TrendCardData t = new CardDataModels.TrendCardData();
        t.badge   = parseBadge(card);
        t.cardKey = key;

        JSONObject d = card.optJSONObject("data");
        if (d != null) {
            t.defaultRange = d.optString("defaultRange", "7days");

            JSONObject currency = d.optJSONObject("currency");
            if (currency != null) t.currencySymbol = currency.optString("symbol");

            JSONObject ranges = d.optJSONObject("ranges");
            if (ranges != null) {
                t.ranges = new java.util.LinkedHashMap<>();
                JSONArray names = ranges.names();
                if (names != null) {
                    for (int i = 0; i < names.length(); i++) {
                        String rKey = names.optString(i);
                        JSONObject rObj = ranges.optJSONObject(rKey);
                        if (rObj == null) continue;

                        CardDataModels.TrendCardData.TrendRange range =
                                new CardDataModels.TrendCardData.TrendRange();

                        // x_axis → labels / key
                        JSONObject xAxis = rObj.optJSONObject("x_axis");
                        if (xAxis != null) {
                            range.xLabels = jsonArrayToStringList(xAxis.optJSONArray("labels"));
                            range.xKey    = xAxis.optString("key");
                        }

                        // y_axis → min / max / step / unit / label
                        JSONObject yAxis = rObj.optJSONObject("y_axis");
                        if (yAxis != null) {
                            range.yMin   = yAxis.optDouble("min",  0);
                            range.yMax   = yAxis.optDouble("max",  100);
                            range.yStep  = yAxis.optDouble("step", 10);
                            range.yUnit  = yAxis.optString("unit");
                            range.yLabel = yAxis.optString("label");
                        }

                        // datasets
                        JSONArray dsArr = rObj.optJSONArray("datasets");
                        if (dsArr != null) {
                            range.datasets = new ArrayList<>();
                            for (int j = 0; j < dsArr.length(); j++) {
                                JSONObject dsObj = dsArr.optJSONObject(j);
                                if (dsObj == null) continue;
                                CardDataModels.TrendCardData.DataSet ds =
                                        new CardDataModels.TrendCardData.DataSet();
                                ds.name  = dsObj.optString("name");
                                ds.color = dsObj.optString("color");
                                ds.data  = jsonArrayToDoubleList(dsObj.optJSONArray("data"));
                                range.datasets.add(ds);
                            }
                        }
                        t.ranges.put(rKey, range);
                    }
                }
            }
        }

        DashboardItem item = new DashboardItem(type, t);
        item.setNavigation(parseNavigation(card));
        out.add(item);
    }

    private static void parseMatter(JSONObject data, List<DashboardItem> out) {
        JSONObject card = data.optJSONObject("matter");
        if (card == null || !card.optBoolean("visible", true)) return;

        CardDataModels.MatterCardData m = new CardDataModels.MatterCardData();
        m.badge = parseBadge(card);

        JSONObject d = card.optJSONObject("data");
        if (d != null) {
            m.hasMatters       = d.optBoolean("hasMatters");
            m.grandTotal       = d.optInt("grandTotal");
            m.activeTotalCount = d.optInt("activeTotalCount");
            m.closedTotalCount = d.optInt("closedTotalCount");

            JSONObject cd = d.optJSONObject("chartData");
            if (cd != null) {
                m.chartData = new CardDataModels.MatterCardData.ChartData();

                JSONObject xAxis = cd.optJSONObject("x_axis");
                if (xAxis != null) {
                    m.chartData.xAxis      = new CardDataModels.MatterCardData.XAxisConfig();
                    m.chartData.xAxis.min  = xAxis.optDouble("min",  0);
                    m.chartData.xAxis.max  = xAxis.optDouble("max",  600);
                    m.chartData.xAxis.step = xAxis.optDouble("step", 100);
                }

                JSONObject yAxis = cd.optJSONObject("y_axis");
                if (yAxis != null)
                    m.chartData.yLabels = jsonArrayToStringList(yAxis.optJSONArray("labels"));

                JSONArray dsArr = cd.optJSONArray("datasets");
                if (dsArr != null) {
                    m.chartData.datasets = new ArrayList<>();
                    for (int i = 0; i < dsArr.length(); i++) {
                        JSONObject dsObj = dsArr.optJSONObject(i);
                        if (dsObj == null) continue;
                        CardDataModels.MatterCardData.DataSet ds =
                                new CardDataModels.MatterCardData.DataSet();
                        ds.name  = dsObj.optString("name");
                        ds.color = dsObj.optString("color");
                        ds.data  = jsonArrayToFloatList(dsObj.optJSONArray("data"));
                        m.chartData.datasets.add(ds);
                    }
                }
            }
        }

        DashboardItem item = new DashboardItem(DashboardItem.TYPE_MATTER, m);
        item.setNavigation(parseNavigation(card));
        out.add(item);
    }

    private static void parseStorage(JSONObject data, List<DashboardItem> out) {
        JSONObject card = data.optJSONObject("storage");
        if (card == null || !card.optBoolean("visible", true)) return;

        CardDataModels.StorageCardData s = new CardDataModels.StorageCardData();
        s.badge = parseBadge(card);

        JSONObject d = card.optJSONObject("data");
        if (d != null) {
            s.usedGB          = d.optDouble("usedGB");
            s.availableGB     = d.optDouble("availableGB");
            s.totalGB         = d.optDouble("totalGB");
            s.usagePercentage = d.optDouble("usagePercentage");
            s.status          = d.optString("status");
            s.displayText     = d.optString("displayText");
        }

        DashboardItem item = new DashboardItem(DashboardItem.TYPE_STORAGE, s);
        item.setNavigation(parseNavigation(card));
        out.add(item);
    }

    /**
     * "hours" API card → TWO dashboard cards:
     *   TYPE_BILLABLE        (billable/non-billable hours + donut)
     *   TYPE_APPROX_REVENUE  (revenue + avg billing rate)
     */
    private static void parseHours(JSONObject data, List<DashboardItem> out) {
        JSONObject card = data.optJSONObject("hours");
        if (card == null || !card.optBoolean("visible", true)) return;

        JSONObject d = card.optJSONObject("data");
        if (d == null) return;

        Navigation nav = parseNavigation(card);

        // ── Billable card ─────────────────────────────────────────────────
        CardDataModels.BillableCardData b = new CardDataModels.BillableCardData();
        b.badge = parseBadge(card);  // ← from the "badge" object in hours card
        b.billableHours = d.optString("billableHours");           // "236:41"
        b.billablePercentage = d.optDouble("billablePercentage"); // 50.51
        b.nonBillableHours = d.optString("nonBillableHours");     // "231:55"
        b.nonBillablePercentage = d.optDouble("nonBillablePercentage"); // 49.49
        b.dateRangeLabel = d.optString("dateRangeLabel");         // "Jan 2025 - Apr 2026"

        DashboardItem billableItem = new DashboardItem(DashboardItem.TYPE_BILLABLE, b);
        billableItem.setNavigation(nav);
        out.add(billableItem);

        // Note: approxRevenue and averageBillingRate are NOT used in BillableCardData
        // They belong to ApproxRevenueCardData (which should be parsed separately)
    }
    private static void parseApproximateRevenue(JSONObject data, List<DashboardItem> out) {
        JSONObject card = data.optJSONObject("approximate_revenue");
        if (card == null || !card.optBoolean("visible", true)) return;

        CardDataModels.ApproxRevenueCardData r = new CardDataModels.ApproxRevenueCardData();
        r.badge = parseBadge(card);

        JSONObject d = card.optJSONObject("data");
        if (d != null) {
            // Parse amount from approximate_revenue card
            r.approxRevenue = d.optDouble("amount", 0.0);
            r.appointmentCount = d.optInt("appointmentCount", 0);
            r.dateRangeLabel = d.optString("dateRangeLabel", "");
            r.description = d.optString("description", "");

            // Parse currency
            JSONObject currency = d.optJSONObject("currency");
            if (currency != null) {
                r.currencyCode = currency.optString("code", "");
                r.currencySymbol = currency.optString("symbol", "₹");
            }

            // Average billing rate may not be available in this card
            // Keep default or calculate from hours if needed
            r.averageBillingRate = 0.0;
        }

        DashboardItem revenueItem = new DashboardItem(DashboardItem.TYPE_APPROX_REVENUE, r);
        revenueItem.setNavigation(parseNavigation(card));
        out.add(revenueItem);
    }
    private static void parseSubscription(JSONObject data, List<DashboardItem> out) {
        JSONObject card = data.optJSONObject("subscription");
        if (card == null || !card.optBoolean("visible", true)) return;

        CardDataModels.SubscriptionCardData s = new CardDataModels.SubscriptionCardData();

        JSONObject d = card.optJSONObject("data");
        if (d != null) {
            s.isPaid      = d.optBoolean("isPaid");
            s.isActive    = d.optBoolean("isActive");
            s.isExpired   = d.optBoolean("isExpired");
            s.canUpgrade  = d.optBoolean("canUpgrade");
            s.canPayNow   = d.optBoolean("canPayNow");
            s.model       = d.optString("model");
            s.planLabel   = d.optString("planLabel");
            s.validTill   = d.optString("validTill");
            s.validityText= d.optString("validityText");

            JSONObject statusBadge = d.optJSONObject("statusBadge");
            if (statusBadge != null) {
                s.status       = new CardDataModels.SubscriptionCardData.StatusBadge();
                s.status.label = statusBadge.optString("label");
                s.status.color = statusBadge.optString("color");
            }

            JSONObject cta = d.optJSONObject("cta");
            if (cta != null) {
                s.cta        = new CardDataModels.SubscriptionCardData.CtaData();
                s.cta.label  = cta.optString("label");
                s.cta.style  = cta.optString("style");
                s.cta.action = cta.optString("action");
            }
        }

        DashboardItem item = new DashboardItem(DashboardItem.TYPE_SUBSCRIPTION, s);
        item.setNavigation(parseNavigation(card));
        out.add(item);
    }

    private static void parseHiring(JSONObject data, List<DashboardItem> out) {
        JSONObject card = data.optJSONObject("hiring");
        if (card == null || !card.optBoolean("visible", true)) return;

        CardDataModels.HiringCardData h = new CardDataModels.HiringCardData();
        h.badge = parseBadge(card);

        JSONObject d = card.optJSONObject("data");
        if (d != null) {
            h.totalMembers = d.optInt("totalMembers");

            JSONArray arr = d.optJSONArray("groups");
            if (arr != null) {
                h.groups = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.optJSONObject(i);
                    if (obj == null) continue;
                    CardDataModels.HiringCardData.GroupItem g =
                            new CardDataModels.HiringCardData.GroupItem();
                    g.id          = obj.optString("id");
                    g.name        = obj.optString("name");
                    g.memberCount = obj.optInt("memberCount");
                    h.groups.add(g);
                }
            }
        }

        DashboardItem item = new DashboardItem(DashboardItem.TYPE_HIRING, h);
        item.setNavigation(parseNavigation(card));
        out.add(item);
    }

    // =========================================================================
    // Shared helpers
    // =========================================================================

    static CardDataModels.BadgeData parseBadge(JSONObject card) {
        JSONObject b = card.optJSONObject("badge");
        if (b == null) return null;
        return new CardDataModels.BadgeData(
                b.optDouble("count"),
                b.optString("label"),
                b.optString("color", "blue"));
    }

    private static List<String> jsonArrayToStringList(JSONArray arr) {
        List<String> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) list.add(arr.optString(i));
        return list;
    }

    private static List<Double> jsonArrayToDoubleList(JSONArray arr) {
        List<Double> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) list.add(arr.optDouble(i));
        return list;
    }

    private static List<Float> jsonArrayToFloatList(JSONArray arr) {
        List<Float> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) list.add((float) arr.optDouble(i));
        return list;
    }
}