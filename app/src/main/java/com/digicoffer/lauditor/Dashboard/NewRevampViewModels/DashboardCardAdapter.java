package com.digicoffer.lauditor.Dashboard.NewRevampViewModels;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digicoffer.lauditor.Dashboard.DahboardModels.CardDataModels;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.highlight.Highlight;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Notifications.Models.Navigation;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * DashboardAdapter
 * <p>
 * Card layout mapping
 * ─────────────────────────────────────────────────────────────────────
 * TYPE_MEETING           → card_activity_common.xml   ActivityHolder
 * TYPE_APPOINTMENT       → card_activity_common.xml   ActivityHolder
 * TYPE_MESSAGES          → card_activity_common.xml   ActivityHolder
 * TYPE_NOTIFICATION      → card_activity_common.xml   ActivityHolder
 * TYPE_REVENUE_TREND     → card_revenue_trend.xml     RevenueTrendHolder  (LineChart)
 * TYPE_APPOINTMENT_TREND → card_appointment_trend.xml ApptTrendHolder     (BarChart)
 * TYPE_MATTER            → card_matter_kpi.xml        MatterHolder
 * TYPE_STORAGE           → card_storage_ds.xml        StorageHolder       (Pie donut)
 * TYPE_BILLABLE          → card_billable.xml          BillableHolder
 * TYPE_APPROX_REVENUE    → card_approx_revenue.xml    ApproxRevenueHolder
 * TYPE_SUBSCRIPTION      → card_subscription.xml      SubscriptionHolder
 * TYPE_HIRING            → card_hiring_kpi.xml        HiringHolder
 */
public class DashboardCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface CardActionListener {
        void onNavigate(int cardType, Navigation navigation);
        void onNavigate(int cardType, Navigation navigation, String subType); // ← must be here
        void onPaySubscription(String planLabel, String validityText);
    }

    private final Context context;
    private final CardActionListener listener;
    private List<DashboardItem> items = new ArrayList<>();

    public DashboardCardAdapter(Context context, CardActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitList(List<DashboardItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return createHolder(viewType, LayoutInflater.from(parent.getContext()), parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DashboardItem item = items.get(position);
        bindHolder(holder, item.getType(), item.getData(), item.getNavigation());
    }

    // =========================================================================
    // Factory
    // =========================================================================

    private RecyclerView.ViewHolder createHolder(int type, LayoutInflater inf, ViewGroup p) {
        switch (type) {
            case DashboardItem.TYPE_MEETING:
            case DashboardItem.TYPE_APPOINTMENT:
            case DashboardItem.TYPE_NOTIFICATION:
                return new ActivityHolder(inf.inflate(R.layout.card_activity_common, p, false), type);

            case DashboardItem.TYPE_MESSAGES:
                return new MessagesHolder(inf.inflate(R.layout.card_messages, p, false));
            // ── Revenue → LineChart ──────────────────────────────────────
            case DashboardItem.TYPE_REVENUE_TREND:
                return new RevenueTrendHolder(inf.inflate(R.layout.card_trend, p, false));

            // ── Appointment → BarChart ───────────────────────────────────
            case DashboardItem.TYPE_APPOINTMENT_TREND:
                return new ApptTrendHolder(inf.inflate(R.layout.card_appointment, p, false));

            case DashboardItem.TYPE_MATTER:
                return new MatterHolder(inf.inflate(R.layout.card_matter_kpi, p, false));

            case DashboardItem.TYPE_STORAGE:
                return new StorageHolder(inf.inflate(R.layout.card_storage_ds, p, false));

            case DashboardItem.TYPE_BILLABLE:
                return new BillableHolder(inf.inflate(R.layout.card_billable, p, false));

            case DashboardItem.TYPE_APPROX_REVENUE:
                return new ApproxRevenueHolder(inf.inflate(R.layout.card_approx_revenue, p, false));

            case DashboardItem.TYPE_SUBSCRIPTION:
                return new SubscriptionHolder(inf.inflate(R.layout.card_subscription, p, false));

            case DashboardItem.TYPE_HIRING:
                return new HiringHolder(inf.inflate(R.layout.card_hiring_kpi, p, false));

            default:
                return new ActivityHolder(inf.inflate(R.layout.card_activity_common, p, false), type);
        }
    }

    // =========================================================================
    // Binder
    // =========================================================================

    // REPLACE
    private void bindHolder(RecyclerView.ViewHolder h, int type, Object data) {
        switch (type) {
            case DashboardItem.TYPE_MEETING:
                ((ActivityHolder) h).bindMeeting((CardDataModels.MeetingCardData) data);
                break;
            case DashboardItem.TYPE_APPOINTMENT:
                ((ActivityHolder) h).bindAppointment((CardDataModels.AppointmentCardData) data);
                break;
            case DashboardItem.TYPE_MESSAGES:
                ((MessagesHolder) h).bind((CardDataModels.MessagesCardData) data);
                break;
            case DashboardItem.TYPE_NOTIFICATION:
                ((ActivityHolder) h).bindNotification((CardDataModels.NotificationCardData) data);
                break;
            case DashboardItem.TYPE_REVENUE_TREND:
                ((RevenueTrendHolder) h).bind((CardDataModels.TrendCardData) data);
                break;
            case DashboardItem.TYPE_APPOINTMENT_TREND:
                ((ApptTrendHolder) h).bind((CardDataModels.TrendCardData) data);
                break;
            case DashboardItem.TYPE_MATTER:
                ((MatterHolder) h).bind((CardDataModels.MatterCardData) data);
                break;
            case DashboardItem.TYPE_STORAGE:
                ((StorageHolder) h).bind((CardDataModels.StorageCardData) data);
                break;
            case DashboardItem.TYPE_BILLABLE:
                ((BillableHolder) h).bind((CardDataModels.BillableCardData) data);
                break;
            case DashboardItem.TYPE_APPROX_REVENUE:
                ((ApproxRevenueHolder) h).bind((CardDataModels.ApproxRevenueCardData) data);
                break;
            case DashboardItem.TYPE_SUBSCRIPTION:
                ((SubscriptionHolder) h).bind((CardDataModels.SubscriptionCardData) data);
                break;
            case DashboardItem.TYPE_HIRING:
                ((HiringHolder) h).bind((CardDataModels.HiringCardData) data);
                break;
        }
    }

    // WITH
    private void bindHolder(RecyclerView.ViewHolder h, int type, Object data, Navigation nav) {
        switch (type) {
            case DashboardItem.TYPE_MEETING:
                ((ActivityHolder) h).nav = nav;
                ((ActivityHolder) h).bindMeeting((CardDataModels.MeetingCardData) data);
                break;
            case DashboardItem.TYPE_APPOINTMENT:
                ((ActivityHolder) h).nav = nav;
                ((ActivityHolder) h).bindAppointment((CardDataModels.AppointmentCardData) data);
                break;
            case DashboardItem.TYPE_MESSAGES:
                ((MessagesHolder) h).nav = nav;
                ((MessagesHolder) h).bind((CardDataModels.MessagesCardData) data);
                break;
            case DashboardItem.TYPE_NOTIFICATION:
                ((ActivityHolder) h).nav = nav;
                ((ActivityHolder) h).bindNotification((CardDataModels.NotificationCardData) data);
                break;
            case DashboardItem.TYPE_REVENUE_TREND:
                ((RevenueTrendHolder) h).nav = nav;
                ((RevenueTrendHolder) h).bind((CardDataModels.TrendCardData) data);
                break;
            case DashboardItem.TYPE_APPOINTMENT_TREND:
                // ApptTrendHolder has no nav field — no click navigation needed
                ((ApptTrendHolder) h).bind((CardDataModels.TrendCardData) data);
                break;
            case DashboardItem.TYPE_MATTER:
                ((MatterHolder) h).nav = nav;
                ((MatterHolder) h).bind((CardDataModels.MatterCardData) data);
                break;
            case DashboardItem.TYPE_STORAGE:
                // StorageHolder has no click navigation
                ((StorageHolder) h).bind((CardDataModels.StorageCardData) data);
                break;
            case DashboardItem.TYPE_BILLABLE:
                ((BillableHolder) h).nav = nav;
                ((BillableHolder) h).bind((CardDataModels.BillableCardData) data);
                break;
            case DashboardItem.TYPE_APPROX_REVENUE:
                ((ApproxRevenueHolder) h).nav = nav;
                ((ApproxRevenueHolder) h).bind((CardDataModels.ApproxRevenueCardData) data);
                break;
            case DashboardItem.TYPE_SUBSCRIPTION:
                // SubscriptionHolder has no nav field — uses onPaySubscription callback
                ((SubscriptionHolder) h).bind((CardDataModels.SubscriptionCardData) data);
                break;
            case DashboardItem.TYPE_HIRING:
                ((HiringHolder) h).nav = nav;
                ((HiringHolder) h).bind((CardDataModels.HiringCardData) data);
                break;
        }
    }

    // =========================================================================
    // VIEW HOLDERS
    // =========================================================================

    // ─────────────────────────────────────────────────────────────────────────
    // ActivityHolder  —  card_activity_common.xml
    // IDs: iv_card_icon, iv_icon_bg, tv_card_label, tv_badge,
    //      ll_content, tv_primary_text, tv_secondary_text, tv_empty_text
    // ─────────────────────────────────────────────────────────────────────────
    class ActivityHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon, ivIconBg;
        final TextView tvLabel, tvBadge, tvPrimary, tvSecondary, tvEmpty;
        final LinearLayout llContent;
        final int cardType;
        Navigation nav;

        ActivityHolder(View v, int type) {
            super(v);
            cardType = type;
            ivIcon = v.findViewById(R.id.iv_card_icon);
            ivIconBg = v.findViewById(R.id.iv_icon_bg);
            tvLabel = v.findViewById(R.id.tv_card_label);
            tvBadge = v.findViewById(R.id.tv_badge);
            tvPrimary = v.findViewById(R.id.tv_primary_text);
            tvSecondary = v.findViewById(R.id.tv_secondary_text);
            tvEmpty = v.findViewById(R.id.tv_empty_text);
            llContent = v.findViewById(R.id.ll_content);
            v.setOnClickListener(x -> {
                listener.onNavigate(cardType, nav);
            });
        }

        void bindMeeting(CardDataModels.MeetingCardData d) {
            ivIcon.setImageResource(R.drawable.ic_calendar_blue);
            ivIcon.clearColorFilter();
            ivIconBg.setImageResource(R.drawable.stat_box_blue);
            tvLabel.setText("Upcoming Meeting");
            applyBadge(tvBadge, d.badge,
                    ContextCompat.getColor(context, R.color.blue),
                    ContextCompat.getColor(context, R.color.blue_pale));
            if (d.hasMeeting) {
                tvPrimary.setText(d.subject);
                tvSecondary.setText(d.startTime + " – " + d.endTime);
                showContent(true);
            } else {
                tvEmpty.setText("No meetings scheduled for today");
                showContent(false);
            }
        }

        void bindAppointment(CardDataModels.AppointmentCardData d) {
            ivIcon.setImageResource(R.drawable.ic_clock);
            ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.chart_purple));
            ivIconBg.setImageResource(R.drawable.stat_box_purple);
            tvLabel.setText("Upcoming Appointment");
            applyBadge(tvBadge, d.badge,
                    ContextCompat.getColor(context, R.color.chart_purple),
                    ContextCompat.getColor(context, R.color.purple_pale));
            if (d.hasAppointment) {
                tvPrimary.setText(d.subject);
                tvSecondary.setText(d.startTime + " – " + d.endTime);
                showContent(true);
            } else {
                tvEmpty.setText("No appointments for today");
                showContent(false);
            }
        }

        void bindMessages(CardDataModels.MessagesCardData d) {
            ivIcon.setImageResource(R.drawable.ic_chat_green);
            ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.dark_shaded_blue));
            ivIconBg.setImageResource(R.drawable.stat_box_blue);
            tvLabel.setText("New Messages");
            applyBadge(tvBadge, d.badge,
                    ContextCompat.getColor(context, R.color.dark_shaded_blue),
                    ContextCompat.getColor(context, R.color.dark_shaded_blue_bg));
            int total = Integer.parseInt(Constants.clientChat_count) + Integer.parseInt(Constants.teamChat_count);
            if (total > 0) {
                if (Constants.ROLE.equals("AAM")) {
                    tvPrimary.setText(Constants.teamChat_count + " Team");
                    tvSecondary.setText("Unread messages");
                } else {
                    tvPrimary.setText(Constants.clientChat_count + " Client  |  " + Constants.teamChat_count + " Team");
                    tvSecondary.setText("Unread messages");
                }
                tvBadge.setText(String.valueOf(total));
                showContent(true);
            } else {
                tvEmpty.setText("No new messages");
                showContent(false);
            }
        }

        void bindNotification(CardDataModels.NotificationCardData d) {
            ivIcon.setImageResource(R.drawable.ic_bell_orange);
            ivIcon.clearColorFilter();
            ivIconBg.setImageResource(R.drawable.stat_box_orange);
            tvLabel.setText("Notifications");
            applyBadge(tvBadge, d.badge,
                    ContextCompat.getColor(context, R.color.orange_color),
                    ContextCompat.getColor(context, R.color.color_orange_icon_bg));
            if (d.notifications != null && !d.notifications.isEmpty()) {
                CardDataModels.NotificationCardData.NotificationItem first = d.notifications.get(0);
                tvPrimary.setText(first.message.isEmpty() ? "New notification" : first.message);
                tvSecondary.setText(first.time);
                showContent(true);
            } else {
                tvEmpty.setText("No new notifications");
                showContent(false);
            }
        }

        private void showContent(boolean hasData) {
            llContent.setVisibility(hasData ? View.VISIBLE : View.GONE);
            tvEmpty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
// MessagesHolder  —  card_messages.xml
// IDs: iv_card_icon, iv_icon_bg, tv_card_label, tv_badge, tv_empty_text,
//      ll_content, ll_client, ll_team,
//      tv_client_count (include bold_txt_layout), tv_client (include owner_layout)
//      tv_team_count   (include bold_txt_layout), tv_team   (include owner_layout)
// ─────────────────────────────────────────────────────────────────────────
    class MessagesHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon, ivIconBg;
        final TextView tvLabel, tvBadge, tvEmpty;
        final LinearLayout llContent, llClient, llTeam;
        final TextView tvClientCount, tvClientLabel;
        final TextView tvTeamCount, tvTeamLabel;
        Navigation nav;

        MessagesHolder(View v) {
            super(v);
            ivIcon = v.findViewById(R.id.iv_card_icon);
            ivIconBg = v.findViewById(R.id.iv_icon_bg);
            tvLabel = v.findViewById(R.id.tv_card_label);
            tvBadge = v.findViewById(R.id.tv_badge);
            tvEmpty = v.findViewById(R.id.tv_empty_text);
            llContent = v.findViewById(R.id.ll_content);
            llClient = v.findViewById(R.id.ll_client);
            llTeam = v.findViewById(R.id.ll_team);

            // included layouts — root of bold_txt_layout and owner_layout are TextViews
            View clientCountInclude = v.findViewById(R.id.tv_client_count);
            View clientLabelInclude = v.findViewById(R.id.tv_client);
            View teamCountInclude = v.findViewById(R.id.tv_team_count);
            View teamLabelInclude = v.findViewById(R.id.tv_team);

            tvClientCount = clientCountInclude instanceof TextView
                    ? (TextView) clientCountInclude
                    : (TextView) ((ViewGroup) clientCountInclude).getChildAt(0);
            tvClientLabel = clientLabelInclude instanceof TextView
                    ? (TextView) clientLabelInclude
                    : (TextView) ((ViewGroup) clientLabelInclude).getChildAt(0);
            tvTeamCount = teamCountInclude instanceof TextView
                    ? (TextView) teamCountInclude
                    : (TextView) ((ViewGroup) teamCountInclude).getChildAt(0);
            tvTeamLabel = teamLabelInclude instanceof TextView
                    ? (TextView) teamLabelInclude
                    : (TextView) ((ViewGroup) teamLabelInclude).getChildAt(0);
            boolean isAAM = Constants.ROLE.equals("AAM");
            if (isAAM) {
                Constants.isClient_chat = false;
            }
            v.setOnClickListener(x -> listener.onNavigate(DashboardItem.TYPE_MESSAGES, nav));
        }

        void bind(CardDataModels.MessagesCardData d) {
            ivIcon.setImageResource(R.drawable.ic_chat_green);
            ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.dark_shaded_blue));
            ivIconBg.setImageResource(R.drawable.stat_box_blue);

            int clientCount = Integer.parseInt(Constants.clientChat_count);
            int teamCount = Integer.parseInt(Constants.teamChat_count);
            int total = clientCount + teamCount;

            applyBadge(tvBadge, d.badge,
                    ContextCompat.getColor(context, R.color.dark_shaded_blue),
                    ContextCompat.getColor(context, R.color.dark_shaded_blue_bg));
            tvBadge.setText(String.valueOf(total));

            llContent.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);

            boolean isAAM = Constants.ROLE.equals("AAM");
            boolean isSolo = Constants.CATEGORY.equals("solo");  // ← solo category check

            // Client column — hidden for AAM
            llClient.setVisibility(isAAM ? View.GONE : View.VISIBLE);
            tvClientCount.setText(String.valueOf(clientCount));
            tvClientLabel.setText("Client");

            // Team column — hidden for solo category
            llTeam.setVisibility(isSolo ? View.GONE : View.VISIBLE);
            tvTeamCount.setText(String.valueOf(teamCount));
            tvTeamLabel.setText("Team");

            llClient.setOnClickListener(v -> {
                Constants.isClient_chat = true;
                listener.onNavigate(DashboardItem.TYPE_MESSAGES, nav);
            });
            llTeam.setOnClickListener(v -> {
                Constants.isClient_chat = false;
                listener.onNavigate(DashboardItem.TYPE_MESSAGES, nav);
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RevenueTrendHolder  —  card_revenue_trend.xml  (LineChart)
    // IDs: line_chart_trend, tv_trend_title, tv_trend_badge, ll_range_tabs
    // ─────────────────────────────────────────────────────────────────────────
    class RevenueTrendHolder extends RecyclerView.ViewHolder {
        final LineChart lineChart;
        final TextView tvTitle, tvBadge;
        final LinearLayout llRangeTabs;
        Navigation nav;

        RevenueTrendHolder(View v) {
            super(v);
            lineChart = v.findViewById(R.id.line_chart_trend);
            tvTitle = v.findViewById(R.id.tv_trend_title);
            tvBadge = v.findViewById(R.id.tv_trend_badge);
            llRangeTabs = v.findViewById(R.id.ll_range_tabs);
        }

        void bind(CardDataModels.TrendCardData d) {
            tvTitle.setText("Revenue Trend");
            buildRangeTabs(llRangeTabs, d);
            selectTabByKey(llRangeTabs, d.defaultRange);
            drawLine(d, d.defaultRange);
            lineChart.setTouchEnabled(false);
            // Prevent touch from bubbling to RecyclerView
//            lineChart.setOnTouchListener((v, e) -> {
//                v.getParent().requestDisallowInterceptTouchEvent(true);
//                return false;
//            });
        }

        private void drawLine(CardDataModels.TrendCardData d, String rangeKey) {
            CardDataModels.TrendCardData.TrendRange range = d.ranges.get(rangeKey);
            if (range == null || range.datasets == null) return;

            int[] colors = {
                    ContextCompat.getColor(context, R.color.chart_green),
                    ContextCompat.getColor(context, R.color.chart_blue),
                    ContextCompat.getColor(context, R.color.chart_red)};

            List<com.github.mikephil.charting.interfaces.datasets.ILineDataSet> sets = new ArrayList<>();
            for (int i = 0; i < range.datasets.size(); i++) {
                CardDataModels.TrendCardData.DataSet ds = range.datasets.get(i);
                List<Entry> entries = new ArrayList<>();
                for (int j = 0; j < ds.data.size(); j++) {
                    entries.add(new Entry(j, ds.data.get(j).floatValue()));
                }
                LineDataSet lds = new LineDataSet(entries, ds.name);
                int c = colors[Math.min(i, colors.length - 1)];
                lds.setColor(c);
                lds.setCircleColor(c);
                lds.setLineWidth(2.5f);
                lds.setCircleRadius(3.5f);
                lds.setDrawValues(false);

                // Conditional logic for chart mode
                if (hasConsecutiveZeros(ds.data)) {
                    lds.setMode(LineDataSet.Mode.LINEAR);
                } else {
                    lds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    lds.setCubicIntensity(0.15f);
                }

                lds.setDrawFilled(range.datasets.size() == 1);
                lds.setFillAlpha(45);
                lds.setFillColor(c);
                lds.setDrawCircles(true);
                lds.setDrawCircleHole(false);
                sets.add(lds);
            }

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(range.xLabels));
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setTextSize(9f);
            xAxis.setAxisMinimum(-0.3f);
            xAxis.setAxisMaximum(range.xLabels.size() - 0.7f);
            xAxis.setAvoidFirstLastClipping(true);

            YAxis left = lineChart.getAxisLeft();

            // Calculate actual max from data
            float actualMax = 0f;
            for (CardDataModels.TrendCardData.DataSet ds : range.datasets) {
                for (Double val : ds.data) {
                    actualMax = Math.max(actualMax, val.floatValue());
                }
            }

            float yMaxValue = Math.max((float) range.yMax, actualMax * 1.1f);
            left.setAxisMinimum(0f);
            left.setAxisMaximum(yMaxValue);
            left.setGranularity((float) range.yStep);
            left.setTextSize(9f);
            left.setDrawGridLines(true);

            if ("currency".equals(range.yUnit) && d.currencySymbol != null) {
                String sym = d.currencySymbol;
                left.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float v) {
                        return sym + String.format("%,.0f", v);
                    }
                });
            }

            lineChart.getAxisRight().setEnabled(false);
            lineChart.setData(new LineData(sets));
            lineChart.getDescription().setEnabled(false);
            lineChart.getLegend().setEnabled(range.datasets.size() > 1);
            lineChart.setDrawGridBackground(false);
            lineChart.setDrawBorders(false);
            lineChart.setTouchEnabled(false);
            lineChart.animateXY(800, 800);
            lineChart.invalidate();
        }

        // Helper method to check for consecutive zeros
        private boolean hasConsecutiveZeros(List<Double> data) {
            int zeroCount = 0;
            for (Double val : data) {
                if (val == 0) {
                    zeroCount++;
                    if (zeroCount >= 2) return true;
                } else {
                    zeroCount = 0;
                }
            }
            return false;
        }

        // ── Tab helpers (shared) ───────────────────────────────────────
        private void buildRangeTabs(LinearLayout ll, CardDataModels.TrendCardData d) {
            ll.removeAllViews();
            for (String key : d.ranges.keySet()) {
                TextView tab = new TextView(context);
                tab.setText(rangeLabel(key));
                tab.setPadding(20, 6, 20, 6);
                tab.setTextSize(11f);
                tab.setTag(key);
                tab.setOnClickListener(vv -> {
                    selectTab(ll, (TextView) vv);
                    drawLine(d, (String) vv.getTag());
                });
                ll.addView(tab);
            }
        }

        private void selectTab(LinearLayout ll, TextView sel) {
            for (int i = 0; i < ll.getChildCount(); i++) {
                View child = ll.getChildAt(i);
                if (child instanceof TextView) {
                    boolean active = child == sel;
                    child.setBackgroundResource(active ? R.drawable.tab_selected_bg : android.R.color.transparent);
                    ((TextView) child).setTextColor(ContextCompat.getColor(context,
                            active ? R.color.blue : R.color.grey_text));
                }
            }
        }

        private void selectTabByKey(LinearLayout ll, String key) {
            for (int i = 0; i < ll.getChildCount(); i++) {
                View child = ll.getChildAt(i);
                if (key.equals(child.getTag())) {
                    selectTab(ll, (TextView) child);
                    return;
                }
            }
        }

        private String rangeLabel(String k) {
            switch (k) {
                case "7days":
                    return "7D";
                case "30days":
                    return "30D";
                case "90days":
                    return "90D";
                default:
                    return k;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ApptTrendHolder  —  card_appointment_trend.xml  (BarChart)
    // IDs: bar_chart_trend, tv_trend_title, tv_trend_badge, ll_range_tabs, ll_legend
    // ─────────────────────────────────────────────────────────────────────────
    class ApptTrendHolder extends RecyclerView.ViewHolder {
        final BarChart barChart;
        final TextView tvTitle, tvBadge;
        final LinearLayout llRangeTabs, llLegend;

        ApptTrendHolder(View v) {
            super(v);
            barChart = v.findViewById(R.id.bar_chart_trend);
            tvTitle = v.findViewById(R.id.tv_trend_title);
            tvBadge = v.findViewById(R.id.tv_trend_badge);
            llRangeTabs = v.findViewById(R.id.ll_range_tabs);
            llLegend = v.findViewById(R.id.ll_legend);
        }

        void bind(CardDataModels.TrendCardData d) {
            tvTitle.setText("Appointment Trend");
            buildRangeTabs(llRangeTabs, d);
            selectTabByKey(llRangeTabs, d.defaultRange);
            drawBars(d, d.defaultRange);
            barChart.setTouchEnabled(false);
            // Prevent touch from bubbling to RecyclerView
//            barChart.setOnTouchListener((v, e) -> {
//                v.getParent().requestDisallowInterceptTouchEvent(true);
//                return false;
//            });
        }

        private void drawBars(CardDataModels.TrendCardData d, String rangeKey) {
            CardDataModels.TrendCardData.TrendRange range = d.ranges.get(rangeKey);
            if (range == null || range.datasets == null) return;

            // Dataset colors — Upcoming, Completed, Cancelled (matching screenshot)
            int[] colors = {
                    ContextCompat.getColor(context, R.color.chart_blue),   // Upcoming
                    ContextCompat.getColor(context, R.color.green),  // Completed
                    ContextCompat.getColor(context, R.color.chart_red)     // Cancelled
            };

            int dsCount = range.datasets.size();
            float groupSpace = 0.12f;
            float barSpace = 0.05f;
            float barWidth = (1f - groupSpace - barSpace * dsCount) / dsCount;

            List<com.github.mikephil.charting.interfaces.datasets.IBarDataSet> sets = new ArrayList<>();
            for (int i = 0; i < dsCount; i++) {
                CardDataModels.TrendCardData.DataSet ds = range.datasets.get(i);
                List<BarEntry> entries = new ArrayList<>();
                for (int j = 0; j < ds.data.size(); j++)
                    entries.add(new BarEntry(j, ds.data.get(j).floatValue()));
                BarDataSet bds = new BarDataSet(entries, ds.name);
                bds.setColor(colors[Math.min(i, colors.length - 1)]);
                bds.setValueTextSize(15f);
                bds.setDrawValues(false);
                sets.add(bds);
            }

            BarData barData = new BarData(sets);
            barData.setBarWidth(barWidth);

            XAxis xAxis = barChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(range.xLabels));
            xAxis.setGranularity(1f);
            xAxis.setCenterAxisLabels(dsCount > 1);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setTextSize(9f);
            xAxis.setAxisMinimum(0f);
            xAxis.setAxisMaximum(range.xLabels != null ? range.xLabels.size() : ds(range));

            YAxis left = barChart.getAxisLeft();
            left.setAxisMinimum(0f);
            left.setAxisMaximum((float) range.yMax);
            left.setGranularity((float) range.yStep);
            left.setTextSize(9f);

            barChart.getAxisRight().setEnabled(false);
            barChart.setData(barData);

            // Group bars side-by-side when multiple datasets
            if (dsCount > 1) barChart.groupBars(0f, groupSpace, barSpace);

            barChart.getDescription().setEnabled(false);
            barChart.getLegend().setEnabled(false); // we render custom legend below
            barChart.setFitBars(true);
//            barChart.setTouchEnabled(true);
            barChart.animateXY(800, 800);
            barChart.invalidate();

            // Build custom legend
            buildLegend(range.datasets, colors);
        }

        private void buildLegend(List<CardDataModels.TrendCardData.DataSet> datasets, int[] colors) {
            if (llLegend == null) return;
            llLegend.removeAllViews();
            for (int i = 0; i < datasets.size(); i++) {
                View dot = new View(context);
                LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(DynamicUtils.twenty, DynamicUtils.twenty);
                dotLp.setMargins(0, 0, 6, 0);
                dot.setLayoutParams(dotLp);
                GradientDrawable dotBg = new GradientDrawable();
                dotBg.setShape(GradientDrawable.OVAL);
                dotBg.setColor(colors[Math.min(i, colors.length - 1)]);
                dot.setBackground(dotBg);

                TextView label = new TextView(context);
                label.setText(datasets.get(i).name);
                label.setTextSize(10f);
                label.setTextColor(ContextCompat.getColor(context, R.color.grey_text));
                LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lblLp.setMargins(0, 0, DynamicUtils.twenty, 0);
                label.setLayoutParams(lblLp);

                llLegend.addView(dot);
                llLegend.addView(label);
            }
        }

        // helper: default x-axis max when xLabels is null
        private float ds(CardDataModels.TrendCardData.TrendRange range) {
            if (range.datasets == null || range.datasets.isEmpty()) return 7f;
            return range.datasets.get(0).data.size();
        }

        private void buildRangeTabs(LinearLayout ll, CardDataModels.TrendCardData d) {
            ll.removeAllViews();
            for (String key : d.ranges.keySet()) {
                TextView tab = new TextView(context);
                tab.setText(rangeLabel(key));
                tab.setPadding(20, 6, 20, 6);
                tab.setTextSize(11f);
                tab.setTag(key);
                tab.setOnClickListener(vv -> {
                    selectTab(ll, (TextView) vv);
                    drawBars(d, (String) vv.getTag());
                });
                ll.addView(tab);
            }
        }

        private void selectTab(LinearLayout ll, TextView sel) {
            for (int i = 0; i < ll.getChildCount(); i++) {
                View child = ll.getChildAt(i);
                if (child instanceof TextView) {
                    boolean active = child == sel;
                    child.setBackgroundResource(active ? R.drawable.tab_selected_bg : android.R.color.transparent);
                    ((TextView) child).setTextColor(ContextCompat.getColor(context,
                            active ? R.color.blue : R.color.grey_text));
                }
            }
        }

        private void selectTabByKey(LinearLayout ll, String key) {
            for (int i = 0; i < ll.getChildCount(); i++) {
                View child = ll.getChildAt(i);
                if (key.equals(child.getTag())) {
                    selectTab(ll, (TextView) child);
                    return;
                }
            }
        }

        private String rangeLabel(String k) {
            switch (k) {
                case "7days":
                    return "7D";
                case "30days":
                    return "30D";
                case "90days":
                    return "90D";
                default:
                    return k;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // StorageHolder  —  card_storage_ds.xml  (PieChart donut)
    // IDs: pie_chart_storage, tv_used_gb, tv_available_gb,
    //      tv_total_gb, tv_usage_percent, tv_storage_status
    // ─────────────────────────────────────────────────────────────────────────
    // ─────────────────────────────────────────────────────────────────────────
// StorageHolder  —  card_storage_ds.xml  (updated for new layout)
// IDs: pie_chart_storage, tv_used_gb, tv_available_gb,
//      tv_total_gb, tv_usage_percent, tv_storage_status
// ─────────────────────────────────────────────────────────────────────────
    class StorageHolder extends RecyclerView.ViewHolder {
        final PieChart pieChart;
        final TextView tvUsed, tvAvailable, tvTotal, tvPercent, tvStatus;

        StorageHolder(View v) {
            super(v);
            pieChart = v.findViewById(R.id.pie_chart_storage);
            tvUsed = v.findViewById(R.id.tv_used_gb);
            tvAvailable = v.findViewById(R.id.tv_available_gb);
            tvTotal = v.findViewById(R.id.tv_total_gb);
            tvPercent = v.findViewById(R.id.tv_usage_percent);   // now shows "18% used"
            tvStatus = v.findViewById(R.id.tv_storage_status);  // gone / hidden
        }

        void bind(CardDataModels.StorageCardData d) {
            tvTotal.setText(d.totalGB + " GB");
            tvUsed.setText(d.usedGB + " GB");
            tvAvailable.setText(d.availableGB + " GB");

            double pct = d.totalGB > 0 ? (d.usedGB / d.totalGB) * 100.0 : 0;

            // "18% used" label beneath Available value
            tvPercent.setText(d.usagePercentage + "%");

            buildDonut(d, (float) pct);
        }

        private void buildDonut(CardDataModels.StorageCardData d, float usedPct) {
            List<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry((float) d.usedGB, "Used"));
            entries.add(new PieEntry((float) d.availableGB, "Available"));

            PieDataSet ds = new PieDataSet(entries, "");
            ds.setColors(
                    ContextCompat.getColor(context, R.color.chart_blue),   // used  → blue slice (matches screenshot)
                    ContextCompat.getColor(context, R.color.grey_badge_bg)
            );
            ds.setDrawValues(false);
            ds.setSliceSpace(2f);

            pieChart.setData(new PieData(ds));
            pieChart.setUsePercentValues(false);
            pieChart.getDescription().setEnabled(false);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleRadius(62f);
            pieChart.setTransparentCircleRadius(67f);
            pieChart.setHoleColor(Color.WHITE);
            pieChart.setEntryLabelColor(Color.TRANSPARENT);
            pieChart.getLegend().setEnabled(false);
            pieChart.setDrawCenterText(false);   // no centre text (clean donut)
            pieChart.setRotationAngle(-90f);
            pieChart.setTouchEnabled(false);
            pieChart.animateXY(800, 800);
            pieChart.invalidate();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BillableHolder  —  card_billable.xml
    // IDs: tv_billing_hours, tv_bh_percentage, tv_date_range
    // ─────────────────────────────────────────────────────────────────────────
    class BillableHolder extends RecyclerView.ViewHolder {
        final TextView tvHours, tvPercent, tvDateRange;
        Navigation nav;

        BillableHolder(View v) {
            super(v);
            tvHours = v.findViewById(R.id.tv_billing_hours);
            tvPercent = v.findViewById(R.id.tv_bh_percentage);
            tvDateRange = v.findViewById(R.id.tv_date_range);
//            v.setOnClickListener(x -> {
//                listener.onNavigate(DashboardItem.TYPE_BILLABLE, nav);
//            });
        }

        void bind(CardDataModels.BillableCardData d) {
            tvHours.setText(d.billableHours);
//            String pct = (d.billablePercentage >= 0 ? "+" : "")
//                    + String.format("%.2f%%", d.billablePercentage);
//            tvPercent.setText(pct);
            tvPercent.setText(d.badge.label);
            // Apply badge color from response
            if (d.badge != null && d.badge.color != null) {
                applyBadgeStyle(tvPercent, d.badge.color);
            } else {
                tvPercent.setTextColor(ContextCompat.getColor(context,
                        d.billablePercentage >= 0 ? R.color.chart_green : R.color.chart_red));
            }

            tvDateRange.setText(d.dateRangeLabel);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ApproxRevenueHolder  —  card_approx_revenue.xml
    // IDs: tv_revenue_value, tv_revenue_percentage, tv_revenue_date_range
    // ─────────────────────────────────────────────────────────────────────────
    class ApproxRevenueHolder extends RecyclerView.ViewHolder {
        final TextView tvRevenue, tvPercent, tvDateRange;
        Navigation nav;

        ApproxRevenueHolder(View v) {
            super(v);
            tvRevenue = v.findViewById(R.id.tv_revenue_value);
            tvPercent = v.findViewById(R.id.tv_revenue_percentage);
            tvDateRange = v.findViewById(R.id.tv_revenue_date_range);
//            v.setOnClickListener(x -> {
//                listener.onNavigate(DashboardItem.TYPE_APPROX_REVENUE, nav);
//            });
        }

        void bind(CardDataModels.ApproxRevenueCardData d) {
            String sym = d.currencySymbol != null ? d.currencySymbol : "";
            tvRevenue.setText(sym + String.format("%,.2f", d.approxRevenue));

            // Show badge label (percentage like "+100.0%")
            if (d.badge != null && d.badge.label != null) {
                tvPercent.setText(d.badge.label);

                // Apply badge color from response
                if (d.badge.color != null) {
                    applyBadgeStyle(tvPercent, d.badge.color);
                }
            } else {
                tvPercent.setText("0%");
                tvPercent.setTextColor(ContextCompat.getColor(context, R.color.chart_green));
            }

            tvDateRange.setText(d.dateRangeLabel);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SubscriptionHolder  —  card_subscription.xml  (new Figma layout)
    // IDs: subscription_card, tv_product_subscription, tv_ps_year,
    //      tv_pending_txt, rightLayout, tv_sub_status_chip, tv_message, btn_pay_now
    // ─────────────────────────────────────────────────────────────────────────
    // ─────────────────────────────────────────────────────────────────────────
// SubscriptionHolder  —  card_subscription.xml  (updated layout)
//
// IDs used:
//   subscription_card     → root LinearLayout
//   tv_sub_status_chip    → "Active" / "Expired" pill  (top-right)
//   tv_product_subscription → caption inside blue banner
//   tv_ps_year            → plan name inside blue banner  (e.g. "Premium Plan")
//   tv_pending_txt        → "Valid till" / "Expired On" label
//   tv_validity_date      → date value  (NEW id — replaces old right-column date)
//   btn_pay_now           → full-width CTA button
//   rightLayout / tv_message → kept as gone for backward-compat
// ─────────────────────────────────────────────────────────────────────────
    // ─────────────────────────────────────────────────────────────────────────
// SubscriptionHolder  —  card_subscription.xml
//
// API JSON shape (CardDataModels.SubscriptionCardData):
//   isActive       → boolean
//   planLabel      → "Free Plan" / "Premium Plan"  (shown in blue banner)
//   validityText   → "N/A" / "Dec 23, 2025"        (shown as date value)
//   statusBadge    → { label: "Inactive"/"Active"/"Expired", color: "grey"/"green"/"red" }
//   cta            → { label: "Upgrade Plan", action: "upgrade" }  or null
//   canPayNow      → boolean
//   canUpgrade     → boolean
// ─────────────────────────────────────────────────────────────────────────
    class SubscriptionHolder extends RecyclerView.ViewHolder {

        final LinearLayout subscriptionCard;
        final LinearLayout rightLayout;   // gone — compat only
        final TextView tvPlan;            // tv_product_subscription  (banner caption)
        final TextView tvValidity;        // tv_ps_year               (plan name hero inside banner)
        final TextView tvPendingTxt;      // tv_pending_txt            ("Valid Until" label)
        final TextView tvValidityDate;    // tv_validity_date          (date value — right side)
        final TextView tvStatusChip;      // tv_sub_status_chip        (pill top-right)
        final TextView tvMessage;         // gone — compat only
        final Button btnPayNow;

        SubscriptionHolder(View v) {
            super(v);
            subscriptionCard = v.findViewById(R.id.subscription_card);
            rightLayout = v.findViewById(R.id.rightLayout);
            tvPlan = v.findViewById(R.id.tv_product_subscription);
            tvValidity = v.findViewById(R.id.tv_ps_year);
            tvPendingTxt = v.findViewById(R.id.tv_pending_txt);
            tvValidityDate = v.findViewById(R.id.tv_validity_date);
            tvStatusChip = v.findViewById(R.id.tv_sub_status_chip);
            tvMessage = v.findViewById(R.id.tv_message);
            btnPayNow = v.findViewById(R.id.btn_pay_now);
        }

        void bind(CardDataModels.SubscriptionCardData d) {

            // ── Blue banner ──────────────────────────────────────────────────
            // Caption: always "Product Subscription"
            tvPlan.setText("Product Subscription");

            // Plan name hero: "Free Plan" / "Premium Plan" from API planLabel
            tvValidity.setText(d.planLabel != null && !d.planLabel.isEmpty()
                    ? d.planLabel : "—");

            // ── Status chip — driven entirely by API statusBadge ─────────────
            tvStatusChip.setVisibility(View.VISIBLE);
            if (d.status != null && d.status.label != null) {
                tvStatusChip.setText(d.status.label); // "Active" / "Inactive" / "Expired"
                applyBadgeStyle(tvStatusChip, d.status.color);
            } else {
                // Fallback: derive from isActive flag
                tvStatusChip.setText(d.isActive ? "Active" : "Inactive");
                applyBadgeStyle(tvStatusChip, d.isActive ? "green" : "grey");
            }

            // ── Row 3: "Valid Until" label — ALWAYS VISIBLE ──────────────────
            // Show label based on expired state; never hide this row
            tvPendingTxt.setVisibility(View.VISIBLE);
            tvPendingTxt.setText(d.isExpired ? "Expired On" : "Valid Until");
            tvPendingTxt.setTextColor(ContextCompat.getColor(context, R.color.grey_text));

            // ── Date value — ALWAYS show validityText from API ───────────────
            // "N/A" for inactive/free, actual date for active/expired
            if (tvValidityDate != null) {
                tvValidityDate.setVisibility(View.VISIBLE);
                String dateVal = (d.validTill != null && !d.validTill.isEmpty())
                        ? d.validTill : "N/A";
                tvValidityDate.setText(dateVal);

                // Style: grey for N/A, bold dark for real dates
                boolean hasRealDate = !dateVal.equalsIgnoreCase("N/A")
                        && !dateVal.equals("—")
                        && !dateVal.isEmpty();

                tvValidityDate.setTextColor(ContextCompat.getColor(context,
                        hasRealDate ? R.color.dark_text : R.color.grey_text));
            }

            // ── CTA button ───────────────────────────────────────────────────
            if (d.canPayNow && d.isExpired) {
                // Expired + payable → "Pay Now"
                btnPayNow.setVisibility(View.VISIBLE);
                btnPayNow.setText("Pay Now");
                btnPayNow.setOnClickListener(vv ->
                        listener.onPaySubscription(d.planLabel, d.validTill));

            } else if (d.canUpgrade) {
                // Free / inactive → "Upgrade Plan"
                btnPayNow.setVisibility(View.VISIBLE);
                btnPayNow.setText("Upgrade Plan");
                btnPayNow.setOnClickListener(vv ->
                        listener.onNavigate(DashboardItem.TYPE_SUBSCRIPTION, null));

            } else {
                btnPayNow.setVisibility(View.GONE);
            }
            btnPayNow.setVisibility(View.GONE);
            // Compat: keep unused views gone
            rightLayout.setVisibility(View.GONE);
            tvMessage.setVisibility(View.GONE);
        }

        /**
         * Applies text colour + background to the status chip
         * based on the color string from the API.
         * <p>
         * Supported values: "green" | "red" | "orange" | "grey"
         */
        private void applyBadgeStyle(TextView chip, String color) {
            if (color == null) color = "grey";

            int textColorRes;
            int bgColorRes;
            int bgDrawable;

            switch (color) {
                case "green":
                    textColorRes = R.color.chart_green;
                    bgColorRes = R.color.completed_bg;
                    bgDrawable = R.drawable.completed_badge;
                    break;
                case "red":
                    textColorRes = R.color.chart_red;
                    bgColorRes = R.color.pending_bg;
                    bgDrawable = R.drawable.pending_badge;
                    break;
                case "orange":
                    textColorRes = R.color.pending_text;
                    bgColorRes = R.color.pending_bg;
                    bgDrawable = R.drawable.pending_badge;
                    break;
                default:
                    // "grey" → Inactive (light grey pill, dark grey text)
                    textColorRes = R.color.grey_text;
                    bgColorRes = R.color.grey_badge_bg;
                    bgDrawable = R.drawable.grey_badge;
                    break;
            }

            chip.setTextColor(ContextCompat.getColor(context, textColorRes));
            chip.setBackgroundResource(bgDrawable);
            chip.getBackground().setTint(ContextCompat.getColor(context, bgColorRes));
        }


        /**
         * Applies the correct text colour + background tint to the status chip
         * based on the color string coming from the API.
         *
         * API color values: "green" | "red" | "orange" | "grey" (default)
         */


    }

    class MatterHolder extends RecyclerView.ViewHolder {

        final HorizontalBarChart barChart;
        final TextView tvGrandTotal, tvActive, tvClosed, tvBadge;
        final LinearLayout llLegend;
        Navigation nav;

        MatterHolder(View v) {
            super(v);
            barChart = v.findViewById(R.id.bar_chart_matter);
            tvGrandTotal = v.findViewById(R.id.tv_grand_total);
            tvActive = v.findViewById(R.id.tv_active_count);
            tvClosed = v.findViewById(R.id.tv_closed_count);
            tvBadge = v.findViewById(R.id.tv_badge);
            llLegend = v.findViewById(R.id.ll_matter_legend);
            v.setOnClickListener(x -> {
                listener.onNavigate(DashboardItem.TYPE_MATTER, nav);
            });
        }

        void bind(CardDataModels.MatterCardData d) {
            if (tvBadge != null) {
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText(String.valueOf(d.grandTotal));
            }
            buildBarChart(d);
        }

        private void buildBarChart(CardDataModels.MatterCardData d) {
            if (d.chartData == null || d.chartData.datasets == null) return;

            CardDataModels.MatterCardData.ChartData chart = d.chartData;
            int dsCount = chart.datasets.size();

            int[] resolvedColors = new int[dsCount];
            for (int i = 0; i < dsCount; i++) {
                resolvedColors[i] = resolveColor(chart.datasets.get(i).color);
            }

            float barWidth = 0.22f;
            float barSpace = 0.04f;
            float groupSpace = 0.50f;

            List<IBarDataSet> sets = new ArrayList<>();
            for (int i = dsCount - 1; i >= 0; i--) {
                CardDataModels.MatterCardData.DataSet ds = chart.datasets.get(i);
                List<BarEntry> entries = new ArrayList<>();
                int labelCount = ds.data.size();
                for (int j = 0; j < labelCount; j++) {
                    int reversedIndex = (labelCount - 1) - j;
                    entries.add(new BarEntry(reversedIndex, ds.data.get(j)));
                }
                BarDataSet bds = new BarDataSet(entries, ds.name);
                bds.setColor(resolvedColors[i]);
                bds.setDrawValues(false);
                sets.add(bds);
            }

            XAxis xAxis = barChart.getXAxis();
            if (chart.yLabels != null && !chart.yLabels.isEmpty()) {
                xAxis.setValueFormatter(new IndexAxisValueFormatter(chart.yLabels));
            }
            xAxis.setCenterAxisLabels(true);

            BarData barData = new BarData(sets);
            barData.setBarWidth(barWidth);

            xAxis.setCenterAxisLabels(true);
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);
            xAxis.setTextSize(11f);
            xAxis.setTextColor(Color.parseColor("#8A8A9A"));
            xAxis.setYOffset(4f);
            xAxis.setSpaceMin(0.6f);
            xAxis.setSpaceMax(0.6f);

            float yMin = chart.xAxis != null ? (float) chart.xAxis.min : 0f;
            float yMax = chart.xAxis != null ? (float) chart.xAxis.max : 600f;
            float yStep = chart.xAxis != null ? (float) chart.xAxis.step : 135f;

            YAxis axisLeft = barChart.getAxisLeft();
            axisLeft.setAxisMinimum(yMin);
            axisLeft.setAxisMaximum(yMax);
            axisLeft.setGranularity(yStep);
            axisLeft.setLabelCount((int) ((yMax - yMin) / yStep) + 1, true);
            axisLeft.setDrawGridLines(true);
            axisLeft.setGridColor(Color.parseColor("#F0F0F5"));
            axisLeft.setGridLineWidth(1f);
            axisLeft.setDrawAxisLine(false);
            axisLeft.setTextSize(9f);
            axisLeft.setTextColor(Color.parseColor("#8A8A9A"));
            axisLeft.setXOffset(4f);

            barChart.getAxisRight().setEnabled(false);

            barChart.setRenderer(new RoundedHorizontalBarChartRenderer(
                    barChart,
                    barChart.getAnimator(),
                    barChart.getViewPortHandler(),
                    8f
            ));

            barChart.setData(barData);

            if (dsCount > 1 && chart.yLabels != null) {
                barChart.groupBars(0f, groupSpace, barSpace);
                xAxis.setAxisMinimum(0f);
                xAxis.setAxisMaximum((float) chart.yLabels.size());
            }

            barChart.getDescription().setEnabled(false);
            barChart.getLegend().setEnabled(false);
            barChart.setFitBars(true);
            barChart.setDrawBorders(false);

            barChart.setViewPortOffsets(
                    dpToPx(70),
                    dpToPx(10),
                    dpToPx(10),
                    dpToPx(20)
            );

            // ── Touch enabled for bar click navigation ────────────────────────────
            barChart.setTouchEnabled(true);
            barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    if (!(e instanceof BarEntry)) return;
                    if (chart.yLabels == null || chart.yLabels.isEmpty()) return;

                    int groupIndex = (int) e.getX();
                    if (groupIndex < 0 || groupIndex >= chart.yLabels.size()) return;

                    final String clickedLabel = chart.yLabels.get(groupIndex);
                    if (listener != null) {
                        listener.onNavigate(DashboardItem.TYPE_MATTER, nav, clickedLabel);
                    }
                }

                @Override
                public void onNothingSelected() {
                }
            });

            barChart.animateY(600);
            barChart.invalidate();

            buildLegend(d, resolvedColors);
        }

        private void buildLegend(CardDataModels.MatterCardData d, int[] colors) {
            if (llLegend == null) return;
            llLegend.removeAllViews();

            int[] counts = {d.activeTotalCount, d.closedTotalCount};

            for (int i = 0; i < d.chartData.datasets.size(); i++) {
                CardDataModels.MatterCardData.DataSet ds = d.chartData.datasets.get(i);
                int color = colors[Math.min(i, colors.length - 1)];
                int count = (i < counts.length) ? counts[i] : 0;

                // Circular dot
                View dot = new View(context);
                int dotPx = dpToPx(8);
                LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dotPx, dotPx);
                dotLp.setMargins(0, 0, dpToPx(4), 0);
                dotLp.gravity = android.view.Gravity.CENTER_VERTICAL;
                dot.setLayoutParams(dotLp);
                GradientDrawable dotBg = new GradientDrawable();
                dotBg.setShape(GradientDrawable.OVAL);
                dotBg.setColor(color);
                dot.setBackground(dotBg);

                // Bold count
                TextView tvCount = new TextView(context);
                tvCount.setText(String.valueOf(count));
                tvCount.setTextSize(DynamicUtils.fifteen);
                tvCount.setTextColor(context.getColor(R.color.black));
                tvCount.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams countLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                countLp.setMargins(0, 0, dpToPx(3), 0);
                tvCount.setLayoutParams(countLp);

                // Label
                TextView tvLabel = new TextView(context);
                tvLabel.setText(ds.name);
                tvLabel.setTextSize(DynamicUtils.twelve);
                tvLabel.setTextColor(context.getColor(R.color.black));
                LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lblLp.setMargins(0, 0,
                        i < d.chartData.datasets.size() - 1 ? dpToPx(20) : 0, 0);
                tvLabel.setLayoutParams(lblLp);

                // ── Legend item click navigation ──────────────────────────────────
                final String labelName = ds.name;
                dot.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onNavigate(DashboardItem.TYPE_MATTER, nav, labelName);
                    }
                });
                tvCount.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onNavigate(DashboardItem.TYPE_MATTER, nav, labelName);
                    }
                });
                tvLabel.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onNavigate(DashboardItem.TYPE_MATTER, nav, labelName);
                    }
                });

                llLegend.addView(dot);
                llLegend.addView(tvCount);
                llLegend.addView(tvLabel);
            }
        }

        private int resolveColor(String apiColor) {
            if (apiColor == null) return Color.parseColor("#2979FF");
            switch (apiColor.toLowerCase()) {
                case "blue":
                    return Color.parseColor("#2979FF");
                case "purple":
                    return Color.parseColor("#FFA726");
                case "orange":
                    return Color.parseColor("#FFA726");
                case "green":
                    return Color.parseColor("#43A047");
                case "red":
                    return Color.parseColor("#E53935");
                case "teal":
                    return Color.parseColor("#00897B");
                default:
                    return Color.parseColor("#2979FF");
            }
        }

        private int dpToPx(int dp) {
            return Math.round(dp * context.getResources().getDisplayMetrics().density);
        }
    }
    // ─────────────────────────────────────────────────────────────────────────
    // HiringHolder  —  card_hiring_kpi.xml
    // ─────────────────────────────────────────────────────────────────────────
//    class HiringHolder extends RecyclerView.ViewHolder {
//        final TextView tvBadge;
//        final LinearLayout llGroupsContainer;
//        Navigation nav;
//
//        HiringHolder(View v) {
//            super(v);
//            tvBadge = v.findViewById(R.id.tv_badge);
//            llGroupsContainer = v.findViewById(R.id.ll_groups_container);
//            v.setOnClickListener(x -> listener.onNavigate(DashboardItem.TYPE_HIRING, nav));
//        }
//
//        void bind(CardDataModels.HiringCardData d) {
//            applyBadge(tvBadge, d.badge,
//                    ContextCompat.getColor(context, R.color.blue),
//                    ContextCompat.getColor(context, R.color.blue_pale));
//            buildGroupColumns(llGroupsContainer, d.groups);
//        }
//
//        private void buildGroupColumns(LinearLayout container,
//                                       List<CardDataModels.HiringCardData.GroupItem> groups) {
//            container.removeAllViews();
//            if (groups == null || groups.isEmpty()) return;
//
//            Context ctx = container.getContext();
//
//            for (int i = 0; i < groups.size(); i++) {
//                CardDataModels.HiringCardData.GroupItem g = groups.get(i);
//
//                // Divider between columns
//                if (i > 0) {
//                    View divider = new View(ctx);
//                    LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
//                            dpToPx(ctx, 1), LinearLayout.LayoutParams.MATCH_PARENT);
//                    dp.setMargins(0, dpToPx(ctx, 8), 0, dpToPx(ctx, 8));
//                    divider.setLayoutParams(dp);
//                    divider.setBackgroundColor(context.getColor(R.color.lite_grey));
//                    container.addView(divider);
//                }
//
//                // Column
//                LinearLayout col = new LinearLayout(ctx);
//                col.setOrientation(LinearLayout.VERTICAL);
//                col.setGravity(Gravity.CENTER);
//                col.setLayoutParams(new LinearLayout.LayoutParams(
//                        0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
//
//                // Count
//                TextView tvCount = new TextView(ctx);
//                tvCount.setText(String.valueOf(g.memberCount));
//                tvCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
//                tvCount.setTextColor(context.getColor(R.color.blue_dark));
//                tvCount.setGravity(Gravity.CENTER);
//                try {
//                    tvCount.setTypeface(ResourcesCompat.getFont(ctx, R.font.gill_sans_bold));
//                } catch (Exception ignored) {
//                }
//
//                // Label
//                TextView tvLabel = new TextView(ctx);
//                tvLabel.setText(g.name);
//                tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
//                tvLabel.setTextColor(context.getColor(R.color.grey_light));
//                tvLabel.setGravity(Gravity.CENTER);
//                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT);
//                lp.topMargin = dpToPx(ctx, 4);
//                tvLabel.setLayoutParams(lp);
//                try {
//                    tvLabel.setTypeface(ResourcesCompat.getFont(ctx, R.font.gill_sans));
//                } catch (Exception ignored) {
//                }
//
//                col.addView(tvCount);
//                col.addView(tvLabel);
//                container.addView(col);
//            }
//        }
//
//        private int dpToPx(Context ctx, int dp) {
//            return (int) (dp * ctx.getResources().getDisplayMetrics().density);
//        }
//    }
    class HiringHolder extends RecyclerView.ViewHolder {
        final TextView tvBadge;
        TextView tvClientCount, tv_client_label;
        TextView tvTeamCount, tv_team_label;
        Navigation nav;

        HiringHolder(View v) {
            super(v);
            tvBadge = v.findViewById(R.id.tv_badge);
            tvClientCount = v.findViewById(R.id.tv_client_count);
            tvTeamCount = v.findViewById(R.id.tv_team_count);
            tv_client_label = v.findViewById(R.id.tv_client_label);
            tv_team_label = v.findViewById(R.id.tv_team_label);
            v.setOnClickListener(x -> listener.onNavigate(DashboardItem.TYPE_HIRING, nav));
        }

        void bind(CardDataModels.HiringCardData d) {
            applyBadge(tvBadge, d.badge,
                    ContextCompat.getColor(context, R.color.blue),
                    ContextCompat.getColor(context, R.color.blue_pale));
            // Show counts from groups list
            if (d.groups != null && !d.groups.isEmpty()) {
                // First group (index 0) -> Client Messages
                if (d.groups.size() > 0) {
                    tvClientCount.setText(String.valueOf(d.groups.get(0).memberCount));
                }
                tv_client_label.setText(d.groups.get(0).name);
                // Second group (index 1) -> Team Messages
                if (d.groups.size() > 1) {
                    tvTeamCount.setText(String.valueOf(d.groups.get(1).memberCount));
                }
                tv_team_label.setText(d.groups.get(1).name);
            }
        }
    }
    // =========================================================================
    // Utility
    // =========================================================================

    private void applyBadge(TextView tv, CardDataModels.BadgeData badge, int textColor, int bgTint) {
        if (tv == null) return;
        tv.setVisibility(View.VISIBLE);
        String label = (badge == null) ? "0"
                : (badge.label != null && !badge.label.isEmpty()) ? badge.label
                : String.valueOf((int) badge.count);
        tv.setText(label);
        tv.setTextColor(textColor);
        tv.setBackgroundResource(R.drawable.badge_bg);
        tv.getBackground().setTint(bgTint);
    }

    private void applyBadgeStyle(TextView chip, String color) {
        if (color == null) color = "grey";

        int textColorRes;
        int bgColorRes;
        int bgDrawable;

        switch (color) {
            case "green":
                textColorRes = R.color.chart_green;
                bgColorRes = R.color.completed_bg;
                bgDrawable = R.drawable.completed_badge;
                break;
            case "red":
                textColorRes = R.color.chart_red;
                bgColorRes = R.color.cancelled_bg;
                bgDrawable = R.drawable.cancelled_badge;
                break;
            case "orange":
                textColorRes = R.color.pending_text;
                bgColorRes = R.color.pending_bg;
                bgDrawable = R.drawable.pending_badge;
                break;
            default:
                // "grey" → Inactive (light grey pill, dark grey text)
                textColorRes = R.color.grey_text;
                bgColorRes = R.color.grey_badge_bg;
                bgDrawable = R.drawable.grey_badge;
                break;
        }

        chip.setTextColor(ContextCompat.getColor(context, textColorRes));
        chip.setBackgroundResource(bgDrawable);
        chip.getBackground().setTint(ContextCompat.getColor(context, bgColorRes));
    }

    private static String yearRange() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String[] m = new DateFormatSymbols().getShortMonths();
        return m[0] + " " + year + " - " + m[11] + " " + year;
    }
}