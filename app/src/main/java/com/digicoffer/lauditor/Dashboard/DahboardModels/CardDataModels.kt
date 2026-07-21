package com.digicoffer.lauditor.Dashboard.DahboardModels

class CardDataModels private constructor() {

    class BadgeData(
        @JvmField var count: Double,
        @JvmField var label: String?,
        @JvmField var color: String?
    )

    // ── Today's Activities ────────────────────────────────────────────────

    class MeetingCardData {
        @JvmField var hasMeeting: Boolean = false
        @JvmField var meetingsCount: Int = 0
        @JvmField var subject: String? = null
        @JvmField var date: String? = null
        @JvmField var startTime: String? = null
        @JvmField var endTime: String? = null
        @JvmField var meetingType: String? = null
        @JvmField var badge: BadgeData? = null
    }

    class AppointmentCardData {
        @JvmField var hasAppointment: Boolean = false
        @JvmField var subject: String? = null
        @JvmField var date: String? = null
        @JvmField var startTime: String? = null
        @JvmField var endTime: String? = null
        @JvmField var badge: BadgeData? = null
    }

    class MessagesCardData {
        @JvmField var hasMessages: Boolean = false
        @JvmField var clientMessages: Int = 0
        @JvmField var teamMessages: Int = 0
        @JvmField var badge: BadgeData? = null
    }

    class NotificationCardData {
        @JvmField var hasNotifications: Boolean = false
        @JvmField var unreadCount: Int = 0
        @JvmField var badge: BadgeData? = null
        @JvmField var notifications: List<NotificationItem>? = null

        class NotificationItem {
            @JvmField var id: String? = null
            @JvmField var message: String? = null
            @JvmField var time: String? = null
            @JvmField var date: String? = null
            @JvmField var isRead: Boolean = false
        }
    }

    // ── Analytics Overview ────────────────────────────────────────────────

    class TrendCardData {
        @JvmField var cardKey: String? = null // "revenueTrend" | "appointmentTrend"
        @JvmField var defaultRange: String? = null
        @JvmField var currencySymbol: String? = null
        @JvmField var ranges: Map<String, TrendRange>? = null
        @JvmField var badge: BadgeData? = null

        class TrendRange {
            @JvmField var xLabels: List<String>? = null
            @JvmField var xKey: String? = null
            @JvmField var yMin: Double = 0.0
            @JvmField var yMax: Double = 0.0
            @JvmField var yStep: Double = 0.0
            @JvmField var yUnit: String? = null
            @JvmField var yLabel: String? = null
            @JvmField var datasets: List<DataSet>? = null
        }

        class DataSet {
            @JvmField var name: String? = null
            @JvmField var color: String? = null
            @JvmField var data: List<Double>? = null
        }
    }

    class MatterCardData {
        @JvmField var badge: BadgeData? = null
        @JvmField var hasMatters: Boolean = false
        @JvmField var grandTotal: Int = 0
        @JvmField var activeTotalCount: Int = 0
        @JvmField var closedTotalCount: Int = 0
        @JvmField var chartData: ChartData? = null

        class ChartData {
            @JvmField var xAxis: XAxisConfig? = null
            @JvmField var yLabels: List<String>? = null
            @JvmField var datasets: List<DataSet>? = null
        }

        class XAxisConfig {
            @JvmField var min: Double = 0.0
            @JvmField var max: Double = 0.0
            @JvmField var step: Double = 0.0
        }

        class DataSet {
            @JvmField var name: String? = null
            @JvmField var color: String? = null
            @JvmField var data: List<Float>? = null
        }
    }

    class StorageCardData {
        @JvmField var usedGB: Double = 0.0
        @JvmField var availableGB: Double = 0.0
        @JvmField var totalGB: Double = 0.0
        @JvmField var usagePercentage: Double = 0.0
        @JvmField var status: String? = null
        @JvmField var displayText: String? = null
        @JvmField var badge: BadgeData? = null
    }

    class BillableCardData {
        @JvmField var billableHours: String? = null
        @JvmField var billablePercentage: Double = 0.0
        @JvmField var nonBillableHours: String? = null
        @JvmField var nonBillablePercentage: Double = 0.0
        @JvmField var dateRangeLabel: String? = null
        @JvmField var badge: BadgeData? = null
    }

    class ApproxRevenueCardData {
        @JvmField var badge: BadgeData? = null
        @JvmField var approxRevenue: Double = 0.0
        @JvmField var averageBillingRate: Double = 0.0
        @JvmField var dateRangeLabel: String? = null
        @JvmField var currencyCode: String? = null
        @JvmField var currencySymbol: String? = null
        @JvmField var appointmentCount: Int = 0
        @JvmField var description: String? = null
    }

    class SubscriptionCardData {
        @JvmField var isPaid: Boolean = false
        @JvmField var isActive: Boolean = false
        @JvmField var isExpired: Boolean = false
        @JvmField var canUpgrade: Boolean = false
        @JvmField var canPayNow: Boolean = false
        @JvmField var model: String? = null
        @JvmField var planLabel: String? = null
        @JvmField var validityText: String? = null
        @JvmField var validTill: String? = null
        @JvmField var cta: CtaData? = null
        @JvmField var status: StatusBadge? = null

        class StatusBadge {
            @JvmField var label: String? = null
            @JvmField var color: String? = null
        }

        class CtaData {
            @JvmField var label: String? = null
            @JvmField var style: String? = null
            @JvmField var action: String? = null
        }
    }

    class HiringCardData {
        @JvmField var totalMembers: Int = 0
        @JvmField var groups: List<GroupItem>? = null
        @JvmField var badge: BadgeData? = null

        class GroupItem {
            @JvmField var id: String? = null
            @JvmField var name: String? = null
            @JvmField var memberCount: Int = 0
        }
    }
}
