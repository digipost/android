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
import android.widget.TextView;
import no.digipost.android.R;
import no.digipost.android.gui.invoice.AgreementFragment
import no.digipost.android.model.Metadata;

public class AppointmentView extends Fragment{

    private static Metadata appointment;
    private TextView title, subTitle, dateTitle, time, date, arrivalTitle, arrivalText, placeTitle, placeWhere, placeAddress, infoTitle1, infoText1, infoTitle2, infoText2;

    public static AppointmentView newInstance(Metadata appointment) {
        AppointmentView.appointment = appointment;
        return new AppointmentView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.appointment_view, container, false);
        ((TextView) view.findViewById(R.id.appointment_title)).setText(appointment.subTitle);

        return view;
    }
}
