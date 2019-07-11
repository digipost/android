/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.digipost.android.gui.datatype;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import no.digipost.android.R;
import no.digipost.android.model.datatypes.Appointment;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.Permissions;

import java.util.Calendar;

public class AppointmentView extends Fragment {

    private Appointment appointment;
    private String appointmentTitle;

    public static AppointmentView newInstance(Appointment appointment, String title) {
        AppointmentView appointmentView = new AppointmentView();
        appointmentView.appointment = appointment;
        appointmentView.appointmentTitle = title;
        return appointmentView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.appointment_view, container, false);
        ((TextView) view.findViewById(R.id.event_title)).setText(appointmentTitle);
        ((TextView) view.findViewById(R.id.event_subtitle)).setText(appointment.subTitle);
        ((TextView) view.findViewById(R.id.event_date_time)).setText(appointment.getStartTimeString() + "\n" + appointment.getStartDateString());
        ((TextView) view.findViewById(R.id.event_place_where)).setText(appointment.getPlace());
        ((TextView) view.findViewById(R.id.event_arrival_info_text)).setText(appointment.getArrivalInfo());

        if (appointment.info != null && appointment.info.size() > 0) {
            ((TextView) view.findViewById(R.id.appointment_info1_title)).setText(appointment.info.get(0).title);
            ((TextView) view.findViewById(R.id.appointment_info1_text)).setText(appointment.info.get(0).text);
        }else{
            ((TextView) view.findViewById(R.id.appointment_info1_title)).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.appointment_info1_text)).setVisibility(View.GONE);
        }

        if(appointment.info != null && appointment.info.size() > 1) {
            ((TextView) view.findViewById(R.id.appointment_info2_title)).setText(appointment.info.get(1).title);
            ((TextView) view.findViewById(R.id.appointment_info2_text)).setText(appointment.info.get(1).text);
        }else{
            ((TextView) view.findViewById(R.id.appointment_info2_title)).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.appointment_info2_text)).setVisibility(View.GONE);
        }

        ((Button) view.findViewById(R.id.event_place_address)).setText(appointment.getPlaceAddress());
        ((Button) view.findViewById(R.id.event_place_address)).setTransformationMethod(null);
        ((Button) view.findViewById(R.id.event_place_address)).setOnClickListener(view12 -> openMaps(appointment.getPlaceAddress()));

        ((Button) view.findViewById(R.id.event_add_to_calendar)).setTransformationMethod(null);
        ((Button) view.findViewById(R.id.event_add_to_calendar)).setOnClickListener(view1 -> showCalendarDialog(appointment));

        return view;
    }

    private void openMaps(String address){
        Uri gmmIntentUri = Uri.parse("geo:0,0?q="+address);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void showCalendarDialog(final Appointment appointment) {
        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(getActivity(), getString(R.string.appointment_calendar_disclaimer), getString(R.string.add_to_calendar));

        builder.setPositiveButton(getString(R.string.add_to_calendar), (dialog, id) -> {
            addToCalendar(appointment);
            dialog.dismiss();
        }).setCancelable(false).setNegativeButton(getString(R.string.abort), (dialog, id) -> dialog.cancel());

        builder.create().show();
        boolean canDoCalendarOperations = Permissions.checkPermissions(getActivity(), Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);
        if (!canDoCalendarOperations) {
            Permissions.requestPermissions(42, getActivity(), Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);
        }

    }

    private void addToCalendar(Appointment appointment){
        String description = appointment.subTitle + "\n\n" + appointment.getInfoTitleAndText();

        Calendar beginTime = Calendar.getInstance();
        beginTime.setTime(appointment.getStartDate());

        Calendar endTime = Calendar.getInstance();
        endTime.setTime(appointment.getEndDate());

        ContentResolver cr = getActivity().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
        values.put(CalendarContract.Events.TITLE, appointmentTitle);
        values.put(CalendarContract.Events.EVENT_LOCATION, appointment.getPlaceAddress() + " - " + appointment.getPlace());
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, beginTime.getTimeZone().toString());
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

    }


}
