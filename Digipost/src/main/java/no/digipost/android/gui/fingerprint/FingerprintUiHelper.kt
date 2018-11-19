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
import android.os.CancellationSignal
import android.widget.ImageView
import android.widget.TextView
import no.digipost.android.R

/**
 * Small helper class to manage text/icon around fingerprint authentication UI.
 */
@TargetApi(Build.VERSION_CODES.M)
class FingerprintUiHelper internal constructor(private val fingerprintMgr: FingerprintManager,
                     private val icon: ImageView,
                     private val errorTextView: TextView,
                     private val callback: Callback
) : FingerprintManager.AuthenticationCallback() {

    private var cancellationSignal: CancellationSignal? = null
    private var selfCancelled = false

    private val resetErrorTextRunnable = Runnable {
        icon.setImageResource(R.drawable.fingerprint_40px_icon)
        errorTextView.run {
            setTextColor(errorTextView.resources.getColor(R.color.fingerprint_hint_color, null))
            text = errorTextView.resources.getString(R.string.fingerprint_hint)
        }
    }

    fun startListening() {
        cancellationSignal = CancellationSignal()
        selfCancelled = false
        fingerprintMgr.authenticate(null, cancellationSignal, 0, this, null)
        icon.setImageResource(R.drawable.fingerprint_40px_icon)
    }

    fun isListening() : Boolean {
        return !selfCancelled && cancellationSignal != null
    }

    fun stopListening() {
        cancellationSignal?.also {
            selfCancelled = true
            it.cancel()
        }
        cancellationSignal = null
    }

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
        if (!selfCancelled) {
            errorTextView.removeCallbacks(resetErrorTextRunnable)
            errorTextView.text = errorTextView.resources.getString(R.string.fingerprint_too_many_request)

            icon.setImageResource(R.drawable.fingerprint_error_icon)
        }
    }

    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) =
        showError(helpString)

    override fun onAuthenticationFailed() =
        showError(icon.resources.getString(R.string.fingerprint_not_recognized))

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        errorTextView.run {
            removeCallbacks(resetErrorTextRunnable)
            setTextColor(errorTextView.resources.getColor(R.color.fingerprint_success_color, null))
            text = errorTextView.resources.getString(R.string.fingerprint_success)
        }
        icon.run {
            setImageResource(R.drawable.fingerprint_success_icon)
            postDelayed({ callback.onFingerprintAuthenticated() }, SUCCESS_DELAY_MILLIS)
        }
    }

    private fun showError(error: CharSequence) {
        icon.setImageResource(R.drawable.fingerprint_error_icon)
        errorTextView.run {
            text = error
            setTextColor(errorTextView.resources.getColor(R.color.fingerprint_warning_color, null))
            removeCallbacks(resetErrorTextRunnable)
            postDelayed(resetErrorTextRunnable, ERROR_TIMEOUT_MILLIS)
        }
    }

    interface Callback {
        fun onFingerprintAuthenticated()
    }

    companion object {
        val ERROR_TIMEOUT_MILLIS: Long = 1600
        val SUCCESS_DELAY_MILLIS: Long = 1300
    }
}
