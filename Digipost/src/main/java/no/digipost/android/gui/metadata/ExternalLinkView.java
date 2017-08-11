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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import no.digipost.android.R;
import no.digipost.android.model.Metadata;

public class ExternalLinkView extends Fragment{

    private Metadata externallink;

    public static ExternalLinkView newInstance() {
        return new ExternalLinkView();
    }

    public void setExternallink(Metadata externallink) {
        this.externallink = externallink;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.externallink_view, container, false);
        ((TextView) view.findViewById(R.id.externallink_title)).setText(externallink.title);
        ((TextView) view.findViewById(R.id.externallink_text)).setText(externallink.text);
        ((TextView) view.findViewById(R.id.externallink_deadline)).setText(externallink.deadline);
        ((Button) view.findViewById(R.id.externallink_open_link)).setTransformationMethod(null);
        ((Button) view.findViewById(R.id.externallink_open_link)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(externallink.url));
                startActivity(browserIntent);
            }
        });
        return view;
    }
}
