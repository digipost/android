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

package no.digipost.android.gui.datatype

import android.Manifest
import android.app.Fragment
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

import java.util.Calendar

import no.digipost.android.R
import no.digipost.android.model.datatypes.Event
import no.digipost.android.utilities.DialogUtitities
import no.digipost.android.utilities.FormatUtilities
import no.digipost.android.utilities.Permissions
import java.net.URI

class EventView : Fragment() {

    private lateinit var event: Event
    private lateinit var eventTitle: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.event_view, container, false)
        view.findViewById<TextView>(R.id.event_title).text = eventTitle
        view.findViewById<TextView>(R.id.event_subtitle).text = event.subTitle

        val timeList = event.time
                .map { interval ->
                    val formattedInterval = FormatUtilities.formatTimeInterval(interval)
                    "• $formattedInterval"
                }.reduce { a, b -> "$a\n$b" }
        view.findViewById<TextView>(R.id.event_date_title).text = event.timeLabel
        view.findViewById<TextView>(R.id.event_date_time) .text = timeList
        view.findViewById<TextView>(R.id.event_place_where).text = event.place
        view.findViewById<TextView>(R.id.event_arrival_info_text).text = event.description

        val textinfolinearView = view.findViewById<LinearLayout>(R.id.event_info_layout)
        for (info in event.info) {
            val infoTitle = TextView(activity)
            infoTitle.text = info.title
            infoTitle.setTextAppearance(activity, R.style.Digipost_InfoTextTitle)
            val infoText = TextView(activity)
            infoText.text = info.text
            infoText.setTextAppearance(activity, R.style.Digipost_InfoText)
            textinfolinearView.addView(infoTitle)
            textinfolinearView.addView(infoText)
        }

        view.findViewById<TextView>(R.id.event_place_title).text = event.placeLabel
        view.findViewById<Button>(R.id.event_place_address).text = event.getPlaceAddress()
        view.findViewById<Button>(R.id.event_place_address).transformationMethod = null
        view.findViewById<Button>(R.id.event_place_address).setOnClickListener { openMaps(event.placeAddress) }

        view.findViewById<Button>(R.id.event_add_to_calendar).transformationMethod = null
        view.findViewById<Button>(R.id.event_add_to_calendar).setOnClickListener { showCalendarDialog(event) }

        val infoAndLinksView = view.findViewById<LinearLayout>(R.id.event_info_and_links)
        for (link in event.links) {
            val linkButton = Button(activity)
            linkButton.text = link.description
            linkButton.transformationMethod = null
            linkButton.setOnClickListener { openExternalLink(link.url) }
            linkButton.setTextAppearance(activity, R.style.Digipost_DataTypeLinkButton)
            linkButton.setBackgroundResource(0)
            linkButton.gravity = Gravity.LEFT
            infoAndLinksView.addView(linkButton)
        }
        return view
    }

    private fun openMaps(address: String) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=$address")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    private fun showCalendarDialog(event: Event) {
        val builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(activity, getString(R.string.appointment_calendar_disclaimer), getString(R.string.add_to_calendar))

        builder.setPositiveButton(getString(R.string.add_to_calendar)) { dialog, id ->
            addToCalendar(event)
            dialog.dismiss()
        }.setCancelable(false).setNegativeButton(getString(R.string.abort)) { dialog, id -> dialog.cancel() }

        builder.create().show()
        val canDoCalendarOps = Permissions.checkPermissions(activity, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
        if (!canDoCalendarOps) {
            Permissions.requestPermissions(43, activity, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
        }

    }

    private fun addToCalendar(event: Event) {
        val description = "${event.subTitle}\n\n${event.infoTitleAndText}"

        for (interval in event.time) {
            val beginTime = Calendar.getInstance()
            beginTime.time = interval.startTime

            val endTime = Calendar.getInstance()
            endTime.time = interval.endTime

            val cr = activity.contentResolver
            val values = ContentValues()
            values.put(CalendarContract.Events.DTSTART, beginTime.timeInMillis)
            values.put(CalendarContract.Events.DTEND, endTime.timeInMillis)
            values.put(CalendarContract.Events.TITLE, eventTitle)
            values.put(CalendarContract.Events.EVENT_LOCATION, "${event.placeAddress} - ${event.place}")
            values.put(CalendarContract.Events.DESCRIPTION, description)
            values.put(CalendarContract.Events.EVENT_TIMEZONE, beginTime.timeZone.toString())
            values.put(CalendarContract.Events.CALENDAR_ID, 1)
            cr.insert(CalendarContract.Events.CONTENT_URI, values)
        }

    }

    private fun openExternalLink(url: URI) {
        val scheme = url.scheme ?: "http"
        if (scheme == "https") {
            val intent = Intent(activity, ExternalLinkWebview::class.java)
            intent.putExtra("url", url.toASCIIString())
            startActivity(intent)
        }else{
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toASCIIString()))
            startActivity(browserIntent)
        }
    }


    companion object {

        fun newInstance(event: Event, title: String): EventView {
            val eventView = EventView()
            eventView.event = event
            eventView.eventTitle = title
            return eventView
        }
    }


}
