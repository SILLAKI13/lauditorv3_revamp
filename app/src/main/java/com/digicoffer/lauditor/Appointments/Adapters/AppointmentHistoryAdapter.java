package com.digicoffer.lauditor.Appointments.Adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Appointments.Models.AppointmentModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AppointmentHistoryAdapter extends RecyclerView.Adapter<AppointmentHistoryAdapter.ViewHolder> {

    private ArrayList<AppointmentModel> appointmentList;
    private Context context;
    private OnNoteActionListener noteActionListener;
    String noteId = "";

    public interface OnNoteActionListener {
        void onSaveNote(AppointmentModel appointment, String note, int position);

        void onCancelNote(AppointmentModel appointment, int position);

        void onEditNote(AppointmentModel appointment, String noteId, String noteText, int position);

        void onDeleteNote(AppointmentModel appointment, String noteId, int position);
    }

    public AppointmentHistoryAdapter(ArrayList<AppointmentModel> appointmentList, Context context, OnNoteActionListener listener) {
        this.appointmentList = appointmentList;
        this.context = context;
        this.noteActionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentModel appointment = appointmentList.get(position);

        // Hide timeline line for last item
        if (position == appointmentList.size() - 1) {
            holder.timelineLine.setVisibility(View.INVISIBLE);
        } else {
            holder.timelineLine.setVisibility(View.VISIBLE);
        }

        // Set appointment title (count from end)
        int appointmentNumber = appointmentList.size() - position;
        String title = getOrdinal(appointmentNumber) + " Appointment";
        holder.tvAppointmentTitle.setText(title);

        // Format and set date/time
        String formattedDateTime = formatDateTime(appointment.getAppointment_from(), appointment.getAppointment_to());
        holder.tvAppointmentDateTime.setText(formattedDateTime);

        // Set payment info
        if (appointment.getPayment() != null) {
            String paymentInfo = "Payment: " + appointment.getPayment().getLabel();
            holder.tvPaymentInfo.setText(paymentInfo);
        }

        // Set status
        holder.tvStatusBadge.setText(appointment.getAppointment_status());
        // Set status badge AND timeline dot color
        setStatusBadgeStyle(holder.tvStatusBadge, holder.timelineDot, appointment.getAppointment_status());

        // Handle note section visibility
        if (appointment.isNoteExpanded()) {
            holder.llNoteSection.setVisibility(View.VISIBLE);
            holder.btnAddNote.setVisibility(View.GONE);
            holder.etNoteInput.setText(appointment.getTempNote());
            holder.etNoteInput.setSelection(holder.etNoteInput.getText().length());
        } else {
            holder.llNoteSection.setVisibility(View.GONE);
            holder.btnAddNote.setVisibility(View.VISIBLE);
        }

        // Add Note button click
        holder.btnAddNote.setOnClickListener(v -> {
            appointment.setNoteExpanded(true);
            notifyItemChanged(position);
        });

        holder.llNotesContainer.removeAllViews();

        if (appointment.isNoteExpanded()) {
            // Editing Mode → Hide previous notes
            holder.llNotesContainer.setVisibility(View.GONE);
            holder.btnAddNote.setVisibility(View.GONE);

        } else if (appointment.getNotes() != null && appointment.getNotes().length() > 0) {

            // Show previous notes only if NOT in edit mode
            holder.llNotesContainer.setVisibility(View.VISIBLE);
            holder.btnAddNote.setVisibility(View.GONE);


            for (int i = 0; i < appointment.getNotes().length(); i++) {

                try {
                    JSONObject noteObj = appointment.getNotes().getJSONObject(i);

                    String noteText = noteObj.optString("note");
                    String createdOn = noteObj.optString("created_on");

                    View noteView = LayoutInflater.from(context)
                            .inflate(R.layout.appointments_notes_item, holder.llNotesContainer, false);

                    TextView tvNoteText = noteView.findViewById(R.id.tv_note_text);
                    TextView tvNoteDate = noteView.findViewById(R.id.tv_note_date);

                    tvNoteText.setText(noteText);
                    tvNoteDate.setText(formatNoteDate(createdOn));
                    ImageView ivEdit = noteView.findViewById(R.id.iv_edit_note);
                    ImageView ivDelete = noteView.findViewById(R.id.iv_delete_note);

                    noteId = noteObj.optString("id");

                    String localNoteId = noteObj.optString("id");

                    ivEdit.setOnClickListener(v -> {
                        appointment.setNoteExpanded(true);
                        appointment.setEditingNote(true);
                        appointment.setEditingNoteId(localNoteId);
                        appointment.setTempNote(noteText);
                        notifyItemChanged(position);
                    });

                    ivDelete.setOnClickListener(v -> {

                        AndroidUtils.showConfirmationDialog(context, "Delete Note", "Are you sure you want to delete this note?", new AndroidUtils.OnConfirmListener() {
                                    @Override
                                    public void onSave() {
//                                    dialog.dismiss();
                                        if (noteActionListener != null) {
                                            noteActionListener.onDeleteNote(appointment, localNoteId, position);
                                        }
                                    }

                                    @Override
                                    public void onCancel() {
//                                dialog.dismiss();
//                                ViewMembersData();
                                    }
                                }
                        );
                    });

                    holder.llNotesContainer.addView(noteView);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
//        String note = holder.etNoteInput.getText().toString().trim();
//        if (note.isEmpty()) {
            AndroidUtils.ToggleButton(0, holder.btnSaveNote);
//        } else {
//            AndroidUtils.ToggleButton(1, holder.btnSaveNote);
//        }

        // Character counter
        holder.etNoteInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                holder.tvCharCounter.setText(length + "/500");
                appointment.setTempNote(s.toString());
                if (s.toString().isEmpty()) {
                    AndroidUtils.ToggleButton(0, holder.btnSaveNote);
                } else {
                    AndroidUtils.ToggleButton(1, holder.btnSaveNote);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Cancel button
        holder.btnCancelNote.setOnClickListener(v -> {
            appointment.setNoteExpanded(false);
            appointment.setTempNote("");
            holder.etNoteInput.setText("");
            notifyItemChanged(position);

            if (noteActionListener != null) {
                noteActionListener.onCancelNote(appointment, position);
            }
        });

        holder.btnSaveNote.setOnClickListener(v -> {
            String noteText = holder.etNoteInput.getText().toString().trim();
            if (noteText.isEmpty()) return;

            if (appointment.isEditingNote()) {
                // EDIT existing note
                if (noteActionListener != null) {
                    noteActionListener.onEditNote(
                            appointment,
                            appointment.getEditingNoteId(),
                            noteText,
                            position
                    );
                }
            } else {
                // NEW note
                if (noteActionListener != null) {
                    noteActionListener.onSaveNote(
                            appointment,
                            noteText,
                            position
                    );
                }
            }

            // Reset UI
            appointment.setNoteExpanded(false);
            appointment.setEditingNote(false);
            appointment.setEditingNoteId("");
            appointment.setTempNote("");

            holder.etNoteInput.setText("");
            notifyItemChanged(position);
        });

    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    private String formatDateTime(String fromDateTime, String toDateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

            Date fromDate = inputFormat.parse(fromDateTime);
            Date toDate = inputFormat.parse(toDateTime);

            if (fromDate != null && toDate != null) {
                String date = dateFormat.format(fromDate);
                String fromTime = timeFormat.format(fromDate);
                String toTime = timeFormat.format(toDate);
                return date + " | " + fromTime + " - " + toTime;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getOrdinal(int number) {
        String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (number % 100) {
            case 11:
            case 12:
            case 13:
                return number + "th";
            default:
                return number + suffixes[number % 10];
        }
    }

    private String formatNoteDate(String dateTime) {
        try {
            SimpleDateFormat inputFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH);

            Date date = inputFormat.parse(dateTime);
            return outputFormat.format(date);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //    private void setStatusBadgeStyle(TextView badge, String status) {
//        if (status == null) {
//            status = "Unknown";
//        }
//
//        String normalizedStatus = status.toLowerCase().trim();
//
//        if ("completed".equals(normalizedStatus)) {
//            badge.setText("Completed");
//            badge.setBackgroundResource(R.drawable.completed_badge);
//            badge.setTextColor(context.getResources().getColor(R.color.completed_text));
//
//        } else if ("cancelled".equals(normalizedStatus) || "canceled".equals(normalizedStatus)) {
//            badge.setText("Cancelled");
//            badge.setBackgroundResource(R.drawable.cancelled_badge);
//            badge.setTextColor(context.getResources().getColor(R.color.cancelled_text));
//
//        } else if ("upcoming".equals(normalizedStatus)|| "ongoing".equals(normalizedStatus)) {
//            badge.setText("Upcoming");
//            badge.setBackgroundResource(R.drawable.scheduled_badge);
//            badge.setTextColor(context.getResources().getColor(R.color.scheduled_text));
//
//        } else if ("payment_pending".equals(normalizedStatus)) {  // ⚠️ WITH UNDERSCORE
//            badge.setText("Payment Pending");
//            badge.setBackgroundResource(R.drawable.pending_badge);
//            badge.setTextColor(context.getResources().getColor(R.color.payment_pending_color));
//
//        } else {
//            // Default/unknown status
//            badge.setText(status);
//            badge.setBackgroundResource(R.drawable.scheduled_badge);
//            badge.setTextColor(context.getResources().getColor(R.color.scheduled_text));
//        }
//    }
    private void setStatusBadgeStyle(TextView badge, View timelineDot, String status) {
        if (status == null) {
            status = "Unknown";
        }

        String normalizedStatus = status.toLowerCase().trim();

        if ("completed".equals(normalizedStatus)) {
            // Badge styling
            badge.setText("Completed");
            badge.setBackgroundResource(R.drawable.completed_badge);
            badge.setTextColor(context.getResources().getColor(R.color.completed_text));

            // Timeline dot - GREEN
            timelineDot.setBackgroundResource(R.drawable.green_circular);

        } else if ("cancelled".equals(normalizedStatus) || "canceled".equals(normalizedStatus)) {
            // Badge styling
            badge.setText("Cancelled");
            badge.setBackgroundResource(R.drawable.cancelled_badge);
            badge.setTextColor(context.getResources().getColor(R.color.cancelled_text));

            // Timeline dot - RED
            timelineDot.setBackgroundResource(R.drawable.red_circular);

        } else if ("upcoming".equals(normalizedStatus) || "ongoing".equals(normalizedStatus)) {
            // Badge styling
            badge.setText("Upcoming");
            badge.setBackgroundResource(R.drawable.scheduled_badge);
            badge.setTextColor(context.getResources().getColor(R.color.scheduled_text));

            // Timeline dot - BLUE
            timelineDot.setBackgroundResource(R.drawable.circle_blue_bg);

        } else if ("payment_pending".equals(normalizedStatus)) {
            // Badge styling
            badge.setText("Payment Pending");
            badge.setBackgroundResource(R.drawable.pending_badge);
            badge.setTextColor(context.getResources().getColor(R.color.payment_pending_color));

            // Timeline dot - YELLOW
            timelineDot.setBackgroundResource(R.drawable.orange_circular);

        } else {
            // Default/unknown status
            badge.setText(status);
            badge.setBackgroundResource(R.drawable.scheduled_badge);
            badge.setTextColor(context.getResources().getColor(R.color.scheduled_text));

            // Timeline dot - GRAY (default)
            timelineDot.setBackgroundResource(R.drawable.circle_blue_bg);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View timelineDot, timelineLine;
        TextView tvAppointmentTitle, tvAppointmentDateTime, tvPaymentInfo, tvStatusBadge, tvCharCounter;
        AppCompatButton btnAddNote, btnCancelNote, btnSaveNote;
        LinearLayout llNoteSection;
        EditText etNoteInput;
        LinearLayout llNotesContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            llNotesContainer = itemView.findViewById(R.id.llNotesContainer);
            timelineDot = itemView.findViewById(R.id.timeline_dot);
            timelineLine = itemView.findViewById(R.id.timeline_line);
            tvAppointmentTitle = itemView.findViewById(R.id.tv_appointment_title);
            tvAppointmentDateTime = itemView.findViewById(R.id.tv_appointment_datetime);
            tvPaymentInfo = itemView.findViewById(R.id.tv_payment_info);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvCharCounter = itemView.findViewById(R.id.tv_char_counter);
            btnAddNote = itemView.findViewById(R.id.btn_add_note);
            btnCancelNote = itemView.findViewById(R.id.btn_cancel_note);
            btnSaveNote = itemView.findViewById(R.id.btn_save_note);
            llNoteSection = itemView.findViewById(R.id.ll_note_section);
            etNoteInput = itemView.findViewById(R.id.et_note_input);
        }
    }
}