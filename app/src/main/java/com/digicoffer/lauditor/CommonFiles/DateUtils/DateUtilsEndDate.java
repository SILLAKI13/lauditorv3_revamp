package com.digicoffer.lauditor.CommonFiles.DateUtils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;
import android.widget.TextView;

import com.digicoffer.lauditor.AuditTrails.AuditTrails;
import com.digicoffer.lauditor.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtilsEndDate {
    public interface OnDateSelectedListenerEndDate {
        void onDateSelectedEndDate(String selectedDate, String FLAG);
    }
    private AuditTrails auditTrails;
    private static OnDateSelectedListenerEndDate dateSelectedListener;

    public DateUtilsEndDate(AuditTrails auditTrails) {
        this.auditTrails = auditTrails;
    }

    public void setOnDateSelectedListener(OnDateSelectedListenerEndDate listener) {
        this.dateSelectedListener = listener;
    }
    public static void showDatePickerDialog(Context context, final TextView textView, Context context1, String FLAG) {
        final Calendar myCalendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(textView, myCalendar, context1,FLAG);
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                R.style.DatePickerStyle,
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
        );
// Set OnClickListener for the "OK" button
        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
            // Handle the "OK" button click if needed
            // For example, you can dismiss the dialog:
            // dialog.dismiss();

        });

        // Set OnClickListener for the "Cancel" button
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            // Handle the "Cancel" button click if needed
            // For example, you can dismiss the dialog:
            // dialog.dismiss();
            textView.setText("");
        });
        // Set the minimum date to the current date
//        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();

    }



    private static void updateLabel(TextView textView, Calendar calendar, Context context, String FLAG) {
        String myFormat = "MMM dd,yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        textView.setText(sdf.format(calendar.getTime()));
        if (dateSelectedListener != null) {
            dateSelectedListener.onDateSelectedEndDate(textView.getText().toString(),FLAG);
        }
//        loadnewPage(textView.getText().toString());
    }

//    private static void loadnewPage(String et_search_relationships) {
//        if (!et_search_relationships.equals("")) {
//
//            // Perform your additional operation here after the date is selected
//            sorted_list.clear();
//            rv_audits.removeAllViews();
//            rv_audits.setAdapter(null);
//            et_search_relationships.setText("");
//            for (int i = 0; i < auditsList.size(); i++) {
//                String timestamp = auditsList.get(i).getTimestamp();
//                Date formatted_date = AndroidUtils.stringToDateTimeDefault(timestamp, "MMM dd,yyyy, hh:mm a");
//                Date updated_date = DateUtils.stringToDate(et_search_relationships.getText().toString());
//                if (formatted_date.after(updated_date) || formatted_date.equals(updated_date)) {
//                    if (auditsList.get(i).getName().startsWith(Catergory_type.toUpperCase(Locale.ROOT))) {
//                        AuditsModel auditsModel = new AuditsModel();
//                        auditsModel.setName(auditsList.get(i).getName());
//                        auditsModel.setTimestamp(auditsList.get(i).getTimestamp());
//                        auditsModel.setMessage(auditsList.get(i).getMessage());
//                        sorted_list.add(auditsModel);
//                    }
//                }
//            }
//            loadRecyclerView(currentPage);
//        }
//    }

    public static Date stringToDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy", Locale.US);
        try {
            // Parse the string into a Date object
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.fillInStackTrace();
            // Handle parsing error if needed
            return null;
        }
    }
}
//}

