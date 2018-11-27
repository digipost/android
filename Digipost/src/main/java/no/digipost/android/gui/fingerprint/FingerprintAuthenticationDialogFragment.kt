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

package no.digipost.android.gui.fingerprint

import android.annotation.TargetApi
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import no.digipost.android.R

@TargetApi(Build.VERSION_CODES.M)
class FingerprintAuthenticationDialogFragment : DialogFragment(), FingerprintUiHelper.Callback {

    private lateinit var fingerprintUiHelper: FingerprintUiHelper
    private lateinit var btnCancel: Button
    private lateinit var btnSecondary: Button
    private lateinit var callback : Callback

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.setCanceledOnTouchOutside(false)
        return inflater.inflate(R.layout.fingerprint_dialog_container,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imageFingerprintIcon  = view.findViewById(R.id.fingerprint_icon) as ImageView
        val textFingerprintStatus = view.findViewById(R.id.fingerprint_status) as TextView

        btnCancel = view.findViewById(R.id.fingerprint_cancel_button)
        btnCancel.setOnClickListener {
            fingerprintUiHelper.stopListening()
            callback.cancelAuthentication()
        }

        btnSecondary = view.findViewById(R.id.fingerprint_second_dialog_button)
        btnSecondary.setOnClickListener {
            callback.backupAuthentication()
        }



        fingerprintUiHelper = FingerprintUiHelper(
            activity!!.getSystemService(FingerprintManager::class.java),
            imageFingerprintIcon,
            textFingerprintStatus,
            this
        )

        fingerprintUiHelper.startListening()

    }

    override fun onResume() {
        super.onResume()
        if( ! fingerprintUiHelper.isListening()){
            fingerprintUiHelper.startListening()
        }
    }

    override fun onPause() {
        super.onPause()
        fingerprintUiHelper.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.finish()
    }

    override fun onFingerprintAuthenticated() {
        callback.authenticationOK("fingerprint")
    }

    interface Callback {
        fun authenticationOK(type: String)
        fun backupAuthentication()
        fun cancelAuthentication()

    }
}
