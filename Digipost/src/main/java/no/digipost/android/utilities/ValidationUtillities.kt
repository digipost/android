package no.digipost.android.utilities

import android.widget.EditText
import no.digipost.android.model.ValidationRules

object ValidationUtillities {
    @JvmStatic
    fun emailAppearsValid(validationRules: ValidationRules, email: EditText) : Boolean {
        return emailAppearsValid(validationRules, email.text.toString())
    }

    @JvmStatic
    fun emailAppearsValid(validationRules: ValidationRules, email: String) : Boolean {
        val trimmedEmail = email.trim()
        return trimmedEmail.isEmpty() || trimmedEmail.matches(validationRules.email.toRegex())
    }


    @JvmStatic
    fun phoneNumberAppearsValid(validationRules: ValidationRules, phoneNumber: EditText): Boolean {
        return phoneNumberAppearsValid(validationRules, phoneNumber.text.toString())
    }

    @JvmStatic
    fun phoneNumberAppearsValid(validationRules: ValidationRules, phoneNumber: String): Boolean {
        val trimmedPhoneNumber = phoneNumber.trim()
        return trimmedPhoneNumber.isEmpty() || trimmedPhoneNumber.matches(validationRules.phoneNumber.toRegex())
    }
}