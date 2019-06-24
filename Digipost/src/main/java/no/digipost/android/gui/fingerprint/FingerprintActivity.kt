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
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.analytics.GoogleAnalytics
import no.digipost.android.R
import no.digipost.android.analytics.GAEventController
import no.digipost.android.utilities.DialogUtitities

@TargetApi(Build.VERSION_CODES.M)
class FingerprintActivity :  AppCompatActivity(), FingerprintAuthenticationDialogFragment.Callback {

    private val CREDENTIAL_REQUEST_CODE_ACITIVTY = 1
    private var IS_AUTHENTICATING = false
    private var CAN_USE_FINGERPRINT = true
    private val fragment = FingerprintAuthenticationDialogFragment()
    private lateinit var nextActivity: Class<*>
    private var nextActivityExtraInfo: Bundle? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nextActivity = intent.extras[NEXT_ACTIVITY_ID] as Class<*>
        nextActivityExtraInfo = intent.extras[NEXT_ACTIVITY_EXTRAS] as Bundle?

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            val fingerprintManager = getSystemService(FingerprintManager::class.java)
            if (fingerprintManager.isHardwareDetected && fingerprintManager.hasEnrolledFingerprints()) {
                fragment.setCallback(this@FingerprintActivity)
                fragment.show(supportFragmentManager, "FINGERPRINT_FRAGMENT")
                GAEventController.sendAuthenticationEvent(this, "påbegynt", "fingerprint" )
            } else {
                CAN_USE_FINGERPRINT = false
                backupAuthentication()
            }
        } else {
            CAN_USE_FINGERPRINT = false
            backupAuthentication()
        }
    }

    override fun onStart() {
        super.onStart()
        GoogleAnalytics.getInstance(this).reportActivityStart(this)
    }

    override fun onPause() {
        super.onPause()
        if (!IS_AUTHENTICATING) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GoogleAnalytics.getInstance(this).reportActivityStop(this)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREDENTIAL_REQUEST_CODE_ACITIVTY) {
            if(resultCode == Activity.RESULT_OK){
                authenticationOK("sekundær")
            }else{
                GAEventController.sendAuthenticationEvent(this, "avbrutt" , "sekundær")
                if (! CAN_USE_FINGERPRINT){
                    finish()
                }
            }
        }
        IS_AUTHENTICATING = false
    }


    override fun authenticationOK(type: String) {
        GAEventController.sendAuthenticationEvent(this, "ok" , type)
        val intent = Intent(this, nextActivity)
        if (nextActivityExtraInfo != null) {
            intent.putExtras(nextActivityExtraInfo!!)
        }
        startActivity(intent)
    }

    override fun cancelAuthentication() {
        GAEventController.sendAuthenticationEvent(this, "avbrutt" , "fingerprint")
        finish()
    }

    override fun backupAuthentication() {
        GAEventController.sendAuthenticationEvent(this, "påbegynt", "sekundær" )
        val keyguard = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val intent = keyguard.createConfirmDeviceCredentialIntent(getString(R.string.fingerprint_secondary_title), null)
        IS_AUTHENTICATING = true
        this.startActivityForResult(intent, CREDENTIAL_REQUEST_CODE_ACITIVTY)
    }


    companion object {
        fun startActivityWithFingerprint (context: Context, activityClass: Class<*>, extraActivityInfo: Bundle?) {
            if (! isKeyguardSecure(context)) {
                DialogUtitities.showLongToast(context, context.getString(R.string.fingerprint_screenlock_tips))
                return
            }

            val intent = Intent(context, FingerprintActivity::class.java)
            intent.putExtra(this.NEXT_ACTIVITY_ID, activityClass)
            if (extraActivityInfo != null) {
                intent.putExtra(this.NEXT_ACTIVITY_EXTRAS, extraActivityInfo)
            }
            context.startActivity(intent)
        }

        fun isKeyguardSecure(context: Context): Boolean {
            val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            return keyguard.isKeyguardSecure
        }

        private const val NEXT_ACTIVITY_ID = "NEXT_ACTIVITY"
        private const val NEXT_ACTIVITY_EXTRAS = "NEXT_ACTIVITY_EXTRAS"

    }

}