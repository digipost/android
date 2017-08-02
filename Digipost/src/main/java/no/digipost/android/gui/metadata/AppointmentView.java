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

package no.digipost.android.gui.metadata;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import no.digipost.android.R;
import no.digipost.android.model.Metadata;

public class AppointmentView extends Fragment{

    private static Metadata appointment;

    public static AppointmentView newInstance(Metadata appointment) {
        AppointmentView.appointment = appointment;
        return new AppointmentView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.appointment_view, container, false);
        ((TextView) view.findViewById(R.id.appointment_title)).setText(appointment.title);
        ((TextView) view.findViewById(R.id.appointment_subtitle)).setText(appointment.subTitle);
        ((TextView) view.findViewById(R.id.appointment_date_time)).setText(appointment.getStartTimeString());
        ((TextView) view.findViewById(R.id.appointment_date_date)).setText(appointment.getStartDateString());
        ((TextView) view.findViewById(R.id.appointment_place_where)).setText(appointment.getPlace());
        ((TextView) view.findViewById(R.id.appointment_place_address)).setText(appointment.getPlaceAddress());
        ((TextView) view.findViewById(R.id.appointment_arrival_info_text)).setText(appointment.getArrivalInfo());

        if (appointment.info.size() > 0) {
            ((TextView) view.findViewById(R.id.appointment_info1_title)).setText(appointment.info.get(0).title);
            ((TextView) view.findViewById(R.id.appointment_info1_text)).setText(appointment.info.get(0).text);
        }else{
            ((TextView) view.findViewById(R.id.appointment_info1_title)).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.appointment_info1_text)).setVisibility(View.GONE);
        }

        if(appointment.info.size() > 1) {
            ((TextView) view.findViewById(R.id.appointment_info2_title)).setText(appointment.info.get(1).title);
            ((TextView) view.findViewById(R.id.appointment_info2_text)).setText(appointment.info.get(1).text);
        }else{
            ((TextView) view.findViewById(R.id.appointment_info2_title)).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.appointment_info2_text)).setVisibility(View.GONE);
        }

        ((Button) view.findViewById(R.id.appointment_add_to_calendar)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToCalendar(appointment);
            }
        });

        return view;
    }

    private void openMaps(){
        Uri gmmIntentUri = Uri.parse("geo:0,0?q="+appointment.getPlaceAddress());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void addToCalendar(Metadata appointment){

    }
}
