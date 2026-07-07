package com.digicoffer.lauditor.Dashboard.DahboardModels;

import java.util.List;
import java.util.Map;

public final class CardDataModels {

    private CardDataModels() {}

    public static class BadgeData {
        public double count;
        public String label;
        public String color;
        public BadgeData(double count, String label, String color) {
            this.count = count; this.label = label; this.color = color;
        }
    }

    // ── Today's Activities ────────────────────────────────────────────────

    public static class MeetingCardData {
        public boolean hasMeeting;
        public int     meetingsCount;
        public String  subject, date, startTime, endTime, meetingType;
        public BadgeData badge;
    }

    public static class AppointmentCardData {
        public boolean hasAppointment;
        public String  subject, date, startTime, endTime;
        public BadgeData badge;
    }

    public static class MessagesCardData {
        public boolean hasMessages;
        public int     clientMessages, teamMessages;
        public BadgeData badge;
    }

    public static class NotificationCardData {
        public boolean hasNotifications;
        public int     unreadCount;
        public BadgeData badge;
        public List<NotificationItem> notifications;

        public static class NotificationItem {
            public String  id, message, time, date;
            public boolean isRead;
        }
    }

    // ── Analytics Overview ────────────────────────────────────────────────

    public static class TrendCardData {
        public String cardKey;       // "revenueTrend" | "appointmentTrend"
        public String defaultRange;
        public String currencySymbol;
        public Map<String, TrendRange> ranges;
        public BadgeData badge;

        public static class TrendRange {
            public List<String>  xLabels;
            public String        xKey;
            public double        yMin, yMax, yStep;
            public String        yUnit, yLabel;
            public List<DataSet> datasets;
        }

        public static class DataSet {
            public String       name, color;
            public List<Double> data;
        }
    }

    // ─── Add / update these fields inside CardDataModels.MatterCardData ──────────
//
// API JSON shape:
// {
//   "grandTotal": 635,
//   "activeTotalCount": 615,
//   "closedTotalCount": 20,
//   "chartData": {
//     "x_axis": { "key": "count", "min": 0, "max": 540, "step": 135 },
//     "y_axis": { "key": "type",  "labels": ["Legal", "General"] },
//     "datasets": [
//       { "name": "Active",  "color": "blue",   "data": [527, 88] },
//       { "name": "Closed",  "color": "purple",  "data": [11,  9] }
//     ]
//   }
// }
// ─────────────────────────────────────────────────────────────────────────────

// ── Replace MatterCardData in CardDataModels.java ────────────────────────────
// Added: public BadgeData badge;  (parser sets this via parseBadge())
// ─────────────────────────────────────────────────────────────────────────────

    public static class MatterCardData {
        public BadgeData badge;            // ← ADD THIS — parser sets m.badge
        public boolean   hasMatters;
        public int       grandTotal;       // 635 — shown in blue circle badge
        public int       activeTotalCount; // 615 — legend count for dataset[0]
        public int       closedTotalCount; // 20  — legend count for dataset[1]
        public ChartData chartData;

        public static class ChartData {
            public XAxisConfig  xAxis;     // from x_axis { min, max, step }
            public List<String> yLabels;   // from y_axis.labels ["Legal","General"]
            public List<DataSet> datasets;
        }

        public static class XAxisConfig {
            public double min;             // 0
            public double max;             // 540
            public double step;            // 135
        }

        public static class DataSet {
            public String      name;       // "Active" / "Closed"
            public String      color;      // "blue"   / "purple"
            public List<Float> data;       // [527, 88]
        }
    }

// ─── Gson / JSON parsing note ──────────────────────────────────────────────
// If you use Gson, add @SerializedName annotations:
//
//   @SerializedName("x_axis")  public XAxisConfig xAxis;
//   @SerializedName("labels")  public List<String> yLabels;  // nested inside y_axis
//
// Or flatten manually in your API response mapper:
//   cardData.chartData.yLabels = response.chartData.yAxis.labels;
//   cardData.chartData.xAxis   = response.chartData.xAxis;

    public static class StorageCardData {
        public double usedGB, availableGB, totalGB, usagePercentage;
        public String status, displayText;
        public BadgeData badge;
    }

    // ── Business Metrics ──────────────────────────────────────────────────
    // Parsed from a single "hours" API card but shown as TWO separate cards

    /** Card: Total Billable Hours + Non-Billable Hours with donut chart */
    public static class BillableCardData {
        public String billableHours;
        public double billablePercentage;
        public String nonBillableHours;
        public double nonBillablePercentage;
        public String dateRangeLabel;
        public BadgeData badge;
    }

    /** Card: Approximate Revenue + Average Billing Rate */
    public static class ApproxRevenueCardData {
        public BadgeData badge;
        public double approxRevenue;
        public double averageBillingRate;
        public String dateRangeLabel;
        public String currencyCode;
        public String currencySymbol;

        // Add these new fields for the approximate_revenue card
        public int appointmentCount;
        public String description;
    }

    public static class SubscriptionCardData {
        public boolean isPaid, isActive, isExpired, canUpgrade, canPayNow;
        public String  model, planLabel, validityText, validTill;
        public CtaData cta;
        public  StatusBadge status;
        public  static class StatusBadge{
            public String label,color;
        }

        public static class CtaData {
            public String label, style, action;
        }
    }

    // ── Extras ────────────────────────────────────────────────────────────

    public static class HiringCardData {
        public int totalMembers;
        public List<GroupItem> groups;
        public BadgeData badge;
        public static class GroupItem {
            public String id, name;
            public int    memberCount;
        }
    }
}