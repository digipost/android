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

class FingerprintActitivy :  AppCompatActivity(), FingerprintAuthenticationDialogFragment.Callback {

    private val CREDENTIAL_REQUEST_CODE_ACITIVTY = 1
    private var IS_AUTHENTICATING = false
    private val fragment = FingerprintAuthenticationDialogFragment()
    private val NEXT_ACTIVITY_ID = "NEXT_ACTIVITY"
    private lateinit var nextActivity: String


    @TargetApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fingerprintManager = getSystemService(FingerprintManager::class.java)
        if (fingerprintManager.isHardwareDetected && fingerprintManager.hasEnrolledFingerprints()) {
            fragment.setCallback(this@FingerprintActitivy)
            fragment.show(supportFragmentManager, "DIALOG_TAG")

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
        supportFragmentManager.beginTransaction().remove(fragment).commit()

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


    fun start(context: Context, activityID: String) {
        val intent = Intent(context, FingerprintActitivy::class.java)
        intent.putExtra(NEXT_ACTIVITY_ID, activityID)
        context.startActivity(intent)
    }

}