package no.digipost.android.utilities

import android.widget.EditText
import no.digipost.android.model.ValidationRules

object ValidationUtillities {
    @JvmStatic
    fun emailAppersValid(validationRules: ValidationRules, email: EditText) : Boolean {
        return emailAppersValid(validationRules, email.text.toString())
    }

    @JvmStatic
    fun emailAppersValid(validationRules: ValidationRules, email: String) : Boolean {
        val trimmedEmail = email.trim()
        return trimmedEmail.isEmpty() || trimmedEmail.matches(validationRules.email.toRegex())
    }


    @JvmStatic
    fun phoneNumberAppersValid(validationRules: ValidationRules, phoneNumber: EditText): Boolean {
        return phoneNumberAppersValid(validationRules, phoneNumber.text.toString())
    }

    @JvmStatic
    fun phoneNumberAppersValid(validationRules: ValidationRules, phoneNumber: String): Boolean {
        val trimmedPhoneNumber = phoneNumber.trim()
        return trimmedPhoneNumber.isEmpty() || trimmedPhoneNumber.matches(validationRules.phoneNumber.toRegex())
    }
}