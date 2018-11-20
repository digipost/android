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
import no.digipost.android.R
import no.digipost.android.utilities.DialogUtitities

class FingerprintActivity :  AppCompatActivity(), FingerprintAuthenticationDialogFragment.Callback {

    private val CREDENTIAL_REQUEST_CODE_ACITIVTY = 1
    private var IS_AUTHENTICATING = false
    private val fragment = FingerprintAuthenticationDialogFragment()
    private lateinit var nextActivity: Class<*>


    @TargetApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nextActivity = intent.extras[NEXT_ACTIVITY_ID] as Class<*>
        val fingerprintManager = getSystemService(FingerprintManager::class.java)
        if (fingerprintManager.isHardwareDetected && fingerprintManager.hasEnrolledFingerprints()) {
            fragment.setCallback(this@FingerprintActivity)
            fragment.show(supportFragmentManager, "FINGERPRINT_FRAGMENT")
        } else {
            backupAuthentication()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!IS_AUTHENTICATING) {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREDENTIAL_REQUEST_CODE_ACITIVTY) {
            if(resultCode == Activity.RESULT_OK){
                authenticationOK()
            }
        }
        IS_AUTHENTICATING = false
    }


    override fun authenticationOK() {
        val intent = Intent(this, nextActivity)
        startActivity(intent)
    }

    override fun cancelAuthentication() {
        finish()
    }

    override fun backupAuthentication() {
        val keyguard = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val intent = keyguard.createConfirmDeviceCredentialIntent(null, null)
        IS_AUTHENTICATING = true
        this.startActivityForResult(intent, CREDENTIAL_REQUEST_CODE_ACITIVTY)
    }


    companion object {
        fun startActivityWithFingerprint (context: Context, activityClass: Class<*>) {
            if (! isKeyguardSecure(context)) {
                DialogUtitities.showLongToast(context, context.getString(R.string.fingerprint_screenlock_tips))
                return
            }

            val intent = Intent(context, FingerprintActivity::class.java)
            intent.putExtra(this.NEXT_ACTIVITY_ID, activityClass)
            context.startActivity(intent)
        }

        fun isKeyguardSecure(context: Context): Boolean {
            val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            return keyguard.isKeyguardSecure
        }

        private const val NEXT_ACTIVITY_ID = "NEXT_ACTIVITY"
    }

}